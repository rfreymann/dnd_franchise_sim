package dnd.franchise.repository;


import dnd.franchise.model.FranchiseLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FranchiseLogRepository extends JpaRepository<FranchiseLogEntry, Long> {
    List<FranchiseLogEntry> findByFranchiseIdOrderByTimestampDesc(Long franchiseId);
}
