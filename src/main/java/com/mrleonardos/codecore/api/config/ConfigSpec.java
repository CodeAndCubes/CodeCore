package com.mrleonardos.codecore.api.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Описание файла настроек: где он лежит, во что разбирается и как обновляется со старых версий.
 *
 * <pre>
 * 
 * ConfigSpec&lt;CoreSettings&gt; spec = ConfigSpec.of(MODID, "core", CoreSettings.class)
 *     .scope(ConfigScope.SETTINGS)
 *     .schemaVersion(2)
 *     .migration(new SplitGreetingMigration())
 *     .build();
 * </pre>
 */
public final class ConfigSpec<T> {

    private final String modid;
    private final String name;
    private final Class<T> type;
    private final ConfigScope scope;
    private final int schemaVersion;
    private final List<Migration> migrations;
    private final Supplier<T> defaults;
    private final Consumer<T> validator;

    private ConfigSpec(Builder<T> builder) {
        this.modid = builder.modid;
        this.name = builder.name;
        this.type = builder.type;
        this.scope = builder.scope;
        this.schemaVersion = builder.schemaVersion;
        this.migrations = Collections.unmodifiableList(new ArrayList<>(builder.migrations));
        this.defaults = builder.defaults != null ? builder.defaults : reflectiveDefaults(builder.type);
        this.validator = builder.validator != null ? builder.validator : value -> {};
    }

    /**
     * Начать описание файла.
     *
     * @param modid идентификатор мода, он задаёт папку
     * @param name  имя файла без расширения
     * @param type  класс, в который разбираются значения; нужен конструктор без аргументов
     */
    public static <T> Builder<T> of(String modid, String name, Class<T> type) {
        return new Builder<>(modid, name, type);
    }

    public String modid() {
        return modid;
    }

    public String name() {
        return name;
    }

    public Class<T> type() {
        return type;
    }

    public ConfigScope scope() {
        return scope;
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

    private static <T> Supplier<T> reflectiveDefaults(Class<T> type) {
        return () -> {
            try {
                return type.getDeclaredConstructor()
                    .newInstance();
            } catch (ReflectiveOperationException failure) {
                throw new IllegalStateException(
                    type.getName() + " needs a no-argument constructor or an explicit defaults supplier",
                    failure);
            }
        };
    }

    public static final class Builder<T> {

        private final String modid;
        private final String name;
        private final Class<T> type;
        private final List<Migration> migrations = new ArrayList<>();
        private ConfigScope scope = ConfigScope.SETTINGS;
        private int schemaVersion = 1;
        private Supplier<T> defaults;
        private Consumer<T> validator;

        private Builder(String modid, String name, Class<T> type) {
            this.modid = modid;
            this.name = name;
            this.type = type;
        }

        /** Назначение файла. По умолчанию настройки сервера. */
        public Builder<T> scope(ConfigScope value) {
            this.scope = value;
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
         * Json, правленный руками, приносит {@code null} там, где в классе стоит инициализатор поля: Gson
         * кладёт его поверх. Проверка вызывается один раз на чтение файла, бросать из неё нельзя.
         */
        public Builder<T> validator(Consumer<T> value) {
            this.validator = value;
            return this;
        }

        public ConfigSpec<T> build() {
            return new ConfigSpec<>(this);
        }
    }
}
