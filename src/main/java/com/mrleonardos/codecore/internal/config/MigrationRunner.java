package com.mrleonardos.codecore.internal.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mrleonardos.codecore.api.config.Migration;

/**
 * Проводит данные файла по цепочке миграций до текущей версии схемы.
 *
 * <p>
 * Ни один сценарий здесь не роняет сервер: файл из будущей версии мода остаётся нетронутым, разрыв в
 * цепочке миграций останавливает обновление на последней достижимой версии. В обоих случаях в логе
 * остаётся внятная причина.
 */
public final class MigrationRunner {

    private MigrationRunner() {}

    /**
     * Привести данные к целевой версии.
     *
     * <p>
     * Оборванная цепочка и дошедшая до конца различаются намеренно: в первом случае разбирать данные
     * текущим классом можно, а переписывать ими файл нельзя, иначе поля, которых ждали пропущенные шаги,
     * исчезнут с диска.
     */
    public static MigrationOutcome run(JsonObject data, List<Migration> migrations, int targetVersion, String fileName,
        Logger log) {
        int current = readVersion(data, targetVersion);
        if (current == targetVersion) {
            return MigrationOutcome.UNCHANGED;
        }
        if (current > targetVersion) {
            log.warn(
                "Config {} has schema version {}, which is newer than supported {}; leaving the file untouched",
                fileName,
                current,
                targetVersion);
            return MigrationOutcome.UNCHANGED;
        }

        Map<Integer, Migration> byVersion = index(migrations, fileName, log);
        boolean complete = true;
        while (current < targetVersion) {
            Migration migration = byVersion.get(current);
            if (migration == null) {
                log.warn("Config {} has no migration from schema version {}, stopping there", fileName, current);
                complete = false;
                break;
            }
            migration.apply(data);
            log.info("Config {} migrated from schema version {} to {}", fileName, current, migration.to());
            current = migration.to();
        }

        data.addProperty(ConfigKeys.SCHEMA_VERSION, current);
        return complete ? MigrationOutcome.MIGRATED : MigrationOutcome.INCOMPLETE;
    }

    private static int readVersion(JsonObject data, int fallback) {
        JsonElement stored = data.get(ConfigKeys.SCHEMA_VERSION);
        if (stored == null || !stored.isJsonPrimitive()) {
            return 1;
        }
        try {
            return stored.getAsInt();
        } catch (NumberFormatException malformed) {
            return fallback;
        }
    }

    private static Map<Integer, Migration> index(List<Migration> migrations, String fileName, Logger log) {
        Map<Integer, Migration> byVersion = new HashMap<>();
        for (Migration migration : migrations) {
            Migration previous = byVersion.put(migration.from(), migration);
            if (previous != null) {
                log.warn(
                    "Config {} has two migrations from schema version {}, using the last one",
                    fileName,
                    migration.from());
            }
        }
        return byVersion;
    }
}
