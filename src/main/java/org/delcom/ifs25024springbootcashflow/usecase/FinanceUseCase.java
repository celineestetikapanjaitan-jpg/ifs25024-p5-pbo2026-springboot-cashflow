package org.delcom.ifs25024springbootcashflow.usecase;

import org.delcom.ifs25024springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25024springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25024springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25024springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25024springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25024springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;

/**
 * Use case catatan keuangan. Semua operasi dibatasi per user: user hanya dapat
 * melihat, mengubah, dan menghapus transaksi miliknya sendiri.
 * Tidak ada I/O di sini; kegagalan dilempar sebagai checked exception domain.
 */
public class FinanceUseCase {
    private final ITransactionRepository repository;

    public FinanceUseCase(ITransactionRepository repository) {
        this.repository = repository;
    }

    /** Mengambil semua transaksi milik user. */
    public List<Transaction> getTransactions(int userId) {
        return repository.findByUserId(userId);
    }

    /** Mencatat pemasukan baru setelah validasi. {@code proofPath} boleh null. */
    public Transaction addIncome(int userId, String description, long amount, String proofPath)
            throws ValidationException {
        validateInput(description, amount);
        return repository.save(userId, TransactionType.INCOME, description.trim(), amount, proofPath);
    }

    /** Mencatat pengeluaran baru setelah validasi. {@code proofPath} boleh null. */
    public Transaction addExpense(int userId, String description, long amount, String proofPath)
            throws ValidationException {
        validateInput(description, amount);
        return repository.save(userId, TransactionType.EXPENSE, description.trim(), amount, proofPath);
    }

    /** Mengubah keterangan dan jumlah transaksi milik user. */
    public Transaction updateTransaction(int userId, int id, String description, long amount)
            throws ValidationException, EntityNotFoundException {
        Transaction transaction = findOwned(userId, id);
        validateInput(description, amount);
        transaction.changeDescription(description.trim());
        transaction.changeAmount(amount);
        repository.update(transaction);
        return transaction;
    }

    /** Menghapus transaksi milik user; melempar exception jika tidak ada atau bukan miliknya. */
    public void removeTransaction(int userId, int id) throws EntityNotFoundException {
        findOwned(userId, id);
        repository.deleteById(id);
    }

    /** Mencari transaksi milik user yang keterangannya mengandung kata kunci (case-insensitive). */
    public List<Transaction> searchTransactions(int userId, String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return repository.findByUserId(userId).stream()
                .filter(t -> t.getDescription().toLowerCase().contains(lowerKeyword))
                .toList();
    }

    /** Mengurutkan transaksi milik user sesuai {@link SortOption}; data asli tidak berubah. */
    public List<Transaction> sortTransactions(int userId, SortOption option) {
        return repository.findByUserId(userId).stream()
                .sorted(option.comparator())
                .toList();
    }

    /** Total seluruh pemasukan user. */
    public long getTotalIncome(int userId) {
        return totalOf(userId, TransactionType.INCOME);
    }

    /** Total seluruh pengeluaran user. */
    public long getTotalExpense(int userId) {
        return totalOf(userId, TransactionType.EXPENSE);
    }

    /** Saldo user: total pemasukan dikurangi total pengeluaran. */
    public long getBalance(int userId) {
        return getTotalIncome(userId) - getTotalExpense(userId);
    }

    private long totalOf(int userId, TransactionType type) {
        return repository.findByUserId(userId).stream()
                .filter(t -> t.getType() == type)
                .mapToLong(Transaction::getAmount)
                .sum();
    }

    private Transaction findOwned(int userId, int id) throws EntityNotFoundException {
        return repository.findById(id)
                .filter(t -> t.getUserId() == userId)
                .orElseThrow(() -> new EntityNotFoundException(id));
    }

    /** Memeriksa input transaksi tanpa menyimpan apa pun; dipakai view sebelum menyimpan file bukti. */
    public void validateInput(String description, long amount) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
        if (amount <= 0) {
            throw new ValidationException("Jumlah harus berupa angka lebih dari 0");
        }
    }
}
