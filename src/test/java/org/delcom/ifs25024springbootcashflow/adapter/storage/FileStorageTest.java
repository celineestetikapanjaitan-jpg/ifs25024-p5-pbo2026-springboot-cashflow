package org.delcom.ifs25024springbootcashflow.adapter.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageTest {

    @Test
    void savesFileWithUuidNameAndSafeExtension(@TempDir Path dir) throws Exception {
        FileStorage storage = new FileStorage(dir.resolve("uploads").toString());
        byte[] data = {1, 2, 3};

        String name = storage.save("Foto Saya.PNG", new ByteArrayInputStream(data));

        assertTrue(name.endsWith(".png"));
        assertEquals(36 + 4, name.length());
        assertArrayEquals(data, Files.readAllBytes(storage.root().resolve(name)));
    }

    @Test
    void extensionIsSanitised() {
        assertEquals("", FileStorage.extensionOf(null));
        assertEquals("", FileStorage.extensionOf("tanpa-titik"));
        assertEquals("", FileStorage.extensionOf("akhir."));
        assertEquals("", FileStorage.extensionOf("a.p/../ng"));
        assertEquals("", FileStorage.extensionOf("a.terlalupanjang"));
        assertEquals(".jpg", FileStorage.extensionOf("a.b.JPG"));
    }

    @Test
    void resourceLocationAlwaysEndsWithSlash(@TempDir Path dir) {
        assertTrue(new FileStorage(dir.toString()).resourceLocation().endsWith("/"));
        FileStorage missing = new FileStorage(dir.resolve("belum-ada").toString());
        assertFalse(Files.exists(missing.root()));
        assertTrue(missing.resourceLocation().endsWith("/"));
    }
}
