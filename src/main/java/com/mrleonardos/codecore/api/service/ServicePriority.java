package com.mrleonardos.codecore.api.service;

/**
 * Вес реализации сервиса. Побеждает большая: так специализированный мод вытесняет встроенную заглушку,
 * не трогая её код.
 */
public enum ServicePriority {

    /** Встроенная реализация ядра. Работает, пока никто не предложил лучше. */
    BUILTIN(0),

    /** Реализация из отдельного мода, например права из CodePerms. */
    ADDON(100),

    /** Осознанная замена всего перечисленного выше. Держать для случаев, когда иначе никак. */
    OVERRIDE(200);

    private final int weight;

    ServicePriority(int weight) {
        this.weight = weight;
    }

    /** Числовой вес: чем больше, тем выше приоритет. */
    public int weight() {
        return weight;
    }
}
