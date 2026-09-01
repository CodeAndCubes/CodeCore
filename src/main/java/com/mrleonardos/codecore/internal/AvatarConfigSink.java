package com.mrleonardos.codecore.internal;

import com.mrleonardos.codecore.api.avatar.AvatarConfig;

/** Что клиент делает с настройкой аватаров, присланной сервером. */
public interface AvatarConfigSink {

    void apply(AvatarConfig config);
}
