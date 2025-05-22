package dnd.franchise.controller;

import dnd.franchise.model.Franchise;
import dnd.franchise.repository.FranchiseRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import lombok.*;

@RestController
@RequestMapping("/api/franchises")
@CrossOrigin(origins = "http://localhost:3000")
public class FranchiseController {

    private final FranchiseRepository repository;

    public FranchiseController(FranchiseRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Franchise> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public Franchise create(@RequestBody Franchise franchise) {
        return repository.save(franchise);
    }
}
