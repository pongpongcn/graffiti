using RealtimePush.Core;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;

namespace RealtimePush.ServerConsole
{
    class Program
    {
        static void Main(string[] args)
        {
            PushServer server = new PushServer();
            server.Start();

            while (true)
            {
                Console.Clear();

                Console.WriteLine("{0}", DateTime.Now);

                Thread.Sleep(1000);
            }
        }
    }
}
