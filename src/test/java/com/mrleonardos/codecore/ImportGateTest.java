package com.mrleonardos.codecore;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.CodeApi;
import com.mrleonardos.codesides.gate.PackageGate;

/**
 * Публичная поверхность ядра не тянет ни игру, ни внутренности.
 *
 * <p>
 * Проверка идёт по скомпилированным классам, а не по тексту импортов: гейт собирает ссылки из надтипов,
 * сигнатур, полей, дескрипторов методов, аннотаций, типов в try/catch, аргументов invokedynamic, фреймов и
 * LDC, то есть видит и то, чего в импортах не написано. Каталог берётся по классу ядра, потому что
 * {@code PackageGate.of} не читает jar.
 *
 * <p>
 * Список запрещённых префиксов шире стандартного {@code PackageGate.PLATFORM}: из api ядра торчали ещё
 * netty, LWJGL и com.mojang. Сверх них запрещены собственные {@code internal} и {@code platform}:
 * api-джар обязан собираться у того, кто взял только его, а этих двух пакетов в нём нет.
 */
class ImportGateTest {

    private static final String API = "com/mrleonardos/codecore/api";

    private static final String[] FORBIDDEN = { "net/minecraft", "net/minecraftforge", "cpw/mods", "io/netty",
        "org/lwjgl", "com/mojang", "com/mrleonardos/codecore/internal", "com/mrleonardos/codecore/platform" };

    /**
     * Классы api, которым типы платформы пока разрешены.
     *
     * <p>
     * Список пуст, и это конец работы: в публичной поверхности ядра не осталось ни одного типа игры,
     * Forge, netty, LWJGL и Mojang. Пополнять его нечем и незачем, а новый класс api со ссылкой на
     * платформу роняет сборку сразу.
     */
    private static final List<String> UNTIL_THE_OLD_SIGNATURES_GO = Collections.emptyList();

    @Test
    @DisplayName("новых типов платформы в api не появилось")
    void apiHoldsNoNewPlatformTypes() throws IOException {
        List<String> unexpected = new ArrayList<>();
        for (String violation : violations()) {
            if (!allowed(violation)) {
                unexpected.add(violation);
            }
        }

        assertTrue(
            unexpected.isEmpty(),
            () -> "api ядра собирается у того, кто взял только api-джар; лишние ссылки:\n"
                + String.join("\n", unexpected));
    }

    @Test
    @DisplayName("список поблажек не переживает свои классы")
    void theAllowanceListShrinksWithTheCleanup() throws IOException {
        Set<String> stillDirty = new LinkedHashSet<>();
        for (String violation : violations()) {
            for (String allowed : UNTIL_THE_OLD_SIGNATURES_GO) {
                if (originOf(violation).startsWith(allowed)) {
                    stillDirty.add(allowed);
                }
            }
        }

        List<String> stale = new ArrayList<>(UNTIL_THE_OLD_SIGNATURES_GO);
        stale.removeAll(stillDirty);

        assertTrue(
            stale.isEmpty(),
            () -> "эти классы уже чистые, вычеркни их из списка поблажек:\n" + String.join("\n", stale));
    }

    @Test
    @DisplayName("гейт не ослеп")
    void gateNoticesAForbiddenReference() throws IOException {
        assertFalse(
            gate().scan(PackageGate.foreignSample())
                .isEmpty(),
            "гейт обязан ловить ссылку на тип Minecraft");
    }

    @Test
    @DisplayName("классы со слушателями событий объявлены публично")
    void eventListenersArePublic() throws IOException {
        List<String> hidden = gate().hiddenListeners();

        assertTrue(hidden.isEmpty(), () -> "классы с @SubscribeEvent обязаны быть public: " + hidden);
    }

    private static boolean allowed(String violation) {
        String origin = originOf(violation);
        for (String allowed : UNTIL_THE_OLD_SIGNATURES_GO) {
            if (origin.startsWith(allowed)) {
                return true;
            }
        }
        return false;
    }

    /** Гейт пишет нарушение как {@code класс#метод(дескриптор): тип}; здесь нужен только класс. */
    private static String originOf(String violation) {
        int marker = violation.indexOf('#');
        if (marker < 0) {
            marker = violation.indexOf(':');
        }
        return marker < 0 ? violation : violation.substring(0, marker);
    }

    private static List<String> violations() throws IOException {
        return gate().violations(API);
    }

    private static PackageGate gate() throws IOException {
        return PackageGate.of(CodeApi.class, FORBIDDEN);
    }
}
