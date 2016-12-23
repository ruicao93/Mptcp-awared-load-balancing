package org.onosproject.mptcp.Impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.felix.scr.annotations.*;
import org.apache.http.annotation.Immutable;
import org.onlab.packet.*;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.mptcp.*;
import org.onosproject.net.*;
import org.onosproject.net.flow.*;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.host.HostService;
import org.onosproject.net.packet.*;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.net.topology.TopologyService;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by cr on 16-12-20.
 */
@Component(immediate = true)
public class MptcpAwaredLoadBalancer {

    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MptcpConnectionService mptcpConnectionService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LoadBalancePathService loadBalancePathService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TopologyService topologyService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowObjectiveService flowObjectiveService;

    private PacketProcessor mptcpPacketProcessor = new ReactivePacketProcessor();

    private ApplicationId appId;

    private int flowTimeout = 10;
    private int flowPriority = 10;

    private ProviderId providerId = ProviderId.NONE;

    @Activate
    public void activate() {
        appId = coreService.registerApplication("org.onosproject.loadbalance");

        packetService.addProcessor(mptcpPacketProcessor, PacketProcessor.director(1));
        log.info("Started...");
    }

    @Deactivate
    public void deactivate() {
        packetService.removeProcessor(mptcpPacketProcessor);
        log.info("Stopped...");
    }

    boolean hasSYN(short flag) {
        return (flag & 0x02) != 0;
    }

    boolean hasACK(short flag) {
        return (flag & 0x10) != 0;
    }

    boolean hasSYNAndACK(short flag) {
        return hasACK(flag) && hasSYN(flag);
    }

    /**
     * Only handle mptcp packet.
     */
    private class ReactivePacketProcessor implements PacketProcessor {
        @Override
        public void process(PacketContext context) {
            // Only handle MPTCP here.
            if (context.isHandled()) {
                return;
            }
            InboundPacket inPkt = context.inPacket();
            Ethernet ethPkt = inPkt.parsed();
            if (ethPkt.getEtherType() != Ethernet.TYPE_IPV4) {
                return;
            }
            IPv4 ipv4Packet = (IPv4) ethPkt.getPayload();
            if (ipv4Packet.getProtocol() != IPv4.PROTOCOL_TCP) {
                return;
            }
            IpAddress srcIp = Ip4Address.valueOf(ipv4Packet.getSourceAddress());
            IpAddress dstIp = Ip4Address.valueOf(ipv4Packet.getDestinationAddress());
            TCP tcpPacket = (TCP) ipv4Packet.getPayload();
            int srcPort = tcpPacket.getSourcePort();
            int dstPort = tcpPacket.getDestinationPort();
            MptcpOption mptcpOption = MptcpOption.parse(tcpPacket.getOptions());
            if (null == hostService) {
                log.error("HostService has no reference instance.");
            }
            Set<Host> dstHosts = hostService.getHostsByIp(dstIp);
            if (null == dstHosts || dstHosts.isEmpty()) {
                return;
            }
            HostId srcHostId = HostId.hostId(ethPkt.getSourceMAC());
            HostId dstHostId = HostId.hostId(ethPkt.getDestinationMAC());
            if (dstHostId.mac().isLinkLocal()) {
                return;
            }
            Host srcHost = hostService.getHost(srcHostId);
            Host dstHost = hostService.getHost(dstHostId);
            // 1. check if is MPTCP packet, if not pass.
            if (!mptcpOption.isMptcpEnabled()) {
                log.info("Not Mptcp cry............");
                //packetOut(dstHost.location(), ethPkt);
                return;
            }

            // 2. Capable or Join or normal transition ?
            if (mptcpOption.hasMptcpCapable()) {
                // 2.1 Capable
                log.info("Mptcp capable.");
                // check SYN or SYN_ACK ?
                if (hasSYN(tcpPacket.getFlags()) && !hasACK(tcpPacket.getFlags())) {
                    log.info("Mptcp capable SYN....");
                    // 2.1.1 if SYN: add shakehand connection
                    MptcpKey senderKey = new MptcpKey(mptcpOption.getKey());
                    MptcpConnection handshakeConnection = new MptcpConnection(srcIp, dstIp,
                            srcPort, dstPort);
                    handshakeConnection.setSenderKey(senderKey);
                    mptcpConnectionService.addHandshakeConnection(handshakeConnection);
                    // TODO: find path and install rule
                    Path path = loadBalancePathService.getOptimalPath(srcHost.location(), dstHost.location());
                    installRulesAlongUndirectionalPath(context, srcHost.location(), dstHost.location(), path);
                    mptcpConnectionService.allocateHandShakePath(srcIp, dstIp, srcPort, dstPort, path);
                    handshakeConnection.setAllocatePath(path);
                    // PacketOut to dst point
                    packetOut(dstHost.location(), ethPkt);
                } else if (hasSYNAndACK(tcpPacket.getFlags())) {
                    log.info("Mptcp capable SYN_ACK......");
                    // 2.1.2 if SYN_ACK: complete connection
                    MptcpKey senderKey = new MptcpKey(mptcpOption.getKey());
                    MptcpToken mptcpToken = senderKey.toToken();
                    MptcpConnection mptcpConnection;
                    mptcpConnection = mptcpConnectionService.getMptcpConnectionByToken(mptcpToken);
                    if (null != mptcpConnection) {
                        //packetOut(dstHost.location(), ethPkt);
                        log.info("Token has exist....");
                        return;
                    }
                    mptcpConnection = mptcpConnectionService.getHandshakeConnection(dstIp, srcIp, dstPort, srcPort);
                    if (null == mptcpConnection) {
                        log.info("Failed to find handshake connection when SYN_ACK come.");
                        return;
                    }
                    mptcpConnection.setReceiverKey(senderKey);
                    mptcpConnection.setToken(mptcpToken);
                    mptcpConnectionService.addMptcpConnection(mptcpToken, mptcpConnection);
                    // TODO: find path and install rule
                    //Path path = loadBalancePathService.getOptimalPath(srcHost.location(), dstHost.location());
                    //installRulesAlongPath(context, srcHost.location(), dstHost.location(), path);
                    Path path =mptcpConnection.getAllocatePath();
                    // Reverce path first
                    if (null != path) {
                        path = reversePathAndInternalLinks(path);
                    }
                    installRulesAlongUndirectionalPath(context, srcHost.location(), dstHost.location(), path);
                    // Packet out to src point
                    packetOut(dstHost.location(), ethPkt);
                } else {
                    // TODO
                    //packetOut(dstHost.location(), ethPkt);
                    context.block();
                    return;
                }


            } else if (mptcpOption.hasMptcpJoin()) {
                // 2.2 Join
                log.info("Mptcp join.");
                if (!hasSYN(tcpPacket.getFlags()) || hasACK(tcpPacket.getFlags())) {
                    //TODO
                    log.info("Mptcp join no SYN or contains ACK");
                    return;
                }
                MptcpToken token = new MptcpToken(mptcpOption.getToken());
                MptcpConnection mptcpConnection = mptcpConnectionService.getMptcpConnectionByToken(token);
                if (null == mptcpConnection) {
                    log.info("Failed to find initial connection when establish subflow.");
                    return;
                }
                MptcpSubFlow subFlow = new MptcpSubFlow(token, srcIp, dstIp,
                        srcPort, dstPort);
//                if (mptcpConnection.getSubFlows().contains(subFlow)) {
//                    //packetOut(dstHost.location(), ethPkt);
//                    log.info("Mptcp join attempt also attached to a connection, don't process. ");
//                    return;
//                }
                mptcpConnection.addSubflow(subFlow);
                // TODO: find path for subflow and install rule
                Path path = loadBalancePathService.getOptimalPath(srcHost.location().deviceId(), dstHost.location().deviceId(), token);
                installRulesAlongPath(context, srcHost.location(), dstHost.location(), path);
                subFlow.setPath(path);
                log.info("Establish  subflow ......");
                // packet Out to dst point
                packetOut(dstHost.location(), ethPkt);
            } else {
                if (hasACK(tcpPacket.getFlags())) {
                    packetOut(dstHost.location(), ethPkt);
                }
                log.debug("Not capable or join, just forward it.");
                // FIXME: Just packet out here for MPTCP inspection test.
                //packetOut(dstHost.location(), ethPkt);
                context.block();
            }
        }
    }

    private Path reversePathAndInternalLinks(Path path) {
        if (null == path) return null;
        List<Link> linkListOld = ImmutableList.copyOf(path.links());
        List<Link> linkListNew = new ArrayList();
        for (Link link : linkListOld) {
            Link newLink = DefaultLink.builder()
                    .providerId(providerId)
                    .src(link.dst())
                    .dst(link.src())
                    .type(link.type())
                    .state(link.state())
                    .annotations(link.annotations())
                    .build();
            linkListNew.add(newLink);
        }
        Path newPath = new DefaultPath(providerId, linkListNew, path.cost(), path.annotations());
        return newPath;
    }
    private void flood(){
        // TODO
    }

    private void packetOut(PacketContext context, PortNumber portNumber) {
        context.treatmentBuilder().setOutput(portNumber);
        context.send();
    }

    private void packetOut(ConnectPoint connectPoint, Ethernet ethpkt) {
        TrafficTreatment.Builder builder = DefaultTrafficTreatment.builder();
        builder.setOutput(connectPoint.port());
        packetService.emit(new DefaultOutboundPacket(connectPoint.deviceId(),
                builder.build(), ByteBuffer.wrap(ethpkt.serialize())));
        return;
    }


    private void installRulesAlongPath(PacketContext context, HostLocation srcHostLocation, HostLocation dstHostLocation, Path path) {
        Ethernet inPkt = context.inPacket().parsed();
        IPv4 ipv4Packet = (IPv4) inPkt.getPayload();
        byte ipv4Protocol = ipv4Packet.getProtocol();
        Ip4Prefix matchIp4SrcPrefix =
                Ip4Prefix.valueOf(ipv4Packet.getSourceAddress(),
                        Ip4Prefix.MAX_MASK_LENGTH);
        Ip4Prefix matchIp4DstPrefix =
                Ip4Prefix.valueOf(ipv4Packet.getDestinationAddress(),
                        Ip4Prefix.MAX_MASK_LENGTH);
        TCP tcpPacket = null;
        UDP udpPacket = null;
        TpPort ipSourcePort = null;
        TpPort ipDestinationPort = null;
        if (ipv4Protocol == IPv4.PROTOCOL_TCP) {
            tcpPacket = (TCP) ipv4Packet.getPayload();
            ipSourcePort = TpPort.tpPort(tcpPacket.getSourcePort());
            ipDestinationPort = TpPort.tpPort(tcpPacket.getDestinationPort());
        } else if (ipv4Protocol == IPv4.PROTOCOL_UDP) {
            udpPacket = (UDP) ipv4Packet.getPayload();
            ipSourcePort = TpPort.tpPort(udpPacket.getSourcePort());
            ipDestinationPort = TpPort.tpPort(udpPacket.getDestinationPort());
        }
        // if srcHost and dstHost on a switch
        if (srcHostLocation.deviceId().equals(dstHostLocation.deviceId())) {
            TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
            TrafficSelector.Builder selectorBuilder2 = DefaultTrafficSelector.builder();
            selectorBuilder.matchEthType(Ethernet.TYPE_IPV4)
                    .matchIPSrc(matchIp4SrcPrefix)
                    .matchIPDst(matchIp4DstPrefix)
                    .matchIPProtocol(ipv4Protocol);
            selectorBuilder2.matchEthType(Ethernet.TYPE_IPV4)
                    .matchIPSrc(matchIp4DstPrefix)
                    .matchIPDst(matchIp4SrcPrefix)
                    .matchIPProtocol(ipv4Protocol);
            if (ipv4Protocol == IPv4.PROTOCOL_TCP) {
                selectorBuilder.matchTcpSrc(ipSourcePort)
                        .matchTcpDst(ipDestinationPort);
                selectorBuilder2.matchTcpSrc(ipDestinationPort)
                        .matchTcpDst(ipSourcePort);
            } else if (ipv4Protocol == IPv4.PROTOCOL_UDP)  {
                selectorBuilder.matchUdpSrc(ipSourcePort)
                        .matchUdpDst(ipDestinationPort);
                selectorBuilder2.matchUdpSrc(ipDestinationPort)
                        .matchUdpDst(ipSourcePort);
            }
            // build selector
            selectorBuilder.matchInPort(srcHostLocation.port());
            selectorBuilder2.matchInPort(dstHostLocation.port());
            // build treatment
            TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                    .setOutput(dstHostLocation.port())
                    .build();
            ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
                    .withSelector(selectorBuilder.build())
                    .withTreatment(treatment)
                    .withPriority(flowPriority)
                    .withFlag(ForwardingObjective.Flag.VERSATILE)
                    .fromApp(appId)
                    .makeTemporary(flowTimeout)
                    .add();
            flowObjectiveService.forward(srcHostLocation.deviceId(), forwardingObjective);
            TrafficTreatment treatment2 = DefaultTrafficTreatment.builder()
                    .setOutput(srcHostLocation.port())
                    .build();
            ForwardingObjective forwardingObjective2 = DefaultForwardingObjective.builder()
                    .withSelector(selectorBuilder2.build())
                    .withTreatment(treatment2)
                    .withPriority(flowPriority)
                    .withFlag(ForwardingObjective.Flag.VERSATILE)
                    .fromApp(appId)
                    .makeTemporary(flowTimeout)
                    .add();
            flowObjectiveService.forward(dstHostLocation.deviceId(), forwardingObjective2);
            return;
        }
        if (null == path) return;
        List<Link>  linksSource = path.links();
        List<Link> links = Lists.newArrayList(linksSource);
        Collections.reverse(links);
        Link rightLink = null;
        for (Link link : links) {
            if (null != rightLink) {
                TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
                TrafficSelector.Builder selectorBuilder2 = DefaultTrafficSelector.builder();
                selectorBuilder.matchEthType(Ethernet.TYPE_IPV4)
                        .matchIPSrc(matchIp4SrcPrefix)
                        .matchIPDst(matchIp4DstPrefix)
                        .matchIPProtocol(ipv4Protocol);
                selectorBuilder2.matchEthType(Ethernet.TYPE_IPV4)
                        .matchIPSrc(matchIp4DstPrefix)
                        .matchIPDst(matchIp4SrcPrefix)
                        .matchIPProtocol(ipv4Protocol);
                if (ipv4Protocol == IPv4.PROTOCOL_TCP) {
                    selectorBuilder.matchTcpSrc(ipSourcePort)
                            .matchTcpDst(ipDestinationPort);
                    selectorBuilder2.matchTcpSrc(ipDestinationPort)
                            .matchTcpDst(ipSourcePort);
                } else if (ipv4Protocol == IPv4.PROTOCOL_UDP)  {
                    selectorBuilder.matchUdpSrc(ipSourcePort)
                            .matchUdpDst(ipDestinationPort);
                    selectorBuilder2.matchUdpSrc(ipDestinationPort)
                            .matchUdpDst(ipSourcePort);
                }
                // build selector
                selectorBuilder.matchInPort(link.dst().port());
                selectorBuilder2.matchInPort(link.src().port());
                // build treatment
                TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                        .setOutput(rightLink.src().port())
                        .build();
                TrafficTreatment treatment2 = DefaultTrafficTreatment.builder()
                        .setOutput(rightLink.dst().port())
                        .build();
                ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
                        .withSelector(selectorBuilder.build())
                        .withTreatment(treatment)
                        .withPriority(flowPriority)
                        .withFlag(ForwardingObjective.Flag.VERSATILE)
                        .fromApp(appId)
                        .makeTemporary(flowTimeout)
                        .add();
                ForwardingObjective forwardingObjective2 = DefaultForwardingObjective.builder()
                        .withSelector(selectorBuilder2.build())
                        .withTreatment(treatment2)
                        .withPriority(flowPriority)
                        .withFlag(ForwardingObjective.Flag.VERSATILE)
                        .fromApp(appId)
                        .makeTemporary(flowTimeout)
                        .add();
                flowObjectiveService.forward(link.dst().deviceId(), forwardingObjective);
                flowObjectiveService.forward(link.src().deviceId(), forwardingObjective2);
            }
            if (link.src().equals(path.src())) {
                //    1.1 if the link is on the head, (srcHostLocation. link.src.port)
                TrafficSelector.Builder selectorBuilderHead = DefaultTrafficSelector.builder();
                selectorBuilderHead.matchEthType(Ethernet.TYPE_IPV4)
                        .matchIPSrc(matchIp4SrcPrefix)
                        .matchIPDst(matchIp4DstPrefix)
                        .matchIPProtocol(ipv4Protocol)
                        .matchInPort(srcHostLocation.port());
                TrafficSelector.Builder selectorBuilderHead2 = DefaultTrafficSelector.builder();
                selectorBuilderHead2.matchEthType(Ethernet.TYPE_IPV4)
                        .matchIPSrc(matchIp4DstPrefix)
                        .matchIPDst(matchIp4SrcPrefix)
                        .matchIPProtocol(ipv4Protocol)
                        .matchInPort(srcHostLocation.port());
                if (ipv4Protocol == IPv4.PROTOCOL_TCP) {
                    selectorBuilderHead.matchTcpSrc(ipSourcePort)
                            .matchTcpDst(ipDestinationPort);
                    selectorBuilderHead2.matchTcpSrc(ipDestinationPort)
                            .matchTcpDst(ipSourcePort);
                } else if (ipv4Protocol == IPv4.PROTOCOL_UDP){
                    selectorBuilderHead.matchUdpSrc(ipSourcePort)
                            .matchUdpDst(ipDestinationPort);
                    selectorBuilderHead2.matchUdpSrc(ipDestinationPort)
                            .matchUdpDst(ipSourcePort);
                }
                TrafficTreatment treatmentHead = DefaultTrafficTreatment.builder()
                        .setOutput(link.src().port())
                        .build();
                ForwardingObjective forwardingObjectiveHead = DefaultForwardingObjective.builder()
                        .withSelector(selectorBuilderHead.build())
                        .withTreatment(treatmentHead)
                        .withPriority(flowPriority)
                        .withFlag(ForwardingObjective.Flag.VERSATILE)
                        .fromApp(appId)
                        .makeTemporary(flowTimeout)
                        .add();
                flowObjectiveService.forward(link.src().deviceId(), forwardingObjectiveHead);
                TrafficTreatment treatmentHead2 = DefaultTrafficTreatment.builder()
                        .setOutput(link.dst().port())
                        .build();
                ForwardingObjective forwardingObjectiveHead2 = DefaultForwardingObjective.builder()
                        .withSelector(selectorBuilderHead2.build())
                        .withTreatment(treatmentHead2)
                        .withPriority(flowPriority)
                        .withFlag(ForwardingObjective.Flag.VERSATILE)
                        .fromApp(appId)
                        .makeTemporary(flowTimeout)
                        .add();
                flowObjectiveService.forward(link.dst().deviceId(), forwardingObjectiveHead2);
            }
            if (link.dst().equals(path.dst())) {
                TrafficSelector.Builder selectorBuilderTail = DefaultTrafficSelector.builder();
                selectorBuilderTail.matchEthType(Ethernet.TYPE_IPV4)
                        .matchIPSrc(matchIp4SrcPrefix)
                        .matchIPDst(matchIp4DstPrefix)
                        .matchIPProtocol(ipv4Protocol)
                        .matchInPort(link.dst().port());
                TrafficSelector.Builder selectorBuilderTail2 = DefaultTrafficSelector.builder();
                selectorBuilderTail2.matchEthType(Ethernet.TYPE_IPV4)
                        .matchIPSrc(matchIp4DstPrefix)
                        .matchIPDst(matchIp4SrcPrefix)
                        .matchIPProtocol(ipv4Protocol)
                        .matchInPort(link.src().port());
                // build treatment
                TrafficTreatment treatmentTail = DefaultTrafficTreatment.builder()
                        .setOutput(dstHostLocation.port())
                        .build();
                TrafficTreatment treatmentTail2 = DefaultTrafficTreatment.builder()
                        .setOutput(srcHostLocation.port())
                        .build();
                if (ipv4Protocol == IPv4.PROTOCOL_TCP) {
                    selectorBuilderTail.matchTcpSrc(ipSourcePort)
                            .matchTcpDst(ipDestinationPort);
                    selectorBuilderTail2.matchTcpSrc(ipDestinationPort)
                            .matchTcpDst(ipSourcePort);
                } else if (ipv4Protocol == IPv4.PROTOCOL_UDP){
                    selectorBuilderTail.matchUdpSrc(ipSourcePort)
                            .matchUdpDst(ipDestinationPort);
                    selectorBuilderTail2.matchUdpSrc(ipDestinationPort)
                            .matchUdpDst(ipSourcePort);
                }
                ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
                        .withSelector(selectorBuilderTail.build())
                        .withTreatment(treatmentTail)
                        .withPriority(flowPriority)
                        .withFlag(ForwardingObjective.Flag.VERSATILE)
                        .fromApp(appId)
                        .makeTemporary(flowTimeout)
                        .add();
                flowObjectiveService.forward(link.dst().deviceId(), forwardingObjective);
                ForwardingObjective forwardingObjective2 = DefaultForwardingObjective.builder()
                        .withSelector(selectorBuilderTail2.build())
                        .withTreatment(treatmentTail2)
                        .withPriority(flowPriority)
                        .withFlag(ForwardingObjective.Flag.VERSATILE)
                        .fromApp(appId)
                        .makeTemporary(flowTimeout)
                        .add();
                flowObjectiveService.forward(link.src().deviceId(), forwardingObjective2);
            }
            rightLink = link;
        }
        //packetOut(dstHostLocation, inPkt);
    }

    private void installRulesAlongUndirectionalPath(PacketContext context, HostLocation srcHostLocation, HostLocation dstHostLocation, Path path) {
        Ethernet inPkt = context.inPacket().parsed();
        IPv4 ipv4Packet = (IPv4) inPkt.getPayload();
        byte ipv4Protocol = ipv4Packet.getProtocol();
        Ip4Prefix matchIp4SrcPrefix =
                Ip4Prefix.valueOf(ipv4Packet.getSourceAddress(),
                        Ip4Prefix.MAX_MASK_LENGTH);
        Ip4Prefix matchIp4DstPrefix =
                Ip4Prefix.valueOf(ipv4Packet.getDestinationAddress(),
                        Ip4Prefix.MAX_MASK_LENGTH);
        TCP tcpPacket = null;
        UDP udpPacket = null;
        TpPort ipSourcePort = null;
        TpPort ipDestinationPort = null;
        if (ipv4Protocol == IPv4.PROTOCOL_TCP) {
            tcpPacket = (TCP) ipv4Packet.getPayload();
            ipSourcePort = TpPort.tpPort(tcpPacket.getSourcePort());
            ipDestinationPort = TpPort.tpPort(tcpPacket.getDestinationPort());
        } else if (ipv4Protocol == IPv4.PROTOCOL_UDP) {
            udpPacket = (UDP) ipv4Packet.getPayload();
            ipSourcePort = TpPort.tpPort(udpPacket.getSourcePort());
            ipDestinationPort = TpPort.tpPort(udpPacket.getDestinationPort());
        }

        // if srcHost and dstHost on a switch
        if (srcHostLocation.deviceId().equals(dstHostLocation.deviceId())) {
            TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
            selectorBuilder.matchEthType(Ethernet.TYPE_IPV4)
                    .matchIPSrc(matchIp4SrcPrefix)
                    .matchIPDst(matchIp4DstPrefix)
                    .matchIPProtocol(ipv4Protocol);
            if (ipv4Protocol == IPv4.PROTOCOL_TCP) {
                selectorBuilder.matchTcpSrc(ipSourcePort)
                        .matchTcpDst(ipDestinationPort);
            } else if (ipv4Protocol == IPv4.PROTOCOL_UDP)  {
                selectorBuilder.matchUdpSrc(ipSourcePort)
                        .matchUdpDst(ipDestinationPort);
            }
            // build selector
            selectorBuilder.matchInPort(srcHostLocation.port());
            // build treatment
            TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                    .setOutput(dstHostLocation.port())
                    .build();
            ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
                    .withSelector(selectorBuilder.build())
                    .withTreatment(treatment)
                    .withPriority(flowPriority)
                    .withFlag(ForwardingObjective.Flag.VERSATILE)
                    .fromApp(appId)
                    .makeTemporary(flowTimeout)
                    .add();
            flowObjectiveService.forward(srcHostLocation.deviceId(), forwardingObjective);
            return;
        }
        if (null == path) return;
        List<Link>  linksSource = path.links();
        List<Link> links = Lists.newArrayList(linksSource);
        Collections.reverse(links);
        Link rightLink = null;
        for (Link link : links) {
            if (null != rightLink) {
                TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
                selectorBuilder.matchEthType(Ethernet.TYPE_IPV4)
                        .matchIPSrc(matchIp4SrcPrefix)
                        .matchIPDst(matchIp4DstPrefix)
                        .matchIPProtocol(ipv4Protocol);
                if (ipv4Protocol == IPv4.PROTOCOL_TCP) {
                    selectorBuilder.matchTcpSrc(ipSourcePort)
                            .matchTcpDst(ipDestinationPort);
                } else if (ipv4Protocol == IPv4.PROTOCOL_UDP)  {
                    selectorBuilder.matchUdpSrc(ipSourcePort)
                            .matchUdpDst(ipDestinationPort);
                }
                // build selector
                selectorBuilder.matchInPort(link.dst().port());
                // build treatment
                TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                        .setOutput(rightLink.src().port())
                        .build();
                ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
                        .withSelector(selectorBuilder.build())
                        .withTreatment(treatment)
                        .withPriority(flowPriority)
                        .withFlag(ForwardingObjective.Flag.VERSATILE)
                        .fromApp(appId)
                        .makeTemporary(flowTimeout)
                        .add();
                flowObjectiveService.forward(link.dst().deviceId(), forwardingObjective);
            }
            if (link.src().equals(path.src())) {
                //    1.1 if the link is on the head, (srcHostLocation. link.src.port)
                TrafficSelector.Builder selectorBuilderHead = DefaultTrafficSelector.builder();
                selectorBuilderHead.matchEthType(Ethernet.TYPE_IPV4)
                        .matchIPSrc(matchIp4SrcPrefix)
                        .matchIPDst(matchIp4DstPrefix)
                        .matchIPProtocol(ipv4Protocol)
                        .matchInPort(srcHostLocation.port());
                if (ipv4Protocol == IPv4.PROTOCOL_TCP) {
                    selectorBuilderHead.matchTcpSrc(ipSourcePort)
                            .matchTcpDst(ipDestinationPort);
                } else if (ipv4Protocol == IPv4.PROTOCOL_UDP){
                    selectorBuilderHead.matchUdpSrc(ipSourcePort)
                            .matchUdpDst(ipDestinationPort);
                }
                TrafficTreatment treatmentHead = DefaultTrafficTreatment.builder()
                        .setOutput(link.src().port())
                        .build();
                ForwardingObjective forwardingObjectiveHead = DefaultForwardingObjective.builder()
                        .withSelector(selectorBuilderHead.build())
                        .withTreatment(treatmentHead)
                        .withPriority(flowPriority)
                        .withFlag(ForwardingObjective.Flag.VERSATILE)
                        .fromApp(appId)
                        .makeTemporary(flowTimeout)
                        .add();
                flowObjectiveService.forward(link.src().deviceId(), forwardingObjectiveHead);
            }
            if (link.dst().equals(path.dst())) {
                TrafficSelector.Builder selectorBuilderTail = DefaultTrafficSelector.builder();
                selectorBuilderTail.matchEthType(Ethernet.TYPE_IPV4)
                        .matchIPSrc(matchIp4SrcPrefix)
                        .matchIPDst(matchIp4DstPrefix)
                        .matchIPProtocol(ipv4Protocol)
                        .matchInPort(link.dst().port());
                // build treatment
                TrafficTreatment treatmentTail = DefaultTrafficTreatment.builder()
                        .setOutput(dstHostLocation.port())
                        .build();
                if (ipv4Protocol == IPv4.PROTOCOL_TCP) {
                    selectorBuilderTail.matchTcpSrc(ipSourcePort)
                            .matchTcpDst(ipDestinationPort);
                } else if (ipv4Protocol == IPv4.PROTOCOL_UDP){
                    selectorBuilderTail.matchUdpSrc(ipSourcePort)
                            .matchUdpDst(ipDestinationPort);
                }
                ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
                        .withSelector(selectorBuilderTail.build())
                        .withTreatment(treatmentTail)
                        .withPriority(flowPriority)
                        .withFlag(ForwardingObjective.Flag.VERSATILE)
                        .fromApp(appId)
                        .makeTemporary(flowTimeout)
                        .add();
                flowObjectiveService.forward(link.dst().deviceId(), forwardingObjective);
            }
            rightLink = link;
        }
        //packetOut(dstHostLocation, inPkt);
    }
}
