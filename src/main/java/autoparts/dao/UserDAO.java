package autoparts.dao;

import autoparts.model.User;
import java.util.List;

/**
 * Operaciones para guardar y consultar usuarios.
 *
 * Los servicios dependen de esta interfaz y no de cómo se guardan los datos:
 * hoy la implementa UserFileDAO (archivos planos) y después podrá implementarla
 * una clase para Oracle sin cambiar el servicio.
 *
 * @author Jahdiel Torres
 */
public interface UserDAO {

    List<User> findAll();

    /**
     * Busca un usuario por su correo, sin importar mayúsculas o minúsculas.
     *
     * @return el usuario, o null si no existe
     */
    User findByEmail(String email);

    /**
     * Guarda un usuario nuevo y le asigna el siguiente identificador libre.
     *
     * @return el identificador asignado
     */
    int save(User user);

    void update(User user);
}
