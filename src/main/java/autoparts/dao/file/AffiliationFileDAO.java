package autoparts.dao.file;

import autoparts.dao.AffiliationDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Affiliation;
import autoparts.util.FileUtil;
import autoparts.util.FlatFiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de {@link AffiliationDAO} con archivos planos: guarda las
 * afiliaciones en {@code affiliations.csv}, una línea por afiliación. Es la
 * única clase que toca ese archivo.
 *
 * Cuando se pase a base de datos, esta clase se reemplaza por otra que
 * implemente la misma interfaz y los servicios no cambian.
 *
 * @author Héctor Guevara
 */
public class AffiliationFileDAO implements AffiliationDAO {

    private static final String PATH = FlatFiles.AFFILIATIONS;

    @Override
    public int nextId() {
        return FileUtil.nextId(PATH, 0);
    }

    @Override
    public void save(Affiliation affiliation) {
        FileUtil.append(PATH, affiliation.toLine());
    }

    @Override
    public boolean update(Affiliation affiliation) {
        List<Affiliation> affiliations = findAll();
        for (int i = 0; i < affiliations.size(); i++) {
            if (affiliations.get(i).getId() == affiliation.getId()) {
                affiliations.set(i, affiliation);
                rewriteAll(affiliations);
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<Affiliation> findById(int id) {
        return findAll().stream().filter(a -> a.getId() == id).findFirst();
    }

    @Override
    public List<Affiliation> findByWorkshop(int workshopId) {
        return findAll().stream().filter(a -> a.getWorkshopId() == workshopId).toList();
    }

    @Override
    public List<Affiliation> findBySupplier(int supplierId) {
        return findAll().stream().filter(a -> a.getSupplierId() == supplierId).toList();
    }

    @Override
    public List<Affiliation> findAll() {
        List<Affiliation> affiliations = new ArrayList<>();
        for (String line : FileUtil.read(PATH)) {
            try {
                affiliations.add(new Affiliation(line));
            } catch (RuntimeException e) {
                throw new PersistenceException("Registro inválido en " + PATH + ": " + line, e);
            }
        }
        return affiliations;
    }

    private void rewriteAll(List<Affiliation> affiliations) {
        List<String> lines = new ArrayList<>();
        for (Affiliation affiliation : affiliations) {
            lines.add(affiliation.toLine());
        }
        FileUtil.rewrite(PATH, lines);
    }
}