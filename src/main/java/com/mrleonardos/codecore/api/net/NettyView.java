package com.mrleonardos.codecore.api.net;

import io.netty.buffer.ByteBuf;

/**
 * Переходник для перегрузок {@link Codec} на {@code ByteBuf}.
 *
 * <p>
 * Живёт здесь, а не в {@code internal}, потому что api-джар обязан собираться у того, кто взял только его.
 * Уходит вместе с самими перегрузками, когда потребители переедут на {@link CodeBuffer}; настоящая
 * реализация поверх netty у ядра одна, {@code internal/net/NettyBuffer}.
 */
final class NettyView implements CodeBuffer {

    private final ByteBuf buffer;

    NettyView(ByteBuf buffer) {
        this.buffer = buffer;
    }

    @Override
    public void writeInt(int value) {
        buffer.writeInt(value);
    }

    @Override
    public int readInt() {
        return buffer.readInt();
    }

    @Override
    public void writeLong(long value) {
        buffer.writeLong(value);
    }

    @Override
    public long readLong() {
        return buffer.readLong();
    }

    @Override
    public void writeBoolean(boolean value) {
        buffer.writeBoolean(value);
    }

    @Override
    public boolean readBoolean() {
        return buffer.readBoolean();
    }

    @Override
    public void writeBytes(byte[] value) {
        buffer.writeBytes(value);
    }

    @Override
    public void readBytes(byte[] into) {
        buffer.readBytes(into);
    }

    @Override
    public int readableBytes() {
        return buffer.readableBytes();
    }
}
