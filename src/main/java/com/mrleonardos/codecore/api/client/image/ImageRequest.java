package com.mrleonardos.codecore.api.client.image;

/**
 * Просьба показать картинку в нужном размере.
 *
 * <p>
 * Размер входит в саму просьбу, потому что нормализация делается один раз при загрузке: масштабировать
 * при каждой отрисовке значит либо мылить края, либо тратить время на то, что не меняется.
 */
public final class ImageRequest {

    private final ImageSource source;
    private final int size;
    private final ImageFit fit;

    private ImageRequest(ImageSource source, int size, ImageFit fit) {
        this.source = source;
        this.size = size;
        this.fit = fit;
    }

    /**
     * Картинка из указанного источника, вписанная в квадрат стороной {@code size}.
     *
     * @throws IllegalArgumentException если размер вне {@link ImageLimits#MIN_SIZE}..{@link
     *                                  ImageLimits#MAX_SIZE}
     */
    public static ImageRequest of(ImageSource source, int size) {
        if (size < ImageLimits.MIN_SIZE || size > ImageLimits.MAX_SIZE) {
            throw new IllegalArgumentException("Image size out of bounds: " + size);
        }
        return new ImageRequest(source, size, ImageFit.COVER);
    }

    /** Та же просьба с другим способом вписывания. */
    public ImageRequest fit(ImageFit value) {
        return new ImageRequest(source, size, value);
    }

    public ImageSource source() {
        return source;
    }

    public int size() {
        return size;
    }

    public ImageFit fit() {
        return fit;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ImageRequest)) {
            return false;
        }
        ImageRequest that = (ImageRequest) other;
        return size == that.size && fit == that.fit && source.equals(that.source);
    }

    @Override
    public int hashCode() {
        return (source.hashCode() * 31 + size) * 31 + fit.hashCode();
    }

    @Override
    public String toString() {
        return source + "@" + size + "/" + fit;
    }
}
