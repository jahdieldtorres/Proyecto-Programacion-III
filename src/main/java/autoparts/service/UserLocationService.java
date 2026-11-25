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
 * El invitado no tiene usuario (llega null): su ubicación se guarda solo en
 * memoria y se olvida al terminar la sesión, sin tocar el archivo de
 * preferencias del computador.
 *
 * @author Jahdiel Torres
 * @author Héctor Guevara (soporte para invitados)
 */
public class UserLocationService {

    /**
     * Punto por defecto: Universidad Popular del Cesar, sede Sabanas del Valle
     * (Valledupar). Coordenadas aproximadas: se ajustan aquí si hace falta.
     */
    public static final GeoPoint DEFAULT_LOCATION = new GeoPoint(10.4500, -73.2600);
    public static final String DEFAULT_LOCATION_NAME = "Universidad Popular del Cesar";

    private final UserLocationDAO locationDAO;

    /** Ubicación que eligió el invitado en el mapa. Solo vive en memoria. */
    private GeoPoint guestLocation;

    /**
     * @param locationDAO dónde se guardan las ubicaciones
     */
    public UserLocationService(UserLocationDAO locationDAO) {
        this.locationDAO = locationDAO;
    }

    /**
     * Ubicación del usuario: la que eligió o, si no eligió, la de la universidad.
     *
     * @param user usuario de la sesión, o null si es invitado
     * @return el punto desde el que se miden las distancias
     */
    public GeoPoint locationOf(User user) {
        if (user == null) {
            return guestLocation != null ? guestLocation : DEFAULT_LOCATION;
        }
        return locationDAO.find(user.getId()).orElse(DEFAULT_LOCATION);
    }

    /**
     * Indica si el usuario ya eligió su ubicación en el mapa.
     */
    public boolean hasChosen(User user) {
        if (user == null) {
            return guestLocation != null;
        }
        return locationDAO.find(user.getId()).isPresent();
    }

    /**
     * Guarda la ubicación que el usuario eligió en el mapa.
     *
     * Si es invitado (user es null), solo se recuerda mientras dure la sesión.
     *
     * @param user     usuario de la sesión, o null si es invitado
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
     * Olvida la ubicación del invitado anterior. Se llama al entrar como
     * invitado, para que cada visita empiece desde la universidad.
     */
    public void forgetGuestLocation() {
        guestLocation = null;
    }

    /**
     * Ordena los socios activos del más cercano al más lejano respecto a la
     * ubicación del usuario.
     *
     * @param user     usuario de la sesión, o null si es invitado
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
