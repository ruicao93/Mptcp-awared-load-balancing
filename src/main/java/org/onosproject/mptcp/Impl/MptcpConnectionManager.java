package org.onosproject.mptcp.Impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.mptcp.MptcpConnection;
import org.onosproject.mptcp.MptcpConnectionService;
import org.onosproject.mptcp.MptcpToken;
import org.onosproject.net.Path;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by cr on 16-12-20.
 */
@Component(immediate = true)
@Service
public class MptcpConnectionManager implements MptcpConnectionService {

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
    public void addMptcpConnection(MptcpToken token, MptcpConnection mptcpConnection) {

    }

    @Override
    public Iterable<MptcpConnection> getMptcpConnections() {
        return null;
    }

    @Override
    public MptcpConnection getMptcpConnectionByToken(MptcpToken token) {
        return null;
    }

    @Override
    public void allocatePath(MptcpToken token, Path path) {

    }

    @Override
    public Path getAllocatedPath(MptcpToken token) {
        return null;
    }
}
