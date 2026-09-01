package com.mrleonardos.codecore.internal.config;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import com.mrleonardos.codecore.api.config.ConfigData;

/** Содержимое одного файла в памяти: то, что прочитали, и то, что напишем. */
interface ConfigDocument {

    /** Дерево значений: по нему ходят миграции. */
    ConfigData data();

    /**
     * Разобрать содержимое в класс настроек.
     *
     * @return разобранное значение или {@code null}, если содержимое на класс не легло
     */
    <T> T bind(Class<T> type);

    /** Наложить значения на содержимое вместе с версией схемы и описаниями полей. */
    void store(Object value, Class<?> type, int schemaVersion);

    /** Ключи файла, которых нет в классе настроек. */
    List<String> unknownKeys(Class<?> type);

    void writeTo(Writer writer) throws IOException;
}
