package org.delcom.ifs25024springbootcashflow.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Hash dan verifikasi password memakai SHA-256 (hex lowercase).
 */
public final class PasswordHasher {

    private PasswordHasher() {
    }

    /** Menghasilkan hash SHA-256 dalam format hex dari password polos. */
    public static String hash(String rawPassword) {
        return hash("SHA-256", rawPassword);
    }

    /** Hash dengan algoritma tertentu; dipisah agar kegagalan algoritma dapat diuji. */
    static String hash(String algorithm, String rawPassword) {
        try {
            byte[] digest = MessageDigest.getInstance(algorithm)
                    .digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritma " + algorithm + " tidak tersedia", e);
        }
    }

    /** Memeriksa apakah password polos cocok dengan hash tersimpan. */
    public static boolean matches(String rawPassword, String hash) {
        return hash(rawPassword).equals(hash);
    }
}
