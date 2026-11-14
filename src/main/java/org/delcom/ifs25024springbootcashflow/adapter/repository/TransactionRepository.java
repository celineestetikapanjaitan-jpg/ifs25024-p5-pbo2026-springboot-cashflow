package org.delcom.ifs25024springbootcashflow.adapter.repository;

import org.delcom.ifs25024springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25024springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25024springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;

/**
 * Implementasi in-memory repository transaksi, dipakai pada unit test.
 * Data hilang saat aplikasi berhenti; runtime memakai {@link JdbcTransactionRepository}.
 */
public class TransactionRepository extends InMemoryRepository<Transaction, Integer>
        implements ITransactionRepository {

    private int idCounter = 0;

    public TransactionRepository() {
        super(Transaction::getId);
    }

    @Override
    public Transaction save(int userId, TransactionType type, String description, long amount, String proofPath) {
        return save(new Transaction(++idCounter, userId, description, amount, type, proofPath));
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return findBy(t -> t.getUserId() == userId);
    }
}
