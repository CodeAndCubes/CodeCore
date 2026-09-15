package com.mrleonardos.codecore.internal.client.image;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import net.minecraft.util.ResourceLocation;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.client.image.ImageHandle;
import com.mrleonardos.codecore.api.client.image.ImageLimits;
import com.mrleonardos.codecore.api.client.image.ImageRequest;
import com.mrleonardos.codecore.api.client.image.ImageService;
import com.mrleonardos.codecore.api.client.image.ImageState;
import com.mrleonardos.codecore.api.util.Scheduler;

/**
 * Загрузка картинок: очередь, кэш и текстуры.
 *
 * <p>
 * Тяжёлая работа вынесена в отдельные потоки: сеть, разбор и масштабирование. В главный возвращается
 * только готовое изображение: там его остаётся отдать видеокарте. Повторная просьба о том же самом
 * отдаёт тот же объект, поэтому сотня строк чата с одним автором даёт одну загрузку.
 *
 * <p>
 * Неудачный адрес запоминается: следующая попытка не раньше, чем через {@link
 * ImageLimits#RETRY_AFTER_FAILURE_MS}. Иначе каждый кадр отрисовки превращался бы в запрос к чужому серверу.
 *
 * <p>
 * Картинок держится не больше {@link ImageLimits#MAX_HANDLES}: самая давняя выселяется вместе с текстурой.
 * Догрузка выселенной картинки уже никому не нужна, поэтому её текстура освобождается сразу после заливки,
 * а не остаётся висеть в видеопамяти до выхода из игры.
 */
public final class ImageServiceImpl implements ImageService {

    private final Map<ImageRequest, ImageHandleImpl> handles = new LinkedHashMap<>(16, 0.75F, true);
    private final Set<String> formats;
    private final ImageDiskCache cache;
    private final Scheduler scheduler;
    private final Executor workers;
    private final ImageTextureSink textures;
    private final Logger log;

    public ImageServiceImpl(Path cacheDirectory, Scheduler scheduler, Executor workers, Logger log) {
        this(cacheDirectory, scheduler, workers, log, new ImageTextures());
    }

    ImageServiceImpl(Path cacheDirectory, Scheduler scheduler, Executor workers, Logger log,
        ImageTextureSink textures) {
        this.cache = new ImageDiskCache(cacheDirectory);
        this.scheduler = scheduler;
        this.workers = workers;
        this.textures = textures;
        this.log = log;
        this.formats = ImageDecoder.formats();
    }

    @Override
    public ImageHandle request(ImageRequest wanted) {
        ImageRequest request = withinLimits(wanted);
        ImageHandleImpl handle;
        boolean fresh = false;
        synchronized (handles) {
            handle = handles.get(request);
            if (handle == null) {
                handle = new ImageHandleImpl();
                handles.put(request, handle);
                evictOverflow();
                fresh = true;
            }
        }

        if (fresh || retryDue(handle)) {
            submit(request, handle);
        }
        return handle;
    }

    @Override
    public void clearMemory() {
        List<ImageHandleImpl> dropped;
        synchronized (handles) {
            dropped = new ArrayList<>(handles.values());
            handles.clear();
        }
        for (ImageHandleImpl handle : dropped) {
            release(handle);
        }
    }

    @Override
    public boolean supports(String formatHint) {
        if (formatHint == null) {
            return false;
        }
        String name = formatHint.toLowerCase(Locale.ROOT);
        int separator = name.lastIndexOf('/');
        String suffix = separator < 0 ? name : name.substring(separator + 1);
        return formats.contains(suffix.startsWith(".") ? suffix.substring(1) : suffix);
    }

    /** Форматы, которые понимает текущая сборка: для строки о возможностях в логе. */
    public Set<String> formats() {
        return formats;
    }

    private void submit(ImageRequest request, ImageHandleImpl handle) {
        workers.execute(() -> load(request, handle));
    }

    private void load(ImageRequest request, ImageHandleImpl handle) {
        try {
            BufferedImage prepared = cache.read(request);
            if (prepared == null) {
                prepared = ImageNormalizer.normalize(
                    ImageDecoder.decode(ImageFetcher.fetch(request.source())),
                    request.size(),
                    request.fit());
                cache.write(request, prepared);
            }
            BufferedImage ready = prepared;
            scheduler.onClientThread(() -> publish(request, handle, ready));
        } catch (Exception failure) {
            log.warn("Image {} could not be loaded: {}", describe(request), failure.toString());
            handle.failed(System.currentTimeMillis());
        }
    }

    private void publish(ImageRequest request, ImageHandleImpl handle, BufferedImage image) {
        ResourceLocation uploaded = textures.upload(image);
        boolean wanted;
        synchronized (handles) {
            wanted = handles.get(request) == handle;
        }
        if (wanted) {
            handle.ready(uploaded);
            return;
        }
        textures.release(uploaded);
    }

    private static String describe(ImageRequest request) {
        return request.source()
            .value() + " "
            + request.size()
            + "px "
            + request.fit()
                .name();
    }

    private static ImageRequest withinLimits(ImageRequest request) {
        int limit = Math.max(
            ImageLimits.current()
                .maxSize(),
            ImageLimits.MIN_SIZE);
        if (request.size() <= limit) {
            return request;
        }
        return ImageRequest.of(request.source(), limit)
            .fit(request.fit());
    }

    private void evictOverflow() {
        Iterator<Map.Entry<ImageRequest, ImageHandleImpl>> iterator = handles.entrySet()
            .iterator();
        int maxHandles = ImageLimits.current()
            .maxHandles();
        while (handles.size() > maxHandles && iterator.hasNext()) {
            ImageHandleImpl evicted = iterator.next()
                .getValue();
            iterator.remove();
            release(evicted);
        }
    }

    private void release(ImageHandleImpl handle) {
        ResourceLocation released = handle.release(System.currentTimeMillis());
        if (released != null) {
            textures.release(released);
        }
    }

    private static boolean retryDue(ImageHandleImpl handle) {
        return handle.state() == ImageState.FAILED
            && System.currentTimeMillis() - handle.failedAt() > ImageLimits.RETRY_AFTER_FAILURE_MS
            && handle.retrying();
    }

}
