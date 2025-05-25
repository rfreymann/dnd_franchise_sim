package dnd.franchise.service;

import dnd.franchise.model.User;
import dnd.franchise.repository.UserRepository;
import dnd.franchise.model.Franchise;
import dnd.franchise.model.UniqueWorker;
import dnd.franchise.repository.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import dnd.franchise.dto.FranchiseUpdateDto;
import dnd.franchise.dto.UniqueWorkerCreateDto;

@Service
@RequiredArgsConstructor
public class FranchiseService {
  private final FranchiseRepository repo;
  private final UserRepository userRepo;

  /**
   * Liefert alle Franchises des angemeldeten Users.
   */
  public List<Franchise> getAllForCurrentUser(Authentication auth) {
    String username = auth.getName();
    return repo.findByOwnerUsername(username);
  }

  /**
   * Erstellt ein neues Franchise für den angemeldeten User.
   */
  public Franchise createForCurrentUser(Franchise franchise, Authentication auth) {
    String username = auth.getName();
    User user = userRepo.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    franchise.setOwner(user);
    return repo.save(franchise);
  }

  public Optional<Franchise> updateFranchise(Long id, FranchiseUpdateDto dto) {
    return repo.findById(id).map(existing -> {
      existing.setName(dto.name);
      existing.setFunds(dto.funds);
      existing.setPropertyValue(dto.propertyValue);
      existing.setUnskilledWorkers(dto.unskilledWorkers);
      existing.setLowskilledWorkers(dto.lowskilledWorkers);
      existing.setHighskilledWorkers(dto.highskilledWorkers);
      existing.setCostUnskilledWorkers(dto.costUnskilledWorkers);
      existing.setCostLowskilledWorkers(dto.costLowskilledWorkers);
      existing.setCostHighskilledWorkers(dto.costHighskilledWorkers);
      existing.setRevenueModifier(dto.revenueModifier);
      existing.setUpkeepModifier(dto.upkeepModifier);
      return repo.save(existing);
    });
  }

  public Optional<Franchise> addUniqueWorker(Long franchiseId, UniqueWorkerCreateDto dto) {
    return repo.findById(franchiseId).map(franchise -> {
      UniqueWorker newWorker = new UniqueWorker();
      newWorker.setName(dto.name);
      newWorker.setMonthlyCost(dto.costPerMonth);
      newWorker.setModifierMarketing(dto.marketingBonus);
      newWorker.setModifierRestructuring(dto.restructuringBonus);
      newWorker.setModifierAccounting(dto.accountingBonus);

      franchise.getUniqueWorkers().add(newWorker);
      return repo.save(franchise);
    });
  }

  public Optional<Franchise> updateUniqueWorker(Long franchiseId, Long workerId, UniqueWorkerCreateDto dto) {
    return repo.findById(franchiseId).map(franchise -> {
      franchise.getUniqueWorkers().stream()
          .filter(w -> w.getId().equals(workerId))
          .findFirst()
          .ifPresent(worker -> {
            worker.setName(dto.name);
            worker.setMonthlyCost(dto.costPerMonth);
            worker.setModifierMarketing(dto.marketingBonus);
            worker.setModifierRestructuring(dto.restructuringBonus);
            worker.setModifierAccounting(dto.accountingBonus);
          });
      return repo.save(franchise);
    });
  }

  public boolean deleteUniqueWorker(Long franchiseId, Long workerId) {
    return repo.findById(franchiseId).map(franchise -> {
      boolean removed = franchise.getUniqueWorkers().removeIf(w -> w.getId().equals(workerId));
      if (removed) {
        repo.save(franchise);
      }
      return removed;
    }).orElse(false);
  }

}