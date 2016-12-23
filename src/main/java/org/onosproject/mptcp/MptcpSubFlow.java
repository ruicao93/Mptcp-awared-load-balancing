package org.onosproject.mptcp;

import org.onlab.packet.IpAddress;
import org.onosproject.net.Path;

/**
 * Created by cr on 16-12-20.
 */
public class MptcpSubFlow {

    private MptcpToken pareMptcpConnectionToken;

    private IpAddress sourceIp;
    private IpAddress destinationIp;

    private int sourcePort;
    private int destinationPort;

    private Path path;

    public MptcpSubFlow(MptcpToken pareMptcpConnectionToken, IpAddress sourceIp, IpAddress destinationIp, int sourcePort, int destinationPort) {
        this.pareMptcpConnectionToken = pareMptcpConnectionToken;
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
    }

    public MptcpToken getPareMptcpConnectionToken() {
        return pareMptcpConnectionToken;
    }

    public void setPareMptcpConnectionToken(MptcpToken pareMptcpConnectionToken) {
        this.pareMptcpConnectionToken = pareMptcpConnectionToken;
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


    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "MptcpSubFlow{" +
                "pareMptcpConnectionToken=" + pareMptcpConnectionToken +
                ", sourceIp=" + sourceIp +
                ", destinationIp=" + destinationIp +
                ", sourcePort=" + sourcePort +
                ", destinationPort=" + destinationPort +
                ", path=" + path +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MptcpSubFlow that = (MptcpSubFlow) o;

        if (sourcePort != that.sourcePort) return false;
        if (destinationPort != that.destinationPort) return false;
        if (pareMptcpConnectionToken != null ? !pareMptcpConnectionToken.equals(that.pareMptcpConnectionToken) : that.pareMptcpConnectionToken != null)
            return false;
        if (sourceIp != null ? !sourceIp.equals(that.sourceIp) : that.sourceIp != null) return false;
        if (destinationIp != null ? !destinationIp.equals(that.destinationIp) : that.destinationIp != null)
            return false;
        return path != null ? path.equals(that.path) : that.path == null;

    }

    @Override
    public int hashCode() {
        int result = pareMptcpConnectionToken != null ? pareMptcpConnectionToken.hashCode() : 0;
        result = 31 * result + (sourceIp != null ? sourceIp.hashCode() : 0);
        result = 31 * result + (destinationIp != null ? destinationIp.hashCode() : 0);
        result = 31 * result + sourcePort;
        result = 31 * result + destinationPort;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }

}
