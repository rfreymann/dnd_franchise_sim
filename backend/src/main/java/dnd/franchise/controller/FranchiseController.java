package dnd.franchise.controller;

import dnd.franchise.model.Franchise;
import dnd.franchise.repository.FranchiseRepository;
import dnd.franchise.repository.UserRepository;
import dnd.franchise.model.User;
import dnd.franchise.service.FranchiseService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import lombok.*;

@RestController
@RequestMapping("/api/franchise")
@RequiredArgsConstructor
public class FranchiseController {
    private final FranchiseService service;
    private final FranchiseRepository franchiseRepository;
    private final UserRepository userRepository;

    @GetMapping
    public List<Franchise> getAllFranchises(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getSubject();
        System.out.println("Franchise-Abfrage von: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Benutzer nicht gefunden"));

        return franchiseRepository.findByOwner(user);
    }

    @PostMapping
    public Franchise createFranchise(@RequestBody Franchise franchise, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        franchise.setOwner(user);
        return franchiseRepository.save(franchise);
    }
}