package com.mrleonardos.codecore.internal.permission;

import java.util.UUID;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrleonardos.codecore.api.service.PermissionService;

/**
 * Права по списку операторов сервера: то, что отвечает, когда роль прав не занята никем.
 *
 * <p>
 * Так ведёт себя ванильный Minecraft без единого мода прав, и это меньшее из зол. Роль без владельца
 * оставляла бы {@code require(PermissionService.class)} без ответа, а исключение посреди чужой команды
 * хуже отказа: админ, снявший роль, получал сервер, где не работает ни одна команда линейки, включая
 * ту, что показывает состояние ролей.
 *
 * <p>
 * Умение здесь одно, {@code has}: групп и меты у списка операторов нет, поэтому {@code group} отвечает
 * пустым именем, а {@code meta} запасным значением. Всё остальное роли в этом исходе не работает и
 * названо в стартовой строке.
 */
final class OperatorPermissions implements PermissionService {

    private static final String NO_GROUP = "";

    private final OperatorCheck operators;

    OperatorPermissions(OperatorCheck operators) {
        this.operators = operators;
    }

    @Override
    public boolean has(UUID player, String node) {
        return operators.isOperator(player);
    }

    @Override
    public boolean has(ICommandSender sender, String node) {
        if (!(sender instanceof EntityPlayerMP)) {
            return true;
        }
        return has(((EntityPlayerMP) sender).getUniqueID(), node);
    }

    @Override
    public String group(UUID player) {
        return NO_GROUP;
    }

    @Override
    public String meta(UUID player, String key, String fallback) {
        return fallback;
    }
}
