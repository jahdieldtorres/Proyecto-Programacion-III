
package autoparts.dao;

import autoparts.model.Partner;
import autoparts.model.PartnerStatus;
import autoparts.model.Supplier;
import autoparts.model.Workshop;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PartnerRepository {

    private static final Path FILE = Paths.get("data", "socios.csv");
    private static final String HEADER =
            "partnerId,type,companyName,taxId,email,phone,addressId,status,"
            + "deliveryTimeSeconds,specialtyDescription,availableSpaces";

    public PartnerRepository() {
        initializeFile();
    }

    private void initializeFile() {
        try {
            Files.createDirectories(FILE.getParent());
            if (Files.notExists(FILE)) {
                Files.write(FILE, List.of(HEADER), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize partners file.", e);
        }
    }

    public void save(Partner partner) {
        List<Partner> partners = findAll();

        if (findById(partner.getPartnerId()).isPresent()) {
            throw new IllegalArgumentException("Partner ID already exists.");
        }
        if (findByTaxId(partner.getTaxId()).isPresent()) {
            throw new IllegalArgumentException("Tax ID already exists.");
        }

        partners.add(partner);
        writeAll(partners);
    }

    public Optional<Partner> findById(int id) {
        return findAll().stream()
                .filter(p -> p.getPartnerId() == id)
                .findFirst();
    }

    public Optional<Partner> findByTaxId(String taxId) {
        if (taxId == null) return Optional.empty();
        return findAll().stream()
                .filter(p -> p.getTaxId().equalsIgnoreCase(taxId))
                .findFirst();
    }

    public List<Partner> findAll() {
        try {
            List<String> lines = Files.readAllLines(FILE, StandardCharsets.UTF_8);
            List<Partner> partners = new ArrayList<>();

            for (int i = 1; i < lines.size(); i++) {
                if (lines.get(i).trim().isEmpty()) continue;
                try {
                    partners.add(parsePartner(parseCsvLine(lines.get(i))));
                } catch (RuntimeException e) {
                    throw new IllegalStateException(
                            "Invalid record in socios.csv at line " + (i + 1), e);
                }
            }
            return partners;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read partners file.", e);
        }
    }

    public List<Supplier> findAllSuppliers() {
        return findAll().stream()
                .filter(Supplier.class::isInstance)
                .map(Supplier.class::cast)
                .collect(Collectors.toList());
    }

    public List<Workshop> findAllWorkshops() {
        return findAll().stream()
                .filter(Workshop.class::isInstance)
                .map(Workshop.class::cast)
                .collect(Collectors.toList());
    }

    public boolean update(Partner updated) {
        List<Partner> partners = findAll();

        for (int i = 0; i < partners.size(); i++) {
            Partner current = partners.get(i);

            if (current.getPartnerId() == updated.getPartnerId()) {
                if (!current.getClass().equals(updated.getClass())) {
                    throw new IllegalArgumentException("Cannot change partner type.");
                }

                boolean duplicate = partners.stream().anyMatch(p ->
                        p.getPartnerId() != updated.getPartnerId()
                        && p.getTaxId().equalsIgnoreCase(updated.getTaxId()));

                if (duplicate) {
                    throw new IllegalArgumentException("Tax ID belongs to another partner.");
                }

                partners.set(i, updated);
                writeAll(partners);
                return true;
            }
        }
        return false;
    }

    public boolean delete(int id) {
        List<Partner> partners = findAll();
        boolean removed = partners.removeIf(p -> p.getPartnerId() == id);
        if (removed) writeAll(partners);
        return removed;
    }

    private Partner parsePartner(List<String> c) {
        if (c.size() != 11) throw new IllegalArgumentException("Expected 11 columns.");

        int id = Integer.parseInt(c.get(0));
        String type = c.get(1);
        String name = c.get(2);
        String taxId = c.get(3);
        String email = c.get(4);
        String phone = c.get(5);
        int addressId = Integer.parseInt(c.get(6));
        PartnerStatus status = PartnerStatus.valueOf(c.get(7));

        if ("SUPPLIER".equals(type)) {
            return new Supplier(id, name, taxId, email, phone, addressId,
                    status, Duration.ofSeconds(Long.parseLong(c.get(8))));
        }
        if ("WORKSHOP".equals(type)) {
            return new Workshop(id, name, taxId, email, phone, addressId,
                    status, c.get(9), Integer.parseInt(c.get(10)));
        }
        throw new IllegalArgumentException("Unknown partner type: " + type);
    }

    private List<String> toRow(Partner p) {
        List<String> c = new ArrayList<>();
        c.add(String.valueOf(p.getPartnerId()));
        c.add(p instanceof Supplier ? "SUPPLIER" : "WORKSHOP");
        c.add(p.getCompanyName());
        c.add(p.getTaxId());
        c.add(p.getEmail());
        c.add(p.getPhone());
        c.add(String.valueOf(p.getAddressId()));
        c.add(p.getStatus().name());

        if (p instanceof Supplier) {
            c.add(String.valueOf(((Supplier) p).getDeliveryTime().getSeconds()));
            c.add("");
            c.add("");
        } else if (p instanceof Workshop) {
            Workshop w = (Workshop) p;
            c.add("");
            c.add(w.getSpecialtyDescription());
            c.add(String.valueOf(w.getAvailableSpaces()));
        } else {
            throw new IllegalArgumentException("Unsupported partner type.");
        }
        return c;
    }

    private void writeAll(List<Partner> partners) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);

        for (Partner p : partners) {
            lines.add(toRow(p).stream()
                    .map(this::escapeCsv)
                    .collect(Collectors.joining(",")));
        }

        Path temp = FILE.resolveSibling("socios.csv.tmp");
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
            throw new IllegalStateException("Could not save partners file.", e);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (ch == ',' && !quoted) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(ch);
            }
        }

        if (quoted) throw new IllegalArgumentException("Unclosed CSV quote.");
        fields.add(field.toString());
        return fields;
    }
}
