
package autoparts.dao;

import autoparts.model.Membership;
import autoparts.model.MembershipStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MembershipRepository {

    private static final Path FILE =
            Paths.get("data", "afiliaciones.csv");

    private static final String HEADER =
            "membershipId,partnerId,rateId,startDate,expirationDate,status";

    public MembershipRepository() {
        try {
            Files.createDirectories(FILE.getParent());
            if (Files.notExists(FILE)) {
                Files.write(FILE, List.of(HEADER), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not initialize memberships file.", e);
        }
    }

    public void save(Membership membership) {
        List<Membership> memberships = findAll();

        if (findById(membership.getMembershipId()).isPresent()) {
            throw new IllegalArgumentException("Membership ID already exists.");
        }

        memberships.add(membership);
        writeAll(memberships);
    }

    public Optional<Membership> findById(int id) {
        return findAll().stream()
                .filter(m -> m.getMembershipId() == id)
                .findFirst();
    }

    public List<Membership> findByPartnerId(int partnerId) {
        List<Membership> result = new ArrayList<>();

        for (Membership membership : findAll()) {
            if (membership.getPartnerId() == partnerId) {
                result.add(membership);
            }
        }

        return result;
    }

    public List<Membership> findAll() {
        try {
            List<String> lines =
                    Files.readAllLines(FILE, StandardCharsets.UTF_8);

            List<Membership> memberships = new ArrayList<>();

            for (int i = 1; i < lines.size(); i++) {
                if (lines.get(i).trim().isEmpty()) continue;

                String[] c = lines.get(i).split(",", -1);

                if (c.length != 6) {
                    throw new IllegalStateException(
                            "Invalid membership record at line " + (i + 1));
                }

                try {
                    memberships.add(new Membership(
                            Integer.parseInt(c[0]),
                            Integer.parseInt(c[1]),
                            Integer.parseInt(c[2]),
                            LocalDate.parse(c[3]),
                            LocalDate.parse(c[4]),
                            MembershipStatus.valueOf(c[5])
                    ));
                } catch (RuntimeException e) {
                    throw new IllegalStateException(
                            "Invalid membership data at line " + (i + 1), e);
                }
            }

            return memberships;
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not read memberships file.", e);
        }
    }

    public boolean update(Membership updated) {
        List<Membership> memberships = findAll();

        for (int i = 0; i < memberships.size(); i++) {
            if (memberships.get(i).getMembershipId()
                    == updated.getMembershipId()) {
                memberships.set(i, updated);
                writeAll(memberships);
                return true;
            }
        }

        return false;
    }

    public boolean delete(int id) {
        List<Membership> memberships = findAll();

        boolean removed = memberships.removeIf(
                m -> m.getMembershipId() == id);

        if (removed) writeAll(memberships);
        return removed;
    }

    private void writeAll(List<Membership> memberships) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);

        for (Membership m : memberships) {
            lines.add(
                    m.getMembershipId() + ","
                    + m.getPartnerId() + ","
                    + m.getRateId() + ","
                    + m.getStartDate() + ","
                    + m.getExpirationDate() + ","
                    + m.getStatus().name()
            );
        }

        Path temp = FILE.resolveSibling("afiliaciones.csv.tmp");

        try {
            Files.write(temp, lines, StandardCharsets.UTF_8);

            try {
                Files.move(temp, FILE,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                Files.move(temp, FILE,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            try { Files.deleteIfExists(temp); } catch (IOException ignored) {}
            throw new IllegalStateException(
                    "Could not save memberships file.", e);
        }
    }
}
