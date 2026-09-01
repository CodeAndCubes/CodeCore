package com.mrleonardos.codecore.internal.permission;

/**
 * Сопоставление правил с нодами.
 *
 * <p>
 * Звёздочка в конце покрывает всё, что глубже: {@code codechat.*} разрешает и
 * {@code codechat.channel.global.write}. Звёздочка в середине заменяет ровно один сегмент, поэтому
 * {@code codechat.channel.*.read} говорит про чтение любого канала, но не про запись в него.
 *
 * <p>
 * Точность правила равна числу сегментов без звёздочек. Из подходящих правил выигрывает самое точное, а
 * при равной точности — запрет.
 */
public final class NodeMatcher {

    private NodeMatcher() {}

    public static boolean matches(String pattern, String node) {
        String[] patternParts = split(pattern);
        String[] nodeParts = split(node);

        for (int index = 0; index < patternParts.length; index++) {
            String part = patternParts[index];
            if (PermissionKeys.WILDCARD.equals(part)) {
                if (index == patternParts.length - 1) {
                    return true;
                }
                if (index >= nodeParts.length) {
                    return false;
                }
                continue;
            }
            if (index >= nodeParts.length || !part.equals(nodeParts[index])) {
                return false;
            }
        }
        return patternParts.length == nodeParts.length;
    }

    public static int specificity(String pattern) {
        int score = 0;
        for (String part : split(pattern)) {
            if (!PermissionKeys.WILDCARD.equals(part)) {
                score++;
            }
        }
        return score;
    }

    private static String[] split(String value) {
        return value.split("\\" + PermissionKeys.NODE_SEPARATOR);
    }
}
