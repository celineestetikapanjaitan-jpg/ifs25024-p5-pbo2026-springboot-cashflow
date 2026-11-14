package org.delcom.ifs25024springbootcashflow.usecase;

import org.delcom.ifs25024springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25024springbootcashflow.domain.entity.Role;
import org.delcom.ifs25024springbootcashflow.domain.entity.User;
import org.delcom.ifs25024springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25024springbootcashflow.domain.repository.IUserRepository;

import java.util.Locale;

/**
 * Use case autentikasi: registrasi, login, dan pembuatan admin awal (root).
 */
public class AuthUseCase {
    /** Panjang minimal password. */
    public static final int MIN_PASSWORD_LENGTH = 6;

    private final IUserRepository userRepository;

    public AuthUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Mendaftarkan user baru dengan role USER. */
    public User register(String username, String password, String fullName) throws ValidationException {
        String normalized = normalize(username);
        if (normalized.isEmpty()) {
            throw new ValidationException("Username tidak boleh kosong");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Nama lengkap tidak boleh kosong");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Password minimal " + MIN_PASSWORD_LENGTH + " karakter");
        }
        if (userRepository.findByUsername(normalized).isPresent()) {
            throw new ValidationException("Username sudah digunakan");
        }
        return userRepository.save(normalized, PasswordHasher.hash(password), fullName.trim(), Role.USER);
    }

    /** Memverifikasi username dan password; melempar exception jika salah. */
    public User login(String username, String password) throws ValidationException {
        User user = userRepository.findByUsername(normalize(username))
                .filter(u -> password != null && PasswordHasher.matches(password, u.getPasswordHash()))
                .orElse(null);
        if (user == null) {
            throw new ValidationException("Username atau password salah");
        }
        return user;
    }

    /** Membuat admin dari konfigurasi ROOT_* bila belum ada. */
    public void ensureRootAdmin(String username, String password) {
        String normalized = normalize(username);
        if (userRepository.findByUsername(normalized).isEmpty()) {
            userRepository.save(normalized, PasswordHasher.hash(password), "Administrator", Role.ADMIN);
        }
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}
