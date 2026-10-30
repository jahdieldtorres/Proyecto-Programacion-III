package autoparts.dao.file;

import autoparts.dao.PartnerDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Partner;
import autoparts.model.Supplier;
import autoparts.model.Workshop;
import autoparts.util.FileUtil;
import autoparts.util.FlatFiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class PartnerFileDAO implements PartnerDAO {

    private static final String PATH = FlatFiles.PARTNERS;

    @Override
    public int nextId() {
        return FileUtil.nextId(PATH, 1);
    }

    @Override
    public void save(Partner partner) {
        FileUtil.append(PATH, partner.toLine());
    }

    @Override
    public boolean update(Partner partner) {
        List<Partner> partners = findAll();
        for (int i = 0; i < partners.size(); i++) {
            if (partners.get(i).getId() == partner.getId()) {
                partners.set(i, partner);
                rewriteAll(partners);
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<Partner> findById(int id) {
        return findAll().stream().filter(p -> p.getId() == id).findFirst();
    }

    @Override
    public Optional<Partner> findByTaxId(String taxId) {
        if (taxId == null) {
            return Optional.empty();
        }
        return findAll().stream().filter(p -> p.getTaxId().equalsIgnoreCase(taxId.trim())).findFirst();
    }

    @Override
    public List<Partner> findAll() {
        List<Partner> partners = new ArrayList<>();
        for (String line : FileUtil.read(PATH)) {
            partners.add(fromLine(line));
        }
        return partners;
    }

    private Partner fromLine(String line) {
        try {
            String type = FileUtil.split(line)[0];
            return switch (type) {
                case Partner.TYPE_SUPPLIER -> new Supplier(line);
                case Partner.TYPE_WORKSHOP -> new Workshop(line);
                default -> throw new IllegalArgumentException("Tipo de socio desconocido: " + type);
            };
        } catch (RuntimeException e) {
            throw new PersistenceException("Registro inválido en " + PATH + ": " + line, e);
        }
    }

    private void rewriteAll(List<Partner> partners) {
        List<String> lines = new ArrayList<>();
        for (Partner partner : partners) {
            lines.add(partner.toLine());
        }
        FileUtil.rewrite(PATH, lines);
    }
}