package org.onosproject.mptcp;

import org.onlab.packet.IpAddress;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Host;
import org.onosproject.net.HostLocation;
import org.onosproject.net.Path;

/**
 * Created by cr on 16-12-20.
 */
public interface LoadBalancePathService {

    Path getOptimalPath(HostLocation srcHostLocation, HostLocation dstHostLocation);
    Path getOptimalPath(MptcpConnection mptcpConnection);
    Path getOptimalPath(MptcpSubFlow subFlow);
    Path getOptimalPath(DeviceId srcDeviceId, DeviceId dstDeviceId);

    /**
     * Find optimal path for subflow avoiding conflict with initial connection.
     * @param srcDeviceId
     * @param dstDeviceId
     * @param token
     * @return
     */
    Path getOptimalPath(DeviceId srcDeviceId, DeviceId dstDeviceId, MptcpToken token);
}
