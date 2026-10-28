package autoparts.model;

import autoparts.util.FileUtil;

/**
 * Representa el componente de transmisión de un vehículo, detallando su tipo y número de velocidades.
 * 
 * @author ALVARO
 */
public class Transmission extends Component {

    private TransmissionType transmissionType;
    private int numberOfSpeeds;

    /**
     * Crea una nueva instancia de Transmission con los valores especificados.
     * 
     * @param type tipo del componente
     * @param id identificador de producto componente
     * @param name nombre descriptivo del componente
     * @param reference referencia de fabricante
     * @param brand marca del componente
     * @param category categoría del componente
     * @param origin origen del componente
     * @param status estado cualitativo del componente
     * @param transmissionType el tipo de transmisión
     * @param numberOfSpeeds el número de velocidades
     */
    public Transmission(ComponentType type, int id, String name, String reference, Brand brand, Category category, String origin, String status, TransmissionType transmissionType, int numberOfSpeeds) {
        super(type, id, name, reference, brand, category, origin, status);
        this.transmissionType = transmissionType;
        this.numberOfSpeeds = numberOfSpeeds;
    }

    /**
     * Convierte el objeto en una línea de registro.
     * @return Devuelve la línea para guardar en components.csv.
     */
    @Override
    public String toLine() {
        return FileUtil.join(getType().getFileValue(), getId(), getName(), getReference(), getBrand().getId(),
                getCategory().getId(), getOrigin(), getStatus(), transmissionType.getFileValue(), numberOfSpeeds);
    }

    public TransmissionType getTransmissionType() {
        return transmissionType;
    }

    public void setTransmissionType(TransmissionType transmissionType) {
        this.transmissionType = transmissionType;
    }

    public int getNumberOfSpeeds() {
        return numberOfSpeeds;
    }

    public void setNumberOfSpeeds(int numberOfSpeeds) {
        this.numberOfSpeeds = numberOfSpeeds;
    }
}
