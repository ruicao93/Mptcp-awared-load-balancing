package org.onosproject.mptcp;

import org.onlab.packet.IpAddress;
import org.onosproject.net.Path;

/**
 * Created by cr on 16-12-20.
 */
public interface LoadBalancePathService {

    Path getOptimalPath(IpAddress srcIp, IpAddress dstIp);
    Path getOptimalPath(MptcpConnection mptcpConnection);
    Path getOptimalPath(MptcpSubFlow subFlow);

}
