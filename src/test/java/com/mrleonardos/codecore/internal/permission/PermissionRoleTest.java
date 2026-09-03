package com.mrleonardos.codecore.internal.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.adapter.PermissionCapabilities;
import com.mrleonardos.codecore.api.adapter.RoleCapability;

/**
 * Перечень умений роли прав и перечень констант в api это одно и то же множество.
 *
 * <p>
 * Разъехаться они могут молча: имя умения это открытая строка, и опечатка превращает умение в
 * недостающее навсегда, а сборка остаётся зелёной. Раньше такие перечни жили двумя копиями, здесь и в
 * заявке CodePerms.
 */
class PermissionRoleTest {

    @Test
    @DisplayName("роль объявляет ровно те умения, что названы в api")
    void roleDeclaresExactlyTheApiCapabilities() {
        assertEquals(
            declaredInApi(),
            new LinkedHashSet<>(
                PermissionRole.spec()
                    .capabilities()));
    }

    @Test
    @DisplayName("шесть умений на месте и названы привычными словами")
    void capabilitiesAreNamedAsExpected() {
        Set<RoleCapability> api = declaredInApi();

        assertEquals(6, api.size());
        for (String name : new String[] { "has", "group", "meta", "contexts", "expiry", "tracks" }) {
            assertTrue(api.contains(RoleCapability.of(name)), "умение " + name + " пропало из api");
        }
    }

    @Test
    @DisplayName("запасная реализация по списку операторов умеет только has")
    void operatorFallbackOnlyAnswersHas() {
        assertEquals(
            "[has]",
            PermissionRole.spec()
                .fallback()
                .capabilities()
                .toString());
        assertTrue(
            PermissionRole.spec()
                .fallback()
                .capabilities()
                .contains(PermissionCapabilities.HAS));
    }

    private static Set<RoleCapability> declaredInApi() {
        Set<RoleCapability> found = new LinkedHashSet<>();
        for (Field field : PermissionCapabilities.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || field.getType() != RoleCapability.class) {
                continue;
            }
            try {
                found.add((RoleCapability) field.get(null));
            } catch (IllegalAccessException unreachable) {
                throw new IllegalStateException("константа " + field.getName() + " закрыта", unreachable);
            }
        }
        return found;
    }
}
