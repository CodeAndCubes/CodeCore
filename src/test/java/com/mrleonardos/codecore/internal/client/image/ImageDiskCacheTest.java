package com.mrleonardos.codecore.internal.client.image;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.mrleonardos.codecore.api.client.image.ImageFit;
import com.mrleonardos.codecore.api.client.image.ImageRequest;
import com.mrleonardos.codecore.api.client.image.ImageSource;

class ImageDiskCacheTest {

    @TempDir
    Path directory;

    @Test
    @DisplayName("ключ кэша собирается из полей: размер и вписывание не путаются, равная просьба находит файл")
    void keyIsBuiltFromTheFields() throws IOException {
        ImageDiskCache cache = new ImageDiskCache(directory);
        ImageSource source = ImageSource.url("https://site/av/Steve.png");
        ImageRequest request = ImageRequest.of(source, 64);
        cache.write(request, new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB));

        assertNotNull(cache.read(request), "та же просьба читает свой файл");
        assertNotNull(cache.read(ImageRequest.of(source, 64)), "равная просьба тоже находит файл");
        assertNull(cache.read(request.fit(ImageFit.CONTAIN)), "другое вписывание это другой файл");
        assertNull(cache.read(ImageRequest.of(source, 32)), "другой размер это другой файл");
    }
}
