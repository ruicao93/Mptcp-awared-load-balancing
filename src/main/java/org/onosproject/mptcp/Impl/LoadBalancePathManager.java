package org.onosproject.mptcp.Impl;

import com.google.common.collect.Lists;
import org.apache.felix.scr.annotations.*;
import org.onlab.packet.IpAddress;
import org.onosproject.incubator.net.PortStatisticsService;
import org.onosproject.mptcp.*;
import org.onosproject.net.DeviceId;
import org.onosproject.net.HostLocation;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.device.PortStatistics;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.statistic.StatisticService;
import org.onosproject.net.topology.TopologyService;
import org.slf4j.Logger;

import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by cr on 16-12-20.
 */
@Component(immediate = true)
@Service
public class LoadBalancePathManager implements LoadBalancePathService {


    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TopologyService topologyService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PortStatisticsService portStatisticsService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected StatisticService statisticService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MptcpConnectionService mptcpConnectionService;

    @Activate
    public void activate() {
        log.info("Started...");
    }

    @Deactivate
    public void deactivate() {
        log.info("Stopped...");
    }

    @Override
    public Path getOptimalPath(HostLocation srcHostLocation, HostLocation dstHostLocation) {
        return getOptimalPath(srcHostLocation.deviceId(), dstHostLocation.deviceId());
    }

    @Override
    public Path getOptimalPath(DeviceId srcDeviceId, DeviceId dstDeviceId) {
        if (srcDeviceId.equals(dstDeviceId)) return null;
        Set<Path> pathSet= topologyService.getPaths(topologyService.currentTopology(), srcDeviceId,
                dstDeviceId);
        if (pathSet.isEmpty()) {
            return null;
        }
        // 1. sort path by conflict factor, bw, hop
        List<Path> pathListOld = Lists.newArrayList(pathSet);
        Map<Path, Long> pathLoadMap = new HashMap<>();
        for (Path path : pathListOld) {
            pathLoadMap.put(path, getMaxLoadOnPath(path));
        }
        pathListOld.sort((p1, p2) ->{
            if ( pathLoadMap.get(p1) < pathLoadMap.get(p2)) {
                return -1;
            } else if (pathLoadMap.get(p1) > pathLoadMap.get(p2)) {
                return 1;
            } else {
                return p1.links().size() - p2.links().size();
            }
        });
        return (Path) pathSet.toArray()[0];
    }

    @Override
    public Path getOptimalPath(DeviceId srcDeviceId, DeviceId dstDeviceId, MptcpToken token) {
        // 1. find possible path set
        Set<Path> pathSet = getOptimalPathSet(srcDeviceId, dstDeviceId);
        if (null == pathSet || pathSet.isEmpty()) return null;
        // 2. get initial connection path
        Path initialPath = mptcpConnectionService.getMptcpConnectionByToken(token).getAllocatePath();
        // 3. caculate conflict factor
        List<Path> pathListOld = Lists.newArrayList(pathSet);
        Map<Path, Long> pathLoadMap = new HashMap<>();
        Map<Path, Double> pathConflictFactorMap = new HashMap<>();
        for (Path path : pathListOld) {
            pathLoadMap.put(path, getMaxLoadOnPath(path));
            int conflictLinkNum = 0;
            for (Link subLink : path.links()) {
                for (Link initialLink : initialPath.links()) {
                    if (subLink.equals(initialLink)) {
                        ++conflictLinkNum;
                    }
                }
            }
            double conflictFactor = conflictLinkNum/Math.max(path.links().size(), initialPath.links().size());
            pathConflictFactorMap.put(path, conflictFactor);
        }
        // 4. sort path by conflict factor, bw, hop
        pathListOld.sort((p1, p2) ->{
            if (pathConflictFactorMap.get(p1) < pathConflictFactorMap.get(p2)) {
                return -1;
            } else if (pathConflictFactorMap.get(p1) > pathConflictFactorMap.get(p2)) {
                return 1;
            } else {
                if ( pathLoadMap.get(p1) < pathLoadMap.get(p2)) {
                    return -1;
                } else if (pathLoadMap.get(p1) > pathLoadMap.get(p2)) {
                    return 1;
                } else {
                    return p1.links().size() - p2.links().size();
                }
            }
        });
        return pathListOld.get(0);
    }

    @Override
    public Path getOptimalPath(MptcpConnection mptcpConnection) {
        return null;
    }

    @Override
    public Path getOptimalPath(MptcpSubFlow subFlow) {
        return null;
    }

    public Set<Path> getOptimalPathSet(DeviceId srcDeviceId, DeviceId dstDeviceId) {
        if (srcDeviceId.equals(dstDeviceId)) return null;
        Set<Path> pathSet= topologyService.getPaths(topologyService.currentTopology(), srcDeviceId,
                dstDeviceId);
        return pathSet;
    }

    private long getMaxLoadOnPath(Path path) {
        Link maxLoadLink = statisticService.max(path);
        if (null == maxLoadLink) {
            maxLoadLink = path.links().get(0);
        }
        return statisticService.load(maxLoadLink).rate();
    }
}
