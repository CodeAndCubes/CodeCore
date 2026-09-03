package com.mrleonardos.codecore.internal.net;

import java.util.Map;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.net.Packet;
import com.mrleonardos.codecore.api.net.PacketSide;
import com.mrleonardos.codecore.internal.schedule.SchedulerImpl;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Принимает пакет из сетевого потока и передаёт его в главный поток нужной стороны.
 *
 * <p>
 * Один обработчик на весь канал: обе стороны канала используют тот же экземпляр, поэтому сторона берётся
 * не из поля, а из самого соединения.
 *
 * <p>
 * Пакет, пришедший не в свою сторону, отбрасывается: клиент вполне может прислать серверу то, что по
 * протоколу ходит только в обратную сторону.
 */
@Sharable
final class PacketDispatcher extends SimpleChannelInboundHandler<Packet> {

    private final String channelName;
    private final Map<Class<? extends Packet>, PacketSide> sides;
    private final SchedulerImpl scheduler;
    private final Logger log;

    PacketDispatcher(String channelName, Map<Class<? extends Packet>, PacketSide> sides, SchedulerImpl scheduler,
        Logger log) {
        this.channelName = channelName;
        this.sides = sides;
        this.scheduler = scheduler;
        this.log = log;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, Packet packet) {
        Class<? extends Packet> type = packet.getClass();
        PacketSide expected = sides.get(type);
        if (expected == null) {
            log.warn("Dropped unregistered packet {} on channel {}", type.getName(), channelName);
            return;
        }

        Side receiving = context.channel()
            .attr(NetworkRegistry.CHANNEL_SOURCE)
            .get();
        if (receiving != receivingSideOf(expected)) {
            log.warn("Dropped {} received on {} while it is {} only", type.getName(), receiving, expected);
            return;
        }

        boolean onServer = receiving == Side.SERVER;
        EntityPlayerMP sender = onServer ? senderOf(context) : null;
        scheduler.queue(receiving)
            .submit(() -> packet.handle(new PacketContextImpl(sender, onServer)));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        log.error("Packet handling failed on channel " + channelName, cause);
    }

    private static Side receivingSideOf(PacketSide direction) {
        return direction == PacketSide.CLIENT_BOUND ? Side.CLIENT : Side.SERVER;
    }

    private EntityPlayerMP senderOf(ChannelHandlerContext context) {
        INetHandler handler = context.channel()
            .attr(NetworkRegistry.NET_HANDLER)
            .get();
        return handler instanceof NetHandlerPlayServer ? ((NetHandlerPlayServer) handler).playerEntity : null;
    }
}
