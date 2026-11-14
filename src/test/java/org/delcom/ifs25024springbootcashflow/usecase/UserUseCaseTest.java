package org.delcom.ifs25024springbootcashflow.usecase;

import org.delcom.ifs25024springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25024springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25024springbootcashflow.domain.entity.Role;
import org.delcom.ifs25024springbootcashflow.domain.entity.User;
import org.delcom.ifs25024springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25024springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserUseCaseTest {

    private UserUseCase useCase;
    private User user;

    @BeforeEach
    void setUp() {
        UserRepository repository = new UserRepository();
        useCase = new UserUseCase(repository);
        user = repository.save("celine", PasswordHasher.hash("lama123"), "Celine", Role.USER);
    }

    @Test
    void listsAndGetsUsers() throws Exception {
        assertEquals(1, useCase.getAllUsers().size());
        assertEquals("celine", useCase.getUser(user.getId()).getUsername());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> useCase.getUser(99));
        assertEquals("Pengguna dengan ID 99 tidak ditemukan.", ex.getMessage());
    }

    @Test
    void updateProfileChangesNameAndKeepsPasswordWhenBlank() throws Exception {
        String oldHash = user.getPasswordHash();

        assertEquals("Baru", useCase.updateProfile(user.getId(), " Baru ", null).getFullName());
        assertEquals(oldHash, useCase.getUser(user.getId()).getPasswordHash());
        useCase.updateProfile(user.getId(), "Baru", "");
        assertEquals(oldHash, useCase.getUser(user.getId()).getPasswordHash());
    }

    @Test
    void updateProfileChangesPasswordWhenGiven() throws Exception {
        useCase.updateProfile(user.getId(), "Celine", "baru1234");
        assertEquals(PasswordHasher.hash("baru1234"), useCase.getUser(user.getId()).getPasswordHash());
    }

    @Test
    void updateProfileValidates() {
        assertThrows(ValidationException.class, () -> useCase.updateProfile(user.getId(), null, null));
        assertThrows(ValidationException.class, () -> useCase.updateProfile(user.getId(), "  ", null));
        assertEquals("Password minimal 6 karakter",
                assertThrows(ValidationException.class, () -> useCase.updateProfile(user.getId(), "N", "123")).getMessage());
        assertThrows(EntityNotFoundException.class, () -> useCase.updateProfile(99, "N", null));
    }

    @Test
    void updatePhotoStoresPath() throws Exception {
        assertEquals("foto.png", useCase.updatePhoto(user.getId(), "foto.png").getPhotoPath());
        assertThrows(EntityNotFoundException.class, () -> useCase.updatePhoto(99, "x.png"));
    }
}
