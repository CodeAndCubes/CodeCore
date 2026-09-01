package com.mrleonardos.codecore.internal.permission.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import net.luckperms.api.LuckPermsProvider;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.service.PermissionService;

class LuckPermsPermissionsTest {

    private static final String NODE = "codechat.channel.staff.read";
    private static final String PREFIX_KEY = "prefix";

    @Test
    @DisplayName("адаптер поднимается, когда LuckPerms есть")
    void adapterAppears() {
        assertNotNull(LuckPermsPermissions.create());
    }

    @Test
    @DisplayName("выданная нода видна через сервис прав")
    void grantedNodeIsVisible() {
        UUID player = UUID.randomUUID();
        LuckPermsProvider.user(player)
            .allow(NODE);

        PermissionService service = LuckPermsPermissions.create();

        assertTrue(service.has(player, NODE));
        assertFalse(service.has(player, "codechat.admin"));
    }

    @Test
    @DisplayName("незнакомый игрок прав не получает")
    void unknownPlayerHasNothing() {
        assertFalse(
            LuckPermsPermissions.create()
                .has(UUID.randomUUID(), NODE));
    }

    @Test
    @DisplayName("группа и мета приходят из LuckPerms")
    void groupAndMetaAreTaken() {
        UUID player = UUID.randomUUID();
        LuckPermsProvider.user(player)
            .primaryGroup("moderator")
            .meta(PREFIX_KEY, "&c[M]");

        PermissionService service = LuckPermsPermissions.create();

        assertEquals("moderator", service.group(player));
        assertEquals("&c[M]", service.meta(player, PREFIX_KEY, ""));
        assertEquals("нет", service.meta(player, "suffix", "нет"));
    }
}
