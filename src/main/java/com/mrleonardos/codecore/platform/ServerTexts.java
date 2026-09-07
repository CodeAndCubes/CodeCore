package com.mrleonardos.codecore.platform;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;

import com.mrleonardos.codecore.internal.text.Texts;

/**
 * Готовая строка вместо ключа перевода.
 *
 * <p>
 * Серверный мод раздаётся только серверу, поэтому его файла перевода на клиенте нет, и
 * {@link net.minecraft.util.ChatComponentTranslation} доехал бы до игрока строкой
 * {@code codeessentials.message.kit_saved}: незнакомый ключ игра показывает как есть. Строку собирает
 * сервер по языку из главного файла линейки и шлёт готовой.
 *
 * <p>
 * Ключа нет ни в одном файле линейки, спрашивается таблица самой игры: ванильные ключи и то, что чужой
 * мод завёл в рантайме, лежат там. Нет и там, показывается сам ключ, как это делает игра.
 */
public final class ServerTexts {

    private ServerTexts() {}

    /** Строка по ключу: для чата, лога и всего, что собирается на сервере. */
    public static String format(String translationKey, Object... arguments) {
        return Texts.current()
            .find(translationKey, arguments)
            .orElseGet(() -> game(translationKey, arguments));
    }

    /** Готовая строка сообщением чата. */
    public static IChatComponent line(String translationKey, Object... arguments) {
        return new ChatComponentText(format(translationKey, arguments));
    }

    private static String game(String translationKey, Object... arguments) {
        return arguments == null || arguments.length == 0 ? StatCollector.translateToLocal(translationKey)
            : StatCollector.translateToLocalFormatted(translationKey, arguments);
    }
}
