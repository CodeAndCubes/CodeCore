package com.mrleonardos.codecore.internal.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.mrleonardos.codecore.api.config.ConfigFile;
import com.mrleonardos.codecore.internal.config.ConfigPaths;
import com.mrleonardos.codecore.internal.config.ConfigServiceImpl;

class CoreGroupsFileTest {

    private static final Logger LOG = LogManager.getLogger(CoreGroupsFileTest.class);

    @TempDir
    Path configDirectory;

    @Test
    @DisplayName("файл групп ядра создаётся в папке прав с группами player и admin")
    void groupsFileIsCreatedInThePermissionsDirectory() throws IOException {
        ConfigFile<CoreGroupsFile> file = new ConfigServiceImpl(new ConfigPaths(configDirectory), LOG)
            .open(BuiltinPermissions.spec());

        assertEquals(path(), file.path());
        assertTrue(file.get().groups.containsKey(PermissionKeys.DEFAULT_GROUP));
        assertEquals(
            PermissionKeys.DEFAULT_GROUP,
            file.get().groups.get(PermissionKeys.OPERATOR_GROUP).inherits.get(0));
        assertEquals(PermissionKeys.WILDCARD, file.get().groups.get(PermissionKeys.OPERATOR_GROUP).nodes.get(0));

        String text = new String(Files.readAllBytes(path()), StandardCharsets.UTF_8);
        assertTrue(text.contains("[groups.player]"), text);
        assertTrue(text.contains("[groups.admin]"), text);
        assertFalse(text.contains("defaultGroup"), "имена групп по умолчанию живут в главном файле");
    }

    @Test
    @DisplayName("файл групп ядра лежит рядом с файлом CodePerms под своим именем")
    void groupsFileSitsNextToTheCodePermsOne() {
        assertEquals(
            "core-groups.toml",
            path().getFileName()
                .toString());
        assertEquals(
            "permissions",
            path().getParent()
                .getFileName()
                .toString());
    }

    private Path path() {
        return configDirectory.resolve("code")
            .resolve("permissions")
            .resolve("core-groups.toml");
    }
}
