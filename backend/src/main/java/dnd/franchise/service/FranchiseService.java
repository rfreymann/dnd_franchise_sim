package dnd.franchise.service;

import dnd.franchise.model.User;
import dnd.franchise.repository.UserRepository;
import dnd.franchise.model.Franchise;
import dnd.franchise.repository.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.List;

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
}