package org.delcom.ifs25024springbootcashflow.adapter.repository;

import org.delcom.ifs25024springbootcashflow.domain.entity.Role;
import org.delcom.ifs25024springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25024springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25024springbootcashflow.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Tes repository JDBC memakai H2 in-memory (mode PostgreSQL), bukan PostgreSQL asli. */
class JdbcRepositoryTest {

    private DataSource dataSource;
    private JdbcUserRepository users;
    private JdbcTransactionRepository transactions;
    private User celine;

    @BeforeEach
    void setUp() {
        dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        SchemaInitializer.initialize(dataSource);
        SchemaInitializer.initialize(dataSource); // IF NOT EXISTS: aman dipanggil dua kali
        users = new JdbcUserRepository(dataSource);
        transactions = new JdbcTransactionRepository(dataSource);
        celine = users.save("celine", "hash", "Celine", Role.USER);
    }

    // ---------------------------------------------------------------- user

    @Test
    void userSaveFindAndUpdate() {
        assertTrue(celine.getId() > 0);
        assertEquals("celine", users.findById(celine.getId()).orElseThrow().getUsername());
        assertEquals(Role.USER, users.findByUsername("celine").orElseThrow().getRole());
        assertTrue(users.findById(999).isEmpty());
        assertTrue(users.findByUsername("lain").isEmpty());

        celine.changeFullName("Celine P");
        celine.changePasswordHash("h2");
        celine.changePhotoPath("foto.png");
        users.update(celine);

        User reloaded = users.findById(celine.getId()).orElseThrow();
        assertEquals("Celine P", reloaded.getFullName());
        assertEquals("h2", reloaded.getPasswordHash());
        assertEquals("foto.png", reloaded.getPhotoPath());
    }

    @Test
    void userSaveEntityFindAllFindByAndDelete() {
        User copy = users.save(new User(0, "budi", "h", "Budi", Role.ADMIN, "p.png"));

        assertEquals(2, users.findAll().size());
        assertEquals("p.png", users.findById(copy.getId()).orElseThrow().getPhotoPath());
        assertEquals(1, users.findBy(u -> u.getRole() == Role.ADMIN).size());
        assertTrue(users.deleteById(copy.getId()));
        assertFalse(users.deleteById(copy.getId()));
        assertNull(users.findByUsername("celine").orElseThrow().getPhotoPath());
    }

    // --------------------------------------------------------- transaction

    @Test
    void transactionSaveFindAndFilterByUser() {
        User other = users.save("budi", "h", "Budi", Role.USER);
        Transaction a = transactions.save(celine.getId(), TransactionType.INCOME, "Gaji", 1_000_000L, "b.png");
        transactions.save(celine.getId(), TransactionType.EXPENSE, "Makan", 25_000L, null);
        transactions.save(other.getId(), TransactionType.INCOME, "Bonus", 5L, null);

        assertEquals(3, transactions.findAll().size());
        assertEquals(2, transactions.findByUserId(celine.getId()).size());
        assertEquals(1, transactions.findBy(t -> t.getType() == TransactionType.EXPENSE).size());

        Transaction found = transactions.findById(a.getId()).orElseThrow();
        assertEquals("Gaji", found.getDescription());
        assertEquals(1_000_000L, found.getAmount());
        assertEquals(TransactionType.INCOME, found.getType());
        assertEquals("b.png", found.getProofPath());
        assertEquals(celine.getId(), found.getUserId());
        assertTrue(transactions.findById(999).isEmpty());
    }

    @Test
    void transactionSaveEntityKeepsCreatedAt() {
        LocalDateTime at = LocalDateTime.of(2026, 1, 2, 3, 4, 5);
        Transaction saved = transactions.save(
                new Transaction(0, celine.getId(), "Lama", 7L, TransactionType.EXPENSE, null, at));

        assertEquals(at, transactions.findById(saved.getId()).orElseThrow().getCreatedAt());
    }

    @Test
    void transactionUpdateAndDelete() {
        Transaction t = transactions.save(celine.getId(), TransactionType.INCOME, "Gaji", 100L, null);

        t.changeDescription("Gaji Naik");
        t.changeAmount(250L);
        transactions.update(t);

        Transaction reloaded = transactions.findById(t.getId()).orElseThrow();
        assertEquals("Gaji Naik", reloaded.getDescription());
        assertEquals(250L, reloaded.getAmount());
        assertTrue(transactions.deleteById(t.getId()));
        assertFalse(transactions.deleteById(t.getId()));
    }

    // ------------------------------------------------------------- support

    @Test
    void sqlExceptionIsWrapped() throws Exception {
        DataSource broken = mock(DataSource.class);
        when(broken.getConnection()).thenThrow(new SQLException("putus"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> JdbcSupport.execute(broken, (Connection c) -> 1));
        assertEquals("Operasi database gagal: putus", ex.getMessage());
        assertInstanceOf(SQLException.class, ex.getCause());
        assertThrows(IllegalStateException.class, () -> new JdbcUserRepository(broken).findAll());
    }

    @Test
    void privateConstructorsCovered() throws Exception {
        for (Class<?> type : new Class<?>[]{JdbcSupport.class, SchemaInitializer.class}) {
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        }
    }
}
