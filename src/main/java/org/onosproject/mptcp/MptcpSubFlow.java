package org.onosproject.mptcp;

import org.onlab.packet.IpAddress;

/**
 * Created by cr on 16-12-20.
 */
public class MptcpSubFlow {

    private MptcpConnection pareMptcpConnection;

    private IpAddress sourceIp;
    private IpAddress destinationIp;

    private int sourcePort;
    private int destinationPort;

    public MptcpSubFlow(MptcpConnection pareMptcpConnection, IpAddress sourceIp, IpAddress destinationIp, int sourcePort, int destinationPort) {
        this.pareMptcpConnection = pareMptcpConnection;
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
    }

    public MptcpConnection getPareMptcpConnection() {
        return pareMptcpConnection;
    }

    public void setPareMptcpConnection(MptcpConnection pareMptcpConnection) {
        this.pareMptcpConnection = pareMptcpConnection;
    }

    public IpAddress getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(IpAddress sourceIp) {
        this.sourceIp = sourceIp;
    }

    public IpAddress getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(IpAddress destinationIp) {
        this.destinationIp = destinationIp;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MptcpSubFlow that = (MptcpSubFlow) o;

        if (sourcePort != that.sourcePort) return false;
        if (destinationPort != that.destinationPort) return false;
        if (pareMptcpConnection != null ? !pareMptcpConnection.equals(that.pareMptcpConnection) : that.pareMptcpConnection != null)
            return false;
        if (sourceIp != null ? !sourceIp.equals(that.sourceIp) : that.sourceIp != null) return false;
        return destinationIp != null ? destinationIp.equals(that.destinationIp) : that.destinationIp == null;

    }

    @Override
    public int hashCode() {
        int result = pareMptcpConnection != null ? pareMptcpConnection.hashCode() : 0;
        result = 31 * result + (sourceIp != null ? sourceIp.hashCode() : 0);
        result = 31 * result + (destinationIp != null ? destinationIp.hashCode() : 0);
        result = 31 * result + sourcePort;
        result = 31 * result + destinationPort;
        return result;
    }
}
