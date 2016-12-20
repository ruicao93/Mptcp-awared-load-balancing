package org.onosproject.mptcp.Impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketProcessor;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by cr on 16-12-20.
 */
@Component(immediate = true)
public class MptcpAwaredLoadBalancer {

    private final Logger log = getLogger(getClass());

    @Activate
    public void activate() {
        log.info("Started...");
    }

    @Deactivate
    public void deactivate() {
        log.info("Stopped...");
    }

    /**
     * Only handle mptcp packet.
     */
    private class ReactivePacketProcessor implements PacketProcessor {
        @Override
        public void process(PacketContext context) {
            // 1. check if is MPTCP packet, if not pass.
            // 2. Capable or Join ?
            // 2.1 Capable
            // 2.2 Join
        }
    }
}
