package org.delcom.ifs25024springbootcashflow.domain;

import org.delcom.ifs25024springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25024springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25024springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainTest {

    @Test
    void hashIsSha256Hex() {
        assertEquals("ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f", PasswordHasher.hash("12345678"));
        assertEquals(64, PasswordHasher.hash("apa saja").length());
    }

    @Test
    void matchesComparesHash() {
        String hash = PasswordHasher.hash("rahasia");
        assertTrue(PasswordHasher.matches("rahasia", hash));
        assertFalse(PasswordHasher.matches("salah", hash));
        assertNotEquals(PasswordHasher.hash("a"), PasswordHasher.hash("b"));
    }

    @Test
    void unknownAlgorithmIsWrapped() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> PasswordHasher.hash("NOPE-256", "x"));
        assertInstanceOf(java.security.NoSuchAlgorithmException.class, ex.getCause());
    }

    @Test
    void privateConstructorCovered() throws Exception {
        Constructor<PasswordHasher> constructor = PasswordHasher.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    void exceptionsCarryMessages() {
        Throwable cause = new RuntimeException("sebab");
        DomainException withCause = new DomainException("pesan", cause);
        assertEquals("pesan", withCause.getMessage());
        assertSame(cause, withCause.getCause());

        EntityNotFoundException notFound = new EntityNotFoundException(5);
        assertEquals("Transaksi dengan ID 5 tidak ditemukan.", notFound.getMessage());
        assertEquals(5, notFound.getId());
        assertEquals("Pengguna dengan ID 9 tidak ditemukan.", new EntityNotFoundException("Pengguna", 9).getMessage());

        assertInstanceOf(DomainException.class, new ValidationException("x"));
        assertInstanceOf(DomainException.class, notFound);
    }
}
