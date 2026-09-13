package com.mrleonardos.codecore.internal;

/**
 * Кто рисует строку над хотбаром.
 *
 * <p>
 * Реализация одна и живёт на клиенте. Пакет ссылаться на неё не может: в серверном jar её нет, поэтому
 * между ними стоит {@link CoreBridge}.
 */
public interface ActionBarSink {

    /** Показать строку вместо той, что видна сейчас. */
    void show(String text, int seconds);

    /** Убрать строку: клиент ушёл с сервера, и причина показа осталась там. */
    void clear();
}
