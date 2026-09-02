package com.mrleonardos.codecore.internal.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.internal.LogCapture;

class PermissionsSectionTest {

    private static final Logger LOG = LogManager.getLogger(PermissionsSectionTest.class);

    @Test
    @DisplayName("пустое имя группы возвращается к заводскому и названо в логе")
    void emptyNameIsReplacedAndReported() {
        PermissionsSection section = new PermissionsSection();
        section.defaultGroup = "";
        section.opGroup = null;
        LogCapture capture = LogCapture.attach(LOG);

        try {
            section.normalize(LOG);

            assertEquals("player", section.defaultGroup);
            assertEquals("admin", section.opGroup);
            assertTrue(
                capture.text()
                    .contains("Config permissions.defaultGroup is empty, using \"player\" instead"),
                capture.text());
            assertTrue(
                capture.text()
                    .contains("Config permissions.opGroup is empty, using \"admin\" instead"),
                capture.text());
        } finally {
            capture.detach();
        }
    }

    @Test
    @DisplayName("заполненные имена молчат: строка не превращается в шум")
    void filledNamesSayNothing() {
        PermissionsSection section = new PermissionsSection();
        section.defaultGroup = "гость";
        section.opGroup = "гость";
        LogCapture capture = LogCapture.attach(LOG);

        try {
            section.normalize(LOG);

            assertEquals("гость", section.defaultGroup);
            assertEquals("гость", section.opGroup, "одинаковые имена выключают сопоставление операторов");
            assertEquals("", capture.text(), "подмены не было, значит и строки нет");
        } finally {
            capture.detach();
        }
    }
}
