package autoparts.dao.local;

import autoparts.dao.UserLocationDAO;
import autoparts.model.GeoPoint;
import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * Guarda la ubicación de cada usuario en las preferencias del computador,
 * con la clase Preferences de Java (en Windows queda en el registro del
 * usuario). Cada cuenta tiene sus propias claves, por ejemplo "lat_5" y "lng_5".
 *
 * @author Jahdiel Torres
 */
public class UserLocationPreferencesDAO implements UserLocationDAO {

    private final Preferences preferences = Preferences.userRoot().node("origen/ubicaciones");

    @Override
    public Optional<GeoPoint> find(int userId) {
        String latitude = preferences.get(latitudeKey(userId), null);
        String longitude = preferences.get(longitudeKey(userId), null);
        if (latitude == null || longitude == null) {
            return Optional.empty();
        }
        return Optional.of(new GeoPoint(Double.parseDouble(latitude), Double.parseDouble(longitude)));
    }

    @Override
    public void save(int userId, GeoPoint location) {
        preferences.put(latitudeKey(userId), String.valueOf(location.latitude()));
        preferences.put(longitudeKey(userId), String.valueOf(location.longitude()));
    }

    private String latitudeKey(int userId) {
        return "lat_" + userId;
    }

    private String longitudeKey(int userId) {
        return "lng_" + userId;
    }
}
