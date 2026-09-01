package com.mrleonardos.codecore.api.command;

/** Аргумент команды: имя, тип и обязательность. */
public final class ArgumentSpec {

    private final String name;
    private final ArgumentType<?> type;
    private final boolean optional;

    ArgumentSpec(String name, ArgumentType<?> type, boolean optional) {
        this.name = name;
        this.type = type;
        this.optional = optional;
    }

    public String name() {
        return name;
    }

    public ArgumentType<?> type() {
        return type;
    }

    public boolean optional() {
        return optional;
    }
}
