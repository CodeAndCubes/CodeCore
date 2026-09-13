package com.mrleonardos.codecore.internal.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.db.Database;
import com.mrleonardos.codecore.api.db.DriverMissingException;
import com.mrleonardos.codecore.api.db.UnknownDatabaseException;
import com.mrleonardos.codecore.internal.LogCapture;

class DatabaseServiceTest {

    private static final Logger LOG = LogManager.getLogger(DatabaseServiceTest.class);
    private static final String SQLITE_DRIVER = "org.sqlite.JDBC";

    private LogCapture capture;

    @AfterEach
    void detach() {
        if (capture != null) {
            capture.detach();
            capture = null;
        }
    }

    private DatabaseServiceImpl service(DatabasesFile file) {
        return DatabaseServiceImpl
            .of(file, () -> "survival-1", Runnable::run, () -> false, System::currentTimeMillis, LOG);
    }

    private static DatabaseEntry entry(String role, String driverClass, String url) {
        DatabaseEntry entry = new DatabaseEntry();
        entry.role = role;
        entry.driverClass = driverClass;
        entry.url = url;
        return entry;
    }

    private static DatabaseEntry sqlite(String role) {
        return entry(role, SQLITE_DRIVER, "jdbc:sqlite::memory:");
    }

    @Test
    @DisplayName("файл без баз это рабочий файл: сервис поднят, спрашивать нечего")
    void anEmptyFileIsValid() {
        DatabaseServiceImpl service = service(new DatabasesFile());

        assertTrue(
            service.ids()
                .isEmpty());
        assertTrue(
            service.roles()
                .isEmpty());
        assertEquals("survival-1", service.serverId());
        assertThrows(UnknownDatabaseException.class, () -> service.database("global"));
    }

    @Test
    @DisplayName("чужая схема адреса отбрасывает запись со строкой в лог")
    void anUnknownSchemeIsDropped() {
        capture = LogCapture.attach(LOG);
        DatabasesFile file = new DatabasesFile();
        file.databases.put("pg", entry("global", "org.postgresql.Driver", "jdbc:postgresql://localhost/mymods"));

        DatabaseServiceImpl service = service(file);

        assertTrue(
            service.ids()
                .isEmpty());
        assertTrue(
            capture.text()
                .contains("jdbc:sqlite:"),
            "в отказе перечисляются понятные схемы: " + capture.text());
    }

    @Test
    @DisplayName("без драйвера запись остаётся в перечне и объясняет причину каждому вызову")
    void aMissingDriverKeepsTheEntry() {
        capture = LogCapture.attach(LOG);
        DatabasesFile file = new DatabasesFile();
        file.databases.put("global", entry("global", "org.mariadb.jdbc.Driver", "jdbc:mariadb://10.0.0.5:3306/mymods"));
        file.databases.put("logs", sqlite("logs"));

        DatabaseServiceImpl service = service(file);
        Database broken = service.database("global");

        assertEquals(Arrays.asList("global", "logs"), service.ids());
        assertThrows(DriverMissingException.class, () -> broken.sync(connection -> null));
        assertFalse(
            broken.status()
                .reachable());
        assertTrue(
            capture.text()
                .contains("org.mariadb.jdbc.Driver"),
            "в логе должно стоять имя класса: " + capture.text());
        service.database("logs")
            .sync(connection -> null);
        service.close();
    }

    @Test
    @DisplayName("роль без двойников находится по метке")
    void aSingleRoleIsFound() {
        DatabasesFile file = new DatabasesFile();
        file.databases.put("local", sqlite("server"));

        DatabaseServiceImpl service = service(file);

        assertSame(service.database("local"), service.byRole("server"));
        assertSame(service.database("local"), service.server());
        assertEquals(Arrays.asList("server"), service.roles());
        service.close();
    }

    @Test
    @DisplayName("неоднозначная роль без выбора админа отказывает и называет кандидатов")
    void anAmbiguousRoleIsRefused() {
        DatabasesFile file = new DatabasesFile();
        file.databases.put("one", sqlite("server"));
        file.databases.put("two", sqlite("server"));
        file.databases.put("three", sqlite("server"));

        DatabaseServiceImpl service = service(file);
        UnknownDatabaseException refusal = assertThrows(UnknownDatabaseException.class, service::server);

        assertTrue(
            refusal.getMessage()
                .contains("one"),
            refusal.getMessage());
        assertTrue(
            refusal.getMessage()
                .contains("three"),
            refusal.getMessage());
        assertTrue(
            refusal.getMessage()
                .contains("roleDefaults"),
            refusal.getMessage());
        service.close();
    }

    @Test
    @DisplayName("выбор админа снимает неоднозначность, а указание на чужую базу отбрасывается")
    void roleDefaultsDecide() {
        capture = LogCapture.attach(LOG);
        DatabasesFile file = new DatabasesFile();
        file.databases.put("one", sqlite("server"));
        file.databases.put("two", sqlite("server"));
        file.roleDefaults.put("server", "two");
        file.roleDefaults.put("logs", "nowhere");

        DatabaseServiceImpl service = service(file);

        assertSame(service.database("two"), service.server());
        assertThrows(UnknownDatabaseException.class, () -> service.byRole("logs"));
        assertTrue(
            capture.text()
                .contains("nowhere"),
            capture.text());
        service.close();
    }

    @Test
    @DisplayName("у sqlite пул всегда в одно соединение, и об этом сказано в логе")
    void sqliteKeepsASingleConnection() {
        capture = LogCapture.attach(LOG);
        DatabasesFile file = new DatabasesFile();
        DatabaseEntry entry = sqlite("logs");
        entry.poolSize = 8;
        file.databases.put("logs", entry);

        DatabaseServiceImpl service = service(file);

        assertTrue(
            capture.text()
                .contains("pool size is forced to 1"),
            capture.text());
        service.close();
    }

    @Test
    @DisplayName("кривые числа заменяются заводскими, а не исполняются как есть")
    void brokenNumbersFallBackToDefaults() {
        assertEquals(
            DbLimits.POOL_SIZE_DEFAULT,
            DbLimits.clamp(0, DbLimits.POOL_SIZE_MIN, DbLimits.POOL_SIZE_MAX, DbLimits.POOL_SIZE_DEFAULT));
        assertEquals(
            DbLimits.POOL_SIZE_DEFAULT,
            DbLimits.clamp(-4, DbLimits.POOL_SIZE_MIN, DbLimits.POOL_SIZE_MAX, DbLimits.POOL_SIZE_DEFAULT));
        assertEquals(
            DbLimits.QUERY_TIMEOUT_DEFAULT,
            DbLimits.clamp(
                DbLimits.QUERY_TIMEOUT_MAX + 1,
                DbLimits.QUERY_TIMEOUT_MIN,
                DbLimits.QUERY_TIMEOUT_MAX,
                DbLimits.QUERY_TIMEOUT_DEFAULT));
        assertEquals(
            16,
            DbLimits.clamp(16, DbLimits.POOL_SIZE_MIN, DbLimits.POOL_SIZE_MAX, DbLimits.POOL_SIZE_DEFAULT));
    }

    @Test
    @DisplayName("метка роли длиннее допустимой снимается, а сама база остаётся")
    void aLongRoleLabelIsDropped() {
        capture = LogCapture.attach(LOG);
        DatabasesFile file = new DatabasesFile();
        StringBuilder tooLong = new StringBuilder();
        for (int letter = 0; letter <= DbLimits.ROLE_MAX_LENGTH; letter++) {
            tooLong.append('a');
        }
        file.databases.put("logs", sqlite(tooLong.toString()));

        DatabaseServiceImpl service = service(file);
        List<String> roles = service.roles();

        assertEquals(Arrays.asList("logs"), service.ids());
        assertTrue(roles.isEmpty(), "метка должна была отпасть");
        service.close();
    }
}
