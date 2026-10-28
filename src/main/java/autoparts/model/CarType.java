package autoparts.model;

/**
 *
 * @author ALVARO
 */
public enum CarType {
    CAMIONETA("CAMIONETA", "Camioneta"),
    HATCHBACK("HATCHBACK", "Hatchback"),
    SEDAN("SEDAN", "Sedan"),
    DEPORTIVO("DEPORTIVO", "Deportivo");

    private final String fileValue;
    private final String displayName;

    CarType(String fileValue, String displayName) {
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
    public static CarType fromFileValue(String value) {
        for (CarType t : values()) {
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
