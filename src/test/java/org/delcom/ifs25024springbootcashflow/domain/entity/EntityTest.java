package org.delcom.ifs25024springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class EntityTest {

    @Test
    void transactionKeepsFieldsAndSignedAmount() {
        LocalDateTime at = LocalDateTime.of(2026, 1, 2, 3, 4);
        Transaction income = new Transaction(1, 7, "Gaji", 1_000L, TransactionType.INCOME, "a.png", at);
        Transaction expense = new Transaction(2, 7, "Makan", 400L, TransactionType.EXPENSE, null);

        assertEquals(1, income.getId());
        assertEquals(7, income.getUserId());
        assertEquals("Gaji", income.getDescription());
        assertEquals(1_000L, income.getAmount());
        assertEquals(TransactionType.INCOME, income.getType());
        assertEquals("a.png", income.getProofPath());
        assertEquals(at, income.getCreatedAt());
        assertEquals(1_000L, income.signedAmount());
        assertEquals(-400L, expense.signedAmount());
        assertNull(expense.getProofPath());
        assertNotNull(expense.getCreatedAt());
    }

    @Test
    void transactionCanChangeDescriptionAndAmount() {
        Transaction t = new Transaction(1, 1, "Lama", 10L, TransactionType.INCOME, null);
        t.changeDescription("Baru");
        t.changeAmount(99L);
        assertEquals("Baru", t.getDescription());
        assertEquals(99L, t.getAmount());
    }

    @Test
    void userKeepsFieldsAndCanChange() {
        User user = new User(3, "celine", "hash", "Celine", Role.USER, null);
        assertEquals(3, user.getId());
        assertEquals("celine", user.getUsername());
        assertEquals("hash", user.getPasswordHash());
        assertEquals("Celine", user.getFullName());
        assertEquals(Role.USER, user.getRole());
        assertNull(user.getPhotoPath());

        user.changePasswordHash("h2");
        user.changeFullName("C");
        user.changePhotoPath("p.png");
        assertEquals("h2", user.getPasswordHash());
        assertEquals("C", user.getFullName());
        assertEquals("p.png", user.getPhotoPath());
    }

    @Test
    void enumsHaveExpectedValues() {
        assertEquals(2, Role.values().length);
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
        assertEquals(TransactionType.INCOME, TransactionType.valueOf("INCOME"));
        assertEquals(SortOption.AMOUNT_ASC, SortOption.valueOf("AMOUNT_ASC"));
    }

    @Test
    void sortOptionsOrderTransactions() {
        Transaction a = new Transaction(1, 1, "a", 300L, TransactionType.EXPENSE, null);
        Transaction b = new Transaction(2, 1, "b", 100L, TransactionType.INCOME, null);
        Transaction c = new Transaction(3, 1, "c", 200L, TransactionType.EXPENSE, null);

        assertEquals(List.of(2, 3, 1), ids(sorted(SortOption.AMOUNT_ASC, a, b, c)));
        assertEquals(List.of(1, 3, 2), ids(sorted(SortOption.AMOUNT_DESC, a, b, c)));
        assertEquals(2, sorted(SortOption.INCOME_FIRST, a, b, c).get(0).getId());
        assertEquals(TransactionType.EXPENSE, sorted(SortOption.EXPENSE_FIRST, a, b, c).get(0).getType());
    }

    private static List<Transaction> sorted(SortOption option, Transaction... items) {
        List<Transaction> list = new ArrayList<>(List.of(items));
        list.sort(option.comparator());
        return list;
    }

    private static List<Integer> ids(List<Transaction> list) {
        return list.stream().map(Transaction::getId).toList();
    }
}
