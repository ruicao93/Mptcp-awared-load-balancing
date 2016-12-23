package org.onosproject.mptcp.cli;


import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.mptcp.MptcpConnection;
import org.onosproject.mptcp.MptcpConnectionService;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by cr on 16-12-20.
 */
@Command(scope = "onos", name="mptcp-hs-list",
  description = "Get mptcp handshake connections list.")
public class MptcpHandshakeConnectionListCommand extends AbstractShellCommand {

    @Override
    protected void execute() {
        Iterable<MptcpConnection> connectionIterator = getService(MptcpConnectionService.class).getHandshakeConnections();
        List<MptcpConnection> connectionList = newArrayList(connectionIterator);
        print("Mptcp handshake connection num:%d", connectionList.size());
    }
}
