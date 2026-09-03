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
 * Переопределять надо пару на {@link CodeBuffer}: пара на {@code ByteBuf} уходит со сносом старых
 * подписей вместе с надтипом {@code IMessage}. Пока живы обе, наследник реализует ровно одну из них, и
 * ядро само сводит их между собой. Не реализовать ни одной сборка не запретит, поэтому такой пакет
 * падает {@link UnsupportedOperationException} на первой же отправке, а не пишет в сеть пустоту.
 *
 * <p>
 * {@link #handle(PacketContext)} вызывается уже в главном потоке, поэтому внутри можно спокойно работать
 * с миром.
 */
public abstract class Packet implements IMessage {

    /** Записать содержимое в буфер. */
    public void write(CodeBuffer buffer) {
        throw new UnsupportedOperationException(getClass().getName() + " must override write(CodeBuffer)");
    }

    /** Прочитать содержимое в том же порядке, в каком оно записывалось. */
    public void read(CodeBuffer buffer) {
        throw new UnsupportedOperationException(getClass().getName() + " must override read(CodeBuffer)");
    }

    /** Записать содержимое в буфер; уходит со сносом старых подписей. */
    public void write(ByteBuf buffer) {
        write(new NettyView(buffer));
    }

    /** Прочитать содержимое в том же порядке; уходит со сносом старых подписей. */
    public void read(ByteBuf buffer) {
        read(new NettyView(buffer));
    }

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
