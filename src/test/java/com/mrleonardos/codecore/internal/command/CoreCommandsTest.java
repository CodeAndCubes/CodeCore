package com.mrleonardos.codecore.internal.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.CoreMessages;
import com.mrleonardos.codecore.CorePermissions;
import com.mrleonardos.codecore.api.adapter.RoleCapability;
import com.mrleonardos.codecore.api.adapter.RoleChoice;
import com.mrleonardos.codecore.api.adapter.RoleStatus;
import com.mrleonardos.codecore.api.command.CommandContext;
import com.mrleonardos.codecore.api.command.CommandNode;

class CoreCommandsTest {

    @Test
    @DisplayName("у команды ядра есть подкоманда состояния ролей")
    void adaptersSubcommandExists() {
        CommandNode adapters = child("adapters");

        assertNotNull(adapters);
        assertEquals(CorePermissions.ADAPTERS, adapters.permissionNode());
        assertNotNull(adapters.action(), "подкоманда без действия ничего не покажет");
    }

    @Test
    @DisplayName("сводка называет владельца, кандидатов и недоступные умения")
    void reportNamesOwnerCandidatesAndMissing() {
        Recorder recorder = new Recorder();

        CoreCommands.report(
            recorder,
            Arrays.asList(
                new RoleStatus(
                    "essentials",
                    "forgeessentials",
                    RoleChoice.NAMED,
                    Arrays.asList("codeessentials", "forgeessentials"),
                    capabilities("back", "tpa")),
                new RoleStatus("economy", null, RoleChoice.OFF, Collections.emptyList(), capabilities())));

        assertEquals(
            Arrays.asList(
                CoreMessages.ADAPTERS_ROLE + "|essentials|forgeessentials|NAMED",
                CoreMessages.ADAPTERS_CANDIDATES + "|codeessentials, forgeessentials",
                CoreMessages.ADAPTERS_MISSING + "|back, tpa",
                CoreMessages.ADAPTERS_NOBODY + "|economy|OFF",
                CoreMessages.ADAPTERS_CANDIDATES + "|-",
                CoreMessages.ADAPTERS_MISSING + "|-"),
            recorder.lines);
    }

    private static LinkedHashSet<RoleCapability> capabilities(String... names) {
        LinkedHashSet<RoleCapability> set = new LinkedHashSet<>();
        for (String name : names) {
            set.add(RoleCapability.of(name));
        }
        return set;
    }

    private static CommandNode child(String name) {
        for (CommandNode node : CoreCommands.root()
            .children()) {
            if (node.matches(name)) {
                return node;
            }
        }
        return null;
    }

    private static final class Recorder implements CommandContext {

        private final List<String> lines = new ArrayList<>();

        @Override
        public ICommandSender sender() {
            return null;
        }

        @Override
        public EntityPlayerMP player() {
            return null;
        }

        @Override
        public <T> T get(String name) {
            throw new IllegalArgumentException(name);
        }

        @Override
        public <T> T getOrDefault(String name, T fallback) {
            return fallback;
        }

        @Override
        public boolean has(String name) {
            return false;
        }

        @Override
        public void reply(String translationKey, Object... arguments) {
            StringBuilder line = new StringBuilder(translationKey);
            for (Object argument : arguments) {
                line.append('|')
                    .append(argument);
            }
            lines.add(line.toString());
        }

        @Override
        public void replyError(String translationKey, Object... arguments) {
            reply(translationKey, arguments);
        }
    }

    @Test
    @DisplayName("подкоманда перезагрузки на месте")
    void reloadSubcommandStays() {
        assertTrue(child("reload") != null);
    }
}
