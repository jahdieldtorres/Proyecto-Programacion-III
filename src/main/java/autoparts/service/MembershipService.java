
package autoparts.service;

import autoparts.dao.MembershipRepository;
import autoparts.dao.PartnerRepository;
import autoparts.dao.RateRepository;
import autoparts.model.Membership;
import autoparts.model.MembershipStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final PartnerRepository partnerRepository;
    private final RateRepository rateRepository;

    public MembershipService(
            MembershipRepository membershipRepository,
            PartnerRepository partnerRepository,
            RateRepository rateRepository) {

        if (membershipRepository == null
                || partnerRepository == null
                || rateRepository == null) {
            throw new IllegalArgumentException("All repositories are required.");
        }

        this.membershipRepository = membershipRepository;
        this.partnerRepository = partnerRepository;
        this.rateRepository = rateRepository;
    }

    public void register(Membership membership) {
        validate(membership);

        if (membershipRepository.findById(
                membership.getMembershipId()).isPresent()) {
            throw new IllegalArgumentException("Membership ID already exists.");
        }

        validateReferences(membership);
        validateActiveMembership(membership, null);
        membershipRepository.save(membership);
    }

    public Optional<Membership> findById(int id) {
        return membershipRepository.findById(id);
    }

    public List<Membership> findByPartnerId(int partnerId) {
        return membershipRepository.findByPartnerId(partnerId);
    }

    public List<Membership> findAll() {
        return membershipRepository.findAll();
    }

    public void update(Membership membership) {
        validate(membership);

        Membership existing = membershipRepository
                .findById(membership.getMembershipId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Membership does not exist."));

        validateReferences(membership);
        validateActiveMembership(membership, existing.getMembershipId());

        if (!membershipRepository.update(membership)) {
            throw new IllegalStateException("Could not update membership.");
        }
    }

    public void delete(int id) {
        if (!membershipRepository.delete(id)) {
            throw new IllegalArgumentException("Membership does not exist.");
        }
    }

    private void validate(Membership membership) {
        if (membership == null) {
            throw new IllegalArgumentException("Membership is required.");
        }

        if (membership.getMembershipId() <= 0
                || membership.getPartnerId() <= 0
                || membership.getRateId() <= 0) {
            throw new IllegalArgumentException("IDs must be positive.");
        }

        LocalDate start = membership.getStartDate();
        LocalDate expiration = membership.getExpirationDate();

        if (start == null || expiration == null) {
            throw new IllegalArgumentException("Membership dates are required.");
        }

        if (expiration.isBefore(start)) {
            throw new IllegalArgumentException(
                    "Expiration date cannot precede the start date.");
        }

        if (membership.getStatus() == null) {
            throw new IllegalArgumentException("Membership status is required.");
        }

        if (membership.getStatus() == MembershipStatus.ACTIVE
                && expiration.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "An expired membership cannot be active.");
        }
    }

    private void validateReferences(Membership membership) {
        if (!partnerRepository.findById(membership.getPartnerId()).isPresent()) {
            throw new IllegalArgumentException("Partner does not exist.");
        }

        if (!rateRepository.findById(membership.getRateId()).isPresent()) {
            throw new IllegalArgumentException("Rate does not exist.");
        }
    }

    private void validateActiveMembership(
            Membership membership, Integer excludedId) {

        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            return;
        }

        boolean alreadyActive = membershipRepository
                .findByPartnerId(membership.getPartnerId())
                .stream()
                .anyMatch(existing ->
                        existing.getStatus() == MembershipStatus.ACTIVE
                        && (excludedId == null
                            || existing.getMembershipId() != excludedId));

        if (alreadyActive) {
            throw new IllegalArgumentException(
                    "Partner already has an active membership.");
        }
    }
}
