using Beetle.Express;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace RealtimePush.Core
{
    public class PushServer
    {
        public PushServer()
        {
            mServer = ServerFactory.CreateTCP();
            mServer.ReceiveBufferSize = 1024 * 2;
            mServer.SendBufferSize = 1024 * 2;
            mServer.Port = 8300;
            mServer.MaxConnections = 900000;

            ServerHandler serverHandler = new ServerHandler();
            mServer.Handler = serverHandler;
        }

        //TCP服务容器
        private IServer mServer;

        public void Start()
        {
            mServer.Open();
        }

        public void Stop()
        {
            
        }
    }
}
