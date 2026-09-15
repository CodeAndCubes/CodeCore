package com.mrleonardos.codecore.platform.present;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Отправка адресату: консоль получает плоский текст без секций цвета.
 *
 * <p>
 * Ветку игрока проверяет живой запуск: {@code EntityPlayerMP} тянет за собой мир и менеджер игроков,
 * и в юнит-тесте его не поднять. Она сводится к выбору между готовым деревом и плоским текстом, а
 * деревья проверены своими наборами.
 */
class PresentRepliesTest {

    @Test
    @DisplayName("консоль получает строку плоским текстом")
    void consoleReceivesFlatLine() {
        Console console = new Console();

        PresentReplies.send(
            console,
            RichLine.of()
                .accent("Spawn")
                .muted(": ")
                .value("Steve"));

        assertEquals(1, console.heard.size());
        assertEquals(
            "Spawn: Steve",
            console.heard.get(0)
                .getUnformattedText());
    }

    @Test
    @DisplayName("консоль получает карточку по строке и без секций стиля")
    void consoleReceivesFlatCard() {
        Console console = new Console();

        PresentReplies.send(
            console,
            RichCard.of("Дом \"Spawn\"")
                .field("codecore.present.pages.back", "Steve")
                .separator()
                .button("codecore.present.pages.next", "/home list 2"));

        assertEquals(4, console.heard.size());
        assertEquals(
            "Дом \"Spawn\"",
            console.heard.get(0)
                .getUnformattedText());
        assertEquals(
            "Back: Steve",
            console.heard.get(1)
                .getUnformattedText());
        for (IChatComponent message : console.heard) {
            assertNull(
                message.getChatStyle()
                    .getColor(),
                "у консоли не должно быть цвета");
            assertNull(
                message.getChatStyle()
                    .getChatClickEvent(),
                "у консоли не должно быть кликов");
            assertNull(
                message.getChatStyle()
                    .getChatHoverEvent(),
                "у консоли не должно быть подсказок");
        }
    }

    @Test
    @DisplayName("консоль получает страницу списком строк, кнопок нет")
    void consoleReceivesFlatPage() {
        Console console = new Console();
        Pages pages = Pages.of(pageRows(), 2);

        PresentReplies.send(console, pages, 2, "Дома", "/home list %d");

        assertEquals(3, console.heard.size());
        assertEquals(
            "Дома · total 3",
            console.heard.get(0)
                .getUnformattedText());
        assertEquals(
            "row 3",
            console.heard.get(1)
                .getUnformattedText());
        assertEquals(
            "Page 2 of 2",
            console.heard.get(2)
                .getUnformattedText());
    }

    private static List<RichLine> pageRows() {
        List<RichLine> rows = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            rows.add(
                RichLine.of()
                    .value("row " + i));
        }
        return rows;
    }

    private static final class Console implements ICommandSender {

        private final List<IChatComponent> heard = new ArrayList<>();

        @Override
        public String getCommandSenderName() {
            return "Server";
        }

        @Override
        public IChatComponent func_145748_c_() {
            return null;
        }

        @Override
        public void addChatMessage(IChatComponent message) {
            heard.add(message);
        }

        @Override
        public boolean canCommandSenderUseCommand(int permissionLevel, String command) {
            return true;
        }

        @Override
        public ChunkCoordinates getPlayerCoordinates() {
            return null;
        }

        @Override
        public World getEntityWorld() {
            return null;
        }
    }
}
