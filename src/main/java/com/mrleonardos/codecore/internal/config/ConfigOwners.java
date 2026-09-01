package com.mrleonardos.codecore.internal.config;

/** Имя владельца в имени файла: modid без приставки {@code code}. */
public final class ConfigOwners {

    private ConfigOwners() {}

    public static String of(String modid) {
        if (modid == null || modid.isEmpty()) {
            throw new IllegalArgumentException("Config modid must not be empty");
        }
        if (modid.length() > ConfigKeys.OWNER_PREFIX.length() && modid.startsWith(ConfigKeys.OWNER_PREFIX)) {
            return modid.substring(ConfigKeys.OWNER_PREFIX.length());
        }
        return modid;
    }
}
