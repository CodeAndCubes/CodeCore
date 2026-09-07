package com.mrleonardos.codecore.internal.config;

import com.mrleonardos.codecore.api.config.Comment;
import com.mrleonardos.codecore.internal.text.Texts;

/** Корень главного файла: то, что лежит до первой секции. */
public final class RootSection {

    /** Имя сервера по умолчанию: один сервер, одна база, размечать нечего. */
    public static final String DEFAULT_SERVER_ID = "main";

    @Comment({ "Имя этого сервера, когда несколько серверов смотрят в одну базу.",
        "Схемы модов сами решают, что размечать этим именем, а что держать общим." })
    public String serverId = DEFAULT_SERVER_ID;

    @Comment({ "Язык сообщений сервера: имя файла перевода без расширения, en_US или ru_RU.",
        "Строки собирает сервер, потому что серверный мод на клиент не попадает и ключа перевода",
        "клиент не знает. Непереведённая строка остаётся английской." })
    public String language = Texts.DEFAULT_LANGUAGE;

    void normalize() {
        if (serverId == null || serverId.isEmpty()) {
            serverId = DEFAULT_SERVER_ID;
        }
        if (language == null || language.isEmpty()) {
            language = Texts.DEFAULT_LANGUAGE;
        }
    }
}
