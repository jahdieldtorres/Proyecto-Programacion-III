package autoparts.dao.file;

import autoparts.dao.BrandDAO;
import autoparts.model.Brand;
import autoparts.util.FileUtil;
import autoparts.util.FlatFiles;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación basada en archivos planos para el acceso a datos de marcas.
 * 
 * @author ALVARO
 */
public class BrandFileDAO implements BrandDAO {

    @Override
    public List<Brand> listAll() {
        List<Brand> brands = new ArrayList<>();
        for (String[] fields : FileUtil.readFields(FlatFiles.BRANDS)) {
            if (fields.length >= 2) {
                int id = Integer.parseInt(fields[0].trim());
                String name = fields[1].trim();
                brands.add(new Brand(id, name));
            }
        }
        return brands;
    }

    @Override
    public Brand findById(int id) {
        for (String[] fields : FileUtil.readFields(FlatFiles.BRANDS)) {
            if (fields.length >= 2 && Integer.parseInt(fields[0].trim()) == id) {
                String name = fields[1].trim();
                return new Brand(id, name);
            }
        }
        return null;
    }

    @Override
    public void save(Brand brand) {
        int nextId = FileUtil.nextId(FlatFiles.BRANDS, 0);
        brand.setId(nextId);
        FileUtil.append(FlatFiles.BRANDS, brand.toLine());
    }

    @Override
    public void update(Brand brand) {
        List<String> lines = new ArrayList<>();
        for (String[] fields : FileUtil.readFields(FlatFiles.BRANDS)) {
            if (fields.length >= 2 && Integer.parseInt(fields[0].trim()) == brand.getId()) {
                lines.add(brand.toLine());
            } else {
                lines.add(FileUtil.join((Object[]) fields));
            }
        }
        FileUtil.rewrite(FlatFiles.BRANDS, lines);
    }

    @Override
    public void delete(int id) {
        List<String> lines = new ArrayList<>();
        for (String[] fields : FileUtil.readFields(FlatFiles.BRANDS)) {
            if (fields.length >= 2 && Integer.parseInt(fields[0].trim()) == id) {
                // Las marcas simples no manejan estado explícito en el formato básico, 
                // se puede omitir o reescribir si aplica. Manteniendo la estructura estándar:
                continue; 
            } else {
                lines.add(FileUtil.join((Object[]) fields));
            }
        }
        FileUtil.rewrite(FlatFiles.BRANDS, lines);
    }
}
