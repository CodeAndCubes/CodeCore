package com.mrleonardos.codecore.internal.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.mrleonardos.codecore.api.config.AuditSettings;
import com.mrleonardos.codecore.api.config.Comment;
import com.mrleonardos.codecore.api.config.ConfigFile;
import com.mrleonardos.codecore.api.config.ConfigRoles;
import com.mrleonardos.codecore.api.config.ConfigScope;
import com.mrleonardos.codecore.api.config.ConfigSpec;
import com.mrleonardos.codecore.api.config.SectionSpec;
import com.mrleonardos.codecore.api.config.StorageSettings;
import com.mrleonardos.codecore.internal.CoreSections;
import com.mrleonardos.codecore.internal.LogCapture;

class MainConfigTest {

    private static final Logger LOG = LogManager.getLogger(MainConfigTest.class);

    private static final String FACTORY = "# Главный файл линейки Code*.\n"
        + "# Описание над полем пишет мод, оно перезаписывается при каждой записи файла.\n"
        + "# Свою заметку кладите над своим ключом: такую строку мод не трогает.\n"
        + "schemaVersion = 1\n"
        + "# Имя этого сервера, когда несколько серверов смотрят в одну базу.\n"
        + "# Схемы модов сами решают, что размечать этим именем, а что держать общим.\n"
        + "serverId = \"main\"\n"
        + "# Язык сообщений сервера: имя файла перевода без расширения, en_US или ru_RU.\n"
        + "# Строки собирает сервер, потому что серверный мод на клиент не попадает и ключа перевода\n"
        + "# клиент не знает. Непереведённая строка остаётся английской.\n"
        + "language = \"en_US\"\n"
        + "\n"
        + "# Кто держит роль целиком.\n"
        + "# \"auto\" наш мод, если он стоит; \"off\" роль не занята никем; иначе имя владельца.\n"
        + "# Известные имена сервер называет при старте и по /codecore adapters.\n"
        + "[owners]\n"
        + "permissions = \"auto\"\n"
        + "\n"
        + "# Хранилище: значение для всех ролей сразу.\n"
        + "# Отличие одной роли пишется соседней секцией, только теми ключами, которые отличаются:\n"
        + "# [storage.economy]\n"
        + "# provider = \"sql\"\n"
        + "# autosaveSeconds = 120\n"
        + "[storage]\n"
        + "# Имя провайдера. Встроен \"json\", остальные приносят моды.\n"
        + "provider = \"json\"\n"
        + "# Через сколько секунд несохранённое уходит на диск.\n"
        + "autosaveSeconds = 30\n"
        + "\n"
        + "# Записи в лог о том, что моды меняют и что проверяют.\n"
        + "[audit]\n"
        + "# Каждая правка группы, счёта, дома с автором и причиной.\n"
        + "logChanges = true\n"
        + "# Объяснение каждого отказа на уровне debug. Шумно, для разбора полётов.\n"
        + "logChecks = false\n"
        + "\n"
        + "# Права. Файлы лежат в config/code/permissions/.\n"
        + "[permissions]\n"
        + "# Группа игрока, которому ничего не выдали.\n"
        + "defaultGroup = \"player\"\n"
        + "# Группа, в которую попадает оператор сервера из ops.json.\n"
        + "opGroup = \"admin\"\n"
        + "\n"
        + "# Картинки: аватары и превью ссылок. Значения только опускают заводские потолки, поднять их нельзя.\n"
        + "[images]\n"
        + "# Больше этого объёма картинка не читается.\n"
        + "maxBytes = 2097152\n"
        + "# Больше этого числа пикселей исходник не декодируется.\n"
        + "maxSourcePixels = 16777216\n"
        + "# Сторона готовой картинки в пикселях.\n"
        + "maxSize = 512\n"
        + "# Сколько картинок держать в памяти клиента.\n"
        + "maxHandles = 256\n"
        + "# Сколько ждать соединения при загрузке картинки, миллисекунды.\n"
        + "connectTimeoutMs = 5000\n"
        + "# Сколько ждать данных от отвечающего сервера, миллисекунды.\n"
        + "readTimeoutMs = 10000\n"
        + "\n"
        + "# Надписи поверх экрана. Строку над хотбаром рисует клиент с CodeCore, чужой клиент её не увидит.\n"
        + "[hud]\n"
        + "# Не чаще раза в столько секунд повторяется строка с одним ключом у одного игрока.\n"
        + "# Мод просит своё окно, но меньше этого числа оно не станет.\n"
        + "actionBarRepeatSeconds = 3\n"
        + "\n"
        + "# Базы данных модов лежат в core/core-databases.toml, а не в этом файле.\n"
        + "# Каждая база это [databases.<имя>] с ключом role и настройками соединения;\n"
        + "# какую брать, когда метку роли носят несколько, решает [roleDefaults].\n";

    @TempDir
    Path configDirectory;

    @Test
    @DisplayName("заводской файл линейки совпадает с описанным в дизайне")
    void factoryFileMatchesTheDesign() throws IOException {
        ConfigServiceImpl service = service();
        new CoreSections(service, LOG);
        service.seal(Collections.singletonList(ConfigRoles.PERMISSIONS));

        assertEquals(FACTORY, text());
    }

    @Test
    @DisplayName("повторная запись заводского файла ничего в нём не двигает")
    void writingTwiceChangesNothing() throws IOException {
        ConfigServiceImpl first = service();
        new CoreSections(first, LOG);
        first.seal(Collections.singletonList(ConfigRoles.PERMISSIONS));

        ConfigServiceImpl second = service();
        new CoreSections(second, LOG);
        second.seal(Collections.singletonList(ConfigRoles.PERMISSIONS));

        assertEquals(FACTORY, text());
    }

    @Test
    @DisplayName("секции разных объявителей лежат в одном файле")
    void sectionsFromDifferentModsShareTheFile() throws IOException {
        ConfigServiceImpl service = service();
        new CoreSections(service, LOG);
        service.section(
            SectionSpec.of("economy", EconomySection.class)
                .build());
        service.section(
            SectionSpec.of("essentials", EssentialsSection.class)
                .build());
        service.seal(Arrays.asList(ConfigRoles.PERMISSIONS, ConfigRoles.ECONOMY, ConfigRoles.ESSENTIALS));

        String written = text();
        assertTrue(written.contains("[economy]"), written);
        assertTrue(written.contains("defaultCurrency = \"coin\""), written);
        assertTrue(written.contains("[essentials]"), written);
        assertTrue(written.contains("# Деньги."), written);
        assertTrue(written.contains("economy = \"auto\""), written);
        assertTrue(written.contains("essentials = \"auto\""), written);
    }

    @Test
    @DisplayName("секция мода, которого сняли с сервера, переживает запись")
    void sectionOfAnAbsentModSurvives() throws IOException {
        ConfigServiceImpl first = service();
        new CoreSections(first, LOG);
        first.section(
            SectionSpec.of("economy", EconomySection.class)
                .build());
        first.seal(Arrays.asList(ConfigRoles.PERMISSIONS, ConfigRoles.ECONOMY));

        ConfigServiceImpl second = service();
        new CoreSections(second, LOG);
        second.seal(Collections.singletonList(ConfigRoles.PERMISSIONS));

        String written = text();
        assertTrue(written.contains("[economy]"), "секция снятого мода остаётся");
        assertTrue(written.contains("# Деньги."), "вместе со своим описанием");
        assertTrue(written.contains("defaultCurrency = \"coin\""), written);
    }

    @Test
    @DisplayName("секция, объявленная после записи файла, отклонена")
    void lateSectionIsRefused() {
        ConfigServiceImpl service = service();
        new CoreSections(service, LOG);
        service.seal(Collections.singletonList(ConfigRoles.PERMISSIONS));

        assertThrows(
            IllegalStateException.class,
            () -> service.section(
                SectionSpec.of("economy", EconomySection.class)
                    .build()));
    }

    @Test
    @DisplayName("секцию с занятым именем второй раз объявить нельзя")
    void sectionNameIsTakenOnce() {
        ConfigServiceImpl service = service();
        new CoreSections(service, LOG);

        assertThrows(
            IllegalStateException.class,
            () -> service.section(
                SectionSpec.of("permissions", EconomySection.class)
                    .build()));
        assertThrows(
            IllegalStateException.class,
            () -> service.section(
                SectionSpec.of("storage", EconomySection.class)
                    .build()));
    }

    @Test
    @DisplayName("роль без своей секции берёт общее значение")
    void roleTakesTheSharedValue() {
        ConfigServiceImpl service = service();

        StorageSettings storage = service.storage(ConfigRoles.ECONOMY);
        assertEquals("json", storage.provider());
        assertEquals(30, storage.autosaveSeconds());

        AuditSettings audit = service.audit(ConfigRoles.ECONOMY);
        assertTrue(audit.logChanges());
        assertFalse(audit.logChecks());
    }

    @Test
    @DisplayName("роль перекрывает только названные ключи")
    void roleOverridesOnlyWhatItNames() throws IOException {
        writeMain(
            "schemaVersion = 1\n" + "[storage]\n"
                + "provider = \"json\"\n"
                + "autosaveSeconds = 30\n"
                + "[storage.economy]\n"
                + "provider = \"sql\"\n"
                + "[audit.economy]\n"
                + "logChecks = true\n");

        ConfigServiceImpl service = service();

        assertEquals(
            "sql",
            service.storage(ConfigRoles.ECONOMY)
                .provider());
        assertEquals(
            30,
            service.storage(ConfigRoles.ECONOMY)
                .autosaveSeconds(),
            "неназванный ключ берётся из общей секции");
        assertEquals(
            "json",
            service.storage(ConfigRoles.PERMISSIONS)
                .provider());
        assertTrue(
            service.audit(ConfigRoles.ECONOMY)
                .logChecks());
        assertFalse(
            service.audit(ConfigRoles.PERMISSIONS)
                .logChecks());
    }

    @Test
    @DisplayName("заводской файл секций перекрытия не содержит")
    void factoryFileHasNoOverrides() throws IOException {
        ConfigServiceImpl service = service();
        new CoreSections(service, LOG);
        service.seal(Collections.singletonList(ConfigRoles.PERMISSIONS));

        for (String line : text().split("\n")) {
            assertFalse(line.startsWith("[storage."), line);
            assertFalse(line.startsWith("[audit."), line);
        }
        assertTrue(text().contains("# [storage.economy]"), "пример перекрытия остаётся комментарием");
    }

    @Test
    @DisplayName("имя сервера читается из главного файла")
    void serverIdComesFromTheMainFile() throws IOException {
        assertEquals("main", service().serverId());

        writeMain("schemaVersion = 1\nserverId = \"lobby\"\n");
        assertEquals("lobby", service().serverId());

        writeMain("schemaVersion = 1\nserverId = \"\"\n");
        assertEquals("main", service().serverId(), "пустое имя возвращается к заводскому");
    }

    @Test
    @DisplayName("владельцы ролей дописываются со значением auto, а выбранное руками остаётся")
    void ownersKeepWhatTheAdminWrote() throws IOException {
        writeMain("schemaVersion = 1\n[owners]\npermissions = \"luckperms\"\n");

        ConfigServiceImpl service = service();
        new CoreSections(service, LOG);
        service.seal(Arrays.asList(ConfigRoles.PERMISSIONS, ConfigRoles.ECONOMY));

        assertTrue(text().contains("permissions = \"luckperms\""), text());
        assertTrue(text().contains("economy = \"auto\""), text());
    }

    @Test
    @DisplayName("битый главный файл откладывается рядом, работа идёт со значениями по умолчанию")
    void brokenMainFileIsQuarantined() throws IOException {
        writeMain("[owners\npermissions = \"auto\"\n");

        ConfigServiceImpl service = service();
        new CoreSections(service, LOG);
        service.seal(Collections.singletonList(ConfigRoles.PERMISSIONS));

        assertTrue(Files.isRegularFile(path().resolveSibling("config.toml" + ConfigKeys.BROKEN_SUFFIX)));
        assertEquals("main", service.serverId());
        assertTrue(text().contains("permissions = \"auto\""), text());
    }

    @Test
    @DisplayName("подсказка про кавычки достаётся и главному файлу")
    void brokenMainFileGetsAHint() throws IOException {
        writeMain("[owners.Ярмарка]\npermissions = \"auto\"\n");
        LogCapture capture = LogCapture.attach(LOG);

        try {
            ConfigServiceImpl service = service();
            new CoreSections(service, LOG);
            service.seal(Collections.singletonList(ConfigRoles.PERMISSIONS));

            assertTrue(
                capture.text()
                    .contains(ConfigHints.quotedNames()),
                capture.text());
        } finally {
            capture.detach();
        }
    }

    @Test
    @DisplayName("комментарий человека в главном файле переживает запись")
    void humanCommentInTheMainFileSurvives() throws IOException {
        ConfigServiceImpl first = service();
        new CoreSections(first, LOG);
        first.seal(Collections.singletonList(ConfigRoles.PERMISSIONS));

        writeMain(text().replace("serverId = \"main\"", "serverId = \"main\"\n# так решили в 2026\nmyOwnKey = 7"));

        ConfigServiceImpl second = service();
        new CoreSections(second, LOG);
        second.seal(Collections.singletonList(ConfigRoles.PERMISSIONS));

        assertTrue(text().contains("# так решили в 2026"), text());
        assertTrue(text().contains("myOwnKey = 7"), text());
    }

    @Test
    @DisplayName("секция, чей валидатор сорвался, откатывается к значениям по умолчанию")
    void sectionRejectedByItsValidatorFallsBackToDefaults() throws IOException {
        writeMain("schemaVersion = 1\n[broken]\nvalue = 5\n");
        LogCapture capture = LogCapture.attach(LOG);

        try {
            ConfigServiceImpl service = service();
            ConfigFile<BrokenSection> section = service.section(
                SectionSpec.of("broken", BrokenSection.class)
                    .validator(value -> { throw new IllegalStateException("значение не то"); })
                    .build());

            assertEquals(9, section.get().value, "работа идёт на значениях по умолчанию");
            assertTrue(
                capture.text()
                    .contains("Section broken of the main config was rejected by its validator"),
                capture.text());
        } finally {
            capture.detach();
        }
    }

    @Test
    @DisplayName("счётчик перезагрузки не считает файлы состояния мира, пока мира нет")
    void reloadCountSkipsWorldStateFiles() {
        ConfigServiceImpl service = service();
        service.open(
            ConfigSpec.of("codetest", "settings", EconomySection.class)
                .role(ConfigRoles.CORE)
                .build());
        service.open(
            ConfigSpec.of("codetest", "state", EconomySection.class)
                .role(ConfigRoles.CORE)
                .scope(ConfigScope.WORLD_STATE)
                .build());
        LogCapture capture = LogCapture.attach(LOG);

        try {
            service.reloadAll();

            assertTrue(
                capture.text()
                    .contains("Reloaded 1 config file(s) and the main config"),
                capture.text());
        } finally {
            capture.detach();
        }
    }

    private ConfigServiceImpl service() {
        return new ConfigServiceImpl(new ConfigPaths(configDirectory), LOG);
    }

    private Path path() {
        return configDirectory.resolve("code")
            .resolve("config.toml");
    }

    private String text() throws IOException {
        return new String(Files.readAllBytes(path()), StandardCharsets.UTF_8);
    }

    private void writeMain(String content) throws IOException {
        Files.createDirectories(path().getParent());
        Files.write(path(), content.getBytes(StandardCharsets.UTF_8));
    }

    @Comment("Деньги. Файлы лежат в config/code/economy/.")
    public static final class EconomySection {

        @Comment("Валюта, которую подставляют команды без явного имени.")
        public String defaultCurrency = "coin";

        @Comment("Границы одного перевода в минорных единицах.")
        public long minTransfer = 1L;
    }

    @Comment("Перемещения. Файлы лежат в config/code/essentials/.")
    public static final class EssentialsSection {

        @Comment("Сколько домов у игрока без личного лимита из меты.")
        public int homes = 3;
    }

    public static final class BrokenSection {

        public int value = 9;
    }
}
