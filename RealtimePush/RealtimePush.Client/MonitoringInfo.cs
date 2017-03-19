using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace RealtimePush.Client
{
    public class MonitoringInfo
    {
        public TimeSpan? LastestDeliverLatency { get; set; }
        public TimeSpan? LastestSendTimeSpan { get; set; }
        public int TotalReceivedMessageCount { get; set; }
        public int DiscontinuousSNMessageCount { get; set; }
        public int InterruptCount { get; set; }
        public Dictionary<int, int> BadDeliverLatencyMessageCounts { get; set; }
        public Dictionary<int, int> BadSendTimeSpanMessageCounts { get; set; }
    }
}
