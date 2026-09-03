package com.mrleonardos.codecore.api.net;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Один набор проверок {@link Codec} на любой реализации {@link CodeBuffer}.
 *
 * <p>
 * Гоняется и на буфере поверх массива байт, и на буфере ядра поверх netty: пакет, написанный чужим модом
 * против api, обязан читаться одинаково там и там.
 */
public abstract class CodecContract {

    /** Новый пустой буфер на каждый вызов. */
    protected abstract CodeBuffer buffer();

    @Test
    void writesAndReadsValuesBack() {
        CodeBuffer buffer = buffer();
        UUID id = UUID.randomUUID();

        Codec.writeString(buffer, "привет, мир");
        Codec.writeUuid(buffer, id);
        Codec.writeEnum(buffer, PacketSide.CLIENT_BOUND);
        Codec.writeStrings(buffer, Arrays.asList("global", "local"));
        Codec.writeUuids(buffer, Arrays.asList(id));
        Codec.writeBlob(buffer, new byte[] { 1, 2, 3 });
        Codec.writeOptionalString(buffer, null);
        Codec.writeOptionalString(buffer, "present");

        assertEquals("привет, мир", Codec.readString(buffer));
        assertEquals(id, Codec.readUuid(buffer));
        assertEquals(PacketSide.CLIENT_BOUND, Codec.readEnum(buffer, PacketSide.class));
        assertEquals(Arrays.asList("global", "local"), Codec.readStrings(buffer));
        assertEquals(Arrays.asList(id), Codec.readUuids(buffer));
        assertArrayEquals(new byte[] { 1, 2, 3 }, Codec.readBlob(buffer));
        assertNull(Codec.readOptionalString(buffer));
        assertEquals("present", Codec.readOptionalString(buffer));
        assertEquals(0, buffer.readableBytes(), "прочитано ровно столько, сколько записано");
    }

    @Test
    void rejectsDeclaredLengthBeyondLimit() {
        CodeBuffer buffer = buffer();
        buffer.writeInt(NetLimits.MAX_STRING_BYTES + 1);

        assertThrows(MalformedPacketException.class, () -> Codec.readString(buffer));
    }

    @Test
    void rejectsLengthLongerThanTheActualData() {
        CodeBuffer buffer = buffer();
        buffer.writeInt(64);
        buffer.writeBytes(new byte[] { 1, 2 });

        assertThrows(MalformedPacketException.class, () -> Codec.readString(buffer));
    }

    @Test
    void rejectsNegativeCollectionSize() {
        CodeBuffer buffer = buffer();
        buffer.writeInt(-1);

        assertThrows(MalformedPacketException.class, () -> Codec.readStrings(buffer));
    }

    @Test
    void rejectsUnknownEnumOrdinal() {
        CodeBuffer buffer = buffer();
        buffer.writeInt(99);

        assertThrows(MalformedPacketException.class, () -> Codec.readEnum(buffer, PacketSide.class));
    }

    @Test
    @DisplayName("строка длиннее потолка в сеть не уходит")
    void refusesToWriteAnOversizedString() {
        CodeBuffer buffer = buffer();
        char[] tooLong = new char[NetLimits.MAX_STRING_BYTES + 1];
        Arrays.fill(tooLong, 'a');

        assertThrows(MalformedPacketException.class, () -> Codec.writeString(buffer, new String(tooLong)));
        assertEquals(0, buffer.readableBytes(), "в буфер ничего не записано");
    }

    @Test
    @DisplayName("буфер, кончившийся посреди чтения, даёт то же исключение")
    void truncatedBufferIsMalformedToo() {
        assertThrows(MalformedPacketException.class, () -> Codec.readString(buffer()));
        assertThrows(MalformedPacketException.class, () -> Codec.readBlob(buffer()));
        assertThrows(MalformedPacketException.class, () -> Codec.readUuid(buffer()));
        assertThrows(MalformedPacketException.class, () -> Codec.readStrings(buffer()));
        assertThrows(MalformedPacketException.class, () -> Codec.readUuids(buffer()));
        assertThrows(MalformedPacketException.class, () -> Codec.readOptionalString(buffer()));
        assertThrows(MalformedPacketException.class, () -> Codec.readEnum(buffer(), PacketSide.class));
    }

    @Test
    @DisplayName("коллекция заявила больше элементов, чем прислала")
    void collectionShorterThanDeclaredIsMalformed() {
        CodeBuffer strings = buffer();
        strings.writeInt(4096);
        Codec.writeString(strings, "global");
        Codec.writeString(strings, "local");

        assertThrows(MalformedPacketException.class, () -> Codec.readStrings(strings));

        CodeBuffer uuids = buffer();
        uuids.writeInt(64);
        Codec.writeUuid(uuids, UUID.randomUUID());

        assertThrows(MalformedPacketException.class, () -> Codec.readUuids(uuids));
    }

    @Test
    @DisplayName("оборванный uuid не берёт половину следующего значения")
    void halfWrittenUuidIsMalformed() {
        CodeBuffer buffer = buffer();
        buffer.writeLong(1L);

        assertThrows(MalformedPacketException.class, () -> Codec.readUuid(buffer));
    }
}
