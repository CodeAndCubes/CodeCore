package com.mrleonardos.codecore.internal.permission.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.service.PermissionService;

class UltraMinePermissionsTest {

    @Test
    @DisplayName("адаптер поднимается, когда ядро UltraMine есть")
    void adapterAppears() {
        assertNotNull(UltraMinePermissions.create());
    }

    @Test
    @DisplayName("без сервера ник не находится и права не выдаются")
    void withoutServerNothingIsGranted() {
        PermissionService service = UltraMinePermissions.create();
        UUID player = UUID.randomUUID();

        assertFalse(service.has(player, "codechat.admin"));
        assertEquals("нет", service.meta(player, "prefix", "нет"));
    }
}
