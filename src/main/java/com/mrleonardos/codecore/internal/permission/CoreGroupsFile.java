package com.mrleonardos.codecore.internal.permission;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mrleonardos.codecore.api.config.Comment;

/**
 * Содержимое {@code config/code/permissions/core-groups.toml}.
 *
 * <p>
 * Имена групп по умолчанию сюда не входят: они лежат в секции {@code [permissions]} главного файла,
 * потому что их спрашивает не только встроенная реализация.
 */
public final class CoreGroupsFile {

    @Comment({ "Группы: что группа наследует, что разрешает и какие значения даёт участникам.",
        "Правило со знаком минус в начале запрещает и перебивает разрешение той же точности." })
    public Map<String, GroupEntry> groups = new LinkedHashMap<>();

    @Comment("Личные правила игрока по его идентификатору. Они сильнее групповых.")
    public Map<String, PlayerEntry> players = new LinkedHashMap<>();

    /**
     * Привести прочитанный файл в рабочий вид.
     *
     * <p>
     * Правится то, что появляется только у файла, отредактированного руками: пропавший список или карта,
     * пустая строка среди правил, запись без содержимого. Ставится хуком проверки в {@code ConfigSpec},
     * поэтому проверять права до неё некому.
     */
    public void normalize() {
        groups = PermissionEntries.named(groups, GroupEntry::normalize);
        players = PermissionEntries.named(players, PlayerEntry::normalize);
    }

    /** Заготовка для нового файла: обычные игроки и операторы, которым можно всё. */
    public static CoreGroupsFile defaults() {
        CoreGroupsFile file = new CoreGroupsFile();
        file.groups.put(PermissionKeys.DEFAULT_GROUP, new GroupEntry());

        GroupEntry operators = new GroupEntry();
        operators.inherits.add(PermissionKeys.DEFAULT_GROUP);
        operators.nodes.add(PermissionKeys.WILDCARD);
        file.groups.put(PermissionKeys.OPERATOR_GROUP, operators);

        return file;
    }
}
