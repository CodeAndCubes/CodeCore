package com.mrleonardos.codecore.api.config;

/**
 * Области ответственности, по которым разложены файлы линейки: {@code config/code/<роль>/}.
 *
 * <p>
 * Роль это строка, а не перечисление: чужой мод вправе завести свою область и свою папку, не трогая
 * ядро. Перечисленные здесь имена заняты линейкой Code*.
 */
public final class ConfigRoles {

    /** Само ядро: аватары, кэш картинок, клиентские предпочтения ядра. */
    public static final String CORE = "core";

    /** Права: встроенная реализация ядра и CodePerms лежат в одной папке. */
    public static final String PERMISSIONS = "permissions";

    /** Деньги. */
    public static final String ECONOMY = "economy";

    /** Дома, варпы, спавн и перемещения. */
    public static final String ESSENTIALS = "essentials";

    /** Чат. */
    public static final String CHAT = "chat";

    /** Служебные мелочи сервера: рассылка, задания по расписанию, перезапуск, очистка, слоты. */
    public static final String UTILS = "utils";

    /** Контент из конфига: архетипы, экземпляры, привязка мест. */
    public static final String CONTENT = "content";

    /** Приваты и регионы: правила, роли, шаблоны, сами регионы. */
    public static final String REGIONS = "regions";

    /** Наибольшая длина имени роли. */
    public static final int MAX_LENGTH = 32;

    private ConfigRoles() {}

    /**
     * Проверить имя роли как имя каталога: нижний регистр, латиница, цифры и дефис.
     *
     * @return то же имя, чтобы проверку можно было поставить прямо в присваивание
     * @throws IllegalArgumentException если имя не годится каталогом
     */
    public static String check(String role) {
        if (role == null || role.isEmpty() || role.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                "Config role must be 1.." + MAX_LENGTH + " characters long, got " + role);
        }
        for (int index = 0; index < role.length(); index++) {
            char symbol = role.charAt(index);
            boolean allowed = (symbol >= 'a' && symbol <= 'z') || (symbol >= '0' && symbol <= '9') || symbol == '-';
            if (!allowed) {
                throw new IllegalArgumentException(
                    "Config role must be lowercase latin, digits and dashes, got " + role);
            }
        }
        return role;
    }
}
