using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace RealtimePush.Core
{
    public class MessageEventArgs : EventArgs
    {
        public Message Message { get; private set; }
        public MessageEventArgs(Message message)
        {
            this.Message = message;
        }
    }
}
