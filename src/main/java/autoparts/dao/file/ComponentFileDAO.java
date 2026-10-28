package autoparts.dao.file;

import autoparts.dao.ComponentDAO;
import autoparts.model.*;
import autoparts.util.FileUtil;
import autoparts.util.FlatFiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación basada en archivos planos para el acceso a datos de componentes.
 * 
 * @author 
 */
public class ComponentFileDAO implements ComponentDAO {

    @Override
    public List<Component> listAll() {
        Map<Integer, Brand> brandsMap = loadBrands();
        Map<Integer, Category> categoriesMap = loadCategories();
        List<Component> components = new ArrayList<>();
        
        for (String[] fields : FileUtil.readFields(FlatFiles.COMPONENTS)) {
            components.add(parseComponent(fields, brandsMap, categoriesMap));
        }
        return components;
    }

    @Override
    public Component findById(int id) {
        Map<Integer, Brand> brandsMap = loadBrands();
        Map<Integer, Category> categoriesMap = loadCategories();
        
        for (String[] fields : FileUtil.readFields(FlatFiles.COMPONENTS)) {
            if (Integer.parseInt(fields[1].trim()) == id) {
                return parseComponent(fields, brandsMap, categoriesMap);
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
            if (Integer.parseInt(fields[1].trim()) == component.getId()) {
                lines.add(component.toLine());
            } else {
                lines.add(FileUtil.join((Object[]) fields));
            }
        }
        FileUtil.rewrite(FlatFiles.COMPONENTS, lines);
    }

    @Override
    public void delete(int id) {
        List<String> lines = new ArrayList<>();
        for (String[] fields : FileUtil.readFields(FlatFiles.COMPONENTS)) {
            if (Integer.parseInt(fields[1].trim()) == id) {
                fields[7] = "INACTIVO";
                lines.add(FileUtil.join((Object[]) fields));
            } else {
                lines.add(FileUtil.join((Object[]) fields));
            }
        }
        FileUtil.rewrite(FlatFiles.COMPONENTS, lines);
    }

    private Map<Integer, Brand> loadBrands() {
        Map<Integer, Brand> map = new HashMap<>();
        for (String[] fields : FileUtil.readFields(FlatFiles.BRANDS)) {
            if (fields.length >= 2) {
                int id = Integer.parseInt(fields[0].trim());
                String name = fields[1].trim();
                Brand brand = new Brand(id, name);
                map.put(id, brand);
            }
        }
        return map;
    }

    private Map<Integer, Category> loadCategories() {
        Map<Integer, Category> map = new HashMap<>();
        for (String[] fields : FileUtil.readFields(FlatFiles.CATEGORIES)) {
            if (fields.length >= 2) {
                int id = Integer.parseInt(fields[0].trim());
                String name = fields[1].trim();
                Category category = new Category(id, name);
                map.put(id, category);
            }
        }
        return map;
    }

    private Component parseComponent(String[] fields, Map<Integer, Brand> brandsMap, Map<Integer, Category> categoriesMap) {
        String typeStr = fields[0].trim();
        int id = Integer.parseInt(fields[1].trim());
        String name = fields[2].trim();
        String reference = fields[3].trim();
        
        int brandId = Integer.parseInt(fields[4].trim());
        Brand brand = brandsMap.getOrDefault(brandId, new Brand(brandId, "Desconocida"));
        
        int categoryId = Integer.parseInt(fields[5].trim());
        Category category = categoriesMap.getOrDefault(categoryId, new Category(categoryId, "Desconocida"));
        
        String origin = fields[6].trim();
        String status = fields[7].trim();

        ComponentType type = ComponentType.fromFileValue(typeStr);

        switch (type) {
            case ENGINE:
                int displacement = Integer.parseInt(fields[8].trim());
                int cylinders = Integer.parseInt(fields[9].trim());
                FuelType fuel = FuelType.fromFileValue(fields[10].trim());
                int hp = Integer.parseInt(fields[11].trim());
                return new Engine(id, name, reference, brand, category, origin, status, displacement, cylinders, fuel, hp);
            
            case RIM:
                int diameter = Integer.parseInt(fields[8].trim());
                double width = Double.parseDouble(fields[9].trim());
                String pcd = fields[10].trim();
                RimMaterial material = RimMaterial.fromFileValue(fields[11].trim());
                return new Rim(id, name, reference, brand, category, origin, status, diameter, width, pcd, material);
            
            case TIRE:
                int widthMm = Integer.parseInt(fields[8].trim());
                int profile = Integer.parseInt(fields[9].trim());
                int tireDia = Integer.parseInt(fields[10].trim());
                int loadIdx = Integer.parseInt(fields[11].trim());
                String speedIdx = fields[12].trim();
                return new Tire(id, name, reference, brand, category, origin, status, widthMm, profile, tireDia, loadIdx, speedIdx);
            
            case TRANSMISSION:
                TransmissionType transType = TransmissionType.fromFileValue(fields[8].trim());
                int speeds = Integer.parseInt(fields[9].trim());
                return new Transmission(id, name, reference, brand, category, origin, status, transType, speeds);
            
            default:
                return new Component(type, id, name, reference, brand, category, origin, status);
        }
    }
}