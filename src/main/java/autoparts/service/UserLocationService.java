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
 * Sabe desde d¢nde busca cada cliente, para ordenar almacenes y talleres
 * por cercan¡a.
 *
 * Si el cliente nunca eligi¢ su ubicaci¢n en el mapa, se usa la Universidad
 * Popular del Cesar como punto de referencia.
 *
 * El invitado no tiene usuario (llega null): su ubicaci¢n se guarda solo en
 * memoria y se olvida al terminar la sesi¢n, sin tocar el archivo de
 * preferencias del computador.
 *
 * @author Jahdiel Torres
 * @author H‚ctor Guevara (soporte para invitados)
 */
public class UserLocationService {

    /**
     * Punto por defecto: Universidad Popular del Cesar, sede Sabanas del Valle
     * (Valledupar). Coordenadas aproximadas: se ajustan aqu¡ si hace falta.
     */
    public static final GeoPoint DEFAULT_LOCATION = new GeoPoint(10.4500, -73.2600);
    public static final String DEFAULT_LOCATION_NAME = "Universidad Popular del Cesar";

    private final UserLocationDAO locationDAO;

    /** Ubicaci¢n que eligi¢ el invitado en el mapa. Solo vive en memoria. */
    private GeoPoint guestLocation;

    /**
     * @param locationDAO d¢nde se guardan las ubicaciones
     */
    public UserLocationService(UserLocationDAO locationDAO) {
        this.locationDAO = locationDAO;
    }

    /**
     * Ubicaci¢n del usuario: la que eligi¢ o, si no eligi¢, la de la universidad.
     *
     * @param user usuario de la sesi¢n, o null si es invitado
     * @return el punto desde el que se miden las distancias
     */
    public GeoPoint locationOf(User user) {
        if (user == null) {
            return guestLocation != null ? guestLocation : DEFAULT_LOCATION;
        }
        return locationDAO.find(user.getId()).orElse(DEFAULT_LOCATION);
    }

    /**
     * Indica si el usuario ya eligi¢ su ubicaci¢n en el mapa.
     */
    public boolean hasChosen(User user) {
        if (user == null) {
            return guestLocation != null;
        }
        return locationDAO.find(user.getId()).isPresent();
    }

    /**
     * Guarda la ubicaci¢n que el usuario eligi¢ en el mapa.
     *
     * Si es invitado (user es null), solo se recuerda mientras dure la sesi¢n.
     *
     * @param user     usuario de la sesi¢n, o null si es invitado
     * @param location punto elegido
     */
    public void choose(User user, GeoPoint location) {
        if (user == null) {
            guestLocation = location;
            return;
        }
        locationDAO.save(user.getId(), location);
    }

    /**
     * Olvida la ubicaci¢n del invitado anterior. Se llama al entrar como
     * invitado, para que cada visita empiece desde la universidad.
     */
    public void forgetGuestLocation() {
        guestLocation = null;
    }

    /**
     * Ordena los socios activos del m s cercano al m s lejano respecto a la
     * ubicaci¢n del usuario.
     *
     * @param user     usuario de la sesi¢n, o null si es invitado
     * @param partners almacenes y talleres
     * @return cada socio con su distancia, ordenados por cercan¡a
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
