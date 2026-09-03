package com.mrleonardos.codecore.api.net;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import io.netty.buffer.ByteBuf;

/**
 * Чтение и запись значений, которых не хватает в голом {@link CodeBuffer}.
 *
 * <p>
 * Все методы чтения проверяют размеры по {@link NetLimits} до выделения памяти и бросают
 * {@link MalformedPacketException} на подделанных данных: обработчик пакета такой пакет просто уронит, а
 * не съест память сервера.
 *
 * <p>
 * Кончившийся посреди чтения буфер тоже даёт {@link MalformedPacketException}, а не
 * {@code IndexOutOfBoundsException} из сетевой библиотеки: мод-потребитель ловит одно исключение и
 * продолжает работать.
 *
 * <p>
 * Перегрузки на {@code ByteBuf} доживают до переезда потребителей на {@link CodeBuffer} и делают ровно то
 * же самое. Новый код пишется на {@link CodeBuffer}.
 */
public final class Codec {

    private static final int INT_BYTES = 4;
    private static final int LONG_BYTES = 8;
    private static final int BOOLEAN_BYTES = 1;

    public static void writeString(CodeBuffer buffer, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > NetLimits.MAX_STRING_BYTES) {
            throw new MalformedPacketException("String is too long to send: " + bytes.length + " bytes");
        }
        buffer.writeInt(bytes.length);
        buffer.writeBytes(bytes);
    }

    public static String readString(CodeBuffer buffer) {
        require(buffer, INT_BYTES);
        int length = buffer.readInt();
        if (length < 0 || length > NetLimits.MAX_STRING_BYTES) {
            throw new MalformedPacketException("String length out of bounds: " + length);
        }
        if (buffer.readableBytes() < length) {
            throw new MalformedPacketException("String is shorter than declared: " + length);
        }
        byte[] bytes = new byte[length];
        buffer.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void writeUuid(CodeBuffer buffer, UUID value) {
        buffer.writeLong(value.getMostSignificantBits());
        buffer.writeLong(value.getLeastSignificantBits());
    }

    public static UUID readUuid(CodeBuffer buffer) {
        require(buffer, LONG_BYTES * 2);
        long most = buffer.readLong();
        long least = buffer.readLong();
        return new UUID(most, least);
    }

    public static <E extends Enum<E>> void writeEnum(CodeBuffer buffer, E value) {
        buffer.writeInt(value.ordinal());
    }

    public static <E extends Enum<E>> E readEnum(CodeBuffer buffer, Class<E> type) {
        require(buffer, INT_BYTES);
        int ordinal = buffer.readInt();
        E[] values = type.getEnumConstants();
        if (ordinal < 0 || ordinal >= values.length) {
            throw new MalformedPacketException("Unknown " + type.getSimpleName() + " ordinal: " + ordinal);
        }
        return values[ordinal];
    }

    public static void writeStrings(CodeBuffer buffer, Collection<String> values) {
        writeSize(buffer, values.size());
        for (String value : values) {
            writeString(buffer, value);
        }
    }

    public static List<String> readStrings(CodeBuffer buffer) {
        int size = readSize(buffer);
        List<String> values = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            values.add(readString(buffer));
        }
        return values;
    }

    public static void writeUuids(CodeBuffer buffer, Collection<UUID> values) {
        writeSize(buffer, values.size());
        for (UUID value : values) {
            writeUuid(buffer, value);
        }
    }

    public static List<UUID> readUuids(CodeBuffer buffer) {
        int size = readSize(buffer);
        List<UUID> values = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            values.add(readUuid(buffer));
        }
        return values;
    }

    public static void writeBlob(CodeBuffer buffer, byte[] value) {
        if (value.length > NetLimits.MAX_BLOB_BYTES) {
            throw new MalformedPacketException("Blob is too large to send: " + value.length + " bytes");
        }
        buffer.writeInt(value.length);
        buffer.writeBytes(value);
    }

    public static byte[] readBlob(CodeBuffer buffer) {
        require(buffer, INT_BYTES);
        int length = buffer.readInt();
        if (length < 0 || length > NetLimits.MAX_BLOB_BYTES) {
            throw new MalformedPacketException("Blob length out of bounds: " + length);
        }
        if (buffer.readableBytes() < length) {
            throw new MalformedPacketException("Blob is shorter than declared: " + length);
        }
        byte[] value = new byte[length];
        buffer.readBytes(value);
        return value;
    }

    /** Записать необязательное значение: сначала признак наличия, потом само значение. */
    public static void writeOptionalString(CodeBuffer buffer, String value) {
        buffer.writeBoolean(value != null);
        if (value != null) {
            writeString(buffer, value);
        }
    }

    /** Прочитать необязательное значение; {@code null}, если его не было. */
    public static String readOptionalString(CodeBuffer buffer) {
        require(buffer, BOOLEAN_BYTES);
        return buffer.readBoolean() ? readString(buffer) : null;
    }

    public static void writeString(ByteBuf buffer, String value) {
        writeString(view(buffer), value);
    }

    public static String readString(ByteBuf buffer) {
        return readString(view(buffer));
    }

    public static void writeUuid(ByteBuf buffer, UUID value) {
        writeUuid(view(buffer), value);
    }

    public static UUID readUuid(ByteBuf buffer) {
        return readUuid(view(buffer));
    }

    public static <E extends Enum<E>> void writeEnum(ByteBuf buffer, E value) {
        writeEnum(view(buffer), value);
    }

    public static <E extends Enum<E>> E readEnum(ByteBuf buffer, Class<E> type) {
        return readEnum(view(buffer), type);
    }

    public static void writeStrings(ByteBuf buffer, Collection<String> values) {
        writeStrings(view(buffer), values);
    }

    public static List<String> readStrings(ByteBuf buffer) {
        return readStrings(view(buffer));
    }

    public static void writeUuids(ByteBuf buffer, Collection<UUID> values) {
        writeUuids(view(buffer), values);
    }

    public static List<UUID> readUuids(ByteBuf buffer) {
        return readUuids(view(buffer));
    }

    public static void writeBlob(ByteBuf buffer, byte[] value) {
        writeBlob(view(buffer), value);
    }

    public static byte[] readBlob(ByteBuf buffer) {
        return readBlob(view(buffer));
    }

    /** Записать необязательное значение: сначала признак наличия, потом само значение. */
    public static void writeOptionalString(ByteBuf buffer, String value) {
        writeOptionalString(view(buffer), value);
    }

    /** Прочитать необязательное значение; {@code null}, если его не было. */
    public static String readOptionalString(ByteBuf buffer) {
        return readOptionalString(view(buffer));
    }

    private static CodeBuffer view(ByteBuf buffer) {
        return new NettyView(buffer);
    }

    private static void writeSize(CodeBuffer buffer, int size) {
        if (size > NetLimits.MAX_COLLECTION_SIZE) {
            throw new MalformedPacketException("Collection is too large to send: " + size);
        }
        buffer.writeInt(size);
    }

    private static int readSize(CodeBuffer buffer) {
        require(buffer, INT_BYTES);
        int size = buffer.readInt();
        if (size < 0 || size > NetLimits.MAX_COLLECTION_SIZE) {
            throw new MalformedPacketException("Collection size out of bounds: " + size);
        }
        return size;
    }

    private static void require(CodeBuffer buffer, int bytes) {
        int available = buffer.readableBytes();
        if (available < bytes) {
            throw new MalformedPacketException(
                "Packet ended early: " + bytes + " byte(s) needed, " + available + " left");
        }
    }

    private Codec() {}
}
