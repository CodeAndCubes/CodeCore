package com.mrleonardos.codecore.api.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Узел дерева команд.
 *
 * <pre>
 *
 * CommandNode.literal("chatadmin")
 *     .permission("codechat.admin")
 *     .child(
 *         CommandNode.literal("mute")
 *             .permission("codechat.admin.mute")
 *             .arg("player", ArgumentTypes.player())
 *             .arg("duration", ArgumentTypes.duration())
 *             .optionalArg("reason", ArgumentTypes.text())
 *             .executes(context -&gt; mute(context)));
 * </pre>
 *
 * <p>
 * Узел либо разветвляется на подкоманды, либо принимает аргументы и что-то делает. Право проверяется на
 * каждом уровне: недоступная ветка не появится и в автодополнении.
 */
public final class CommandNode {

    private final String name;
    private final List<String> aliases = new ArrayList<>();
    private final List<CommandNode> children = new ArrayList<>();
    private final List<ArgumentSpec> arguments = new ArrayList<>();
    private String permission;
    private String usageKey;
    private CommandAction action;

    private CommandNode(String name) {
        this.name = name;
    }

    /** Начать команду или подкоманду с указанным словом. */
    public static CommandNode literal(String name) {
        return new CommandNode(name);
    }

    /** Другие слова, по которым узел тоже отзывается. */
    public CommandNode alias(String... values) {
        aliases.addAll(Arrays.asList(values));
        return this;
    }

    /** Право, без которого узел недоступен и невидим. */
    public CommandNode permission(String node) {
        this.permission = node;
        return this;
    }

    /** Ключ перевода с подсказкой по использованию. */
    public CommandNode usage(String translationKey) {
        this.usageKey = translationKey;
        return this;
    }

    /** Обязательный аргумент. */
    public CommandNode arg(String argumentName, ArgumentType<?> type) {
        arguments.add(new ArgumentSpec(argumentName, type, false));
        return this;
    }

    /** Необязательный аргумент. Идёт после обязательных. */
    public CommandNode optionalArg(String argumentName, ArgumentType<?> type) {
        arguments.add(new ArgumentSpec(argumentName, type, true));
        return this;
    }

    /** Подкоманда. */
    public CommandNode child(CommandNode node) {
        children.add(node);
        return this;
    }

    /** Что выполнить, когда разбор дошёл до этого узла. */
    public CommandNode executes(CommandAction value) {
        this.action = value;
        return this;
    }

    public String name() {
        return name;
    }

    public List<String> aliases() {
        return Collections.unmodifiableList(aliases);
    }

    public List<CommandNode> children() {
        return Collections.unmodifiableList(children);
    }

    public List<ArgumentSpec> arguments() {
        return Collections.unmodifiableList(arguments);
    }

    public String permissionNode() {
        return permission;
    }

    public String usageKey() {
        return usageKey;
    }

    public CommandAction action() {
        return action;
    }

    /** Отзывается ли узел на это слово. */
    public boolean matches(String word) {
        if (name.equalsIgnoreCase(word)) {
            return true;
        }
        for (String alias : aliases) {
            if (alias.equalsIgnoreCase(word)) {
                return true;
            }
        }
        return false;
    }
}
