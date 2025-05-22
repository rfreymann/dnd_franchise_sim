package com.dnd.franchise.controller;

import com.dnd.franchise.model.Franchise;
import com.dnd.franchise.repository.FranchiseRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
