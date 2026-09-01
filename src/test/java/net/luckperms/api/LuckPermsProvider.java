package net.luckperms.api;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Подставной LuckPerms для проверки адаптера.
 *
 * <p>
 * Адаптер ищет классы по именам, поэтому достаточно повторить точку входа и форму методов: настоящая
 * библиотека для 1.7.10 в сборке отсутствует, а проверять цепочку вызовов надо.
 */
public final class LuckPermsProvider {

    private static final FakeLuckPerms INSTANCE = new FakeLuckPerms();

    private LuckPermsProvider() {}

    public static FakeLuckPerms get() {
        return INSTANCE;
    }

    /** Права и мета, которые подставной LuckPerms отдаст адаптеру. */
    public static FakeUser user(UUID id) {
        return INSTANCE.manager.users.computeIfAbsent(id, key -> new FakeUser());
    }

    public static final class FakeLuckPerms {

        private final FakeUserManager manager = new FakeUserManager();

        public FakeUserManager getUserManager() {
            return manager;
        }
    }

    public static final class FakeUserManager {

        private final Map<UUID, FakeUser> users = new HashMap<>();

        public FakeUser getUser(UUID id) {
            return users.get(id);
        }
    }

    public static final class FakeUser {

        private final Map<String, Boolean> permissions = new HashMap<>();
        private final Map<String, String> meta = new HashMap<>();
        private final FakeCachedData cachedData = new FakeCachedData(this);
        private String primaryGroup = "default";

        public FakeUser allow(String node) {
            permissions.put(node, Boolean.TRUE);
            return this;
        }

        public FakeUser meta(String key, String value) {
            meta.put(key, value);
            return this;
        }

        public FakeUser primaryGroup(String value) {
            primaryGroup = value;
            return this;
        }

        public String getPrimaryGroup() {
            return primaryGroup;
        }

        public FakeCachedData getCachedData() {
            return cachedData;
        }
    }

    public static final class FakeCachedData {

        private final FakeUser owner;

        FakeCachedData(FakeUser owner) {
            this.owner = owner;
        }

        public FakePermissionData getPermissionData() {
            return new FakePermissionData(owner);
        }

        public FakeMetaData getMetaData() {
            return new FakeMetaData(owner);
        }
    }

    public static final class FakePermissionData {

        private final FakeUser owner;

        FakePermissionData(FakeUser owner) {
            this.owner = owner;
        }

        public FakeTristate checkPermission(String node) {
            return new FakeTristate(owner.permissions.containsKey(node));
        }
    }

    public static final class FakeMetaData {

        private final FakeUser owner;

        FakeMetaData(FakeUser owner) {
            this.owner = owner;
        }

        public String getMetaValue(String key) {
            return owner.meta.get(key);
        }
    }

    public static final class FakeTristate {

        private final boolean value;

        FakeTristate(boolean value) {
            this.value = value;
        }

        public boolean asBoolean() {
            return value;
        }
    }
}
