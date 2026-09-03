package com.mrleonardos.codecore.internal.net;

import java.util.Optional;

import net.minecraft.entity.player.EntityPlayerMP;

import com.mrleonardos.codecore.api.actor.PlayerRef;
import com.mrleonardos.codecore.api.net.PacketContext;
import com.mrleonardos.codecore.platform.PlayerRefs;

public final class PacketContextImpl implements PacketContext {

    private final EntityPlayerMP sender;
    private final boolean onServer;

    PacketContextImpl(EntityPlayerMP sender, boolean onServer) {
        this.sender = sender;
        this.onServer = onServer;
    }

    @Override
    public Optional<PlayerRef> player() {
        return Optional.ofNullable(PlayerRefs.of(sender));
    }

    @Override
    public EntityPlayerMP sender() {
        return sender;
    }

    @Override
    public boolean onServer() {
        return onServer;
    }
}
