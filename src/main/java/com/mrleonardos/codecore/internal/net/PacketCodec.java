package com.mrleonardos.codecore.internal.net;

import com.mrleonardos.codecore.api.net.Packet;

import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Перевод пакета в байты и обратно.
 *
 * <p>
 * Номер типа в потоке ставит сам Forge по таблице, которую наполняет канал при регистрации. Здесь же
 * буфер сетевой библиотеки заворачивается в {@code CodeBuffer}: дальше по коду его не видит никто.
 */
final class PacketCodec extends FMLIndexedMessageToMessageCodec<Packet> {

    @Override
    public void encodeInto(ChannelHandlerContext context, Packet packet, ByteBuf target) {
        packet.write(new NettyBuffer(target));
    }

    @Override
    public void decodeInto(ChannelHandlerContext context, ByteBuf source, Packet packet) {
        packet.read(new NettyBuffer(source));
    }
}
