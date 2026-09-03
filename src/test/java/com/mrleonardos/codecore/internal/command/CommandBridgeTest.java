package com.mrleonardos.codecore.internal.command;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.command.ArgumentType;
import com.mrleonardos.codecore.api.command.ArgumentTypes;
import com.mrleonardos.codecore.api.command.CommandInputException;
import com.mrleonardos.codecore.api.command.CommandMessages;
import com.mrleonardos.codecore.api.command.CommandNode;
import com.mrleonardos.codecore.api.command.CommandSender;
import com.mrleonardos.codecore.api.command.SenderKind;

/**
 * Мост переводит ошибку разбора в игровое исключение на границе с игрой.
 *
 * <p>
 * Внутри линейки бросается {@link CommandInputException}, наружу уходит то же, что и раньше, поэтому
 * сообщение игроку не меняется, а разбор проверяется без запуска игры.
 */
class CommandBridgeTest {

    @Test
    @DisplayName("ошибка разбора доезжает до игры тем же ключом и теми же аргументами")
    void inputFailureBecomesAGameException() {
        CommandBridge bridge = new CommandBridge(
            CommandNode.literal("test")
                .arg("count", ArgumentTypes.integer(1, 64))
                .executes(context -> {}));

        CommandException failure = assertThrows(
            CommandException.class,
            () -> bridge.processCommand(new Console(), new String[] { "100" }));

        assertEquals(CommandMessages.OUT_OF_RANGE, failure.getMessage());
        assertArrayEquals(new Object[] { "100", 1, 64 }, failure.getErrorOjbects());
    }

    @Test
    @DisplayName("ошибка, брошенная действием команды, переводится так же")
    void failureFromTheActionIsTranslatedToo() {
        CommandBridge bridge = new CommandBridge(
            CommandNode.literal("test")
                .executes(context -> { throw new CommandInputException(CommandMessages.PLAYER_NOT_FOUND, "Ghost"); }));

        CommandException failure = assertThrows(
            CommandException.class,
            () -> bridge.processCommand(new Console(), new String[0]));

        assertEquals(CommandMessages.PLAYER_NOT_FOUND, failure.getMessage());
        assertArrayEquals(new Object[] { "Ghost" }, failure.getErrorOjbects());
    }

    @Test
    @DisplayName("отправитель доезжает до действия без типов игры")
    void actionSeesTheSenderWithoutGameTypes() {
        List<SenderKind> seen = new ArrayList<>();
        CommandBridge bridge = new CommandBridge(
            CommandNode.literal("test")
                .executes(
                    context -> seen.add(
                        context.caller()
                            .kind())));

        bridge.processCommand(new Console(), new String[0]);

        assertEquals(Arrays.asList(SenderKind.CONSOLE), seen);
    }

    @Test
    @DisplayName("подсказки собираются с обеих подписей, пока живы обе")
    void suggestionsAreAskedOnBothSignatures() {
        CommandBridge bridge = new CommandBridge(
            CommandNode.literal("test")
                .arg("who", new BothWays())
                .executes(context -> {}));

        List<String> options = bridge.addTabCompletionOptions(new Console(), new String[] { "" });

        assertTrue(options.contains("новая"), options.toString());
        assertTrue(options.contains("старая"), options.toString());
        assertEquals(2, options.size(), "повторов быть не должно");
    }

    /** Тип, у которого переопределены обе подписи: так видно, что мост спрашивает обе. */
    private static final class BothWays implements ArgumentType<String> {

        @Override
        public String parse(String raw) {
            return raw;
        }

        @Override
        public List<String> suggestions(CommandSender sender, String partial) {
            return Arrays.asList("новая");
        }

        @Override
        public List<String> suggestions(ICommandSender sender, String partial) {
            return Arrays.asList("старая");
        }
    }

    private static final class Console implements ICommandSender {

        @Override
        public String getCommandSenderName() {
            return "Server";
        }

        @Override
        public IChatComponent func_145748_c_() {
            return null;
        }

        @Override
        public void addChatMessage(IChatComponent message) {}

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
