package autoparts.model;

import autoparts.util.FileUtil;

/**
 * Representa el componente de transmisiÃ³n de un vehÃ­culo, detallando su tipo y nÃºmero de velocidades.
 * 
 * @author ALVARO
 */
public class Transmission extends Component {

    private TransmissionType transmissionType;
    private int numberOfSpeeds;

    /**
     * Crea una nueva instancia de Transmission con los valores especificados.
     * 
     * @param id identificador de producto componente
     * @param name nombre descriptivo del componente
     * @param reference referencia de fabricante
     * @param brand marca del componente
     * @param category categorÃ­a del componente
     * @param origin origen del componente
     * @param status estado cualitativo del componente
     * @param transmissionType el tipo de transmisiÃ³n
     * @param numberOfSpeeds el nÃºmero de velocidades
     */
    public Transmission(int id, String name, String reference, Brand brand, Category category, String origin, String status, TransmissionType transmissionType, int numberOfSpeeds) {
        super(ComponentType.TRANSMISSION, id, name, reference, brand, category, origin, status);
        this.transmissionType = transmissionType;
        this.numberOfSpeeds = numberOfSpeeds;
    }

    /**
     * Convierte el objeto en una lÃ­nea de registro.
     * @return Devuelve la lÃ­nea para guardar en components.csv.
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
