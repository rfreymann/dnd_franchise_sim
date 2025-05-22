package dnd.franchise.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
public class UniqueWorker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int modifierMarketing;
    private int modifierRestructuring;
    private int modifierAccounting;

    private int monthlyCost;

    // Standard-Konstruktoren, Getter/Setter

    public UniqueWorker() {}

    public UniqueWorker(String name, int modMarketing, int modRestructuring, int modAccounting, int cost) {
        this.name = name;
        this.modifierMarketing = modMarketing;
        this.modifierRestructuring = modRestructuring;
        this.modifierAccounting = modAccounting;
        this.monthlyCost = cost;
    }

    // Getters and Setters
}
