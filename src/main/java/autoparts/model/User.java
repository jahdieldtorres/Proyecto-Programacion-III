package autoparts.model;

import autoparts.util.FileUtil;
import java.time.LocalDate;

/**
 * Usuario del sistema: administrador o cliente.
 *
 * Se guarda en data/users.csv con el formato
 * id|nombre|correo|clave_hash|rol|estado|fecha_registro.
 *
 * @author Jahdiel Torres
 */
public class User {

    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private Role role;
    private Status status;
    private LocalDate registrationDate;

    public User(int id, String name, String email, String passwordHash, Role role,
            Status status, LocalDate registrationDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
        this.registrationDate = registrationDate;
    }

    /**
     * Crea el usuario a partir de los campos de una línea de users.csv.
     *
     * @param fields campos de la línea, en el orden del archivo
     */
    public User(String[] fields) {
        this(Integer.parseInt(fields[0]), fields[1], fields[2], fields[3],
                Role.valueOf(fields[4].trim()), Status.valueOf(fields[5].trim()),
                LocalDate.parse(fields[6]));
    }

    /**
     * Convierte el usuario en una línea para guardarla en users.csv.
     */
    public String toLine() {
        return FileUtil.join(id, name, email, passwordHash, role, status, registrationDate);
    }

    public boolean isActive() {
        return status == Status.ACTIVO;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }
}
