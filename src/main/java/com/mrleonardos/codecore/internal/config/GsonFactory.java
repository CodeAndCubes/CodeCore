package com.mrleonardos.codecore.internal.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/** Единственный настроенный {@link Gson} на все файлы: одинаковое форматирование и одинаковые правила. */
public final class GsonFactory {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
        .disableHtmlEscaping()
        .create();

    public static Gson gson() {
        return GSON;
    }

    private GsonFactory() {}
}
