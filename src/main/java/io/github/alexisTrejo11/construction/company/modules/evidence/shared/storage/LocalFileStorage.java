package io.github.alexisTrejo11.construction.company.modules.evidence.shared.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocalFileStorage implements FileStorage {
    private final Path rootDirectory;

    public LocalFileStorage(@Value("${file.upload.dir}") String rootDirectory) {
        this.rootDirectory = Path.of(rootDirectory).toAbsolutePath().normalize();
    }

    @Override
    public String store(byte[] content) throws IOException {
        Files.createDirectories(rootDirectory);
        String storageKey = UUID.randomUUID().toString();
        Files.write(resolve(storageKey), content);
        return storageKey;
    }

    @Override
    public void delete(String storageKey) throws IOException {
        Files.deleteIfExists(resolve(storageKey));
    }

    private Path resolve(String storageKey) {
        Path path = rootDirectory.resolve(storageKey).normalize();
        if (!path.startsWith(rootDirectory)) {
            throw new IllegalArgumentException("Invalid storage key");
        }
        return path;
    }
}
