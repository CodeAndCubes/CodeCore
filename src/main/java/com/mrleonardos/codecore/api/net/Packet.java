package com.mrleonardos.codecore.api.net;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * Базовый пакет мода.
 *
 * <p>
 * Наследнику нужен конструктор без аргументов: по нему пакет создаётся при получении. Чтение и запись
 * должны идти в одном и том же порядке: это единственное, что связывает две стороны.
 *
 * <p>
 * {@link #handle(PacketContext)} вызывается уже в главном потоке, поэтому внутри можно спокойно работать
 * с миром.
 */
public abstract class Packet implements IMessage {

    /** Записать содержимое в буфер. */
    public abstract void write(ByteBuf buffer);

    /** Прочитать содержимое в том же порядке, в каком оно записывалось. */
    public abstract void read(ByteBuf buffer);

    /** Обработать пакет на принимающей стороне. */
    public abstract void handle(PacketContext context);

    @Override
    public final void toBytes(ByteBuf buffer) {
        write(buffer);
    }

    @Override
    public final void fromBytes(ByteBuf buffer) {
        read(buffer);
    }
}
