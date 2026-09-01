package com.mrleonardos.codecore.api.avatar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AvatarConfigTest {

    @Test
    @DisplayName("незнакомый провайдер выключает аватары")
    void unknownProviderIsRefused() {
        AvatarConfig config = AvatarConfig.of("magic", "https://site/a.png", "", "", 64);

        assertEquals(AvatarConfig.PROVIDER_NONE, config.provider());
        assertFalse(config.enabled());
    }

    @Test
    @DisplayName("регистр в названии провайдера не важен")
    void providerIsCaseInsensitive() {
        assertEquals(
            AvatarConfig.PROVIDER_URL,
            AvatarConfig.of("URLTEMPLATE", "https://site/{name}", "", "", 64)
                .provider());
        assertEquals(
            AvatarConfig.PROVIDER_FOLDER,
            AvatarConfig.of("localfolder", "", "", "avatars", 64)
                .provider());
    }

    @Test
    @DisplayName("размер зажимается в разумные границы")
    void sizeIsClamped() {
        assertEquals(
            AvatarConfig.MAX_SIZE,
            AvatarConfig.of(AvatarConfig.PROVIDER_URL, "https://site/{name}", "", "", 4096)
                .size());
        assertEquals(
            AvatarConfig.MIN_SIZE,
            AvatarConfig.of(AvatarConfig.PROVIDER_URL, "https://site/{name}", "", "", 1)
                .size());
    }

    @Test
    @DisplayName("пустые поля не превращаются в null")
    void nullsBecomeEmptyStrings() {
        AvatarConfig config = AvatarConfig.of(AvatarConfig.PROVIDER_FOLDER, null, null, null, 64);

        assertEquals("", config.url());
        assertEquals("", config.jsonPath());
        assertEquals("", config.folder());
        assertTrue(config.enabled());
    }

    @Test
    @DisplayName("выключенная настройка так и говорит")
    void disabledIsDisabled() {
        assertFalse(
            AvatarConfig.disabled()
                .enabled());
    }
}
