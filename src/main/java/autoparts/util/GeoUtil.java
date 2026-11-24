package autoparts.util;

import autoparts.model.GeoPoint;

/**
 * Cálculos con puntos del mapa.
 *
 * @author Jahdiel Torres
 */
public final class GeoUtil {

    /** Radio promedio de la Tierra en kilómetros. */
    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoUtil() {
    }

    /**
     * Distancia en línea recta entre dos puntos, con la fórmula de Haversine.
     * Tiene en cuenta que la Tierra es redonda, por eso no basta con Pitágoras.
     *
     * @param from punto de partida
     * @param to   punto de llegada
     * @return distancia en kilómetros
     */
    public static double distanceKm(GeoPoint from, GeoPoint to) {
        double latFrom = Math.toRadians(from.latitude());
        double latTo = Math.toRadians(to.latitude());
        double deltaLat = latTo - latFrom;
        double deltaLon = Math.toRadians(to.longitude() - from.longitude());

        double a = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(latFrom) * Math.cos(latTo) * Math.pow(Math.sin(deltaLon / 2), 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(a));
    }
}
