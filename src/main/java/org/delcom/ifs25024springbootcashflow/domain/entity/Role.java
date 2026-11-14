package org.delcom.ifs25024springbootcashflow.domain.entity;

/** Peran pengguna aplikasi. */
public enum Role {
    /** Dapat melihat daftar pengguna dan transaksi pengguna lain (read-only). */
    ADMIN,

    /** Hanya dapat mengelola transaksi miliknya sendiri. */
    USER
}
