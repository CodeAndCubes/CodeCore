package com.mrleonardos.codecore.internal.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.mrleonardos.codecore.CoreConstants;
import com.mrleonardos.codecore.api.client.image.ImageLimits;

/**
 * Открытие http-соединений для клиента: таймауты картинок и имя мода одним местом.
 *
 * <p>
 * Загрузка картинок и запрос эндпоинта аватаров настраивают соединение одинаково, и разъехаться этим
 * настройкам нельзя: потолки ожидания читаются из одной секции настроек.
 */
public final class HttpConnections {

    /** Ответ, который считает успешным любой клиентский запрос ядра. */
    public static final int HTTP_OK = 200;

    private static final String USER_AGENT_HEADER = "User-Agent";

    private HttpConnections() {}

    /** Соединение с таймаутами из секции {@code [images]} и именем мода в User-Agent. */
    public static HttpURLConnection open(String address) throws IOException {
        ImageLimits limits = ImageLimits.current();
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setConnectTimeout(limits.connectTimeoutMs());
        connection.setReadTimeout(limits.readTimeoutMs());
        connection.setRequestProperty(USER_AGENT_HEADER, CoreConstants.MOD_NAME);
        return connection;
    }
}
