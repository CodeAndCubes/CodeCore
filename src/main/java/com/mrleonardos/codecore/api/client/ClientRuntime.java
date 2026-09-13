package com.mrleonardos.codecore.api.client;

import com.mrleonardos.codecore.api.client.avatar.AvatarService;
import com.mrleonardos.codecore.api.client.image.ImageService;
import com.mrleonardos.codecore.api.client.ui.render.Painter;
import com.mrleonardos.codecore.api.client.ui.render.WorldOutline;

/** Что клиентская часть ядра предоставляет модам. */
public interface ClientRuntime {

    ImageService images();

    AvatarService avatars();

    Painter painter();

    WorldOutline outline();
}
