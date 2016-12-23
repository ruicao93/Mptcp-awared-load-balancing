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
@Command(scope = "onos", name="mptcp-list",
  description = "Get mptcp initial connections list.")
public class MptcpConnectionListCommand extends AbstractShellCommand {

    @Override
    protected void execute() {
        MptcpConnectionService connectionService = getService(MptcpConnectionService.class);
        Iterable<MptcpConnection> connectionIterator = connectionService.getMptcpConnections();
        List<MptcpConnection> connectionList = newArrayList(connectionIterator);
        print("Mptcp initial connection num:%d", connectionList.size());
        int i = 0;
        for (MptcpConnection connection : connectionIterator) {
            print("%d: %s", ++i, connection.toString());
        }
    }
}
