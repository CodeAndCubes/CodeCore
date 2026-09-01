package com.mrleonardos.codecore.internal.client.image;

import net.minecraft.util.ResourceLocation;

import com.mrleonardos.codecore.api.client.image.ImageHandle;
import com.mrleonardos.codecore.api.client.image.ImageState;

/**
 * Состояние одной картинки.
 *
 * <p>
 * Поля меняются из главного потока, а читаются им же при отрисовке; {@code volatile} нужен, потому что
 * неудачу отмечает рабочий поток.
 *
 * <p>
 * Отпущенный handle переходит в FAILED и обратно уже не возвращается: сервис его выбросил, догрузить
 * картинку некому, и виджет должен нарисовать запасной вариант, а не остаться в вечном LOADING.
 */
final class ImageHandleImpl implements ImageHandle {

    private volatile ImageState state = ImageState.LOADING;
    private volatile ResourceLocation texture;
    private volatile long failedAt;

    @Override
    public ImageState state() {
        return state;
    }

    @Override
    public boolean ready() {
        return state == ImageState.READY;
    }

    @Override
    public ResourceLocation texture() {
        return texture;
    }

    void ready(ResourceLocation location) {
        this.texture = location;
        this.state = ImageState.READY;
    }

    void failed(long now) {
        this.failedAt = now;
        this.state = ImageState.FAILED;
    }

    /**
     * Начать загрузку заново.
     *
     * @return {@code false}, если повтор уже начал кто-то другой
     */
    synchronized boolean retrying() {
        if (state != ImageState.FAILED) {
            return false;
        }
        state = ImageState.LOADING;
        return true;
    }

    long failedAt() {
        return failedAt;
    }

    /** Отпустить текстуру: возвращается то, что осталось освободить видеокарте. */
    synchronized ResourceLocation release(long now) {
        ResourceLocation released = texture;
        texture = null;
        failedAt = now;
        state = ImageState.FAILED;
        return released;
    }
}
