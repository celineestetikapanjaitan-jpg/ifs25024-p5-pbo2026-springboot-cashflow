package org.delcom.ifs25024springbootcashflow.domain.repository;

import org.delcom.ifs25024springbootcashflow.domain.entity.Role;
import org.delcom.ifs25024springbootcashflow.domain.entity.User;

import java.util.Optional;

/**
 * Port spesifik pengguna. Update profil dan foto memakai {@link IRepository#update(Object)}.
 */
public interface IUserRepository extends IRepository<User, Integer> {
    /** Menyimpan user baru dan mengembalikannya lengkap dengan ID. */
    User save(String username, String passwordHash, String fullName, Role role);

    /** Mencari user berdasarkan username (username sudah dinormalisasi huruf kecil). */
    Optional<User> findByUsername(String username);
}
