package dnd.franchise.model;

import jakarta.persistence.*;
import java.util.List;

import lombok.*;

@Entity
@Data
public class Franchise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @ManyToOne 
    User owner;


    private int funds;
    private int propertyValue;

    private int unskilledWorkers;
    private int lowskilledWorkers;
    private int highskilledWorkers;

    private int costUnskilledWorkers;
    private int costLowskilledWorkers;
    private int costHighskilledWorkers;

    private int revenueModifier;
    private int upkeepModifier;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "franchise_id")
    private List<UniqueWorker> uniqueWorkers;

    public Franchise() {}

    public Franchise(String name, int funds, int propertyValue) {
        this.name = name;
        this.funds = funds;
        this.propertyValue = propertyValue;

        this.unskilledWorkers = 0;
        this.lowskilledWorkers = 0;
        this.highskilledWorkers = 0;
        this.costUnskilledWorkers = 0;
        this.costLowskilledWorkers = 0;
        this.costHighskilledWorkers = 0;
        this.revenueModifier = 0;
        this.upkeepModifier = 0;
    }

    // Getters and Setters
}
