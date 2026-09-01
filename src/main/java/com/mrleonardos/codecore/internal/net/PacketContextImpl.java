package com.mrleonardos.codecore.internal.net;

import net.minecraft.entity.player.EntityPlayerMP;

import com.mrleonardos.codecore.api.net.PacketContext;

public final class PacketContextImpl implements PacketContext {

    private final EntityPlayerMP sender;
    private final boolean onServer;

    PacketContextImpl(EntityPlayerMP sender, boolean onServer) {
        this.sender = sender;
        this.onServer = onServer;
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
