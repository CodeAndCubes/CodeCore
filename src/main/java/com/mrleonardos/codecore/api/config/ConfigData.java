package com.mrleonardos.codecore.api.config;

import java.util.List;
import java.util.Set;

/**
 * Содержимое файла настроек до разбора в объект: дерево таблиц, списков и простых значений.
 *
 * <p>
 * Одинаково выглядит для toml и для json, поэтому шаг миграции пишется один раз и не зависит от формата
 * файла. Путь записывается через точку: {@code storage.provider} это ключ {@code provider} в таблице
 * {@code storage}.
 *
 * <p>
 * Значением бывает {@link String}, {@link Number}, {@link Boolean}, {@link List} или вложенный
 * {@code ConfigData}. Пустого значения нет: ключ либо есть, либо его нет.
 */
public interface ConfigData {

    /** Есть ли значение по этому пути. */
    boolean has(String path);

    /** Значение по пути или {@code null}, если ключа нет. */
    Object get(String path);

    /** Строка по пути или {@code fallback}, если ключа нет или там не строка. */
    String string(String path, String fallback);

    /** Целое по пути или {@code fallback}, если ключа нет или там не число. */
    int integer(String path, int fallback);

    /** Целое по пути или {@code fallback}, если ключа нет или там не число. */
    long number(String path, long fallback);

    /** Признак по пути или {@code fallback}, если ключа нет или там не признак. */
    boolean flag(String path, boolean fallback);

    /** Вложенная таблица по пути или {@code null}, если её там нет. */
    ConfigData table(String path);

    /** Список таблиц по пути; пустой список, если ключа нет или это не список таблиц. */
    List<ConfigData> tables(String path);

    /** Ключи верхнего уровня в порядке, в котором они лежат в файле. */
    Set<String> keys();

    /** Положить значение, создав по дороге недостающие таблицы. */
    void set(String path, Object value);

    /** Убрать значение вместе с его описанием. */
    void remove(String path);

    /** Пустая таблица того же формата: её кладут в {@link #set(String, Object)}. */
    ConfigData newTable();
}
