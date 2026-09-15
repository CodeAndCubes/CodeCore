package com.mrleonardos.codecore.internal.adapter;

import java.util.Set;

import com.mrleonardos.codecore.api.adapter.RoleCapability;
import com.mrleonardos.codecore.api.adapter.RoleStatus;

/**
 * Строки о состоянии ролей: журнал старта и команда {@code /codecore adapters} собирают их одинаково.
 *
 * <p>
 * Имена собираются из полей, а не печатанием самих объектов: строки ролей живут в логах чужих серверов и
 * переживают обновления мода.
 */
public final class RoleLines {

    private static final String SEPARATOR = ", ";

    private RoleLines() {}

    /** Имена заявок роли через запятую, недоступные уже помечены звёздочкой. */
    public static String candidates(RoleStatus status) {
        return String.join(SEPARATOR, status.candidates());
    }

    /** Имена умений через запятую. */
    public static String capabilities(Set<RoleCapability> capabilities) {
        StringBuilder text = new StringBuilder();
        for (RoleCapability capability : capabilities) {
            if (text.length() > 0) {
                text.append(SEPARATOR);
            }
            text.append(capability.name());
        }
        return text.toString();
    }
}
