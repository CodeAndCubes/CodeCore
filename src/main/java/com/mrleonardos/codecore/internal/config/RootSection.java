package com.mrleonardos.codecore.internal.config;

import com.mrleonardos.codecore.api.config.Comment;

/** Корень главного файла: то, что лежит до первой секции. */
public final class RootSection {

    /** Имя сервера по умолчанию: один сервер, одна база, размечать нечего. */
    public static final String DEFAULT_SERVER_ID = "main";

    @Comment({ "Имя этого сервера, когда несколько серверов смотрят в одну базу.",
        "Схемы модов сами решают, что размечать этим именем, а что держать общим." })
    public String serverId = DEFAULT_SERVER_ID;

    void normalize() {
        if (serverId == null || serverId.isEmpty()) {
            serverId = DEFAULT_SERVER_ID;
        }
    }
}
