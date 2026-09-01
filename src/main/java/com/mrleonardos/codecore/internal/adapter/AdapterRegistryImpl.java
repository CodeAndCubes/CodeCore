package com.mrleonardos.codecore.internal.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.adapter.AdapterRegistry;
import com.mrleonardos.codecore.api.adapter.RoleAdapter;
import com.mrleonardos.codecore.api.adapter.RoleCapability;
import com.mrleonardos.codecore.api.adapter.RoleChoice;
import com.mrleonardos.codecore.api.adapter.RoleOwnerKind;
import com.mrleonardos.codecore.api.adapter.RoleServices;
import com.mrleonardos.codecore.api.adapter.RoleSpec;
import com.mrleonardos.codecore.api.adapter.RoleStatus;
import com.mrleonardos.codecore.api.config.ConfigData;
import com.mrleonardos.codecore.api.service.ServicePriority;
import com.mrleonardos.codecore.api.service.ServiceRegistry;
import com.mrleonardos.codecore.internal.config.MainConfig;

/**
 * Кто держит каждую роль.
 *
 * <p>
 * Заявки собираются в фазе init, решение принимается один раз в конце постинициализации и дальше не
 * меняется: горячей смены владельца нет, потому что половина сервера уже построена на том, что ответил
 * реестр сервисов.
 *
 * <p>
 * Имя владельца из конфига превращается во внутренний вес: названный админом встаёт
 * {@link ServicePriority#OVERRIDE}, выбранный по {@code auto} наш мод {@link ServicePriority#ADDON},
 * встроенная реализация ядра {@link ServicePriority#BUILTIN}. Числовая шкала остаётся делом реестра,
 * потому что чужой мод, который про роли не знает, регистрирует сервис напрямую своим весом.
 */
public final class AdapterRegistryImpl implements AdapterRegistry {

    private static final String UNAVAILABLE_MARK = "*";

    private final Map<String, RoleSpec> declared = new LinkedHashMap<>();
    private final Map<String, List<RoleAdapter>> offers = new LinkedHashMap<>();
    private final Map<String, RoleStatus> statuses = new LinkedHashMap<>();
    private final ServiceRegistry services;
    private final Logger log;

    private boolean decided;

    public AdapterRegistryImpl(ServiceRegistry services, Logger log) {
        this.services = services;
        this.log = log;
    }

    @Override
    public void declareRole(RoleSpec spec) {
        requireOpen("declare role " + spec.role());
        if (declared.containsKey(spec.role())) {
            throw new IllegalArgumentException("Role " + spec.role() + " is already declared");
        }
        declared.put(spec.role(), spec);
    }

    @Override
    public void offer(RoleAdapter adapter) {
        requireOpen("offer " + adapter.name() + " for role " + adapter.role());
        offers.computeIfAbsent(adapter.role(), role -> new ArrayList<>())
            .add(adapter);
    }

    @Override
    public String owner(String role) {
        RoleStatus status = statuses.get(role);
        return status == null ? null : status.owner();
    }

    @Override
    public List<String> candidates(String role) {
        RoleStatus status = statuses.get(role);
        if (status != null) {
            return status.candidates();
        }
        List<String> names = new ArrayList<>();
        for (RoleAdapter adapter : offers.getOrDefault(role, Collections.emptyList())) {
            names.add(adapter.name());
        }
        return names;
    }

    @Override
    public Set<RoleCapability> missing(String role) {
        RoleStatus status = statuses.get(role);
        if (status != null) {
            return status.missing();
        }
        RoleSpec spec = declared.get(role);
        return spec == null ? Collections.emptySet() : spec.capabilities();
    }

    @Override
    public List<String> roles() {
        return new ArrayList<>(declared.keySet());
    }

    @Override
    public List<RoleStatus> statuses() {
        return new ArrayList<>(statuses.values());
    }

    @Override
    public boolean decided() {
        return decided;
    }

    /** Конец постинициализации: прочитать {@code [owners]}, решить каждую роль и зарегистрировать победителей. */
    public void decide(ConfigData main) {
        ConfigData owners = main.table(MainConfig.OWNERS_SECTION);
        for (RoleSpec spec : declared.values()) {
            String requested = owners == null ? MainConfig.OWNER_AUTO
                : owners.string(spec.role(), MainConfig.OWNER_AUTO);
            statuses.put(spec.role(), resolve(spec, requested));
        }
        decided = true;
        report();
    }

    private RoleStatus resolve(RoleSpec spec, String requested) {
        List<RoleAdapter> candidates = offers.getOrDefault(spec.role(), Collections.emptyList());
        List<String> names = names(candidates);

        if (MainConfig.OWNER_OFF.equalsIgnoreCase(requested)) {
            return new RoleStatus(spec.role(), null, RoleChoice.OFF, names, spec.capabilities());
        }
        if (!MainConfig.OWNER_AUTO.equalsIgnoreCase(requested)) {
            RoleAdapter named = byName(candidates, requested);
            if (named != null && named.available()) {
                return install(spec, named, ServicePriority.OVERRIDE, RoleChoice.NAMED, names);
            }
            log.warn(
                named == null
                    ? "Role {} was given to \"{}\", which is not among its candidates {}; falling back to auto"
                    : "Role {} was given to \"{}\", which is not available on this server, candidates: {}; falling back to auto",
                spec.role(),
                requested,
                names);
            return automatic(spec, candidates, names, RoleChoice.UNKNOWN_NAME);
        }
        return automatic(spec, candidates, names, RoleChoice.AUTO);
    }

    private RoleStatus automatic(RoleSpec spec, List<RoleAdapter> candidates, List<String> names, RoleChoice choice) {
        RoleAdapter chosen = firstOf(candidates, RoleOwnerKind.MOD);
        if (chosen == null) {
            chosen = firstOf(candidates, RoleOwnerKind.BUILTIN);
        }
        if (chosen == null) {
            return new RoleStatus(spec.role(), null, choice, names, spec.capabilities());
        }
        ServicePriority priority = chosen.kind() == RoleOwnerKind.BUILTIN ? ServicePriority.BUILTIN
            : ServicePriority.ADDON;
        return install(spec, chosen, priority, choice, names);
    }

    private RoleStatus install(RoleSpec spec, RoleAdapter winner, ServicePriority priority, RoleChoice choice,
        List<String> names) {
        RoleServices created = winner.create();
        for (Class<?> type : created.types()) {
            register(type, created, priority);
        }
        for (Class<?> type : spec.services()) {
            if (created.find(type) == null) {
                log.warn("Role {} owner {} provides no {}", spec.role(), winner.name(), type.getName());
            }
        }
        return new RoleStatus(spec.role(), winner.name(), choice, names, missingOf(spec, winner));
    }

    @SuppressWarnings("unchecked")
    private <T> void register(Class<T> type, RoleServices created, ServicePriority priority) {
        services.register(type, (T) created.find(type), priority);
    }

    private static Set<RoleCapability> missingOf(RoleSpec spec, RoleAdapter winner) {
        Set<RoleCapability> missing = new LinkedHashSet<>(spec.capabilities());
        missing.removeAll(winner.capabilities());
        return missing;
    }

    private static RoleAdapter firstOf(List<RoleAdapter> candidates, RoleOwnerKind kind) {
        for (RoleAdapter adapter : candidates) {
            if (adapter.kind() == kind && adapter.available()) {
                return adapter;
            }
        }
        return null;
    }

    private static RoleAdapter byName(List<RoleAdapter> candidates, String name) {
        for (RoleAdapter adapter : candidates) {
            if (adapter.name()
                .equalsIgnoreCase(name)) {
                return adapter;
            }
        }
        return null;
    }

    private static List<String> names(List<RoleAdapter> candidates) {
        List<String> names = new ArrayList<>();
        for (RoleAdapter adapter : candidates) {
            names.add(adapter.available() ? adapter.name() : adapter.name() + UNAVAILABLE_MARK);
        }
        return names;
    }

    private void report() {
        for (RoleStatus status : statuses.values()) {
            if (status.owner() == null) {
                log.warn(
                    "Role {} is held by nobody ({}), candidates: {}",
                    status.role(),
                    status.choice(),
                    status.candidates());
            } else {
                log.info(
                    "Role {} is held by {} ({}), candidates: {}",
                    status.role(),
                    status.owner(),
                    status.choice(),
                    status.candidates());
            }
            if (!status.missing()
                .isEmpty()) {
                log.warn("Role {} does not work: {}", status.role(), status.missing());
            }
        }
    }

    private void requireOpen(String action) {
        if (decided) {
            throw new IllegalStateException(
                "Roles are already decided, cannot " + action + " after post initialization");
        }
    }
}
