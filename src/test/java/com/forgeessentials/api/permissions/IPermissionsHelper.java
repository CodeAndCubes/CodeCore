package com.forgeessentials.api.permissions;

import com.forgeessentials.api.UserIdent;

/**
 * Подставной IPermissionsHelper: сигнатуры дословные, а методов ровно столько, сколько трогают мост и проверка.
 *
 * <p>
 * Настоящий интерфейс наследует {@code net.minecraftforge.permission.IPermissionProvider}, которого в чистом Forge
 * нет, и несёт ещё зоны, области и глобальные проверки. Повторять их незачем: мост ищет методы по имени у того
 * объекта, что лежит в {@code APIRegistry.perms}.
 */
public interface IPermissionsHelper {

    boolean checkUserPermission(UserIdent ident, String permissionNode);

    String getUserPermissionProperty(UserIdent ident, String permissionNode);

    String getPrimaryGroup(UserIdent ident);

    void setPlayerPermission(UserIdent ident, String permissionNode, boolean value);

    void setPlayerPermissionProperty(UserIdent ident, String permissionNode, String value);

    boolean groupExists(String groupName);

    boolean createGroup(String groupName);

    void addPlayerToGroup(UserIdent ident, String group);

    void setDirty(boolean registeredPermission);
}
