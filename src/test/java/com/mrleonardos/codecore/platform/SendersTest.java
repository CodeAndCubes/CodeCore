package com.mrleonardos.codecore.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.command.CommandSender;
import com.mrleonardos.codecore.api.command.FakeSender;
import com.mrleonardos.codecore.api.command.SenderKind;
import com.mrleonardos.codecore.api.command.SenderPosition;

import io.netty.buffer.ByteBuf;

/**
 * Раскладка игровых отправителей по видам.
 *
 * <p>
 * Сервер здесь не поднимается: {@code instanceof} и {@code getPlayerCoordinates} обходятся без него.
 * Игрока в этом наборе нет намеренно: {@code EntityPlayerMP} тянет за собой мир и менеджер игроков, и его
 * ветку проверяет живой запуск.
 */
class SendersTest {

    @Test
    @DisplayName("обычный отправитель без мира это консоль")
    void plainSenderIsTheConsole() {
        CommandSender sender = Senders.of(new Console());

        assertEquals(SenderKind.CONSOLE, sender.kind());
        assertEquals("Server", sender.name());
        assertFalse(
            sender.player()
                .isPresent());
        assertFalse(
            sender.position()
                .isPresent(),
            "у консоли координат нет");
    }

    @Test
    @DisplayName("RCON отличается от консоли")
    void rconIsItsOwnKind() {
        CommandSender sender = Senders.of(RConConsoleSource.instance);

        assertEquals(SenderKind.RCON, sender.kind());
        assertFalse(
            sender.position()
                .isPresent());
    }

    @Test
    @DisplayName("командный блок отдаёт свои координаты")
    void commandBlockReportsItsPosition() {
        CommandSender sender = Senders.of(new Block(new ChunkCoordinates(100, 64, -30)));

        assertEquals(SenderKind.COMMAND_BLOCK, sender.kind());
        assertTrue(
            sender.position()
                .isPresent());
        SenderPosition position = sender.position()
            .get();
        assertEquals(100, position.x());
        assertEquals(64, position.y());
        assertEquals(-30, position.z());
    }

    @Test
    @DisplayName("игровой отправитель достаётся мостам к чужим модам")
    void platformSenderStaysReachable() {
        Console console = new Console();

        assertSame(console, Senders.platform(Senders.of(console)));
    }

    @Test
    @DisplayName("у отправителя, сделанного не здесь, игрового двойника нет")
    void foreignSenderHasNoPlatformTwin() {
        assertNull(Senders.platform(FakeSender.console()));
        assertNull(Senders.of(null));
    }

    private static class Console implements ICommandSender {

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

    private static final class Block extends CommandBlockLogic {

        private final ChunkCoordinates coordinates;

        Block(ChunkCoordinates coordinates) {
            this.coordinates = coordinates;
        }

        @Override
        public void func_145756_e() {}

        @Override
        public int func_145751_f() {
            return 0;
        }

        @Override
        public void func_145757_a(ByteBuf buffer) {}

        @Override
        public ChunkCoordinates getPlayerCoordinates() {
            return coordinates;
        }

        @Override
        public World getEntityWorld() {
            return null;
        }
    }
}
