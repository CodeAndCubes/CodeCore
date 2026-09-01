package com.mrleonardos.codecore.internal.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Чтение потока с потолком объёма.
 *
 * <p>
 * Адрес приходит от сервера, а по адресу может отвечать что угодно: заголовок длины врёт, поток не
 * кончается, таймаут чтения не срабатывает, пока данные идут. Единственная защита — считать прочитанное
 * самому и оборвать чтение.
 */
public final class LimitedStream {

    private static final int BUFFER = 8192;

    private LimitedStream() {}

    /**
     * Прочитать поток целиком, но не больше {@code limit} байт.
     *
     * @throws IOException если данных оказалось больше предела
     */
    public static byte[] readAll(InputStream stream, int limit, String address) throws IOException {
        ByteArrayOutputStream collected = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER];
        int read;
        while ((read = stream.read(buffer)) != -1) {
            if (collected.size() + read > limit) {
                throw new IOException("Response from " + address + " is larger than " + limit + " bytes");
            }
            collected.write(buffer, 0, read);
        }
        return collected.toByteArray();
    }
}
