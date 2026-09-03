package com.mrleonardos.codecore.internal.net;

import com.mrleonardos.codecore.api.net.CodeBuffer;

import io.netty.buffer.ByteBuf;

public final class NettyBuffer implements CodeBuffer {

    private final ByteBuf buffer;

    public NettyBuffer(ByteBuf buffer) {
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
