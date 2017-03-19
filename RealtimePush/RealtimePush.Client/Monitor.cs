using RealtimePush.Core;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace RealtimePush.Client
{
    public class Monitor
    {
        private Communication communication;
        private ConcurrentQueue<ReceivedMessage> receivedMessageQueue;
        private ManualResetEvent newMessageReceived;
        private bool running;
        private Thread processReceivedMessageThread;
        private TimeSpan? bestLatency;
        private TimeSpan? lastestDeliverLatency;
        private TimeSpan? lastestSendTimeSpan;
        private ReceivedMessage lastReceivedMessage;
        private int totalReceivedMessageCount;
        private Dictionary<int, int> badDeliverLatencyMessageCounts;
        private Dictionary<int, int> badSendTimeSpanMessageCounts;
        private int discontinuousSNMessageCount;
        private int interruptCount;
        private object syncRoot = new object();

        public MonitoringInfo Info
        {
            get
            {
                lock(syncRoot)
                {
                    MonitoringInfo info = new MonitoringInfo();

                    info.LastestDeliverLatency = lastestDeliverLatency;
                    info.LastestSendTimeSpan = lastestSendTimeSpan;
                    info.TotalReceivedMessageCount = totalReceivedMessageCount;
                    info.DiscontinuousSNMessageCount = discontinuousSNMessageCount;
                    info.InterruptCount = interruptCount;
                    Dictionary<int, int> copyOfBadDeliverLatencyMessageCounts = new Dictionary<int, int>();
                    if (badDeliverLatencyMessageCounts != null)
                    {
                        foreach(int key in badDeliverLatencyMessageCounts.Keys)
                        {
                            copyOfBadDeliverLatencyMessageCounts.Add(key, badDeliverLatencyMessageCounts[key]);
                        }
                        info.BadDeliverLatencyMessageCounts = copyOfBadDeliverLatencyMessageCounts;
                    }
                    Dictionary<int, int> copyOfBadSendTimeSpanMessageCounts = new Dictionary<int, int>();
                    if (badSendTimeSpanMessageCounts != null)
                    {
                        foreach (int key in badSendTimeSpanMessageCounts.Keys)
                        {
                            copyOfBadSendTimeSpanMessageCounts.Add(key, badSendTimeSpanMessageCounts[key]);
                        }
                        info.BadSendTimeSpanMessageCounts = copyOfBadSendTimeSpanMessageCounts;
                    }

                    return info;
                }
            }
        }

        public Monitor(Communication communication)
        {
            this.communication = communication;
        }

        public void Start()
        {
            newMessageReceived = new ManualResetEvent(false);

            receivedMessageQueue = new ConcurrentQueue<ReceivedMessage>();
            communication.MessageReceived += Communication_MessageReceived;

            bestLatency = null;
            lastReceivedMessage = null;

            lock(syncRoot)
            {
                lastestDeliverLatency = null;
                lastestSendTimeSpan = null;
                totalReceivedMessageCount = 0;
                badDeliverLatencyMessageCounts = new Dictionary<int, int>();
                badSendTimeSpanMessageCounts = new Dictionary<int, int>();
                discontinuousSNMessageCount = 0;
                interruptCount = 0;
            }

            running = true;

            processReceivedMessageThread = new Thread(ProcessReceivedMessages);
            processReceivedMessageThread.Start();
        }

        public void Stop()
        {
            running = false;

            processReceivedMessageThread.Join();
            processReceivedMessageThread = null;

            communication.MessageReceived -= Communication_MessageReceived;
            receivedMessageQueue = null;

            newMessageReceived = null;
        }

        private void ProcessReceivedMessages()
        {
            ReceivedMessage receivedMessage;

            while (running)
            {
                newMessageReceived.Reset();
                if (receivedMessageQueue.TryDequeue(out receivedMessage))
                {
                    ProcessReceivedMessage(receivedMessage);
                }
                else
                {
                    newMessageReceived.WaitOne();
                }
            }
        }

        private void ProcessReceivedMessage(ReceivedMessage receivedMessage)
        {
            lock(syncRoot)
            {
                if (receivedMessage.Message == null)
                {
                    bestLatency = null;
                    lastestDeliverLatency = null;
                    lastestSendTimeSpan = null;
                    lastReceivedMessage = null;
                    interruptCount++;
                    return;
                }

                totalReceivedMessageCount++;


                //表面上的延迟
                TimeSpan simpleDelay = receivedMessage.ReceivedTime - receivedMessage.Message.Time;
                if (bestLatency == null || simpleDelay < bestLatency)
                {
                    bestLatency = simpleDelay;
                    Debug.WriteLine("ServerClientTimeOffset Updated: {0}", bestLatency);
                }

                TimeSpan increasedDelay = simpleDelay - bestLatency.Value;
                Debug.WriteLine("increasedDelay: {0}", increasedDelay);

                lastestDeliverLatency = increasedDelay;

                if (increasedDelay.TotalSeconds > 1)
                {
                    int key = (int)increasedDelay.TotalSeconds;
                    if (key > 5)
                    {
                        key = 5;//超过5的归入5
                    }
                    if (badDeliverLatencyMessageCounts.ContainsKey(key))
                    {
                        badDeliverLatencyMessageCounts[key]++;
                    }
                    else
                    {
                        badDeliverLatencyMessageCounts.Add(key, 1);
                    }
                }

                if (lastReceivedMessage != null)
                {
                    if (receivedMessage.Message.SN - lastReceivedMessage.Message.SN > 1)
                    {
                        Debug.WriteLine("Discontinuous SN: {0}, {1}", lastReceivedMessage.Message.SN, receivedMessage.Message.SN);

                        discontinuousSNMessageCount++;
                    }

                    TimeSpan messageTimeSpan = receivedMessage.Message.Time - lastReceivedMessage.Message.Time;
                    Debug.WriteLine("messageTimeSpan: {0}", messageTimeSpan);

                    lastestSendTimeSpan = messageTimeSpan;

                    if (messageTimeSpan.TotalSeconds > 2)
                    {
                        int key = (int)messageTimeSpan.TotalSeconds;
                        if (key > 5)
                        {
                            key = 5;//超过5的归入5
                        }
                        if (badSendTimeSpanMessageCounts.ContainsKey(key))
                        {
                            badSendTimeSpanMessageCounts[key]++;
                        }
                        else
                        {
                            badSendTimeSpanMessageCounts.Add(key, 1);
                        }
                    }
                }

                lastReceivedMessage = receivedMessage;
            }
        }

        private void Communication_MessageReceived(object sender, MessageEventArgs e)
        {
            ReceivedMessage receivedMessage = new ReceivedMessage { Message = e.Message, ReceivedTime = DateTime.Now };
            receivedMessageQueue.Enqueue(receivedMessage);
            newMessageReceived.Set();
        }

        private class ReceivedMessage
        {
            public Message Message { get; set; }
            public DateTime ReceivedTime { get; set; }

            public override string ToString()
            {
                return string.Format("{0} receive at {1:yyyy-MM-dd HH:mm:ss}", Message, ReceivedTime);
            }
        }
    }
}
