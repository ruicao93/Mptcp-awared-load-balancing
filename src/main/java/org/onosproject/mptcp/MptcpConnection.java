package org.onosproject.mptcp;

import org.onlab.packet.IpAddress;
import org.onosproject.net.Path;
import org.onosproject.net.flow.FlowId;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cr on 16-12-20.
 */
public class MptcpConnection {

    private IpAddress primarySourceIp;
    private IpAddress primaryDestinationIp;

    private int primarySourcePort;
    private int primaryDestinationPort;

    private MptcpToken token;
    private MptcpKey senderKey;
    private MptcpKey receiverKey;

    private Path allocatePath;
    private FlowId lastHopFlowId;

    private List<MptcpSubFlow> subFlows = new ArrayList<>();

    public MptcpConnection(IpAddress primarySourceIp, IpAddress primaryDestinationIp, int primarySourcePort, int primaryDestinationPort) {
        this.primarySourceIp = primarySourceIp;
        this.primaryDestinationIp = primaryDestinationIp;
        this.primarySourcePort = primarySourcePort;
        this.primaryDestinationPort = primaryDestinationPort;
    }

    public IpAddress getPrimarySourceIp() {
        return primarySourceIp;
    }

    public void setPrimarySourceIp(IpAddress primarySourceIp) {
        this.primarySourceIp = primarySourceIp;
    }

    public IpAddress getPrimaryDestinationIp() {
        return primaryDestinationIp;
    }

    public void setPrimaryDestinationIp(IpAddress primaryDestinationIp) {
        this.primaryDestinationIp = primaryDestinationIp;
    }

    public int getPrimarySourcePort() {
        return primarySourcePort;
    }

    public void setPrimarySourcePort(int primarySourcePort) {
        this.primarySourcePort = primarySourcePort;
    }

    public int getPrimaryDestinationPort() {
        return primaryDestinationPort;
    }

    public void setPrimaryDestinationPort(int primaryDestinationPort) {
        this.primaryDestinationPort = primaryDestinationPort;
    }

    public void setToken(MptcpToken token) {
        this.token = token;
    }

    public void setSenderKey(MptcpKey senderKey) {
        this.senderKey = senderKey;
    }

    public void setReceiverKey(MptcpKey receiverKey) {
        this.receiverKey = receiverKey;
    }

    public MptcpToken getToken() {
        return token;
    }

    public MptcpKey getSenderKey() {
        return senderKey;
    }

    public MptcpKey getReceiverKey() {
        return receiverKey;
    }

    public List<MptcpSubFlow> getSubFlows() {
        return subFlows;
    }

    public Path getAllocatePath() {
        return allocatePath;
    }

    public void setAllocatePath(Path allocatePath) {
        this.allocatePath = allocatePath;
    }

    public FlowId getLastHopFlowId() {
        return lastHopFlowId;
    }

    public void setLastHopFlowId(FlowId lastHopFlowId) {
        this.lastHopFlowId = lastHopFlowId;
    }

    public void addSubflow(MptcpSubFlow subFlow) {
        subFlows.add(subFlow);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MptcpConnection that = (MptcpConnection) o;

        if (primarySourcePort != that.primarySourcePort) return false;
        if (primaryDestinationPort != that.primaryDestinationPort) return false;
        if (primarySourceIp != null ? !primarySourceIp.equals(that.primarySourceIp) : that.primarySourceIp != null)
            return false;
        if (primaryDestinationIp != null ? !primaryDestinationIp.equals(that.primaryDestinationIp) : that.primaryDestinationIp != null)
            return false;
        if (token != null ? !token.equals(that.token) : that.token != null) return false;
        if (senderKey != null ? !senderKey.equals(that.senderKey) : that.senderKey != null) return false;
        if (receiverKey != null ? !receiverKey.equals(that.receiverKey) : that.receiverKey != null) return false;
        if (allocatePath != null ? !allocatePath.equals(that.allocatePath) : that.allocatePath != null) return false;
        if (lastHopFlowId != null ? !lastHopFlowId.equals(that.lastHopFlowId) : that.lastHopFlowId != null)
            return false;
        return subFlows != null ? subFlows.equals(that.subFlows) : that.subFlows == null;

    }

    @Override
    public int hashCode() {
        int result = primarySourceIp != null ? primarySourceIp.hashCode() : 0;
        result = 31 * result + (primaryDestinationIp != null ? primaryDestinationIp.hashCode() : 0);
        result = 31 * result + primarySourcePort;
        result = 31 * result + primaryDestinationPort;
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (senderKey != null ? senderKey.hashCode() : 0);
        result = 31 * result + (receiverKey != null ? receiverKey.hashCode() : 0);
        result = 31 * result + (allocatePath != null ? allocatePath.hashCode() : 0);
        result = 31 * result + (lastHopFlowId != null ? lastHopFlowId.hashCode() : 0);
        result = 31 * result + (subFlows != null ? subFlows.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MptcpConnection{" +
                "primarySourceIp=" + primarySourceIp +
                ", primaryDestinationIp=" + primaryDestinationIp +
                ", primarySourcePort=" + primarySourcePort +
                ", primaryDestinationPort=" + primaryDestinationPort +
                ", token=" + token +
                ", senderKey=" + senderKey +
                ", receiverKey=" + receiverKey +
                ", allocatePath=" + allocatePath +
                ", lastHopFlowId=" + lastHopFlowId +
                ", subFlows=" + subFlows +
                '}';
    }
}
