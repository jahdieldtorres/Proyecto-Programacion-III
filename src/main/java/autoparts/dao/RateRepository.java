
package autoparts.dao;

import autoparts.model.Rate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RateRepository {

    private static final Path FILE = Paths.get("data", "tarifas.csv");
    private static final String HEADER = "rateId,name,percentage";

    public RateRepository() {
        try {
            Files.createDirectories(FILE.getParent());
            if (Files.notExists(FILE)) {
                Files.write(FILE, List.of(HEADER), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize rates file.", e);
        }
    }

    public void save(Rate rate) {
        List<Rate> rates = findAll();
        if (findById(rate.getRateId()).isPresent()) {
            throw new IllegalArgumentException("Rate ID already exists.");
        }
        if (findByName(rate.getName()).isPresent()) {
            throw new IllegalArgumentException("Rate name already exists.");
        }
        rates.add(rate);
        writeAll(rates);
    }

    public Optional<Rate> findById(int id) {
        return findAll().stream().filter(r -> r.getRateId() == id).findFirst();
    }

    public Optional<Rate> findByName(String name) {
        if (name == null) return Optional.empty();
        return findAll().stream()
                .filter(r -> r.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public List<Rate> findAll() {
        try {
            List<String> lines = Files.readAllLines(FILE, StandardCharsets.UTF_8);
            List<Rate> rates = new ArrayList<>();

            for (int i = 1; i < lines.size(); i++) {
                if (lines.get(i).trim().isEmpty()) continue;
                String[] c = lines.get(i).split(",", -1);
                if (c.length != 3) {
                    throw new IllegalStateException("Invalid record at line " + (i + 1));
                }
                rates.add(new Rate(
                        Integer.parseInt(c[0]), c[1], Double.parseDouble(c[2])));
            }
            return rates;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read rates file.", e);
        }
    }

    public boolean update(Rate updated) {
        List<Rate> rates = findAll();

        for (int i = 0; i < rates.size(); i++) {
            if (rates.get(i).getRateId() == updated.getRateId()) {
                boolean duplicate = rates.stream().anyMatch(r ->
                        r.getRateId() != updated.getRateId()
                        && r.getName().equalsIgnoreCase(updated.getName()));
                if (duplicate) {
                    throw new IllegalArgumentException("Rate name already exists.");
                }
                rates.set(i, updated);
                writeAll(rates);
                return true;
            }
        }
        return false;
    }

    public boolean delete(int id) {
        List<Rate> rates = findAll();
        boolean removed = rates.removeIf(r -> r.getRateId() == id);
        if (removed) writeAll(rates);
        return removed;
    }

    private void writeAll(List<Rate> rates) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);

        for (Rate rate : rates) {
            String name = rate.getName().replace("\"", "\"\"");
            if (name.contains(",") || name.contains("\"")
                    || name.contains("\n") || name.contains("\r")) {
                name = "\"" + name + "\"";
            }
            lines.add(rate.getRateId() + "," + name + "," + rate.getPercentage());
        }

        Path temp = FILE.resolveSibling("tarifas.csv.tmp");
        try {
            Files.write(temp, lines, StandardCharsets.UTF_8);
            try {
                Files.move(temp, FILE, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                Files.move(temp, FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            try { Files.deleteIfExists(temp); } catch (IOException ignored) {}
            throw new IllegalStateException("Could not save rates file.", e);
        }
    }
}
