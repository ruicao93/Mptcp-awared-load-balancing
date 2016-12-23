package org.onosproject.mptcp.Impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.onlab.packet.IpAddress;
import org.onosproject.mptcp.MptcpConnection;
import org.onosproject.mptcp.MptcpConnectionService;
import org.onosproject.mptcp.MptcpToken;
import org.onosproject.net.Path;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by cr on 16-12-20.
 */
@Component(immediate = true)
@Service
public class MptcpConnectionManager implements MptcpConnectionService {

    private final Logger log = getLogger(getClass());

    private Map<MptcpToken, MptcpConnection> connectionMap = new HashMap<>();
    private Map<ConnectionKey, MptcpConnection> connectionHelperMap = new HashMap<>();
    private Map<ConnectionKey, MptcpConnection> handshakeConnectionMap = new HashMap<>();
    private Map<MptcpToken, Path> pathMap = new HashMap<>();
    private Map<ConnectionKey, Path> handShakePathMap = new HashMap<>();

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
        connectionMap.put(token, mptcpConnection);
        ConnectionKey connectionKey = new ConnectionKey(mptcpConnection.getPrimarySourceIp(), mptcpConnection.getPrimaryDestinationIp(),
                mptcpConnection.getPrimarySourcePort(), mptcpConnection.getPrimaryDestinationPort());
        ConnectionKey connectionKeyReverse = new ConnectionKey(mptcpConnection.getPrimaryDestinationIp(),mptcpConnection.getPrimarySourceIp() ,
                mptcpConnection.getPrimaryDestinationPort(),mptcpConnection.getPrimarySourcePort());
        connectionHelperMap.put(connectionKey, mptcpConnection);
        connectionHelperMap.put(connectionKeyReverse, mptcpConnection);
    }

    @Override
    public Iterable<MptcpConnection> getMptcpConnections() {
        return connectionMap.values();
    }

    @Override
    public MptcpConnection getMptcpConnectionByToken(MptcpToken token) {
        return connectionMap.get(token);
    }

    @Override
    public void allocatePath(MptcpToken token, Path path) {
        pathMap.put(token, path);
    }

    @Override
    public Path getAllocatedPath(MptcpToken token) {
        return pathMap.get(token);
    }

    @Override
    public MptcpConnection getHandshakeConnection(IpAddress srcIp, IpAddress dstIp, int srcPort, int dstPort) {
        ConnectionKey connectionKey = new ConnectionKey(srcIp, dstIp, srcPort, dstPort);
        return handshakeConnectionMap.get(connectionKey);
    }

    @Override
    public Iterable<MptcpConnection> getHandshakeConnections() {
        return  handshakeConnectionMap.values();
    }

    @Override
    public MptcpConnection addHandshakeConnection(MptcpConnection handshakeConnection) {
        ConnectionKey connectionKey = new ConnectionKey(handshakeConnection.getPrimarySourceIp(),
                handshakeConnection.getPrimaryDestinationIp(),
                handshakeConnection.getPrimarySourcePort(),
                handshakeConnection.getPrimaryDestinationPort());
        return handshakeConnectionMap.put(connectionKey, handshakeConnection);
    }

    @Override
    public void allocateHandShakePath(IpAddress srcIp, IpAddress dstIp, int srcPort, int dstPort, Path path) {
        ConnectionKey connectionKey = new ConnectionKey(srcIp, dstIp, srcPort, dstPort);
        handShakePathMap.put(connectionKey, path);
    }

    @Override
    public Path getAllocatedHandShakePath(IpAddress srcIp, IpAddress dstIp, int srcPort, int dstPort) {
        ConnectionKey connectionKey = new ConnectionKey(srcIp, dstIp, srcPort, dstPort);
        return handShakePathMap.get(connectionKey);
    }

    @Override
    public boolean isConnectionEstablishingOrEstablished(IpAddress srcIp, IpAddress dstIp, int srcPort, int dstPort) {
        ConnectionKey connectionKey = new ConnectionKey(srcIp, dstIp, srcPort, dstPort);
        return handshakeConnectionMap.containsKey(connectionKey) || connectionMap.containsKey(connectionKey)
                || connectionHelperMap.containsKey(connectionKey);
    }

    private class ConnectionKey {
        private IpAddress srcIp;
        private IpAddress dstIp;
        private int srcPort;
        private int dstPort;

        public ConnectionKey(IpAddress srcIp, IpAddress dstIp, int srcPort, int dstPort) {
            this.srcIp = srcIp;
            this.dstIp = dstIp;
            this.srcPort = srcPort;
            this.dstPort = dstPort;
        }

        public IpAddress getSrcIp() {
            return srcIp;
        }

        public void setSrcIp(IpAddress srcIp) {
            this.srcIp = srcIp;
        }

        public IpAddress getDstIp() {
            return dstIp;
        }

        public void setDstIp(IpAddress dstIp) {
            this.dstIp = dstIp;
        }

        public int getSrcPort() {
            return srcPort;
        }

        public void setSrcPort(int srcPort) {
            this.srcPort = srcPort;
        }

        public int getDstPort() {
            return dstPort;
        }

        public void setDstPort(int dstPort) {
            this.dstPort = dstPort;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ConnectionKey that = (ConnectionKey) o;

            if (srcPort != that.srcPort) return false;
            if (dstPort != that.dstPort) return false;
            if (srcIp != null ? !srcIp.equals(that.srcIp) : that.srcIp != null) return false;
            return dstIp != null ? dstIp.equals(that.dstIp) : that.dstIp == null;

        }

        @Override
        public int hashCode() {
            int result = srcIp != null ? srcIp.hashCode() : 0;
            result = 31 * result + (dstIp != null ? dstIp.hashCode() : 0);
            result = 31 * result + srcPort;
            result = 31 * result + dstPort;
            return result;
        }
    }
}
