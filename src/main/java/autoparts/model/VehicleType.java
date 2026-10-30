package autoparts.model;

/**
 * Tipo de caja de una transmisión. Cada constante guarda el texto que se
 * escribe en el campo tipo_caja de components.csv.
 *
 * @author ALVARO
 */
public enum VehicleType {

    MOTOCICLETA("MOTOCICLETA", "Motocicleta"),
    COCHE("COCHE", "Coche");

    private final String fileValue;
    private final String displayName;

    VehicleType(String fileValue, String displayName) {
        this.fileValue = fileValue;
        this.displayName = displayName;
    }

    /** Texto que se guarda en el archivo. */
    public String getFileValue() {
        return fileValue;
    }

    /** Texto que ve el usuario en pantalla. */
    public String getDisplayName() {
        return displayName;
    }

    /** Devuelve el tipo que corresponde al texto leído del archivo. */
    public static VehicleType fromFileValue(String value) {
        for (VehicleType t : values()) {
            if (t.fileValue.equals(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Tipo de caja desconocido: " + value);
    }

    /** Lo que se muestra en un ComboBox. */
    @Override
    public String toString() {
        return displayName;
    }
}
