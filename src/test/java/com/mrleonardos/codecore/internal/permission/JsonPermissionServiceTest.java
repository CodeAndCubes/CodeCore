package com.mrleonardos.codecore.internal.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.config.ConfigFile;

class JsonPermissionServiceTest {

    private static final UUID PLAYER = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final String READ = "codechat.channel.global.read";
    private static final String CREATE = "codechat.create";

    @Test
    void grantsWhatTheDefaultGroupAllows() {
        PermissionFile file = PermissionFile.defaults();
        file.groups.get(PermissionKeys.DEFAULT_GROUP).nodes.add("codechat.channel.*.read");

        JsonPermissionService service = serviceFor(file, false);

        assertTrue(service.has(PLAYER, READ));
        assertFalse(service.has(PLAYER, "codechat.channel.global.write"));
    }

    @Test
    void operatorFallsIntoTheOperatorGroup() {
        JsonPermissionService service = serviceFor(PermissionFile.defaults(), true);

        assertEquals(PermissionKeys.OPERATOR_GROUP, service.group(PLAYER));
        assertTrue(service.has(PLAYER, CREATE), "группа операторов получает всё");
    }

    @Test
    void inheritedGroupRulesApply() {
        PermissionFile file = PermissionFile.defaults();
        file.groups.get(PermissionKeys.DEFAULT_GROUP).nodes.add(READ);
        GroupEntry moderator = new GroupEntry();
        moderator.inherits.add(PermissionKeys.DEFAULT_GROUP);
        file.groups.put("moderator", moderator);
        assign(file, "moderator");

        JsonPermissionService service = serviceFor(file, false);

        assertTrue(service.has(PLAYER, READ), "право досталось по наследованию");
    }

    @Test
    void personalDenyBeatsGroupGrant() {
        PermissionFile file = PermissionFile.defaults();
        file.groups.get(PermissionKeys.DEFAULT_GROUP).nodes.add("codechat.*");
        PlayerEntry entry = assign(file, PermissionKeys.DEFAULT_GROUP);
        entry.nodes.add("-" + CREATE);

        JsonPermissionService service = serviceFor(file, false);

        assertFalse(service.has(PLAYER, CREATE), "личный запрет сильнее группового разрешения");
        assertTrue(service.has(PLAYER, READ), "остальное остаётся разрешённым");
    }

    @Test
    void moreSpecificRuleWinsAndDenyBreaksTheTie() {
        PermissionFile file = PermissionFile.defaults();
        GroupEntry group = file.groups.get(PermissionKeys.DEFAULT_GROUP);
        group.nodes.add("codechat.*");
        group.nodes.add("-codechat.channel.global.read");

        JsonPermissionService service = serviceFor(file, false);

        assertFalse(service.has(PLAYER, READ), "точечный запрет перебивает широкое разрешение");
        assertTrue(service.has(PLAYER, CREATE));
    }

    @Test
    void metaComesFromGroupAndIsOverriddenPersonally() {
        PermissionFile file = PermissionFile.defaults();
        file.groups.get(PermissionKeys.DEFAULT_GROUP).meta.put("prefix", "&7");
        JsonPermissionService fromGroup = serviceFor(file, false);
        assertEquals("&7", fromGroup.meta(PLAYER, "prefix", ""));
        assertEquals("none", fromGroup.meta(PLAYER, "suffix", "none"));

        assign(file, PermissionKeys.DEFAULT_GROUP).meta.put("prefix", "&c");
        JsonPermissionService personal = serviceFor(file, false);
        assertEquals("&c", personal.meta(PLAYER, "prefix", ""));
    }

    private static PlayerEntry assign(PermissionFile file, String group) {
        PlayerEntry entry = file.players.get(PLAYER.toString());
        if (entry == null) {
            entry = new PlayerEntry();
            file.players.put(PLAYER.toString(), entry);
        }
        entry.group = group;
        return entry;
    }

    private static JsonPermissionService serviceFor(PermissionFile file, boolean operator) {
        return new JsonPermissionService(new StaticConfig(file), player -> operator);
    }

    private static final class StaticConfig implements ConfigFile<PermissionFile> {

        private final PermissionFile file;

        private StaticConfig(PermissionFile file) {
            this.file = file;
        }

        @Override
        public PermissionFile get() {
            return file;
        }

        @Override
        public boolean loaded() {
            return true;
        }

        @Override
        public void save() {}

        @Override
        public void reload() {}

        @Override
        public Path path() {
            return null;
        }
    }
}
