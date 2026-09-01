package com.mrleonardos.codecore.internal.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.config.ConfigData;
import com.mrleonardos.codecore.api.config.ConfigRoles;
import com.mrleonardos.codecore.api.service.PermissionService;
import com.mrleonardos.codecore.internal.adapter.AdapterRegistryImpl;
import com.mrleonardos.codecore.internal.config.TestConfigData;
import com.mrleonardos.codecore.internal.service.ServiceRegistryImpl;

/**
 * Роль прав, оставшаяся без владельца.
 *
 * <p>
 * Живой прогон линейки на {@code permissions = "off"} показал, чем кончается пустая роль: первый же
 * {@code require} у соседнего мода бросал исключение, и падали все команды линейки разом, включая ту,
 * что показывает состояние ролей.
 */
class OperatorPermissionsTest {

    private static final Logger LOG = LogManager.getLogger(OperatorPermissionsTest.class);
    private static final String NODE = "codechat.channel.staff.read";

    private final ServiceRegistryImpl services = new ServiceRegistryImpl(LOG);
    private final AdapterRegistryImpl adapters = new AdapterRegistryImpl(services, LOG);

    @Test
    @DisplayName("оператор получает право, обычный игрок нет")
    void operatorIsAllowedAndTheRestAreNot() {
        UUID operator = UUID.randomUUID();
        PermissionService service = new OperatorPermissions(operator::equals);

        assertTrue(service.has(operator, NODE));
        assertFalse(service.has(UUID.randomUUID(), NODE));
    }

    @Test
    @DisplayName("групп и меты у списка операторов нет")
    void groupsAndMetaAreEmpty() {
        UUID operator = UUID.randomUUID();
        PermissionService service = new OperatorPermissions(operator::equals);

        assertEquals("", service.group(operator));
        assertEquals("нет", service.meta(operator, "prefix", "нет"));
    }

    @Test
    @DisplayName("роль off: сервис есть, владельца нет, работает только has")
    void offLeavesTheRoleUnownedButAnswering() throws IOException {
        adapters.declareRole(PermissionRole.spec());

        adapters.decide(owners("off"));

        assertNull(adapters.owner(ConfigRoles.PERMISSIONS), "роль по-прежнему ничья");
        assertTrue(
            services.find(PermissionService.class)
                .isPresent(),
            "спросить право есть у кого, иначе падают все команды линейки");
        assertFalse(
            services.require(PermissionService.class)
                .has(UUID.randomUUID(), NODE),
            "без сервера операторов нет, и права никто не получает");
        assertEquals(
            "[group, meta, contexts, expiry, tracks]",
            adapters.missing(ConfigRoles.PERMISSIONS)
                .toString(),
            "всё, кроме has, названо недоступным");
        assertEquals(
            "permission checks fall back to the server operator list",
            adapters.statuses()
                .get(0)
                .fallback());
    }

    private static ConfigData owners(String value) throws IOException {
        return TestConfigData.of("[owners]\npermissions = \"" + value + "\"\n");
    }
}
