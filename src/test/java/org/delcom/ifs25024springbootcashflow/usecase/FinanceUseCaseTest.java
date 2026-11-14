package org.delcom.ifs25024springbootcashflow.usecase;

import org.delcom.ifs25024springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25024springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25024springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25024springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25024springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceUseCaseTest {

    private static final int ME = 1;
    private static final int OTHER = 2;

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FinanceUseCase(new TransactionRepository());
    }

    @Test
    void addIncomeAndExpenseTrimDescriptionAndKeepProof() throws Exception {
        Transaction income = useCase.addIncome(ME, "  Gaji  ", 5_000_000L, "bukti.png");
        Transaction expense = useCase.addExpense(ME, "Makan", 25_000L, null);

        assertEquals("Gaji", income.getDescription());
        assertEquals("bukti.png", income.getProofPath());
        assertEquals(2, expense.getId());
        assertEquals(2, useCase.getTransactions(ME).size());
    }

    @Test
    void validationRejectsBlankDescriptionAndNonPositiveAmount() {
        assertEquals("Keterangan tidak boleh kosong",
                assertThrows(ValidationException.class, () -> useCase.addIncome(ME, null, 10L, null)).getMessage());
        assertThrows(ValidationException.class, () -> useCase.addIncome(ME, "   ", 10L, null));
        assertEquals("Jumlah harus berupa angka lebih dari 0",
                assertThrows(ValidationException.class, () -> useCase.addExpense(ME, "x", 0L, null)).getMessage());
        assertThrows(ValidationException.class, () -> useCase.addExpense(ME, "x", -5L, null));
        assertTrue(useCase.getTransactions(ME).isEmpty());
    }

    @Test
    void dataIsScopedPerUser() throws Exception {
        useCase.addIncome(ME, "Gaji", 1_000L, null);
        useCase.addIncome(OTHER, "Bonus", 9_000L, null);

        assertEquals(1, useCase.getTransactions(ME).size());
        assertEquals(1_000L, useCase.getTotalIncome(ME));
        assertEquals(9_000L, useCase.getTotalIncome(OTHER));
    }

    @Test
    void balanceIsIncomeMinusExpense() throws Exception {
        useCase.addIncome(ME, "Gaji", 5_000_000L, null);
        useCase.addExpense(ME, "Sewa", 25_000L, null);

        assertEquals(5_000_000L, useCase.getTotalIncome(ME));
        assertEquals(25_000L, useCase.getTotalExpense(ME));
        assertEquals(4_975_000L, useCase.getBalance(ME));
        assertEquals(0L, useCase.getBalance(OTHER));
    }

    @Test
    void searchIsCaseInsensitiveAndScoped() throws Exception {
        useCase.addIncome(ME, "Gaji Bulanan", 100L, null);
        useCase.addExpense(ME, "Makan", 10L, null);
        useCase.addIncome(OTHER, "Gaji Orang Lain", 100L, null);

        List<Transaction> found = useCase.searchTransactions(ME, "gAjI");
        assertEquals(1, found.size());
        assertEquals("Gaji Bulanan", found.get(0).getDescription());
        assertTrue(useCase.searchTransactions(ME, "tidak ada").isEmpty());
    }

    @Test
    void sortDoesNotChangeStoredOrder() throws Exception {
        useCase.addExpense(ME, "a", 300L, null);
        useCase.addIncome(ME, "b", 100L, null);

        assertEquals(100L, useCase.sortTransactions(ME, SortOption.AMOUNT_ASC).get(0).getAmount());
        assertEquals(300L, useCase.sortTransactions(ME, SortOption.AMOUNT_DESC).get(0).getAmount());
        assertEquals(1, useCase.getTransactions(ME).get(0).getId());
    }

    @Test
    void updateChangesOwnTransactionOnly() throws Exception {
        Transaction t = useCase.addIncome(ME, "Lama", 100L, null);

        Transaction updated = useCase.updateTransaction(ME, t.getId(), " Baru ", 250L);
        assertEquals("Baru", updated.getDescription());
        assertEquals(250L, updated.getAmount());

        assertThrows(EntityNotFoundException.class, () -> useCase.updateTransaction(OTHER, t.getId(), "x", 1L));
        assertThrows(EntityNotFoundException.class, () -> useCase.updateTransaction(ME, 99, "x", 1L));
        assertThrows(ValidationException.class, () -> useCase.updateTransaction(ME, t.getId(), "", 1L));
        assertEquals("Baru", useCase.getTransactions(ME).get(0).getDescription());
    }

    @Test
    void removeOnlyOwnTransaction() throws Exception {
        Transaction t = useCase.addIncome(ME, "Gaji", 100L, null);

        EntityNotFoundException notOwner = assertThrows(EntityNotFoundException.class,
                () -> useCase.removeTransaction(OTHER, t.getId()));
        assertEquals(t.getId(), notOwner.getId());
        assertThrows(EntityNotFoundException.class, () -> useCase.removeTransaction(ME, 99));
        assertEquals(1, useCase.getTransactions(ME).size());

        useCase.removeTransaction(ME, t.getId());
        assertTrue(useCase.getTransactions(ME).isEmpty());
    }
}
