package dnd.franchise.repository;

import dnd.franchise.model.Franchise;
import dnd.franchise.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface FranchiseRepository extends JpaRepository<Franchise, Long> {
  List<Franchise> findByOwnerUsername(String username);
  List<Franchise> findByOwner(User owner);
}