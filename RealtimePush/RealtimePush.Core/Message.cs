using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace RealtimePush.Core
{
    /// <summary>
    /// 代表一条消息
    /// </summary>
    public class Message
    {
        /// <summary>
        /// 本消息产生的时间
        /// </summary>
        public DateTime Time { get; set; }
        /// <summary>
        /// 序列号
        /// </summary>
        public int SN { get; set; }
        /// <summary>
        /// 消息的内容
        /// </summary>
        public string Data { get; set; }

        public override string ToString()
        {
            return string.Format("<Message, Time: {0:yyyy-MM-dd HH:mm:ss} SN: {1}, DataLength: {2}>", Time, SN, Data.Length);
        }
    }
}
