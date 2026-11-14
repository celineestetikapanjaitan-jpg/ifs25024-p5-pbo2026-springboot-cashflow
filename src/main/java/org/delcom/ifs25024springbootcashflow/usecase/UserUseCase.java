package org.delcom.ifs25024springbootcashflow.usecase;

import org.delcom.ifs25024springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25024springbootcashflow.domain.entity.User;
import org.delcom.ifs25024springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25024springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25024springbootcashflow.domain.repository.IUserRepository;

import java.util.List;

/**
 * Use case pengelolaan pengguna: daftar user, ubah profil, dan ubah foto.
 */
public class UserUseCase {
    private final IUserRepository userRepository;

    public UserUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Mengambil semua user. */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /** Mengambil satu user berdasarkan ID. */
    public User getUser(int id) throws EntityNotFoundException {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Pengguna", id));
    }

    /**
     * Mengubah nama lengkap, dan password bila {@code newPassword} tidak kosong.
     * Password kosong atau null berarti password tidak diubah.
     */
    public User updateProfile(int id, String fullName, String newPassword)
            throws EntityNotFoundException, ValidationException {
        User user = getUser(id);
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Nama lengkap tidak boleh kosong");
        }
        boolean changePassword = newPassword != null && !newPassword.isEmpty();
        if (changePassword && newPassword.length() < AuthUseCase.MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Password minimal " + AuthUseCase.MIN_PASSWORD_LENGTH + " karakter");
        }
        user.changeFullName(fullName.trim());
        if (changePassword) {
            user.changePasswordHash(PasswordHasher.hash(newPassword));
        }
        userRepository.update(user);
        return user;
    }

    /** Mengubah foto profil. */
    public User updatePhoto(int id, String photoPath) throws EntityNotFoundException {
        User user = getUser(id);
        user.changePhotoPath(photoPath);
        userRepository.update(user);
        return user;
    }
}
