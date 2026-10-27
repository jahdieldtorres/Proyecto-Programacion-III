
package autoparts.service;

import autoparts.dao.RateRepository;
import autoparts.model.Rate;

import java.util.List;
import java.util.Optional;

public class RateService {

    private final RateRepository repository;

    public RateService(RateRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Rate repository is required.");
        }
        this.repository = repository;
    }

    public void register(Rate rate) {
        validate(rate);
        rate.setName(rate.getName().trim());

        if (repository.findById(rate.getRateId()).isPresent()) {
            throw new IllegalArgumentException("Rate ID already exists.");
        }
        if (repository.findByName(rate.getName()).isPresent()) {
            throw new IllegalArgumentException("Rate name already exists.");
        }
        repository.save(rate);
    }

    public Optional<Rate> findById(int id) {
        return repository.findById(id);
    }

    public Optional<Rate> findByName(String name) {
        return repository.findByName(name);
    }

    public List<Rate> findAll() {
        return repository.findAll();
    }

    public void update(Rate rate) {
        validate(rate);
        rate.setName(rate.getName().trim());

        if (!repository.findById(rate.getRateId()).isPresent()) {
            throw new IllegalArgumentException("Rate does not exist.");
        }

        Optional<Rate> sameName = repository.findByName(rate.getName());
        if (sameName.isPresent()
                && sameName.get().getRateId() != rate.getRateId()) {
            throw new IllegalArgumentException("Rate name belongs to another rate.");
        }

        if (!repository.update(rate)) {
            throw new IllegalStateException("Could not update rate.");
        }
    }

    public void delete(int id) {
        if (!repository.delete(id)) {
            throw new IllegalArgumentException("Rate does not exist.");
        }
    }

    private void validate(Rate rate) {
        if (rate == null) {
            throw new IllegalArgumentException("Rate is required.");
        }
        if (rate.getRateId() <= 0) {
            throw new IllegalArgumentException("Rate ID must be positive.");
        }
        if (rate.getName() == null || rate.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Rate name is required.");
        }

        double percentage = rate.getPercentage();
        if (!Double.isFinite(percentage) || percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException(
                    "Percentage must be between 0 and 100.");
        }
    }
}
