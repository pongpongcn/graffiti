using RealtimePush.Core;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.IO;

namespace RealtimePush.Client
{
    class Program
    {
        private static Monitor monitor;
        private static bool running;

        static void Main(string[] args)
        {
            string hostNameOrAddress = "localhost";
            int port = 10086;
            int receiveSendBufferSize = 1024;

            if (args.Length > 0)
            {
                hostNameOrAddress = args[0];
            }
            else
            {
                Console.Write("Host({0}):", hostNameOrAddress);
                string inputValue = Console.ReadLine();
                if (!string.IsNullOrWhiteSpace(inputValue))
                {
                    hostNameOrAddress = inputValue.Trim();
                }
            }

            IPAddress ipAddress;
            try
            {
                ipAddress = IPAddress.Parse(hostNameOrAddress);
            }
            catch
            {
                try
                {
                    IPHostEntry ipHostInfo = Dns.GetHostEntry(hostNameOrAddress);
                    ipAddress = ipHostInfo.AddressList.FirstOrDefault(item => item.AddressFamily == AddressFamily.InterNetwork);
                }
                catch
                {
                    ipAddress = null;
                }
            }

            EndPoint remoteEP = new IPEndPoint(ipAddress, port);

            Communication communication = new Communication(remoteEP, receiveSendBufferSize);
            monitor = new Monitor(communication);

            communication.Init();

            running = true;

            communication.Start();

            monitor.Start();

            Thread workThread = new Thread(Work);
            workThread.Start();

            Console.WriteLine("Press Enter To Stop.");
            Console.ReadLine();

            monitor.Stop();

            communication.Stop();

            running = false;

            workThread.Join();

            Console.WriteLine("=End=");
            Console.ReadLine();
        }

        private static void Work()
        {
            DateTime tempCurrentTime = DateTime.Now;
            DateTime loopShoudEndTime = new DateTime(tempCurrentTime.Year, tempCurrentTime.Month, tempCurrentTime.Day, tempCurrentTime.Hour, tempCurrentTime.Minute, tempCurrentTime.Second).AddSeconds(1);

            string logFileName = string.Format("{0:yyyyMMddHHmmss}.log", DateTime.Now);
            
            StreamWriter logger;
            try
            {
                FileStream fs = new FileStream(logFileName, FileMode.OpenOrCreate, FileAccess.Write);
                logger = new StreamWriter(fs, Encoding.UTF8);
            }
            catch
            {
                logger = null;
            }

            while (running)
            {
                MonitoringInfo monitoringInfo = monitor.Info;
                if (monitoringInfo != null)
                {
                    StringBuilder sb = new StringBuilder();
                    if (monitoringInfo.LastestDeliverLatency != null)
                    {
                        sb.AppendLine(string.Format("Lastest Deliver Latency: {0:F}s", monitoringInfo.LastestDeliverLatency.Value.TotalSeconds));
                    }
                    else
                    {
                        sb.AppendLine("LastestDeliverLatency: N/A");
                    }
                    if (monitoringInfo.LastestSendTimeSpan != null)
                    {
                        sb.AppendLine(string.Format("Lastest Send Time Span: {0:F}s", monitoringInfo.LastestSendTimeSpan.Value.TotalSeconds));
                    }
                    else
                    {
                        sb.AppendLine("LastestSendTimeSpan: N/A");
                    }
                    sb.AppendLine(string.Format("Total Received Message Count: {0}", monitoringInfo.TotalReceivedMessageCount));
                    sb.AppendLine(string.Format("Discontinuous SN Message Count: {0}", monitoringInfo.DiscontinuousSNMessageCount));
                    sb.AppendLine(string.Format("Interrupt Count: {0}", monitoringInfo.InterruptCount));
                    sb.AppendLine("Bad Deliver Latency Message Counts:");
                    foreach (int key in monitoringInfo.BadDeliverLatencyMessageCounts.Keys.OrderBy(item => item))
                    {
                        if (key >= 5)
                        {
                            sb.AppendLine(string.Format("    {0}s and above: {1}", key, monitoringInfo.BadDeliverLatencyMessageCounts[key]));
                        }
                        else
                        {
                            sb.AppendLine(string.Format("    {0}s: {1}", key, monitoringInfo.BadDeliverLatencyMessageCounts[key]));
                        }
                    }
                    sb.AppendLine("Bad Send Time Span Message Counts:");
                    foreach (int key in monitoringInfo.BadSendTimeSpanMessageCounts.Keys.OrderBy(item => item))
                    {
                        if (key >= 5)
                        {
                            sb.AppendLine(string.Format("    {0}s and above: {1}", key, monitoringInfo.BadSendTimeSpanMessageCounts[key]));
                        }
                        else
                        {
                            sb.AppendLine(string.Format("    {0}s: {1}", key, monitoringInfo.BadSendTimeSpanMessageCounts[key]));
                        }
                    }

                    string logText = sb.ToString();

                    logger.WriteLine(string.Format("{0:yyyy-MM-dd HH:mm:ss}", DateTime.Now));
                    logger.Write(logText);
                    logger.WriteLine();
                    logger.Flush();

                    Console.Clear();
                    Console.Write(logText);
                }

                DateTime loopEndTime = DateTime.Now;
                if (loopEndTime <= loopShoudEndTime)
                {
                    int sleepTime = (int)(loopShoudEndTime - loopEndTime).TotalMilliseconds + 1;
                    Thread.Sleep(sleepTime);
                    loopShoudEndTime = loopShoudEndTime.AddSeconds(1);
                }
                else
                {
                    loopShoudEndTime = loopShoudEndTime.AddSeconds(1 + (int)(loopEndTime - loopShoudEndTime).TotalSeconds);
                }
            }

            if (logger != null)
            {
                try
                {
                    logger.Close();
                }
                catch { }
            }
        }
    }
}
