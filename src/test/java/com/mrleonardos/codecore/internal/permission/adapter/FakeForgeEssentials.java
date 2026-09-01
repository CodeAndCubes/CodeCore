package com.mrleonardos.codecore.internal.permission.adapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.forgeessentials.api.UserIdent;
import com.forgeessentials.api.permissions.IPermissionsHelper;

/**
 * Подставное хранилище прав ForgeEssentials.
 *
 * <p>
 * Держит то, что мост у настоящего FE спрашивает: узлы игрока, свойства игрока и его группу. Главной считается
 * последняя выданная группа, а в незаведённую группу игрок не попадает, как и у настоящего FE.
 *
 * <p>
 * {@code setDirty(true)} у настоящего FE помечает конфиг к сохранению, без него правка не переживёт перезапуск.
 * Хранилище живёт в памяти теста и сохранять ему некуда, но метод есть: тот, кто пишет через api FE, обязан его
 * позвать.
 */
final class FakeForgeEssentials implements IPermissionsHelper {

    private final Map<UUID, Set<String>> nodes = new HashMap<>();
    private final Map<UUID, Map<String, String>> properties = new HashMap<>();
    private final Map<UUID, String> groups = new HashMap<>();
    private final Set<String> known = new HashSet<>();

    @Override
    public boolean checkUserPermission(UserIdent ident, String permissionNode) {
        return nodes.getOrDefault(ident.uuid(), Collections.emptySet())
            .contains(permissionNode);
    }

    @Override
    public String getUserPermissionProperty(UserIdent ident, String permissionNode) {
        return properties.getOrDefault(ident.uuid(), Collections.emptyMap())
            .get(permissionNode);
    }

    @Override
    public String getPrimaryGroup(UserIdent ident) {
        return groups.get(ident.uuid());
    }

    @Override
    public void setPlayerPermission(UserIdent ident, String permissionNode, boolean value) {
        Set<String> granted = nodes.computeIfAbsent(ident.uuid(), key -> new HashSet<>());
        if (value) {
            granted.add(permissionNode);
        } else {
            granted.remove(permissionNode);
        }
    }

    @Override
    public void setPlayerPermissionProperty(UserIdent ident, String permissionNode, String value) {
        properties.computeIfAbsent(ident.uuid(), key -> new HashMap<>())
            .put(permissionNode, value);
    }

    @Override
    public boolean groupExists(String groupName) {
        return known.contains(groupName);
    }

    @Override
    public boolean createGroup(String groupName) {
        return known.add(groupName);
    }

    @Override
    public void addPlayerToGroup(UserIdent ident, String group) {
        if (known.contains(group)) {
            groups.put(ident.uuid(), group);
        }
    }

    @Override
    public void setDirty(boolean registeredPermission) {}
}
