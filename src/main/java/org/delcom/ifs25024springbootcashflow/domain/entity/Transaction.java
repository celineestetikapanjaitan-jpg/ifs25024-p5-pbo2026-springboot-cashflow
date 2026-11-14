package org.delcom.ifs25024springbootcashflow.domain.entity;

import java.time.LocalDateTime;

/**
 * Entity inti yang merepresentasikan satu transaksi keuangan milik satu pengguna.
 * Berada di layer domain, bebas dari urusan tampilan maupun penyimpanan.
 */
public class Transaction {
    private final int id;
    private final int userId;
    private String description;

    /** Jumlah uang dalam rupiah; selalu positif, arah ditentukan oleh {@link #type}. */
    private long amount;

    private final TransactionType type;

    /** Path relatif bukti transaksi di folder upload; null jika tidak ada bukti. */
    private String proofPath;

    private final LocalDateTime createdAt;

    /** Membuat transaksi baru yang dicatat pada waktu sekarang. */
    public Transaction(int id, int userId, String description, long amount, TransactionType type,
                       String proofPath) {
        this(id, userId, description, amount, type, proofPath, LocalDateTime.now());
    }

    /** Membuat transaksi dengan waktu pencatatan yang sudah ditentukan. */
    public Transaction(int id, int userId, String description, long amount, TransactionType type,
                       String proofPath, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.proofPath = proofPath;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public long getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public String getProofPath() {
        return proofPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void changeAmount(long amount) {
        this.amount = amount;
    }

    /** Jumlah bertanda untuk perhitungan saldo: pemasukan positif, pengeluaran negatif. */
    public long signedAmount() {
        return type == TransactionType.INCOME ? amount : -amount;
    }
}
