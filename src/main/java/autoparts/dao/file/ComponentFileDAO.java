package autoparts.dao.file;

import autoparts.dao.ComponentDAO;
import autoparts.model.*;
import autoparts.util.FileUtil;
import autoparts.util.FlatFiles;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author ALVARO
 */
public class ComponentFileDAO implements ComponentDAO {

    @Override
    public List<Component> listAll() {
        List<Component> components = new ArrayList<>();
        for (String[] fields : FileUtil.readFields(FlatFiles.COMPONENTS)) {
            if (fields.length >= 8) {
                String status = fields[7].trim();
                if (!"INACTIVO".equalsIgnoreCase(status)) {
                    components.add(parseComponent(fields));
                }
            }
        }
        return components;
    }

    @Override
    public Component findById(Long id) {
        for (String[] fields : FileUtil.readFields(FlatFiles.COMPONENTS)) {
            if (fields.length >= 2 && Long.valueOf(fields[1].trim()).equals(id)) {
                String status = fields[7].trim();
                if (!"INACTIVO".equalsIgnoreCase(status)) {
                    return parseComponent(fields);
                }
            }
        }
        return null;
    }

    @Override
    public void save(Component component) {
        int nextId = FileUtil.nextId(FlatFiles.COMPONENTS, 1);
        component.setId(nextId);
        if (component.getStatus() == null || component.getStatus().isBlank()) {
            component.setStatus("ACTIVO");
        }
        FileUtil.append(FlatFiles.COMPONENTS, component.toLine());
    }

    @Override
    public void update(Component component) {
        List<String> lines = new ArrayList<>();
        for (String[] fields : FileUtil.readFields(FlatFiles.COMPONENTS)) {
            if (fields.length >= 2 && Long.valueOf(fields[1].trim()).equals((long) component.getId())) {
                lines.add(component.toLine());
            } else {
                lines.add(FileUtil.join((Object[]) fields));
            }
        }
        FileUtil.rewrite(FlatFiles.COMPONENTS, lines);
    }

    @Override
    public void delete(Long id) {
        List<String> lines = new ArrayList<>();
        for (String[] fields : FileUtil.readFields(FlatFiles.COMPONENTS)) {
            if (fields.length >= 2 && Long.valueOf(fields[1].trim()).equals(id)) {
                fields[7] = "INACTIVO";
                lines.add(FileUtil.join((Object[]) fields));
            } else {
                lines.add(FileUtil.join((Object[]) fields));
            }
        }
        FileUtil.rewrite(FlatFiles.COMPONENTS, lines);
    }

    private Component parseComponent(String[] fields) {
        String typeStr = fields[0].trim();
        int id = Integer.parseInt(fields[1].trim());
        String name = fields[2].trim();
        String reference = fields[3].trim();
        
        Brand brand = new Brand();
        brand.setId(Integer.parseInt(fields[4].trim()));
        
        Category category = new Category();
        category.setId(Integer.parseInt(fields[5].trim()));
        
        String origin = fields[6].trim();
        String status = fields[7].trim();

        ComponentType type = ComponentType.fromFileValue(typeStr);
        if (type == null) {
            type = ComponentType.GENERAL;
        }

        switch (type) {
            case ENGINE:
                int displacement = fields.length > 8 ? Integer.parseInt(fields[8].trim()) : 0;
                int cylinders = fields.length > 9 ? Integer.parseInt(fields[9].trim()) : 0;
                FuelType fuel = fields.length > 10 ? FuelType.fromFileValue(fields[10].trim()) : null;
                int hp = fields.length > 11 ? Integer.parseInt(fields[11].trim()) : 0;
                return new Engine(id, name, reference, brand, category, origin, status, displacement, cylinders, fuel, hp);
            
            case RIM:
                int diameter = fields.length > 8 ? Integer.parseInt(fields[8].trim()) : 0;
                double width = fields.length > 9 ? Double.parseDouble(fields[9].trim()) : 0.0;
                String pcd = fields.length > 10 ? fields[10].trim() : "";
                RimMaterial material = fields.length > 11 ? RimMaterial.fromFileValue(fields[11].trim()) : null;
                return new Rim(id, name, reference, brand, category, origin, status, diameter, width, pcd, material);
            
            case TIRE:
                int widthMm = fields.length > 8 ? Integer.parseInt(fields[8].trim()) : 0;
                int profile = fields.length > 9 ? Integer.parseInt(fields[9].trim()) : 0;
                int tireDia = fields.length > 10 ? Integer.parseInt(fields[10].trim()) : 0;
                int loadIdx = fields.length > 11 ? Integer.parseInt(fields[11].trim()) : 0;
                String speedIdx = fields.length > 12 ? fields[12].trim() : "";
                return new Tire(id, name, reference, brand, category, origin, status, widthMm, profile, tireDia, loadIdx, speedIdx);
            
            case TRANSMISSION:
                TransmissionType transType = fields.length > 8 ? TransmissionType.fromFileValue(fields[8].trim()) : null;
                int speeds = fields.length > 9 ? Integer.parseInt(fields[9].trim()) : 0;
                return new Transmission(id, name, reference, brand, category, origin, status, transType, speeds);
            
            default:
                return new Component(type, id, name, reference, brand, category, origin, status);
        }
    }
}