package com.mrleonardos.codecore.internal.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GroupsPermissionServiceTest {

    private static final UUID PLAYER = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final String READ = "codechat.channel.global.read";
    private static final String CREATE = "codechat.create";

    @Test
    void grantsWhatTheDefaultGroupAllows() {
        CoreGroupsFile file = CoreGroupsFile.defaults();
        file.groups.get(PermissionKeys.DEFAULT_GROUP).nodes.add("codechat.channel.*.read");

        GroupsPermissionService service = serviceFor(file, false);

        assertTrue(service.has(PLAYER, READ));
        assertFalse(service.has(PLAYER, "codechat.channel.global.write"));
    }

    @Test
    void operatorFallsIntoTheOperatorGroup() {
        GroupsPermissionService service = serviceFor(CoreGroupsFile.defaults(), true);

        assertEquals(PermissionKeys.OPERATOR_GROUP, service.group(PLAYER));
        assertTrue(service.has(PLAYER, CREATE), "группа операторов получает всё");
    }

    @Test
    @DisplayName("оффлайн-игрок из ops.json попадает в группу по умолчанию")
    void offlineOperatorTakesTheDefaultGroup() {
        GroupsPermissionService service = serviceFor(CoreGroupsFile.defaults(), false);

        assertEquals(PermissionKeys.DEFAULT_GROUP, service.group(PLAYER));
    }

    @Test
    void inheritedGroupRulesApply() {
        CoreGroupsFile file = CoreGroupsFile.defaults();
        file.groups.get(PermissionKeys.DEFAULT_GROUP).nodes.add(READ);
        GroupEntry moderator = new GroupEntry();
        moderator.inherits.add(PermissionKeys.DEFAULT_GROUP);
        file.groups.put("moderator", moderator);
        assign(file, "moderator");

        GroupsPermissionService service = serviceFor(file, false);

        assertTrue(service.has(PLAYER, READ), "право досталось по наследованию");
    }

    @Test
    void personalDenyBeatsGroupGrant() {
        CoreGroupsFile file = CoreGroupsFile.defaults();
        file.groups.get(PermissionKeys.DEFAULT_GROUP).nodes.add("codechat.*");
        PlayerEntry entry = assign(file, PermissionKeys.DEFAULT_GROUP);
        entry.nodes.add("-" + CREATE);

        GroupsPermissionService service = serviceFor(file, false);

        assertFalse(service.has(PLAYER, CREATE), "личный запрет сильнее группового разрешения");
        assertTrue(service.has(PLAYER, READ), "остальное остаётся разрешённым");
    }

    @Test
    void moreSpecificRuleWinsAndDenyBreaksTheTie() {
        CoreGroupsFile file = CoreGroupsFile.defaults();
        GroupEntry group = file.groups.get(PermissionKeys.DEFAULT_GROUP);
        group.nodes.add("codechat.*");
        group.nodes.add("-codechat.channel.global.read");

        GroupsPermissionService service = serviceFor(file, false);

        assertFalse(service.has(PLAYER, READ), "точечный запрет перебивает широкое разрешение");
        assertTrue(service.has(PLAYER, CREATE));
    }

    @Test
    void metaComesFromGroupAndIsOverriddenPersonally() {
        CoreGroupsFile file = CoreGroupsFile.defaults();
        file.groups.get(PermissionKeys.DEFAULT_GROUP).meta.put("prefix", "&7");
        GroupsPermissionService fromGroup = serviceFor(file, false);
        assertEquals("&7", fromGroup.meta(PLAYER, "prefix", ""));
        assertEquals("none", fromGroup.meta(PLAYER, "suffix", "none"));

        assign(file, PermissionKeys.DEFAULT_GROUP).meta.put("prefix", "&c");
        GroupsPermissionService personal = serviceFor(file, false);
        assertEquals("&c", personal.meta(PLAYER, "prefix", ""));
    }

    @Test
    @DisplayName("группы по умолчанию берутся из секции главного файла")
    void defaultGroupsComeFromTheMainFile() {
        CoreGroupsFile file = CoreGroupsFile.defaults();
        PermissionsSection section = new PermissionsSection();
        section.defaultGroup = "guest";
        file.groups.put("guest", new GroupEntry());

        GroupsPermissionService service = new GroupsPermissionService(
            new StaticConfig<>(file),
            new StaticConfig<>(section),
            player -> false);

        assertEquals("guest", service.group(PLAYER));
    }

    @Test
    @DisplayName("пустое имя группы по умолчанию возвращается к заводскому")
    void emptyGroupNameFallsBack() {
        PermissionsSection section = new PermissionsSection();
        section.defaultGroup = "";
        section.opGroup = null;

        section.normalize();

        assertEquals(PermissionKeys.DEFAULT_GROUP, section.defaultGroup);
        assertEquals(PermissionKeys.OPERATOR_GROUP, section.opGroup);
    }

    @Test
    @DisplayName("файл, правленный руками, не роняет первую же проверку")
    void handEditedFileIsRepaired() {
        CoreGroupsFile file = new CoreGroupsFile();
        GroupEntry empty = new GroupEntry();
        empty.inherits = null;
        empty.nodes = null;
        empty.meta = null;
        file.groups.put(PermissionKeys.DEFAULT_GROUP, empty);
        GroupEntry withGarbage = new GroupEntry();
        withGarbage.nodes.add("");
        withGarbage.nodes.add(READ);
        file.groups.put("moderator", withGarbage);

        file.normalize();

        assertFalse(serviceFor(file, false).has(PLAYER, READ), "пустая группа отвечает без исключения");
        assertEquals(1, file.groups.get("moderator").nodes.size(), "пустая строка среди правил выброшена");
    }

    private static PlayerEntry assign(CoreGroupsFile file, String group) {
        PlayerEntry entry = file.players.get(PLAYER.toString());
        if (entry == null) {
            entry = new PlayerEntry();
            file.players.put(PLAYER.toString(), entry);
        }
        entry.group = group;
        return entry;
    }

    private static GroupsPermissionService serviceFor(CoreGroupsFile file, boolean operator) {
        return new GroupsPermissionService(
            new StaticConfig<>(file),
            new StaticConfig<>(new PermissionsSection()),
            player -> operator);
    }
}
