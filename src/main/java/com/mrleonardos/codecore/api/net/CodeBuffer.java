package com.mrleonardos.codecore.api.net;

/**
 * Буфер пакета: ровно те девять операций, на которых стоит {@link Codec}.
 *
 * <p>
 * Заменяет {@code io.netty.buffer.ByteBuf} на границе api. Netty приезжает вместе с игрой, и в 1.17 сетевой
 * слой Forge переписан целиком, поэтому наследник {@link Packet} не должен знать, чем именно ему подставили
 * байты. Строки, uuid, перечисления и коллекции живут в {@link Codec} поверх этих девяти методов вместе со
 * всеми потолками {@link NetLimits}.
 *
 * <p>
 * Чтение за границей записанного бросает {@link MalformedPacketException}, а не исключение сетевой
 * библиотеки: обработчик пакета ловит одно исключение и продолжает работать.
 */
public interface CodeBuffer {

    void writeInt(int value);

    int readInt();

    void writeLong(long value);

    long readLong();

    void writeBoolean(boolean value);

    boolean readBoolean();

    void writeBytes(byte[] value);

    /** Прочитать столько байт, сколько влезает в массив. */
    void readBytes(byte[] into);

    /** Сколько байт ещё не прочитано. */
    int readableBytes();
}
