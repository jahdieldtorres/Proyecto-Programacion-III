package autoparts.model;

import autoparts.util.FileUtil;
import java.time.LocalDate;

/**
 * Usuario del sistema: cliente, administrador o representante de un socio.
 *
 * Se guarda en data/users.csv con el formato
 * id|nombre|correo|clave_hash|rol|estado|fecha_registro|id_socio.
 *
 * @author Jahdiel Torres
 */
public class User {

    public static final String ROLE_CLIENT = "CLIENTE";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_PARTNER = "SOCIO";

    public static final String STATUS_ACTIVE = "ACTIVO";
    public static final String STATUS_INACTIVE = "INACTIVO";

    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private String role;
    private String status;
    private LocalDate registrationDate;
    private int partnerId;

    public User(int id, String name, String email, String passwordHash, String role,
            String status, LocalDate registrationDate, int partnerId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
        this.registrationDate = registrationDate;
        this.partnerId = partnerId;
    }

    /**
     * Crea el usuario a partir de los campos de una línea de users.csv.
     *
     * @param fields campos de la línea, en el orden del archivo
     */
    public User(String[] fields) {
        this(Integer.parseInt(fields[0]), fields[1], fields[2], fields[3], fields[4],
                fields[5], LocalDate.parse(fields[6]), Integer.parseInt(fields[7]));
    }

    /**
     * Convierte el usuario en una línea para guardarla en users.csv.
     */
    public String toLine() {
        return FileUtil.join(id, name, email, passwordHash, role, status, registrationDate, partnerId);
    }

    public boolean isActive() {
        return STATUS_ACTIVE.equals(status);
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public int getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(int partnerId) {
        this.partnerId = partnerId;
    }
}
