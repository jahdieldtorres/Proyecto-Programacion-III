package autoparts.service;

import autoparts.dao.UserLocationDAO;
import autoparts.model.GeoPoint;
import autoparts.model.NearbyPartner;
import autoparts.model.Partner;
import autoparts.model.Status;
import autoparts.model.User;
import autoparts.util.GeoUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Sabe desde dónde busca cada cliente, para ordenar almacenes y talleres
 * por cercanía.
 *
 * Si el cliente nunca eligió su ubicación en el mapa, se usa la Universidad
 * Popular del Cesar como punto de referencia.
 *
 * @author Jahdiel Torres
 */
public class UserLocationService {

    /**
     * Punto por defecto: Universidad Popular del Cesar, sede Sabanas del Valle
     * (Valledupar). Coordenadas aproximadas: se ajustan aquí si hace falta.
     */
    public static final GeoPoint DEFAULT_LOCATION = new GeoPoint(10.4500, -73.2600);
    public static final String DEFAULT_LOCATION_NAME = "Universidad Popular del Cesar";

    private final UserLocationDAO locationDAO;

    /**
     * @param locationDAO dónde se guardan las ubicaciones
     */
    public UserLocationService(UserLocationDAO locationDAO) {
        this.locationDAO = locationDAO;
    }

    /**
     * Ubicación del usuario: la que eligió o, si no eligió, la de la universidad.
     *
     * @param user usuario de la sesión
     * @return el punto desde el que se miden las distancias
     */
    public GeoPoint locationOf(User user) {
        return locationDAO.find(user.getId()).orElse(DEFAULT_LOCATION);
    }

    /**
     * Indica si el usuario ya eligió su ubicación en el mapa.
     */
    public boolean hasChosen(User user) {
        return locationDAO.find(user.getId()).isPresent();
    }

    /**
     * Guarda la ubicación que el usuario eligió en el mapa.
     *
     * @param user     usuario de la sesión
     * @param location punto elegido
     */
    public void choose(User user, GeoPoint location) {
        locationDAO.save(user.getId(), location);
    }

    /**
     * Ordena los socios activos del más cercano al más lejano respecto a la
     * ubicación del usuario.
     *
     * @param user     usuario de la sesión
     * @param partners almacenes y talleres
     * @return cada socio con su distancia, ordenados por cercanía
     */
    public List<NearbyPartner> sortByDistance(User user, List<? extends Partner> partners) {
        GeoPoint from = locationOf(user);
        List<NearbyPartner> result = new ArrayList<>();
        for (Partner partner : partners) {
            if (partner.getStatus() == Status.ACTIVO) {
                GeoPoint to = new GeoPoint(partner.getLatitude(), partner.getLongitude());
                result.add(new NearbyPartner(partner, GeoUtil.distanceKm(from, to)));
            }
        }
        result.sort(Comparator.comparingDouble(NearbyPartner::distanceKm));
        return result;
    }
}
