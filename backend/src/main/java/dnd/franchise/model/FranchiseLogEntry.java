package dnd.franchise.model;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import lombok.Data;

@Entity
@Data
public class FranchiseLogEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Franchise franchise;

    private LocalDateTime timestamp;
    private String bookkeepingDescription;
    private int roll, modifier, finalRoll, revenue, upkeep, profit;

    private Long marketingWorkerId;
    private Long restructuringWorkerId;
    private Long accountingWorkerId;

    // optional: Worker-Namen speichern als Snapshot
    private String marketingWorkerName;
    private String restructuringWorkerName;
    private String accountingWorkerName;

    // Getter, Setter, Constructor ...
}
