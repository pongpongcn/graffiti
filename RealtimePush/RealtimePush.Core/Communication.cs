using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace RealtimePush.Core
{
    public class Communication
    {
        private int m_receiveBufferSize;
        private byte[] m_messageBuffer;
        private int m_messageBufferOffset;
        private EndPoint m_remoteEP;
        private Socket m_connectSocket;
        private SocketAsyncEventArgs m_readEventArgs;
        private SocketAsyncEventArgs m_writeEventArgs;//目前没有使用，后续会需要。
        private object syncRoot = new object();
        private bool m_running;
        private ManualResetEvent m_receiveDone;
        private ManualResetEvent m_closeDone;
        private System.Timers.Timer m_receive_timer;

        public event EventHandler<MessageEventArgs> MessageReceived;

        protected virtual void OnMessageReceived(MessageEventArgs e)
        {
            EventHandler<MessageEventArgs> handler = MessageReceived;
            if (handler != null)
            {
                handler(this, e);
            }
        }

        public Communication(EndPoint remoteEP, int receiveBufferSize)
        {
            m_remoteEP = remoteEP;
            m_receiveBufferSize = receiveBufferSize;
        }

        public void Init()
        {
            //Pre-allocate reusable SocketAsyncEventArgs
            m_readEventArgs = new SocketAsyncEventArgs();
            m_readEventArgs.Completed += IO_Completed;
            m_readEventArgs.SetBuffer(new byte[m_receiveBufferSize], 0, m_receiveBufferSize);
            m_receive_timer = new System.Timers.Timer(10000);
            m_receive_timer.Elapsed += Receive_Timeout;

            m_messageBuffer = new byte[1024];
        }

        private void Receive_Timeout(object sender, System.Timers.ElapsedEventArgs e)
        {
            m_receive_timer.Stop();

            Debug.WriteLine("Receive Timeout");

            Disconnect();
        }

        public void Start()
        {
            Debug.WriteLine("Starting");

            m_running = true;
            m_receiveDone = new ManualResetEvent(true);
            m_closeDone = new ManualResetEvent(true);

            m_messageBufferOffset = 0;
            m_readEventArgs.RemoteEndPoint = m_remoteEP;

            Connect();

            Debug.WriteLine("Started");
        }

        private void Connect()
        {
            lock (syncRoot)
            {
                //对于连接已建立完成情况，无需处理。
                if (m_connectSocket == null)
                {
                    Debug.WriteLine("Connecting");

                    Socket socket = new Socket(m_remoteEP.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
                    bool willRaiseEvent = socket.ConnectAsync(m_readEventArgs);
                    if (!willRaiseEvent)
                    {
                        ProcessConnect(m_readEventArgs);
                    }
                }
            }
        }

        private void Receive()
        {
            Debug.WriteLine("Receive");

            try
            {
                m_receiveDone.Reset();
                bool willRaiseEvent = m_connectSocket.ReceiveAsync(m_readEventArgs);
                if (!willRaiseEvent)
                {
                    m_receiveDone.Set();
                    ProcessReceive(m_readEventArgs);
                }
                else
                {
                    m_receive_timer.Start();
                }
            }
            catch (Exception ex)
            {
                Debug.WriteLine(string.Format("Receive Failed, {0}", ex.Message));

                OnMessageReceived(new MessageEventArgs(null));

                m_receiveDone.Set();

                if (m_running)
                {
                    Disconnect();
                    Connect();
                }
            }
        }

        public void Disconnect()
        {
            lock(syncRoot)
            {
                //对于连接尚未建立完成情况，无需处理。
                if (m_connectSocket != null)
                {
                    Debug.WriteLine("Closing");

                    m_connectSocket.Shutdown(SocketShutdown.Send);

                    m_connectSocket.Close();

                    m_connectSocket = null;

                    //确保异步操作已经完成
                    m_receiveDone.WaitOne();

                    Debug.WriteLine("Closed");

                    m_closeDone.Set();
                }
            }
        }

        public void Stop()
        {
            //通知停止
            m_running = false;
            m_closeDone.Reset();

            //进行停止
            Disconnect();

            //等待停止
            m_closeDone.WaitOne();
        }

        private void IO_Completed(object sender, SocketAsyncEventArgs e)
        {
            Debug.WriteLine("IO_Completed, LastOperation: {0}, SocketError:{1}", e.LastOperation, e.SocketError);

            switch (e.LastOperation)
            {
                case SocketAsyncOperation.Connect:
                    ProcessConnect(e);
                    break;
                case SocketAsyncOperation.Receive:
                    m_receive_timer.Stop();
                    m_receiveDone.Set();
                    ProcessReceive(e);
                    break;
            }
        }

        private void ProcessConnect(SocketAsyncEventArgs e)
        {
            Thread.Sleep(1000);

            if (e.SocketError == SocketError.Success)
            {
                m_connectSocket = e.ConnectSocket;

                //建立连接成功
                Debug.WriteLine("Connected");

                if (!m_running)
                {
                    Disconnect();
                    return;
                }

                Receive();
            }
            else
            {
                //建立连接失败
                Debug.WriteLine("Connect Failed");

                if (!m_running)
                {
                    m_closeDone.Set();
                    return;
                }

                //重新尝试连接
                Connect();
            }
        }

        private void ProcessReceive(SocketAsyncEventArgs e)
        {
            if (e.SocketError == SocketError.Success && e.BytesTransferred > 0)
            {
                int bytesCopyed = 0;

                while (bytesCopyed < e.BytesTransferred)
                {
                    //将SocketAsyncEventArgs缓冲区的数据（剩余的）复制到m_messageBuffer中，具体可以复制多少，取决于m_messageBuffer中剩余的空间。
                    int saeaLeftBytes = e.BytesTransferred - bytesCopyed;
                    int maxCopyBytes = m_messageBuffer.Length - m_messageBufferOffset;
                    int toCopyBytes;
                    if (saeaLeftBytes <= maxCopyBytes)
                    {
                        toCopyBytes = saeaLeftBytes;
                    }
                    else
                    {
                        toCopyBytes = maxCopyBytes;
                    }
                    Array.Copy(e.Buffer, bytesCopyed, m_messageBuffer, m_messageBufferOffset, toCopyBytes);
                    bytesCopyed += toCopyBytes;
                    m_messageBufferOffset += toCopyBytes;

                    ProcessMessageBuffer();
                }

                Receive();
            }
            else
            {
                Debug.WriteLine("ProcessReceive SocketError: {0}, BytesTransferred: {1}", e.SocketError, e.BytesTransferred);
                OnMessageReceived(new MessageEventArgs(null));

                if (m_running)
                {
                    Disconnect();
                    Connect();
                }
            }
        }

        private void ProcessMessageBuffer()
        {
            while (m_messageBufferOffset >= 200)
            {
                int sourceIndex = 0;

                Encoding dataEncoding = Encoding.UTF8;

                string data = dataEncoding.GetString(m_messageBuffer, sourceIndex, 200);

                Debug.WriteLine(string.Format("Received message {0}", data));

                string[] dataParts = data.Split(',');
                Message message = new Message {
                    Time = DateTime.Parse(dataParts[0]),
                    SN = int.Parse(dataParts[1]),
                    Data = dataParts[2]
                };

                OnMessageReceived(new MessageEventArgs(message));

                Array.Copy(m_messageBuffer, 200, m_messageBuffer, 0, m_messageBufferOffset - 200);
                m_messageBufferOffset -= 200;
            }
        }
    }
}
