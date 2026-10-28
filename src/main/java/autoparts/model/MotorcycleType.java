package autoparts.model;

/**
 *
 * @author ALVARO
 */
public enum MotorcycleType {
    SCOOTER("SCOOTER", "Scooter"),
    NAKED("NAKED", "Naked"),
    DEPORTIVAS("DEPORTIVAS","Deportivas");

    private final String fileValue;
    private final String displayName;

    MotorcycleType(String fileValue, String displayName) {
        this.fileValue = fileValue;
        this.displayName = displayName;
    }

    /**
     * Texto que se guarda en el archivo.
     */
    public String getFileValue() {
        return fileValue;
    }

    /**
     * Texto que ve el usuario en pantalla.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Devuelve el tipo que corresponde al texto leído del archivo.
     */
    public static MotorcycleType fromFileValue(String value) {
        for (MotorcycleType t : values()) {
            if (t.fileValue.equals(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Tipo de caja desconocido: " + value);
    }

    /**
     * Lo que se muestra en un ComboBox.
     */
    @Override
    public String toString() {
        return displayName;
    }
}
