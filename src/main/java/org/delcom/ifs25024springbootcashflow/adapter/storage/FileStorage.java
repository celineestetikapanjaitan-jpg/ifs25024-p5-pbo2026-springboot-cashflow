package org.delcom.ifs25024springbootcashflow.adapter.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

/**
 * Penyimpanan file upload (foto profil dan bukti transaksi) di folder lokal.
 * Nama file dibuat acak (UUID) agar tidak bisa menimpa file lain, dan hanya
 * path relatif yang dikembalikan sehingga mudah dilayani di {@code /uploads/**}.
 */
public class FileStorage {
    private final Path root;

    public FileStorage(String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    /** Folder upload absolut. */
    public Path root() {
        return root;
    }

    /** Lokasi folder upload sebagai URI berakhiran "/" untuk Spring resource handler. */
    public String resourceLocation() {
        String uri = root.toUri().toString();
        return uri.endsWith("/") ? uri : uri + "/";
    }

    /** Menyimpan isi file dan mengembalikan nama file relatif terhadap folder upload. */
    public String save(String originalName, InputStream content) throws IOException {
        Files.createDirectories(root);
        String fileName = UUID.randomUUID() + extensionOf(originalName);
        Files.copy(content, root.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    /** Mengambil ekstensi aman (huruf kecil dan angka, maksimal 8 karakter) beserta titiknya. */
    static String extensionOf(String originalName) {
        if (originalName == null) {
            return "";
        }
        int dot = originalName.lastIndexOf('.');
        if (dot < 0 || dot == originalName.length() - 1) {
            return "";
        }
        String extension = originalName.substring(dot + 1).toLowerCase(Locale.ROOT);
        return extension.matches("[a-z0-9]{1,8}") ? "." + extension : "";
    }
}
