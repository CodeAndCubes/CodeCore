package com.mrleonardos.codecore.api.net;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

class CodecTest {

    @Test
    void writesAndReadsValuesBack() {
        ByteBuf buffer = Unpooled.buffer();
        UUID id = UUID.randomUUID();

        Codec.writeString(buffer, "привет, мир");
        Codec.writeUuid(buffer, id);
        Codec.writeEnum(buffer, PacketSide.CLIENT_BOUND);
        Codec.writeStrings(buffer, Arrays.asList("global", "local"));
        Codec.writeBlob(buffer, new byte[] { 1, 2, 3 });
        Codec.writeOptionalString(buffer, null);
        Codec.writeOptionalString(buffer, "present");

        assertEquals("привет, мир", Codec.readString(buffer));
        assertEquals(id, Codec.readUuid(buffer));
        assertEquals(PacketSide.CLIENT_BOUND, Codec.readEnum(buffer, PacketSide.class));
        assertEquals(Arrays.asList("global", "local"), Codec.readStrings(buffer));
        assertArrayEquals(new byte[] { 1, 2, 3 }, Codec.readBlob(buffer));
        assertNull(Codec.readOptionalString(buffer));
        assertEquals("present", Codec.readOptionalString(buffer));
    }

    @Test
    void rejectsDeclaredLengthBeyondLimit() {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(NetLimits.MAX_STRING_BYTES + 1);

        assertThrows(MalformedPacketException.class, () -> Codec.readString(buffer));
    }

    @Test
    void rejectsLengthLongerThanTheActualData() {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(64);
        buffer.writeBytes(new byte[] { 1, 2 });

        assertThrows(MalformedPacketException.class, () -> Codec.readString(buffer));
    }

    @Test
    void rejectsNegativeCollectionSize() {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(-1);

        assertThrows(MalformedPacketException.class, () -> Codec.readStrings(buffer));
    }

    @Test
    void rejectsUnknownEnumOrdinal() {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(99);

        assertThrows(MalformedPacketException.class, () -> Codec.readEnum(buffer, PacketSide.class));
    }

    @Test
    @DisplayName("буфер, кончившийся посреди чтения, даёт то же исключение")
    void truncatedBufferIsMalformedToo() {
        assertThrows(MalformedPacketException.class, () -> Codec.readString(Unpooled.buffer()));
        assertThrows(MalformedPacketException.class, () -> Codec.readBlob(Unpooled.buffer()));
        assertThrows(MalformedPacketException.class, () -> Codec.readUuid(Unpooled.buffer()));
        assertThrows(MalformedPacketException.class, () -> Codec.readStrings(Unpooled.buffer()));
        assertThrows(MalformedPacketException.class, () -> Codec.readUuids(Unpooled.buffer()));
        assertThrows(MalformedPacketException.class, () -> Codec.readOptionalString(Unpooled.buffer()));
        assertThrows(MalformedPacketException.class, () -> Codec.readEnum(Unpooled.buffer(), PacketSide.class));
    }

    @Test
    @DisplayName("коллекция заявила больше элементов, чем прислала")
    void collectionShorterThanDeclaredIsMalformed() {
        ByteBuf strings = Unpooled.buffer();
        strings.writeInt(4096);
        Codec.writeString(strings, "global");
        Codec.writeString(strings, "local");

        assertThrows(MalformedPacketException.class, () -> Codec.readStrings(strings));

        ByteBuf uuids = Unpooled.buffer();
        uuids.writeInt(64);
        Codec.writeUuid(uuids, UUID.randomUUID());

        assertThrows(MalformedPacketException.class, () -> Codec.readUuids(uuids));
    }

    @Test
    @DisplayName("оборванный uuid не берёт половину следующего значения")
    void halfWrittenUuidIsMalformed() {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeLong(1L);

        assertThrows(MalformedPacketException.class, () -> Codec.readUuid(buffer));
    }
}
