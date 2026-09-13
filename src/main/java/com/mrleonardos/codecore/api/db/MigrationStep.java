package com.mrleonardos.codecore.api.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Один шаг схемы: номер версии и стейтменты, которые к ней ведут.
 *
 * <p>
 * Шаг применяется одной транзакцией вместе со строкой о версии, поэтому наполовину применённого шага не
 * бывает. Исключение одно, и оно от СУБД: MariaDB завершает транзакцию на каждом DDL сама, поэтому шаги
 * со схемой пишутся идемпотентно ({@code create table if not exists}).
 *
 * <p>
 * Список стейтментов копируется при создании: шаг живёт дольше вызова и должен читаться одинаково в
 * любом потоке.
 */
public final class MigrationStep {

    private final int version;
    private final List<String> statements;

    private MigrationStep(int version, List<String> statements) {
        this.version = version;
        this.statements = Collections.unmodifiableList(new ArrayList<>(statements));
    }

    /**
     * Шаг к указанной версии.
     *
     * @throws IllegalArgumentException если версия не положительна или стейтментов нет
     */
    public static MigrationStep of(int version, String... statements) {
        return of(version, Arrays.asList(statements));
    }

    /** То же со списком: удобно, когда стейтменты пришли из ресурса. */
    public static MigrationStep of(int version, List<String> statements) {
        if (version <= 0) {
            throw new IllegalArgumentException("Migration version must be positive, got " + version);
        }
        if (statements == null || statements.isEmpty()) {
            throw new IllegalArgumentException("Migration step " + version + " has no statements");
        }
        for (String statement : statements) {
            if (statement == null || statement.trim()
                .isEmpty()) {
                throw new IllegalArgumentException("Migration step " + version + " has an empty statement");
            }
        }
        return new MigrationStep(version, statements);
    }

    public int version() {
        return version;
    }

    /** Стейтменты в порядке исполнения. Список менять нельзя. */
    public List<String> statements() {
        return statements;
    }
}
