package org.delcom.ifs25024springbootcashflow.adapter.presenter;

import org.delcom.ifs25024springbootcashflow.domain.entity.Role;
import org.delcom.ifs25024springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25024springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25024springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FinancePresenterTest {

    private final FinancePresenter presenter = new FinancePresenter();
    private final Transaction income = new Transaction(1, 1, "Gaji", 1_000_000L, TransactionType.INCOME, null,
            LocalDateTime.of(2026, 11, 7, 9, 5));
    private final Transaction expense = new Transaction(2, 1, "Makan", 25_000L, TransactionType.EXPENSE, null);

    @Test
    void formatMoneyUsesCommaThousandsAndTwoDecimals() {
        assertEquals("Rp 1,000,000.00", presenter.formatMoney(1_000_000L));
        assertEquals("Rp 0.00", presenter.formatMoney(0L));
        assertEquals("Rp 4,975,000.00", presenter.formatMoney(4_975_000L));
    }

    @Test
    void formatsTypeRoleSortAndTime() {
        assertEquals("Pemasukan", presenter.formatType(TransactionType.INCOME));
        assertEquals("Pengeluaran", presenter.formatType(TransactionType.EXPENSE));
        assertEquals("Admin", presenter.formatRole(Role.ADMIN));
        assertEquals("Pengguna", presenter.formatRole(Role.USER));
        assertEquals("Jumlah terkecil", presenter.formatSortOption(SortOption.AMOUNT_ASC));
        assertEquals("Pengeluaran dulu", presenter.formatSortOption(SortOption.EXPENSE_FIRST));
        assertEquals("07/11/2026 09:05", presenter.formatCreatedAt(income));
    }

    @Test
    void uploadUrlHandlesMissingPath() {
        assertNull(presenter.uploadUrl(null));
        assertNull(presenter.uploadUrl("  "));
        assertEquals("/uploads/a.png", presenter.uploadUrl("a.png"));
    }

    @Test
    void formatsLinesBalanceAndLists() {
        assertEquals("[1] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.format(income));
        assertEquals("Saldo: Rp 4,975,000.00", presenter.showBalance(4_975_000L));
        assertEquals("Daftar Transaksi:\n- Belum ada transaksi!", presenter.showTransactions(List.of()));
        assertEquals("Daftar Transaksi:\n[1] Gaji | Rp 1,000,000.00 | [Pemasukan]\n[2] Makan | Rp 25,000.00 | [Pengeluaran]",
                presenter.showTransactions(List.of(income, expense)));
        assertEquals("Hasil Pencarian: \"x\"\n- Transaksi tidak ditemukan!", presenter.showSearchResults(List.of(), "x"));
        assertEquals("Hasil Pencarian: \"Gaji\"\n[1] Gaji | Rp 1,000,000.00 | [Pemasukan]",
                presenter.showSearchResults(List.of(income), "Gaji"));
    }

    @Test
    void formatsMessages() {
        assertEquals("Berhasil mencatat transaksi: [1] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.showAddSuccess(income));
        assertEquals("Berhasil menghapus transaksi.", presenter.showRemoveSuccess());
        assertEquals("[!] Gagal menghapus transaksi dengan ID: 7.", presenter.showRemoveFailed(7));
        assertEquals("[!] Keterangan tidak boleh kosong", presenter.showValidationError("Keterangan tidak boleh kosong"));
    }
}
