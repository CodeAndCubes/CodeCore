package com.mrleonardos.codecore.internal.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.net.CodeBuffer;
import com.mrleonardos.codecore.api.net.CodecContract;

import io.netty.buffer.Unpooled;

/** Тот же набор проверок Codec на буфере ядра поверх netty. */
class NettyBufferCodecTest extends CodecContract {

    @Override
    protected CodeBuffer buffer() {
        return new NettyBuffer(Unpooled.buffer());
    }

    @Test
    @DisplayName("девять операций буфера возвращают записанное")
    void primitivesGoThereAndBack() {
        CodeBuffer buffer = buffer();

        buffer.writeInt(-7);
        buffer.writeLong(Long.MIN_VALUE);
        buffer.writeBoolean(true);
        buffer.writeBoolean(false);
        buffer.writeBytes(new byte[] { 1, 2, 3 });

        assertEquals(4 + 8 + 1 + 1 + 3, buffer.readableBytes());
        assertEquals(-7, buffer.readInt());
        assertEquals(Long.MIN_VALUE, buffer.readLong());
        assertTrue(buffer.readBoolean());
        assertFalse(buffer.readBoolean());

        byte[] tail = new byte[3];
        buffer.readBytes(tail);
        assertEquals(0, buffer.readableBytes());
    }
}
