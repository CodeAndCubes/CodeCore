package com.mrleonardos.codecore.internal.permission;

import java.util.LinkedHashMap;
import java.util.Map;

/** Содержимое {@code config/codecore/permissions.json}. */
public final class PermissionFile {

    public String defaultGroup = PermissionKeys.DEFAULT_GROUP;
    public String opGroup = PermissionKeys.OPERATOR_GROUP;
    public Map<String, GroupEntry> groups = new LinkedHashMap<>();
    public Map<String, PlayerEntry> players = new LinkedHashMap<>();

    /**
     * Привести прочитанный файл в рабочий вид.
     *
     * <p>
     * Правится то, что появляется только у файла, отредактированного руками: {@code null} вместо списка или
     * карты, пустое имя группы по умолчанию, {@code null} среди правил. Ставится хуком проверки в
     * {@code ConfigSpec}, поэтому проверять права до неё некому.
     */
    public void normalize() {
        if (defaultGroup == null || defaultGroup.isEmpty()) {
            defaultGroup = PermissionKeys.DEFAULT_GROUP;
        }
        if (opGroup == null || opGroup.isEmpty()) {
            opGroup = PermissionKeys.OPERATOR_GROUP;
        }
        groups = PermissionEntries.named(groups, GroupEntry::normalize);
        players = PermissionEntries.named(players, PlayerEntry::normalize);
    }

    /** Заготовка для нового файла: обычные игроки и операторы, которым можно всё. */
    public static PermissionFile defaults() {
        PermissionFile file = new PermissionFile();
        file.groups.put(PermissionKeys.DEFAULT_GROUP, new GroupEntry());

        GroupEntry operators = new GroupEntry();
        operators.inherits.add(PermissionKeys.DEFAULT_GROUP);
        operators.nodes.add(PermissionKeys.WILDCARD);
        file.groups.put(PermissionKeys.OPERATOR_GROUP, operators);

        return file;
    }
}
