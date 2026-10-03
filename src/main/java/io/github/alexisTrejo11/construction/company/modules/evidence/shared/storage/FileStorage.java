package io.github.alexisTrejo11.construction.company.modules.evidence.shared.storage;

import java.io.IOException;

public interface FileStorage {
    String store(byte[] content) throws IOException;

    void delete(String storageKey) throws IOException;
}
