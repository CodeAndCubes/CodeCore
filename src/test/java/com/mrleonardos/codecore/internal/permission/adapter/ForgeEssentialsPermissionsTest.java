package com.mrleonardos.codecore.internal.permission.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.api.UserIdent;
import com.mrleonardos.codecore.api.service.PermissionService;

class ForgeEssentialsPermissionsTest {

    private static final String NODE = "codechat.channel.staff.read";
    private static final String PREFIX_KEY = "prefix";

    private final FakeForgeEssentials perms = new FakeForgeEssentials();

    @AfterEach
    void permissionModuleIsGone() {
        APIRegistry.perms = null;
    }

    @Test
    @DisplayName("мост поднимается, когда ForgeEssentials есть")
    void adapterAppears() {
        assertNotNull(ForgeEssentialsPermissions.create());
    }

    @Test
    @DisplayName("модуль прав FE ещё не поднялся: прав нет, а мост цел")
    void emptyRegistryGrantsNothing() {
        APIRegistry.perms = null;
        PermissionService service = ForgeEssentialsPermissions.create();
        UUID player = UUID.randomUUID();

        assertFalse(service.has(player, NODE));
        assertEquals("", service.group(player));
        assertEquals("нет", service.meta(player, PREFIX_KEY, "нет"));
    }

    @Test
    @DisplayName("мост, созданный до модуля прав, отвечает, как только поле заполнилось")
    void lateModuleIsPickedUp() {
        APIRegistry.perms = null;
        PermissionService service = ForgeEssentialsPermissions.create();
        UUID player = UUID.randomUUID();
        assertFalse(service.has(player, NODE));

        perms.setPlayerPermission(UserIdent.get(player, null), NODE, true);
        APIRegistry.perms = perms;

        assertTrue(service.has(player, NODE), "поле читается перед каждым вопросом");
    }

    @Test
    @DisplayName("выданный узел виден через сервис прав")
    void grantedNodeIsVisible() {
        APIRegistry.perms = perms;
        UUID player = UUID.randomUUID();
        perms.setPlayerPermission(UserIdent.get(player, null), NODE, true);

        PermissionService service = ForgeEssentialsPermissions.create();

        assertTrue(service.has(player, NODE));
        assertFalse(service.has(player, "codechat.admin"));
        assertFalse(service.has(UUID.randomUUID(), NODE), "незнакомый игрок прав не получает");
    }

    @Test
    @DisplayName("группа и свойства приходят из ForgeEssentials")
    void groupAndMetaAreTaken() {
        APIRegistry.perms = perms;
        UUID player = UUID.randomUUID();
        perms.createGroup("moderator");
        perms.addPlayerToGroup(UserIdent.get(player, null), "moderator");
        perms.setPlayerPermissionProperty(UserIdent.get(player, null), PREFIX_KEY, "&c[M]");

        PermissionService service = ForgeEssentialsPermissions.create();

        assertEquals("moderator", service.group(player));
        assertEquals("&c[M]", service.meta(player, PREFIX_KEY, ""));
        assertEquals("нет", service.meta(player, "suffix", "нет"));
    }
}
