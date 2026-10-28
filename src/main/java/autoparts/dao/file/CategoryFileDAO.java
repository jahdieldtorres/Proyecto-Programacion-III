package autoparts.dao.file;

import autoparts.dao.CategoryDAO;
import autoparts.model.Category;
import autoparts.util.FileUtil;
import autoparts.util.FlatFiles;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación basada en archivos planos para el acceso a datos de categorías.
 * 
 * @author ALVARO
 */
public class CategoryFileDAO implements CategoryDAO {

    @Override
    public List<Category> listAll() {
        List<Category> categories = new ArrayList<>();
        for (String[] fields : FileUtil.readFields(FlatFiles.CATEGORIES)) {
            if (fields.length >= 2) {
                int id = Integer.parseInt(fields[0].trim());
                String name = fields[1].trim();
                categories.add(new Category(id, name));
            }
        }
        return categories;
    }

    @Override
    public Category findById(int id) {
        for (String[] fields : FileUtil.readFields(FlatFiles.CATEGORIES)) {
            if (fields.length >= 2 && Integer.parseInt(fields[0].trim()) == id) {
                String name = fields[1].trim();
                return new Category(id, name);
            }
        }
        return null;
    }

    @Override
    public void save(Category category) {
        int nextId = FileUtil.nextId(FlatFiles.CATEGORIES, 0);
        category.setId(nextId);
        FileUtil.append(FlatFiles.CATEGORIES, category.toLine());
    }

    @Override
    public void update(Category category) {
        List<String> lines = new ArrayList<>();
        for (String[] fields : FileUtil.readFields(FlatFiles.CATEGORIES)) {
            if (fields.length >= 2 && Integer.parseInt(fields[0].trim()) == category.getId()) {
                lines.add(category.toLine());
            } else {
                lines.add(FileUtil.join((Object[]) fields));
            }
        }
        FileUtil.rewrite(FlatFiles.CATEGORIES, lines);
    }

    @Override
    public void delete(int id) {
        List<String> lines = new ArrayList<>();
        for (String[] fields : FileUtil.readFields(FlatFiles.CATEGORIES)) {
            if (fields.length >= 2 && Integer.parseInt(fields[0].trim()) == id) {
                continue;
            } else {
                lines.add(FileUtil.join((Object[]) fields));
            }
        }
        FileUtil.rewrite(FlatFiles.CATEGORIES, lines);
    }
}
