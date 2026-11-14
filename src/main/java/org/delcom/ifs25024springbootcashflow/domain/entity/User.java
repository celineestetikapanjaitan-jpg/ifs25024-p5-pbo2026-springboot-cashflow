package org.delcom.ifs25024springbootcashflow.domain.entity;

/**
 * Entity pengguna aplikasi. Password tidak pernah disimpan polos, hanya hash SHA-256.
 */
public class User {
    private final int id;
    private final String username;
    private String passwordHash;
    private String fullName;
    private final Role role;
    private String photoPath;

    public User(int id, String username, String passwordHash, String fullName, Role role, String photoPath) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.photoPath = photoPath;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    /** Path relatif foto profil di folder upload; null jika belum ada foto. */
    public String getPhotoPath() {
        return photoPath;
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void changeFullName(String fullName) {
        this.fullName = fullName;
    }

    public void changePhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
}
