package org.delcom.ifs25024springbootcashflow.domain.exception;

/**
 * Exception yang dilempar ketika entitas dengan ID tertentu tidak ditemukan.
 */
public class EntityNotFoundException extends DomainException {
    private final int id;

    /** Entitas transaksi tidak ditemukan. */
    public EntityNotFoundException(int id) {
        this("Transaksi", id);
    }

    /** Entitas dengan nama tertentu tidak ditemukan. */
    public EntityNotFoundException(String entityName, int id) {
        super(entityName + " dengan ID " + id + " tidak ditemukan.");
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
