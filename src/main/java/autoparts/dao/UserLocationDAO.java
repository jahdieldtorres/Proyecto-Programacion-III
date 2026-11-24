package autoparts.dao;

import autoparts.model.GeoPoint;
import java.util.Optional;

/**
 * Guarda la ubicación que cada usuario eligió en el mapa.
 *
 * Por ahora se guarda en el computador (UserLocationPreferencesDAO). Para
 * guardarla en Oracle bastaría con otra clase que implemente esta interfaz.
 *
 * @author Jahdiel Torres
 */
public interface UserLocationDAO {

    /**
     * @param userId id del usuario
     * @return la ubicación guardada, o vacío si nunca eligió una
     */
    Optional<GeoPoint> find(int userId);

    /**
     * Guarda (o reemplaza) la ubicación del usuario.
     *
     * @param userId   id del usuario
     * @param location punto elegido en el mapa
     */
    void save(int userId, GeoPoint location);
}
