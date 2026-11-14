package org.delcom.ifs25024springbootcashflow.adapter.repository;

import org.delcom.ifs25024springbootcashflow.domain.entity.Role;
import org.delcom.ifs25024springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryRepositoryTest {

    @Test
    void transactionRepositoryAssignsIdsAndFiltersByUser() {
        TransactionRepository repo = new TransactionRepository();
        var a = repo.save(1, TransactionType.INCOME, "a", 10L, null);
        var b = repo.save(2, TransactionType.EXPENSE, "b", 20L, "p.png");

        assertEquals(1, a.getId());
        assertEquals(2, b.getId());
        assertEquals(1, repo.findByUserId(1).size());
        assertEquals(2, repo.findAll().size());
        assertEquals("b", repo.findById(2).orElseThrow().getDescription());
        assertTrue(repo.findById(9).isEmpty());
        assertEquals(1, repo.findBy(t -> t.getAmount() > 15L).size());
    }

    @Test
    void findAllReturnsDefensiveCopy() {
        TransactionRepository repo = new TransactionRepository();
        repo.save(1, TransactionType.INCOME, "a", 10L, null);
        repo.findAll().clear();
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void deleteAndUpdate() {
        TransactionRepository repo = new TransactionRepository();
        var t = repo.save(1, TransactionType.INCOME, "a", 10L, null);

        t.changeAmount(99L);
        repo.update(t);
        assertEquals(99L, repo.findById(1).orElseThrow().getAmount());
        assertTrue(repo.deleteById(1));
        assertFalse(repo.deleteById(1));
    }

    @Test
    void userRepositoryFindsByUsername() {
        UserRepository repo = new UserRepository();
        repo.save("celine", "h", "Celine", Role.USER);

        assertEquals(1, repo.findByUsername("celine").orElseThrow().getId());
        assertTrue(repo.findByUsername("lain").isEmpty());
    }
}
