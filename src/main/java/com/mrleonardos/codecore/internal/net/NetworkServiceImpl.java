package com.mrleonardos.codecore.internal.net;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.net.NetChannel;
import com.mrleonardos.codecore.api.net.NetLimits;
import com.mrleonardos.codecore.api.net.NetworkService;
import com.mrleonardos.codecore.internal.schedule.SchedulerImpl;

public final class NetworkServiceImpl implements NetworkService {

    private final Map<String, NetChannel> channels = new HashMap<>();
    private final SchedulerImpl scheduler;
    private final Logger log;

    public NetworkServiceImpl(SchedulerImpl scheduler, Logger log) {
        this.scheduler = scheduler;
        this.log = log;
    }

    @Override
    public NetChannel open(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Channel name must not be empty");
        }
        if (name.length() > NetLimits.MAX_CHANNEL_NAME_LENGTH) {
            throw new IllegalArgumentException(
                "Channel name " + name + " is longer than " + NetLimits.MAX_CHANNEL_NAME_LENGTH + " characters");
        }
        return channels.computeIfAbsent(name, key -> new NetChannelImpl(key, scheduler, log));
    }
}
