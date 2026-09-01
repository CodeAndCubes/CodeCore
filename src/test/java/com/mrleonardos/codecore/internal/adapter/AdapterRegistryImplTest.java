package com.mrleonardos.codecore.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.adapter.RoleAdapter;
import com.mrleonardos.codecore.api.adapter.RoleCapability;
import com.mrleonardos.codecore.api.adapter.RoleChoice;
import com.mrleonardos.codecore.api.adapter.RoleOwnerKind;
import com.mrleonardos.codecore.api.adapter.RoleServices;
import com.mrleonardos.codecore.api.adapter.RoleSpec;
import com.mrleonardos.codecore.api.config.ConfigData;
import com.mrleonardos.codecore.api.service.ServicePriority;
import com.mrleonardos.codecore.internal.config.TestConfigData;
import com.mrleonardos.codecore.internal.service.ServiceRegistryImpl;

class AdapterRegistryImplTest {

    private static final Logger LOG = LogManager.getLogger(AdapterRegistryImplTest.class);
    private static final String ROLE = "permissions";
    private static final RoleCapability HAS = RoleCapability.of("has");
    private static final RoleCapability GROUP = RoleCapability.of("group");
    private static final RoleCapability TRACKS = RoleCapability.of("tracks");

    private final ServiceRegistryImpl services = new ServiceRegistryImpl(LOG);
    private final AdapterRegistryImpl adapters = new AdapterRegistryImpl(services, LOG);

    @Test
    @DisplayName("auto отдаёт роль нашему моду, адаптер к чужому не подключается")
    void autoPrefersOurMod() throws IOException {
        declare();
        TestAdapter builtin = offer("codecore", RoleOwnerKind.BUILTIN, true);
        TestAdapter ours = offer("codeperms", RoleOwnerKind.MOD, true);
        TestAdapter foreign = offer("luckperms", RoleOwnerKind.ADAPTER, true);

        adapters.decide(owners("auto"));

        assertEquals("codeperms", adapters.owner(ROLE));
        assertEquals(RoleChoice.AUTO, status().choice());
        assertTrue(ours.created, "победитель собирает свои реализации");
        assertFalse(builtin.created, "проигравшие ничего не создают");
        assertFalse(foreign.created, "мост к чужому моду сам по себе не подключается");
        assertEquals(
            "ours",
            services.require(Greeter.class)
                .greet());
    }

    @Test
    @DisplayName("без нашего мода роль остаётся за встроенной реализацией")
    void autoFallsBackToTheBuiltin() throws IOException {
        declare();
        offer("codecore", RoleOwnerKind.BUILTIN, true);
        offer("luckperms", RoleOwnerKind.ADAPTER, true);

        adapters.decide(owners("auto"));

        assertEquals("codecore", adapters.owner(ROLE));
    }

    @Test
    @DisplayName("названный владелец встаёт весом override, и чужая заявка его не двигает")
    void namedOwnerIsNotOutbid() throws IOException {
        declare();
        offer("codecore", RoleOwnerKind.BUILTIN, true);
        offer("luckperms", RoleOwnerKind.ADAPTER, true);

        adapters.decide(owners("luckperms"));

        assertEquals("luckperms", adapters.owner(ROLE));
        assertEquals(RoleChoice.NAMED, status().choice());

        services.register(Greeter.class, () -> "чужой мод", ServicePriority.ADDON);
        assertEquals(
            "luckperms",
            services.require(Greeter.class)
                .greet(),
            "вес override спорить не даёт");
    }

    @Test
    @DisplayName("выбранный по auto наш мод чужой мод весом override всё же вытесняет")
    void autoLeavesRoomForAForeignMod() throws IOException {
        declare();
        offer("codeperms", RoleOwnerKind.MOD, true);

        adapters.decide(owners("auto"));

        services.register(Greeter.class, () -> "чужой мод", ServicePriority.OVERRIDE);
        assertEquals(
            "чужой мод",
            services.require(Greeter.class)
                .greet());
    }

    @Test
    @DisplayName("off оставляет роль незанятой, сервер стартует")
    void offLeavesTheRoleEmpty() throws IOException {
        declare();
        TestAdapter ours = offer("codeperms", RoleOwnerKind.MOD, true);

        adapters.decide(owners("off"));

        assertNull(adapters.owner(ROLE));
        assertEquals(RoleChoice.OFF, status().choice());
        assertFalse(ours.created);
        assertFalse(
            services.find(Greeter.class)
                .isPresent());
        assertEquals(
            "[has, group, tracks]",
            status().missing()
                .toString(),
            "не работает вся роль целиком");
    }

    @Test
    @DisplayName("опечатка в имени владельца уводит роль по правилу auto")
    void unknownNameFallsBackToAuto() throws IOException {
        declare();
        offer("codeperms", RoleOwnerKind.MOD, true);

        adapters.decide(owners("codepems"));

        assertEquals("codeperms", adapters.owner(ROLE));
        assertEquals(RoleChoice.UNKNOWN_NAME, status().choice());
    }

    @Test
    @DisplayName("названного мода на сервере нет: роль уходит по правилу auto")
    void namedButAbsentFallsBackToAuto() throws IOException {
        declare();
        offer("codeperms", RoleOwnerKind.MOD, true);
        offer("luckperms", RoleOwnerKind.ADAPTER, false);

        adapters.decide(owners("luckperms"));

        assertEquals("codeperms", adapters.owner(ROLE));
        assertEquals(RoleChoice.UNKNOWN_NAME, status().choice());
        assertEquals(
            "[codeperms, luckperms*]",
            adapters.candidates(ROLE)
                .toString(),
            "недоступная заявка видна среди кандидатов");
    }

    @Test
    @DisplayName("чего владелец не умеет, то перечислено")
    void missingCapabilitiesAreCounted() throws IOException {
        declare();
        offer("codeperms", RoleOwnerKind.MOD, true, HAS, GROUP);

        adapters.decide(owners("auto"));

        assertEquals(
            "[tracks]",
            adapters.missing(ROLE)
                .toString());
    }

    @Test
    @DisplayName("заявка после решения ролей отклонена")
    void lateOfferIsRefused() throws IOException {
        declare();
        offer("codeperms", RoleOwnerKind.MOD, true);
        adapters.decide(owners("auto"));

        assertThrows(
            IllegalStateException.class,
            () -> adapters.offer(new TestAdapter("late", RoleOwnerKind.MOD, true, new HashSet<>(Arrays.asList(HAS)))));
        assertThrows(
            IllegalStateException.class,
            () -> adapters.declareRole(
                RoleSpec.of("economy")
                    .build()));
        assertEquals("codeperms", adapters.owner(ROLE), "владелец роли не меняется");
    }

    @Test
    @DisplayName("одну роль дважды не объявляют")
    void roleIsDeclaredOnce() {
        declare();
        assertThrows(IllegalArgumentException.class, this::declare);
    }

    @Test
    @DisplayName("до решения ролей владельца нет")
    void ownerIsUnknownBeforeTheDecision() {
        declare();
        offer("codeperms", RoleOwnerKind.MOD, true);

        assertFalse(adapters.decided());
        assertNull(adapters.owner(ROLE));
        assertEquals(
            "[codeperms]",
            adapters.candidates(ROLE)
                .toString());
    }

    private void declare() {
        adapters.declareRole(
            RoleSpec.of(ROLE)
                .capabilities(HAS, GROUP, TRACKS)
                .services(Greeter.class)
                .build());
    }

    private TestAdapter offer(String name, RoleOwnerKind kind, boolean available, RoleCapability... capabilities) {
        Set<RoleCapability> declared = capabilities.length == 0 ? new HashSet<>(Arrays.asList(HAS, GROUP, TRACKS))
            : new HashSet<>(Arrays.asList(capabilities));
        TestAdapter adapter = new TestAdapter(name, kind, available, declared);
        adapters.offer(adapter);
        return adapter;
    }

    private com.mrleonardos.codecore.api.adapter.RoleStatus status() {
        return adapters.statuses()
            .get(0);
    }

    private static ConfigData owners(String value) throws IOException {
        return TestConfigData.of("[owners]\npermissions = \"" + value + "\"\n");
    }

    interface Greeter {

        String greet();
    }

    private static final class TestAdapter implements RoleAdapter {

        private final String name;
        private final RoleOwnerKind kind;
        private final boolean available;
        private final Set<RoleCapability> capabilities;

        private boolean created;

        private TestAdapter(String name, RoleOwnerKind kind, boolean available, Set<RoleCapability> capabilities) {
            this.name = name;
            this.kind = kind;
            this.available = available;
            this.capabilities = capabilities;
        }

        @Override
        public String role() {
            return ROLE;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public RoleOwnerKind kind() {
            return kind;
        }

        @Override
        public boolean available() {
            return available;
        }

        @Override
        public Set<RoleCapability> capabilities() {
            return capabilities;
        }

        @Override
        public RoleServices create() {
            created = true;
            String answer = "codeperms".equals(name) ? "ours" : name;
            return RoleServices.builder()
                .add(Greeter.class, () -> answer)
                .build();
        }
    }
}
