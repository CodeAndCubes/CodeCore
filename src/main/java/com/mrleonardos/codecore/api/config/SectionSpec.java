package com.mrleonardos.codecore.api.config;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Описание секции главного файла {@code config/code/config.toml}.
 *
 * <p>
 * У каждой секции ровно один объявитель: мод приносит описание в фазе init, а ядро в конце
 * постинициализации пишет файл со всеми секциями сразу. Секция мода, которого сейчас на сервере нет,
 * остаётся в файле нетронутой вместе со своими комментариями.
 *
 * <pre>
 *
 * ConfigFile&lt;EconomySection&gt; economy = CodeApi.configs()
 *     .section(
 *         SectionSpec.of("economy", EconomySection.class)
 *             .build());
 * </pre>
 */
public final class SectionSpec<T> {

    private final String name;
    private final Class<T> type;
    private final Supplier<T> defaults;
    private final Consumer<T> validator;

    private SectionSpec(Builder<T> builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.defaults = builder.defaults != null ? builder.defaults : ConfigDefaults.reflective(builder.type);
        this.validator = builder.validator != null ? builder.validator : value -> {};
    }

    /**
     * Начать описание секции.
     *
     * @param name имя секции в файле, оно же заголовок в квадратных скобках
     * @param type класс, в который разбираются значения; нужен конструктор без аргументов
     */
    public static <T> Builder<T> of(String name, Class<T> type) {
        return new Builder<>(name, type);
    }

    public String name() {
        return name;
    }

    public Class<T> type() {
        return type;
    }

    /** Значения секции, когда её в файле ещё нет. */
    public Supplier<T> defaults() {
        return defaults;
    }

    /** Что прогоняется по разобранным значениям до того, как их увидит мод. */
    public Consumer<T> validator() {
        return validator;
    }

    public static final class Builder<T> {

        private final String name;
        private final Class<T> type;
        private Supplier<T> defaults;
        private Consumer<T> validator;

        private Builder(String name, Class<T> type) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Section name must not be empty");
            }
            this.name = name;
            this.type = type;
        }

        /** Значения для новой секции, если конструктора без аргументов недостаточно. */
        public Builder<T> defaults(Supplier<T> value) {
            this.defaults = value;
            return this;
        }

        /** Починка разобранных значений перед выдачей моду. Бросать из неё нельзя. */
        public Builder<T> validator(Consumer<T> value) {
            this.validator = value;
            return this;
        }

        public SectionSpec<T> build() {
            return new SectionSpec<>(this);
        }
    }
}
