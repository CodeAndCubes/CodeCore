package com.mrleonardos.codecore.internal.client.avatar;

/**
 * Содержимое {@code config/codecore/client/avatars.json}: то немногое, что решает сам игрок.
 *
 * <p>
 * Откуда берутся аватары, говорит сервер. У себя можно только отказаться от них совсем: тогда клиент не
 * пойдёт ни по какому адресу, что бы сервер ни прислал.
 */
public final class ClientAvatarSettings {

    /** Показывать ли аватары вообще. */
    public boolean enabled = true;
}
