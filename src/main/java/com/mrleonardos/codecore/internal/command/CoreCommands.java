package com.mrleonardos.codecore.internal.command;

import java.util.List;

import com.mrleonardos.codecore.CoreMessages;
import com.mrleonardos.codecore.CorePermissions;
import com.mrleonardos.codecore.api.CodeApi;
import com.mrleonardos.codecore.api.adapter.RoleStatus;
import com.mrleonardos.codecore.api.command.CommandContext;
import com.mrleonardos.codecore.api.command.CommandNode;
import com.mrleonardos.codecore.internal.adapter.RoleLines;

/** Обслуживающие команды ядра. */
public final class CoreCommands {

    private static final String NOTHING = "-";

    private CoreCommands() {}

    /** Дерево {@code /codecore}: перезагрузка настроек и состояние ролей. */
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
                    }))
            .child(
                CommandNode.literal("adapters")
                    .permission(CorePermissions.ADAPTERS)
                    .executes(
                        context -> report(
                            context,
                            CodeApi.adapters()
                                .statuses())));
    }

    static void report(CommandContext context, List<RoleStatus> statuses) {
        for (RoleStatus status : statuses) {
            if (status.owner() == null) {
                context.reply(
                    CoreMessages.ADAPTERS_NOBODY,
                    status.role(),
                    status.choice()
                        .name());
            } else {
                context.reply(
                    CoreMessages.ADAPTERS_ROLE,
                    status.role(),
                    status.owner(),
                    status.choice()
                        .name());
            }
            context.reply(CoreMessages.ADAPTERS_CANDIDATES, shown(RoleLines.candidates(status)));
            context.reply(CoreMessages.ADAPTERS_MISSING, shown(RoleLines.capabilities(status.missing())));
            if (status.fallback() != null) {
                context.reply(CoreMessages.ADAPTERS_FALLBACK, status.fallback());
            }
        }
    }

    private static String shown(String line) {
        return line.isEmpty() ? NOTHING : line;
    }
}
