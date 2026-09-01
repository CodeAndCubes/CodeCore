package com.mrleonardos.codecore.internal.permission;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.mrleonardos.codecore.CoreConstants;
import com.mrleonardos.codecore.api.CodeApi;
import com.mrleonardos.codecore.api.adapter.RoleAdapter;
import com.mrleonardos.codecore.api.adapter.RoleCapability;
import com.mrleonardos.codecore.api.adapter.RoleOwnerKind;
import com.mrleonardos.codecore.api.adapter.RoleServices;
import com.mrleonardos.codecore.api.config.ConfigFile;
import com.mrleonardos.codecore.api.config.ConfigRoles;
import com.mrleonardos.codecore.api.config.ConfigScope;
import com.mrleonardos.codecore.api.config.ConfigSpec;
import com.mrleonardos.codecore.api.service.PermissionService;

/**
 * Заявка встроенной реализации прав.
 *
 * <p>
 * Файл групп открывается только в {@link #create()}, то есть когда роль действительно досталась ядру:
 * плодить {@code core-groups.toml} рядом с файлами CodePerms, который эту роль и держит, значило бы
 * запутывать админа.
 */
public final class BuiltinPermissions implements RoleAdapter {

    private final ConfigFile<PermissionsSection> section;

    public BuiltinPermissions(ConfigFile<PermissionsSection> section) {
        this.section = section;
    }

    @Override
    public String role() {
        return ConfigRoles.PERMISSIONS;
    }

    @Override
    public String name() {
        return CoreConstants.MODID;
    }

    @Override
    public RoleOwnerKind kind() {
        return RoleOwnerKind.BUILTIN;
    }

    @Override
    public boolean available() {
        return true;
    }

    @Override
    public Set<RoleCapability> capabilities() {
        return new HashSet<>(Arrays.asList(PermissionRole.HAS, PermissionRole.GROUP, PermissionRole.META));
    }

    @Override
    public RoleServices create() {
        ConfigFile<CoreGroupsFile> groups = CodeApi.configs()
            .open(spec());
        return RoleServices.builder()
            .add(PermissionService.class, new GroupsPermissionService(groups, section, OperatorLookup::isOperator))
            .build();
    }

    static ConfigSpec<CoreGroupsFile> spec() {
        return ConfigSpec.of(CoreConstants.MODID, PermissionKeys.FILE_NAME, CoreGroupsFile.class)
            .role(ConfigRoles.PERMISSIONS)
            .scope(ConfigScope.SETTINGS)
            .defaults(CoreGroupsFile::defaults)
            .validator(CoreGroupsFile::normalize)
            .build();
    }
}
