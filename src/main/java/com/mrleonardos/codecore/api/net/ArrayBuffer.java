package com.mrleonardos.codecore.api.net;

import java.util.Arrays;

/**
 * Буфер поверх массива байт: тот же {@link CodeBuffer}, но без единого класса сетевой библиотеки.
 *
 * <p>
 * Нужен там, где канала нет, а пакет проверить надо: круговой прогон {@code write} и {@code read} в тесте
 * мода. Настоящий буфер ядра сидит на netty и требует живого соединения, поэтому без этой замены каждый
 * мод линейки писал бы девять методов заново, и копии успели бы разойтись.
 *
 * <p>
 * Здесь же {@link Codec} проверяется теми же тестами, что и на реализации ядра поверх netty: расхождение
 * между ними тут же видно, а чужому моду видно, что своей реализации буфера хватает.
 */
public final class ArrayBuffer implements CodeBuffer {

    private static final int INITIAL = 16;
    private static final int INT_BYTES = 4;
    private static final int BYTE_MASK = 0xFF;

    private byte[] bytes = new byte[INITIAL];
    private int write;
    private int read;

    @Override
    public void writeInt(int value) {
        room(INT_BYTES);
        bytes[write++] = (byte) (value >>> 24);
        bytes[write++] = (byte) (value >>> 16);
        bytes[write++] = (byte) (value >>> 8);
        bytes[write++] = (byte) value;
    }

    @Override
    public int readInt() {
        require(INT_BYTES);
        return ((bytes[read++] & BYTE_MASK) << 24) | ((bytes[read++] & BYTE_MASK) << 16)
            | ((bytes[read++] & BYTE_MASK) << 8)
            | (bytes[read++] & BYTE_MASK);
    }

    @Override
    public void writeLong(long value) {
        writeInt((int) (value >>> 32));
        writeInt((int) value);
    }

    @Override
    public long readLong() {
        long high = readInt() & 0xFFFFFFFFL;
        long low = readInt() & 0xFFFFFFFFL;
        return (high << 32) | low;
    }

    @Override
    public void writeBoolean(boolean value) {
        room(1);
        bytes[write++] = (byte) (value ? 1 : 0);
    }

    @Override
    public boolean readBoolean() {
        require(1);
        return bytes[read++] != 0;
    }

    @Override
    public void writeBytes(byte[] value) {
        room(value.length);
        System.arraycopy(value, 0, bytes, write, value.length);
        write += value.length;
    }

    @Override
    public void readBytes(byte[] into) {
        require(into.length);
        System.arraycopy(bytes, read, into, 0, into.length);
        read += into.length;
    }

    @Override
    public int readableBytes() {
        return write - read;
    }

    private void room(int needed) {
        if (write + needed > bytes.length) {
            bytes = Arrays.copyOf(bytes, Math.max(bytes.length * 2, write + needed));
        }
    }

    private void require(int needed) {
        if (readableBytes() < needed) {
            throw new MalformedPacketException(
                "Buffer is out of bytes: " + needed + " needed, " + readableBytes() + " left");
        }
    }
}
