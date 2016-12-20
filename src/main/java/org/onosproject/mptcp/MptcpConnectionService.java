package org.onosproject.mptcp;

import org.onosproject.net.Path;

/**
 * Created by cr on 16-12-20.
 */
public interface MptcpConnectionService {
    void addMptcpConnection(MptcpToken token, MptcpConnection mptcpConnection);
    Iterable<MptcpConnection> getMptcpConnections();
    MptcpConnection getMptcpConnectionByToken(MptcpToken token);
    void allocatePath(MptcpToken token, Path path);
    Path getAllocatedPath(MptcpToken token);
}
