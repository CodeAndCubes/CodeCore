package com.mrleonardos.codecore.api.net;

import java.util.Arrays;

/**
 * Буфер поверх массива байт: тот же {@link CodeBuffer}, но без единого класса сетевой библиотеки.
 *
 * <p>
 * Нужен затем, чтобы {@link Codec} проверялся ровно теми же тестами и на нём, и на реализации ядра поверх
 * netty: расхождение между ними тут же видно, а чужому моду видно, что своей реализации буфера хватает.
 */
final class ArrayBuffer implements CodeBuffer {

    private byte[] bytes = new byte[16];
    private int write;
    private int read;

    @Override
    public void writeInt(int value) {
        room(4);
        bytes[write++] = (byte) (value >>> 24);
        bytes[write++] = (byte) (value >>> 16);
        bytes[write++] = (byte) (value >>> 8);
        bytes[write++] = (byte) value;
    }

    @Override
    public int readInt() {
        require(4);
        return ((bytes[read++] & 0xFF) << 24) | ((bytes[read++] & 0xFF) << 16)
            | ((bytes[read++] & 0xFF) << 8)
            | (bytes[read++] & 0xFF);
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
            throw new IndexOutOfBoundsException("буфер кончился: нужно " + needed + ", осталось " + readableBytes());
        }
    }
}
