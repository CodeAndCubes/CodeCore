package com.mrleonardos.codecore.internal.net;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.player.EntityPlayerMP;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.net.NetChannel;
import com.mrleonardos.codecore.api.net.Packet;
import com.mrleonardos.codecore.api.net.PacketSide;
import com.mrleonardos.codecore.internal.schedule.SchedulerImpl;

import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.FMLOutboundHandler.OutboundTarget;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import io.netty.channel.ChannelFutureListener;

/**
 * Канал поверх сетевого слоя Forge.
 *
 * <p>
 * Номер пакета в протоколе выдаётся по порядку регистрации, поэтому обе стороны обязаны регистрировать
 * типы одинаково. Повторную регистрацию одного типа канал не пропускает: разъехавшаяся нумерация даёт
 * пакеты, которые читаются не тем классом, и ловить это в рантайме крайне неприятно.
 *
 * <p>
 * Готовая обёртка Forge здесь не годится: она заводит отдельный обработчик на каждый тип пакета и
 * называет его по имени класса, так что один общий обработчик пропускает только первый тип.
 */
public final class NetChannelImpl implements NetChannel {

    private static final int MAX_PACKET_TYPES = 256;

    private final String name;
    private final Map<Class<? extends Packet>, PacketSide> sides = new ConcurrentHashMap<>();
    private final PacketCodec codec = new PacketCodec();
    private final EnumMap<Side, FMLEmbeddedChannel> channels;

    NetChannelImpl(String name, SchedulerImpl scheduler, Logger log) {
        this.name = name;
        this.channels = NetworkRegistry.INSTANCE
            .newChannel(name, codec, new PacketDispatcher(name, sides, scheduler, log));
    }

    @Override
    public <T extends Packet> void register(Class<T> type, PacketSide side) {
        if (sides.containsKey(type)) {
            throw new IllegalStateException("Packet " + type.getName() + " is already registered on channel " + name);
        }
        int discriminator = sides.size();
        if (discriminator >= MAX_PACKET_TYPES) {
            throw new IllegalStateException(
                "Channel " + name + " cannot hold more than " + MAX_PACKET_TYPES + " packet types");
        }
        codec.addDiscriminator(discriminator, type);
        sides.put(type, side);
    }

    @Override
    public void toPlayer(Packet packet, EntityPlayerMP player) {
        FMLEmbeddedChannel channel = outbound(packet, PacketSide.CLIENT_BOUND);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(OutboundTarget.PLAYER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
            .set(player);
        send(channel, packet);
    }

    @Override
    public void toPlayers(Packet packet, Iterable<EntityPlayerMP> players) {
        for (EntityPlayerMP player : players) {
            toPlayer(packet, player);
        }
    }

    @Override
    public void toAll(Packet packet) {
        FMLEmbeddedChannel channel = outbound(packet, PacketSide.CLIENT_BOUND);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(OutboundTarget.ALL);
        send(channel, packet);
    }

    @Override
    public void toServer(Packet packet) {
        FMLEmbeddedChannel channel = outbound(packet, PacketSide.SERVER_BOUND);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(OutboundTarget.TOSERVER);
        send(channel, packet);
    }

    private FMLEmbeddedChannel outbound(Packet packet, PacketSide direction) {
        PacketSide declared = sides.get(packet.getClass());
        if (declared == null) {
            throw new IllegalStateException(
                "Packet " + packet.getClass()
                    .getName() + " is not registered on channel " + name);
        }
        if (declared != direction) {
            throw new IllegalStateException(
                "Packet " + packet.getClass()
                    .getName() + " is " + declared + " and cannot be sent as " + direction);
        }
        return channels.get(direction == PacketSide.CLIENT_BOUND ? Side.SERVER : Side.CLIENT);
    }

    private void send(FMLEmbeddedChannel channel, Packet packet) {
        channel.writeAndFlush(packet)
            .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }
}
