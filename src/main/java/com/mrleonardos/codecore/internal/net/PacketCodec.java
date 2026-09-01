package com.mrleonardos.codecore.internal.net;

import com.mrleonardos.codecore.api.net.Packet;

import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Перевод пакета в байты и обратно.
 *
 * <p>
 * Номер типа в потоке ставит сам Forge по таблице, которую наполняет канал при регистрации.
 */
final class PacketCodec extends FMLIndexedMessageToMessageCodec<Packet> {

    @Override
    public void encodeInto(ChannelHandlerContext context, Packet packet, ByteBuf target) {
        packet.write(target);
    }

    @Override
    public void decodeInto(ChannelHandlerContext context, ByteBuf source, Packet packet) {
        packet.read(source);
    }
}
