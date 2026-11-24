package autoparts.model;

/**
 * Un punto en el mapa: latitud y longitud en grados.
 *
 * Es un record de Java: una clase pequeña que solo guarda datos y que Java
 * completa sola con su constructor, sus getters (latitude() y longitude()),
 * equals y toString.
 *
 * @author Jahdiel Torres
 * @param latitude  latitud en grados (positiva al norte)
 * @param longitude longitud en grados (negativa al oeste)
 */
public record GeoPoint(double latitude, double longitude) {
}
