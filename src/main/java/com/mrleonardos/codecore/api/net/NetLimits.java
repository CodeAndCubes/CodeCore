package com.mrleonardos.codecore.api.net;

/**
 * Границы, за которые не выходит ни один пакет.
 *
 * <p>
 * Пакет приходит по сети и может быть подделан: длина строки или размер коллекции читаются из него до
 * выделения памяти. Без потолка достаточно одного числа в заголовке, чтобы уронить сервер по памяти.
 */
public final class NetLimits {

    /** Длина имени сетевого канала: ограничение самого Forge. */
    public static final int MAX_CHANNEL_NAME_LENGTH = 20;

    /** Максимум байт в одной строке. */
    public static final int MAX_STRING_BYTES = 32767;

    /** Максимум элементов в передаваемой коллекции. */
    public static final int MAX_COLLECTION_SIZE = 4096;

    /** Максимум байт в одном блоке двоичных данных. */
    public static final int MAX_BLOB_BYTES = 1024 * 1024;

    private NetLimits() {}
}
