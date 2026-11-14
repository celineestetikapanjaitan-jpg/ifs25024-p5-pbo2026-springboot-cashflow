package org.delcom.ifs25024springbootcashflow.domain.repository;

import org.delcom.ifs25024springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25024springbootcashflow.domain.entity.TransactionType;

import java.util.List;

/**
 * Port spesifik transaksi yang memperluas generic {@link IRepository}.
 * ID dibuat otomatis oleh implementasi, sehingga penyimpanan memakai method khusus.
 */
public interface ITransactionRepository extends IRepository<Transaction, Integer> {
    /** Menyimpan transaksi baru milik user dan mengembalikannya lengkap dengan ID. */
    Transaction save(int userId, TransactionType type, String description, long amount, String proofPath);

    /** Mengambil semua transaksi milik satu user, urut berdasarkan ID. */
    List<Transaction> findByUserId(int userId);
}
