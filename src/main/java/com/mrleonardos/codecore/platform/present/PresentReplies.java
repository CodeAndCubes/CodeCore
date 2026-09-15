package com.mrleonardos.codecore.platform.present;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

/**
 * Отправка богатых ответов адресату.
 *
 * <p>
 * Игроку строка уходит целиком: цвета, клики и подсказки работают на ванильном клиенте 1.7.10 без
 * клиентских модов. Консоли, RCON и командному блоку достаётся плоский текст, потому что стилей там
 * нет: у постраничника вместо кнопок листания строка положения.
 *
 * <p>
 * Зовётся из потока команды или главного потока сервера, как и ответ ядра у отправителя: своей
 * синхронизации отправка не имеет.
 */
public final class PresentReplies {

    private PresentReplies() {}

    /** Одну строку: игроку со стилями, остальным плоским текстом. */
    public static void send(ICommandSender sender, RichLine line) {
        if (sender instanceof EntityPlayerMP) {
            sender.addChatMessage(line.build());
            return;
        }
        sender.addChatMessage(new ChatComponentText(line.plain()));
    }

    /** Карточку: по строке чата на каждую строку карточки. */
    public static void send(ICommandSender sender, RichCard card) {
        if (sender instanceof EntityPlayerMP) {
            for (IChatComponent line : card.build()) {
                sender.addChatMessage(line);
            }
            return;
        }
        for (String line : card.plainLines()) {
            sender.addChatMessage(new ChatComponentText(line));
        }
    }

    /** Страницу списка: шапка, строки и листание. */
    public static void send(ICommandSender sender, Pages pages, int number, String title, String commandTemplate) {
        if (sender instanceof EntityPlayerMP) {
            send(sender, pages.card(number, title, commandTemplate));
            return;
        }
        for (String line : pages.plainLines(number, title)) {
            sender.addChatMessage(new ChatComponentText(line));
        }
    }
}
