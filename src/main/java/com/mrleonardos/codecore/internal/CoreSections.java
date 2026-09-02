package com.mrleonardos.codecore.internal;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.config.ConfigFile;
import com.mrleonardos.codecore.api.config.ConfigService;
import com.mrleonardos.codecore.api.config.SectionSpec;
import com.mrleonardos.codecore.internal.config.ImagesSection;
import com.mrleonardos.codecore.internal.permission.PermissionsSection;

/**
 * Секции главного файла, которые объявляет само ядро.
 *
 * <p>
 * Объявляются раньше всех остальных, поэтому в файле стоят сверху: сначала общее для линейки, потом
 * секции модов в порядке их загрузки.
 */
public final class CoreSections {

    private static final String IMAGES_SECTION = "images";
    private static final String PERMISSIONS_SECTION = "permissions";

    private final ConfigFile<PermissionsSection> permissions;
    private final ConfigFile<ImagesSection> images;

    public CoreSections(ConfigService configs, Logger log) {
        this.permissions = configs.section(
            SectionSpec.of(PERMISSIONS_SECTION, PermissionsSection.class)
                .validator(section -> section.normalize(log))
                .build());
        this.images = configs.section(
            SectionSpec.of(IMAGES_SECTION, ImagesSection.class)
                .build());
    }

    /** Группы по умолчанию: их спрашивает не только встроенная реализация прав. */
    public ConfigFile<PermissionsSection> permissions() {
        return permissions;
    }

    /** Пределы картинок: их читает клиентская сторона. */
    public ConfigFile<ImagesSection> images() {
        return images;
    }
}
