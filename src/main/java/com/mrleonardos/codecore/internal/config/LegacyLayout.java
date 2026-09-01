package com.mrleonardos.codecore.internal.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;

/**
 * Папки прежней раскладки {@code config/<modid>}.
 *
 * <p>
 * Конвертеров нет: старые файлы просто перестают читаться и остаются лежать нетронутыми. Молчать об
 * этом нельзя, иначе админ, который правил свои варпы, не поймёт, куда они делись.
 */
public final class LegacyLayout {

    private static final String[] MODIDS = { "codecore", "codeperms", "codeeconomy", "codeessentials", "codechat" };

    private LegacyLayout() {}

    public static void report(Path configDirectory, Logger log) {
        for (String modid : find(configDirectory)) {
            log.warn("Found config/{} from the previous layout, nothing was read from it", modid);
        }
    }

    /** Папки прежней раскладки, которые лежат рядом прямо сейчас. */
    public static List<String> find(Path configDirectory) {
        List<String> found = new ArrayList<>();
        for (String modid : MODIDS) {
            if (Files.isDirectory(configDirectory.resolve(modid))) {
                found.add(modid);
            }
        }
        return found;
    }
}
