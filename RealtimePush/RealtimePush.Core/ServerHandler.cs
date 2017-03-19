using Beetle.Express;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading;

namespace RealtimePush.Core
{
    class ServerHandler : IServerHandler
    {
        /// <summary>
        /// 当前连接的客户端数目
        /// </summary>
        public int ClientCount { get { return clientCount; } }
        
        /// <summary>
        /// 接收次数
        /// </summary>
        public int ReceiveTimes { get { return receiveTimes; } }

        /// <summary>
        /// 发送完成次数
        /// </summary>
        public int SendTimes { get { return sendTimes; } }

        private int clientCount;
        
        private int receiveTimes;
        
        private int sendTimes;

        public void Connect(IServer server, ChannelConnectEventArgs e)
        {
            Debug.WriteLine("ServerHandler Client Connect: {0}, Time: {1}", e.Channel.ID, DateTime.Now);
            Interlocked.Increment(ref clientCount);
        }

        public void Disposed(IServer server, ChannelEventArgs e)
        {
            Interlocked.Decrement(ref clientCount);
        }

        public void Error(IServer server, ErrorEventArgs e)
        {
            Debug.WriteLine("ServerHandler Error: {0}, Channel ID: {1}, Tag: {2}", e.Error.Message, e.Channel.ID, e.Tag);
        }

        public void Opened(IServer server)
        {
            clientCount = 0;
            receiveTimes = 0;
            sendTimes = 0;
        }

        public void Receive(IServer server, ChannelReceiveEventArgs e)
        {
            Debug.WriteLine("ServerHandler Client Receive: {0}, Time: {1}", e.Data.Count, DateTime.Now);
            Interlocked.Increment(ref receiveTimes);
        }

        public void SendCompleted(IServer server, ChannelSendEventArgs e)
        {
            Debug.WriteLine("ServerHandler Client SendCompleted: {0}, Time: {1}", e.Data.Count, DateTime.Now);

            Interlocked.Increment(ref sendTimes);
        }
    }
}
