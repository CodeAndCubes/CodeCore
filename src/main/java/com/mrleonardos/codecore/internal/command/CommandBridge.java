package com.mrleonardos.codecore.internal.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

import com.mrleonardos.codecore.api.CodeApi;
import com.mrleonardos.codecore.api.command.ArgumentSpec;
import com.mrleonardos.codecore.api.command.CommandMessages;
import com.mrleonardos.codecore.api.command.CommandNode;
import com.mrleonardos.codecore.api.service.PermissionService;

/**
 * Показывает дерево команд ядра как обычную команду Minecraft.
 *
 * <p>
 * Здесь же живёт разбор: спуск по подкомандам, проверка прав на каждом уровне, разбор аргументов и
 * автодополнение. Ветка, на которую у игрока нет права, не выполнится и не появится в подсказках: знать
 * о её существовании ему незачем.
 */
public final class CommandBridge implements ICommand {

    private final CommandNode root;

    CommandBridge(CommandNode root) {
        this.root = root;
    }

    @Override
    public String getCommandName() {
        return root.name();
    }

    @Override
    public List<String> getCommandAliases() {
        return root.aliases();
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return root.usageKey() != null ? root.usageKey() : "/" + root.name();
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return allowed(sender, root);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        CommandNode node = root;
        int cursor = 0;

        while (cursor < args.length) {
            CommandNode child = childMatching(node, args[cursor]);
            if (child == null) {
                break;
            }
            if (!allowed(sender, child)) {
                throw new CommandException(CommandMessages.NO_PERMISSION);
            }
            node = child;
            cursor++;
        }

        if (node.action() == null) {
            throw new CommandException(
                node.children()
                    .isEmpty() ? usageOf(node) : CommandMessages.UNKNOWN_SUBCOMMAND);
        }

        Map<String, Object> values = parseArguments(node, args, cursor);
        node.action()
            .run(new CommandContextImpl(sender, values));
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        CommandNode node = root;
        int cursor = 0;
        while (cursor < args.length - 1) {
            CommandNode child = childMatching(node, args[cursor]);
            if (child == null || !allowed(sender, child)) {
                break;
            }
            node = child;
            cursor++;
        }

        String partial = args.length == 0 ? "" : args[args.length - 1];
        int argumentIndex = args.length - 1 - cursor;
        List<String> options = new ArrayList<>();

        if (argumentIndex == 0) {
            for (CommandNode child : node.children()) {
                if (allowed(sender, child) && startsWith(child.name(), partial)) {
                    options.add(child.name());
                }
            }
        }

        List<ArgumentSpec> arguments = node.arguments();
        if (argumentIndex >= 0 && argumentIndex < arguments.size()) {
            options.addAll(
                arguments.get(argumentIndex)
                    .type()
                    .suggestions(sender, partial));
        }
        return options;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(Object other) {
        return getCommandName().compareTo(((ICommand) other).getCommandName());
    }

    private Map<String, Object> parseArguments(CommandNode node, String[] args, int start) {
        Map<String, Object> values = new LinkedHashMap<>();
        int cursor = start;

        for (ArgumentSpec spec : node.arguments()) {
            if (cursor >= args.length) {
                if (spec.optional()) {
                    break;
                }
                throw new CommandException(CommandMessages.MISSING_ARGUMENT, spec.name());
            }
            if (spec.type()
                .greedy()) {
                values.put(
                    spec.name(),
                    spec.type()
                        .parse(join(args, cursor)));
                break;
            }
            values.put(
                spec.name(),
                spec.type()
                    .parse(args[cursor]));
            cursor++;
        }
        return values;
    }

    private static String join(String[] args, int from) {
        StringBuilder text = new StringBuilder();
        for (int index = from; index < args.length; index++) {
            if (text.length() > 0) {
                text.append(' ');
            }
            text.append(args[index]);
        }
        return text.toString();
    }

    private static CommandNode childMatching(CommandNode node, String word) {
        for (CommandNode child : node.children()) {
            if (child.matches(word)) {
                return child;
            }
        }
        return null;
    }

    private static String usageOf(CommandNode node) {
        return node.usageKey() != null ? node.usageKey() : CommandMessages.USAGE;
    }

    private static boolean startsWith(String candidate, String partial) {
        return candidate.toLowerCase(Locale.ROOT)
            .startsWith(partial.toLowerCase(Locale.ROOT));
    }

    private static boolean allowed(ICommandSender sender, CommandNode node) {
        String permission = node.permissionNode();
        if (permission == null) {
            return true;
        }
        return CodeApi.services()
            .require(PermissionService.class)
            .has(sender, permission);
    }
}
