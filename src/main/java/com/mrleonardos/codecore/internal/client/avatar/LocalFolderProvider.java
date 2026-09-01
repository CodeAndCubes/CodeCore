package com.mrleonardos.codecore.internal.client.avatar;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import com.mrleonardos.codecore.api.client.avatar.AvatarProvider;
import com.mrleonardos.codecore.api.client.image.ImageSource;

/**
 * Аватар из папки на диске игрока.
 *
 * <p>
 * Ищутся файлы с ником и с идентификатором, в обоих написаниях: так один и тот же набор картинок годится
 * и для сервера с лицензией, и для оффлайн-сервера.
 */
final class LocalFolderProvider implements AvatarProvider {

    private static final String[] EXTENSIONS = { ".png", ".jpg", ".jpeg", ".gif", ".webp" };
    private static final String DASH = "-";

    private final Path folder;

    LocalFolderProvider(Path folder) {
        this.folder = folder;
    }

    @Override
    public ImageSource sourceFor(UUID playerId, String playerName) {
        Path found = firstExisting(AvatarPlaceholders.fileName(playerName));
        if (found == null && playerId != null) {
            found = firstExisting(playerId.toString());
        }
        if (found == null && playerId != null) {
            found = firstExisting(
                playerId.toString()
                    .replace(DASH, ""));
        }
        return found == null ? null : ImageSource.file(found);
    }

    private Path firstExisting(String base) {
        if (base.isEmpty()) {
            return null;
        }
        for (String extension : EXTENSIONS) {
            Path candidate = folder.resolve(base + extension);
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        return null;
    }
}
