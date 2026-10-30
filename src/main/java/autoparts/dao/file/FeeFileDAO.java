package autoparts.dao.file;

import autoparts.dao.FeeDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Fee;
import autoparts.util.FileUtil;
import autoparts.util.FlatFiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;



public class FeeFileDAO implements FeeDAO {

    private static final String PATH = FlatFiles.FEES;

    @Override
    public int nextId() {
        return FileUtil.nextId(PATH, 0);
    }

    @Override
    public void save(Fee fee) {
        FileUtil.append(PATH, fee.toLine());
    }

    @Override
    public boolean update(Fee fee) {
        List<Fee> fees = findAll();
        for (int i = 0; i < fees.size(); i++) {
            if (fees.get(i).getId() == fee.getId()) {
                fees.set(i, fee);
                rewriteAll(fees);
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<Fee> findById(int id) {
        return findAll().stream().filter(f -> f.getId() == id).findFirst();
    }

    @Override
    public List<Fee> findByService(int serviceId) {
        return findAll().stream().filter(f -> f.getServiceId() == serviceId).toList();
    }

    @Override
    public List<Fee> findAll() {
        List<Fee> fees = new ArrayList<>();
        for (String line : FileUtil.read(PATH)) {
            try {
                fees.add(new Fee(line));
            } catch (RuntimeException e) {
                throw new PersistenceException("Registro inválido en " + PATH + ": " + line, e);
            }
        }
        return fees;
    }

    private void rewriteAll(List<Fee> fees) {
        List<String> lines = new ArrayList<>();
        for (Fee fee : fees) {
            lines.add(fee.toLine());
        }
        FileUtil.rewrite(PATH, lines);
    }
}