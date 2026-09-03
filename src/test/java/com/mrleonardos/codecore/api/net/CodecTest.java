package com.mrleonardos.codecore.api.net;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/** Codec на буфере поверх массива байт: ни одного класса сетевой библиотеки в тесте. */
class CodecTest extends CodecContract {

    @Override
    protected CodeBuffer buffer() {
        return new ArrayBuffer();
    }

    @Test
    @DisplayName("перегрузки на ByteBuf пишут те же байты, что и перегрузки на CodeBuffer")
    void byteBufOverloadsWriteTheSameBytes() {
        UUID id = UUID.randomUUID();

        ByteBuf legacy = Unpooled.buffer();
        Codec.writeString(legacy, "привет");
        Codec.writeUuid(legacy, id);
        Codec.writeBlob(legacy, new byte[] { 7, 8 });

        CodeBuffer fresh = buffer();
        Codec.writeString(fresh, "привет");
        Codec.writeUuid(fresh, id);
        Codec.writeBlob(fresh, new byte[] { 7, 8 });

        byte[] written = new byte[legacy.readableBytes()];
        legacy.getBytes(0, written);
        byte[] mirrored = new byte[fresh.readableBytes()];
        fresh.readBytes(mirrored);

        assertArrayEquals(written, mirrored);
        assertEquals("привет", Codec.readString(legacy));
        assertEquals(id, Codec.readUuid(legacy));
        assertArrayEquals(new byte[] { 7, 8 }, Codec.readBlob(legacy));
    }
}
