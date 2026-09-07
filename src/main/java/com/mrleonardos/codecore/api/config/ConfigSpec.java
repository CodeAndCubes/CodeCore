package com.mrleonardos.codecore.api.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Описание файла настроек: где он лежит, во что разбирается и как обновляется со старых версий.
 *
 * <p>
 * Путь собирает ядро из роли, скоупа, владельца и имени. Владелец получается из modid отбрасыванием
 * приставки {@code code}: {@code codeperms} даёт {@code perms}, а чужой {@code myperms} идёт в имя
 * целиком.
 *
 * <pre>
 *
 * ConfigSpec&lt;PermsSettings&gt; own = ConfigSpec.settings(MODID, PermsSettings.class)
 *     .role(ConfigRoles.PERMISSIONS)
 *     .build();                                     config/code/permissions/perms.toml
 *
 * ConfigSpec&lt;GroupsFile&gt; groups = ConfigSpec.of(MODID, "groups", GroupsFile.class)
 *     .role(ConfigRoles.PERMISSIONS)
 *     .build();                                     config/code/permissions/perms-groups.toml
 *
 * ConfigSpec&lt;PlayersFile&gt; players = ConfigSpec.of(MODID, "players", PlayersFile.class)
 *     .role(ConfigRoles.PERMISSIONS)
 *     .format(ConfigFormat.JSON)
 *     .build();                                     config/code/permissions/perms-players.json
 * </pre>
 */
public final class ConfigSpec<T> {

    /** Имя файла, который принадлежит владельцу целиком: {@code perms.toml}, а не {@code perms-что.toml}. */
    public static final String OWN_NAME = "";

    private final String modid;
    private final String name;
    private final Class<T> type;
    private final String role;
    private final ConfigScope scope;
    private final ConfigFormat format;
    private final int schemaVersion;
    private final List<Migration> migrations;
    private final Supplier<T> defaults;
    private final Consumer<T> validator;
    private final boolean foreign;

    private ConfigSpec(Builder<T> builder) {
        if (builder.role == null) {
            throw new IllegalArgumentException(
                "Config " + builder.modid + "/" + builder.name + " needs a role, see ConfigRoles");
        }
        this.modid = builder.modid;
        this.name = builder.name;
        this.type = builder.type;
        this.role = builder.role;
        this.scope = builder.scope;
        this.format = builder.format;
        this.schemaVersion = builder.schemaVersion;
        this.migrations = Collections.unmodifiableList(new ArrayList<>(builder.migrations));
        this.defaults = builder.defaults != null ? builder.defaults : ConfigDefaults.reflective(builder.type);
        this.validator = builder.validator != null ? builder.validator : value -> {};
        this.foreign = builder.foreign;
    }

    /**
     * Начать описание файла {@code <владелец>-<name>}.
     *
     * @param modid идентификатор мода, из него получается владелец в имени файла
     * @param name  что это за файл: {@code groups}, {@code warps}, {@code channels}
     * @param type  класс, в который разбираются значения; нужен конструктор без аргументов
     */
    public static <T> Builder<T> of(String modid, String name, Class<T> type) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Config name must not be empty, use ConfigSpec.settings instead");
        }
        return new Builder<>(modid, name, type);
    }

    /** Начать описание собственного файла настроек владельца: {@code <владелец>.toml}. */
    public static <T> Builder<T> settings(String modid, Class<T> type) {
        return new Builder<>(modid, OWN_NAME, type);
    }

    public String modid() {
        return modid;
    }

    /** Что это за файл или {@link #OWN_NAME} для собственных настроек владельца. */
    public String name() {
        return name;
    }

    public Class<T> type() {
        return type;
    }

    /** Область ответственности: она же папка внутри {@code config/code}. */
    public String role() {
        return role;
    }

    public ConfigScope scope() {
        return scope;
    }

    public ConfigFormat format() {
        return format;
    }

    public int schemaVersion() {
        return schemaVersion;
    }

    public List<Migration> migrations() {
        return migrations;
    }

    /** Значения по умолчанию для нового файла. */
    public Supplier<T> defaults() {
        return defaults;
    }

    /** Что прогоняется по разобранному значению до того, как его увидит мод. */
    public Consumer<T> validator() {
        return validator;
    }

    /** Принадлежит ли файл другому моду: тогда ядро само в него не пишет. */
    public boolean foreign() {
        return foreign;
    }

    public static final class Builder<T> {

        private final String modid;
        private final String name;
        private final Class<T> type;
        private final List<Migration> migrations = new ArrayList<>();
        private String role;
        private ConfigScope scope = ConfigScope.SETTINGS;
        private ConfigFormat format = ConfigFormat.TOML;
        private int schemaVersion = 1;
        private Supplier<T> defaults;
        private Consumer<T> validator;
        private boolean foreign;

        private Builder(String modid, String name, Class<T> type) {
            this.modid = modid;
            this.name = name;
            this.type = type;
        }

        /**
         * Область ответственности, она же папка. Имена линейки перечислены в {@link ConfigRoles}.
         *
         * @throws IllegalArgumentException если имя не годится именем каталога
         */
        public Builder<T> role(String value) {
            this.role = ConfigRoles.check(value);
            return this;
        }

        /** Назначение файла. По умолчанию настройки сервера. */
        public Builder<T> scope(ConfigScope value) {
            this.scope = value;
            return this;
        }

        /** Формат файла. По умолчанию toml; json берут для данных, которые правит только мод. */
        public Builder<T> format(ConfigFormat value) {
            this.format = value;
            return this;
        }

        /** Текущая версия схемы. По умолчанию первая. */
        public Builder<T> schemaVersion(int value) {
            this.schemaVersion = value;
            return this;
        }

        /** Добавить шаг обновления со старой версии. Порядок добавления не важен. */
        public Builder<T> migration(Migration value) {
            this.migrations.add(value);
            return this;
        }

        /** Значения для нового файла, если конструктора без аргументов недостаточно. */
        public Builder<T> defaults(Supplier<T> value) {
            this.defaults = value;
            return this;
        }

        /**
         * Починка разобранного значения перед выдачей моду.
         *
         * <p>
         * Файл, правленный руками, приносит пустую строку, отрицательное число или список там, где ждали
         * карту. Проверка вызывается один раз на чтение файла, бросать из неё нельзя.
         */
        public Builder<T> validator(Consumer<T> value) {
            this.validator = value;
            return this;
        }

        /**
         * Файл принадлежит другому моду, мы его читаем.
         *
         * <p>
         * Ядро тогда не пишет в него само: не создаёт отсутствующий, не дописывает свои поля, не
         * отодвигает нечитаемый в {@code .broken}. Нечитаемый файл остаётся на месте, а
         * {@link ConfigFile#loaded()} отвечает «нет». Запись остаётся только явная, через
         * {@link ConfigFile#save()}: так импорт помечает чужой файл перенесённым.
         */
        public Builder<T> foreign() {
            this.foreign = true;
            return this;
        }

        public ConfigSpec<T> build() {
            return new ConfigSpec<>(this);
        }
    }
}
