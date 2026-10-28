package autoparts.dao.file;

import autoparts.dao.VehicleDAO;
import autoparts.model.*;
import autoparts.util.FileUtil;
import autoparts.util.FlatFiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación basada en archivos planos para el acceso a datos de vehículos.
 * 
 * @author ALVARO
 */
public class VehicleFileDAO implements VehicleDAO {

    @Override
    public List<Vehicle> listAll() {
        Map<String, Brand> brandsMap = loadBrandsByName();
        List<Vehicle> vehicles = new ArrayList<>();
        for (String[] fields : FileUtil.readFields(FlatFiles.VEHICLES)) {
            if (fields.length >= 6) {
                vehicles.add(parseVehicle(fields, brandsMap));
            }
        }
        return vehicles;
    }

    @Override
    public Vehicle findById(int id) {
        Map<String, Brand> brandsMap = loadBrandsByName();
        for (String[] fields : FileUtil.readFields(FlatFiles.VEHICLES)) {
            if (fields.length >= 2 && Integer.parseInt(fields[1].trim()) == id) {
                return parseVehicle(fields, brandsMap);
            }
        }
        return null;
    }

    @Override
    public void save(Vehicle vehicle) {
        int nextId = FileUtil.nextId(FlatFiles.VEHICLES, 1);
        vehicle.setId(nextId);
        if (vehicle.getStatus() == null || vehicle.getStatus().isBlank()) {
            vehicle.setStatus("ACTIVO");
        }
        FileUtil.append(FlatFiles.VEHICLES, vehicle.toLine());
    }

    @Override
    public void update(Vehicle vehicle) {
        List<String> lines = new ArrayList<>();
        for (String[] fields : FileUtil.readFields(FlatFiles.VEHICLES)) {
            if (fields.length >= 2 && Integer.parseInt(fields[1].trim()) == vehicle.getId()) {
                lines.add(vehicle.toLine());
            } else {
                lines.add(FileUtil.join((Object[]) fields));
            }
        }
        FileUtil.rewrite(FlatFiles.VEHICLES, lines);
    }

    @Override
    public void delete(int id) {
        List<String> lines = new ArrayList<>();
        for (String[] fields : FileUtil.readFields(FlatFiles.VEHICLES)) {
            if (fields.length >= 2 && Integer.parseInt(fields[1].trim()) == id) {
                fields[5] = "INACTIVO";
                lines.add(FileUtil.join((Object[]) fields));
            } else {
                lines.add(FileUtil.join((Object[]) fields));
            }
        }
        FileUtil.rewrite(FlatFiles.VEHICLES, lines);
    }

    private Map<String, Brand> loadBrandsByName() {
        Map<String, Brand> map = new HashMap<>();
        for (String[] fields : FileUtil.readFields(FlatFiles.BRANDS)) {
            if (fields.length >= 2) {
                int id = Integer.parseInt(fields[0].trim());
                String name = fields[1].trim();
                map.put(name, new Brand(id, name));
            }
        }
        return map;
    }

    private Vehicle parseVehicle(String[] fields, Map<String, Brand> brandsMap) {
        String typeStr = fields[0].trim();
        int id = Integer.valueOf(fields[1].trim());
        String brandName = fields[2].trim();
        Brand brand = brandsMap.getOrDefault(brandName, new Brand(0, brandName));
        String modelName = fields[3].trim();
        int yearRelease = Integer.parseInt(fields[4].trim());
        String status = fields[5].trim();

        VehicleType vType = VehicleType.fromFileValue(typeStr);

        switch (vType) {
            case COCHE:
                int numSeats = fields.length > 6 ? Integer.parseInt(fields[6].trim()) : 0;
                CarType carType = fields.length > 7 ? CarType.fromFileValue(fields[7].trim()) : null;
                return new Car(vType, id, brand, modelName, yearRelease, status, numSeats, carType);
            
            case MOTOCICLETA:
                int cylinders = fields.length > 6 ? Integer.parseInt(fields[6].trim()) : 0;
                MotorcycleType motoType = fields.length > 7 ? MotorcycleType.fromFileValue(fields[7].trim()) : null;
                return new Motorcycle(vType, id, brand, modelName, yearRelease, status, cylinders, motoType);
            
            default:
                return new Car(vType, id, brand, modelName, yearRelease, status, 0, null);
        }
    }
}
