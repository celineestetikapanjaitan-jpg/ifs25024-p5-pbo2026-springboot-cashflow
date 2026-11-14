package org.delcom.ifs25024springbootcashflow.usecase;

import org.delcom.ifs25024springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25024springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25024springbootcashflow.domain.entity.Role;
import org.delcom.ifs25024springbootcashflow.domain.entity.User;
import org.delcom.ifs25024springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthUseCaseTest {

    private UserRepository repository;
    private AuthUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new UserRepository();
        useCase = new AuthUseCase(repository);
    }

    @Test
    void registerNormalizesUsernameAndHashesPassword() throws Exception {
        User user = useCase.register("  Celine  ", "rahasia1", "  Celine P  ");

        assertEquals("celine", user.getUsername());
        assertEquals("Celine P", user.getFullName());
        assertEquals(Role.USER, user.getRole());
        assertEquals(PasswordHasher.hash("rahasia1"), user.getPasswordHash());
    }

    @Test
    void registerValidatesEveryField() throws Exception {
        assertEquals("Username tidak boleh kosong",
                assertThrows(ValidationException.class, () -> useCase.register("  ", "rahasia1", "N")).getMessage());
        assertThrows(ValidationException.class, () -> useCase.register(null, "rahasia1", "N"));
        assertEquals("Nama lengkap tidak boleh kosong",
                assertThrows(ValidationException.class, () -> useCase.register("a", "rahasia1", " ")).getMessage());
        assertThrows(ValidationException.class, () -> useCase.register("a", "rahasia1", null));
        assertEquals("Password minimal 6 karakter",
                assertThrows(ValidationException.class, () -> useCase.register("a", "123", "N")).getMessage());
        assertThrows(ValidationException.class, () -> useCase.register("a", null, "N"));

        useCase.register("a", "rahasia1", "N");
        assertEquals("Username sudah digunakan",
                assertThrows(ValidationException.class, () -> useCase.register("A", "rahasia1", "N")).getMessage());
    }

    @Test
    void loginChecksUsernameAndPassword() throws Exception {
        useCase.register("celine", "rahasia1", "Celine");

        assertEquals("celine", useCase.login(" CELINE ", "rahasia1").getUsername());
        assertThrows(ValidationException.class, () -> useCase.login("celine", "salah"));
        assertThrows(ValidationException.class, () -> useCase.login("celine", null));
        assertEquals("Username atau password salah",
                assertThrows(ValidationException.class, () -> useCase.login("tidak-ada", "x")).getMessage());
        assertThrows(ValidationException.class, () -> useCase.login(null, "x"));
    }

    @Test
    void ensureRootAdminCreatesOnlyOnce() throws Exception {
        useCase.ensureRootAdmin("Admin", "admin123");
        useCase.ensureRootAdmin("admin", "lain");

        assertEquals(1, repository.findAll().size());
        User admin = useCase.login("admin", "admin123");
        assertEquals(Role.ADMIN, admin.getRole());
        assertTrue(PasswordHasher.matches("admin123", admin.getPasswordHash()));
    }
}
