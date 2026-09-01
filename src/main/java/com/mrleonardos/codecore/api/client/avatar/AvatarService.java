package com.mrleonardos.codecore.api.client.avatar;

import java.util.UUID;

import com.mrleonardos.codecore.api.client.image.ImageHandle;

/**
 * Аватары игроков.
 *
 * <p>
 * Настроенный провайдер спрашивается первым; если он ничего не дал или картинка не загрузилась, рисующему
 * коду остаётся запасной вариант: голова из скина, а за ней буква ника. Поэтому метод возвращает
 * состояние, а не картинку: решение, что показать вместо неё, принимает тот, кто рисует.
 */
public interface AvatarService {

    /**
     * Аватар игрока в указанном размере.
     *
     * @return состояние загрузки; {@code null}, если аватары выключены или провайдер не знает игрока
     */
    ImageHandle avatar(UUID playerId, String playerName, int size);

    /** Добавить свой источник аватаров; он спрашивается раньше встроенного. */
    void addProvider(AvatarProvider provider);

    /** Включены ли аватары в настройках. */
    boolean enabled();
}
