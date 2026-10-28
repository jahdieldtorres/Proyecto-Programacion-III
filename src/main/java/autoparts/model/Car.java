package autoparts.model;

import autoparts.util.FileUtil;

/**
 * Representa un vehículo de tipo automóvil en el sistema.
 * 
 * @author ALVARO
 */
public class Car extends Vehicle {

    private int numSeats;
    private CarType type;

    /**
     * Crea una nueva instancia de Car con los valores especificados.
     * 
     * @param vehicleType el tipo general del vehículo
     * @param id el identificador del vehículo
     * @param brand la marca del vehículo
     * @param modelName el nombre del modelo del vehículo
     * @param yearRelease el año de lanzamiento del vehículo
     * @param status el estado cualitativo del vehículo
     * @param numSeats el número de asientos del automóvil
     * @param type el tipo específico del automóvil
     */
    public Car(VehicleType vehicleType, String id, Brand brand, String modelName, int yearRelease, String status, int numSeats, CarType type) {
        super(vehicleType.COCHE, id, brand, modelName, yearRelease, status);
        this.numSeats = numSeats;
        this.type = type;
    }

    /**
     * Convierte el objeto en una línea de registro.
     * @return Devuelve la línea para guardar en el archivo.
     */
    @Override
    public String toLine() {
        return FileUtil.join(getType().getFileValue(), getId(), getBrand().getName(), getModelName(), getYearRelease(), getStatus(), numSeats, type.getFileValue());
    }

    public int getNumSeats() {
        return numSeats;
    }

    public void setNumSeats(int numSeats) {
        this.numSeats = numSeats;
    }

    public CarType getCarType() {
        return type;
    }

    public void setCarType(CarType type) {
        this.type = type;
    }
}