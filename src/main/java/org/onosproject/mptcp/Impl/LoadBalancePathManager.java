package org.onosproject.mptcp.Impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.onlab.packet.IpAddress;
import org.onosproject.mptcp.LoadBalancePathService;
import org.onosproject.mptcp.MptcpConnection;
import org.onosproject.mptcp.MptcpSubFlow;
import org.onosproject.net.Path;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by cr on 16-12-20.
 */
@Component(immediate = true)
@Service
public class LoadBalancePathManager implements LoadBalancePathService {


    private final Logger log = getLogger(getClass());

    @Activate
    public void activate() {
        log.info("Started...");
    }

    @Deactivate
    public void deactivate() {
        log.info("Stopped...");
    }

    @Override
    public Path getOptimalPath(IpAddress srcIp, IpAddress dstIp) {
        return null;
    }

    @Override
    public Path getOptimalPath(MptcpConnection mptcpConnection) {
        return null;
    }

    @Override
    public Path getOptimalPath(MptcpSubFlow subFlow) {
        return null;
    }
}
