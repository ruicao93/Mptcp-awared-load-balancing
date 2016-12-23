package org.onosproject.mptcp;

import org.onlab.packet.IpAddress;
import org.onosproject.net.Path;

/**
 * Created by cr on 16-12-20.
 */
public interface MptcpConnectionService {
    void addMptcpConnection(MptcpToken token, MptcpConnection mptcpConnection);
    Iterable<MptcpConnection> getMptcpConnections();
    MptcpConnection getMptcpConnectionByToken(MptcpToken token);
    void allocatePath(MptcpToken token, Path path);
    void allocateHandShakePath(IpAddress srcIp, IpAddress dstIp, int srcPort, int dstPort, Path path);
    Path getAllocatedPath(MptcpToken token);
    Path getAllocatedHandShakePath(IpAddress srcIp, IpAddress dstIp, int srcPort, int dstPort);
    MptcpConnection getHandshakeConnection(IpAddress srcIp, IpAddress dstIp, int srcPort, int dstPort);
    MptcpConnection addHandshakeConnection(MptcpConnection handshakeConnection);
    Iterable<MptcpConnection> getHandshakeConnections();
    boolean isConnectionEstablishingOrEstablished(IpAddress srcIp, IpAddress dstIp, int srcPort, int dstPort);
}
