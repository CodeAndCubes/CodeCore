package com.mrleonardos.codecore.internal.command;

import com.mrleonardos.codecore.CoreMessages;
import com.mrleonardos.codecore.CorePermissions;
import com.mrleonardos.codecore.api.CodeApi;
import com.mrleonardos.codecore.api.command.CommandNode;

/** Обслуживающие команды ядра. */
public final class CoreCommands {

    private CoreCommands() {}

    /** Дерево {@code /codecore}: пока только перезагрузка настроек. */
    public static CommandNode root() {
        return CommandNode.literal("codecore")
            .permission(CorePermissions.ADMIN)
            .usage(CoreMessages.USAGE_ROOT)
            .child(
                CommandNode.literal("reload")
                    .permission(CorePermissions.RELOAD)
                    .executes(context -> {
                        CodeApi.configs()
                            .reloadAll();
                        context.reply(CoreMessages.CONFIGS_RELOADED);
                    }));
    }
}
