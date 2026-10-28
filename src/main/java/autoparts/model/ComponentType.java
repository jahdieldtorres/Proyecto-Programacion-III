package autoparts.model;

/**
 * Tipo de componente. Cada constante guarda el texto que se escribe en
 * el primer campo de components.csv.
 *
 * @author alvaro
 */
public enum ComponentType {

    GENERAL("GENERAL"),
    TIRE("LLANTA"),
    RIM("RIN"),
    ENGINE("MOTOR"),
    TRANSMISSION("TRANSMISION");

    private final String fileValue;

    ComponentType(String fileValue) {
        this.fileValue = fileValue;
    }

    /** Texto que se guarda en el archivo. */
    public String getFileValue() {
        return fileValue;
    }

    /** Devuelve el tipo que corresponde al texto leído del archivo. */
    public static ComponentType fromFileValue(String value) {
        for (ComponentType t : values()) {
            if (t.fileValue.equals(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Tipo de componente desconocido: " + value);
    }
}
