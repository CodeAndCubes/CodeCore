package com.mrleonardos.codecore.api.adapter;

/**
 * Умения роли {@code permissions}: перечень объявляет ядро, потому что реализация по умолчанию живёт у него.
 *
 * <p>
 * Константы лежат в api, а не рядом с объявлением роли, ровно затем, чтобы их было чем назвать снаружи.
 * Чужой мод спрашивает {@code CodeApi.adapters().missing(ConfigRoles.PERMISSIONS).contains(META)} и не лезет
 * за строкой во внутренности ядра. По той же причине заявка на роль называет своё подмножество этими же
 * константами, а не повторяет строки: две копии перечня расходятся молча, и опечатка в любой из них делает
 * умение навсегда недостающим.
 */
public final class PermissionCapabilities {

    /** Ответ на вопрос, есть ли у игрока право. */
    public static final RoleCapability HAS = RoleCapability.of("has");

    /** Имя группы игрока. */
    public static final RoleCapability GROUP = RoleCapability.of("group");

    /** Значения меты: префикс, лимит домов, что угодно ещё. */
    public static final RoleCapability META = RoleCapability.of("meta");

    /** Права, действующие только в мире, режиме или другом контексте. */
    public static final RoleCapability CONTEXTS = RoleCapability.of("contexts");

    /** Выдачи на срок, которые снимаются сами. */
    public static final RoleCapability EXPIRY = RoleCapability.of("expiry");

    /** Треки: порядок групп для повышения и понижения. */
    public static final RoleCapability TRACKS = RoleCapability.of("tracks");

    private PermissionCapabilities() {}
}
