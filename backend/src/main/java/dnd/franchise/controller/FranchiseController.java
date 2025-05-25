package dnd.franchise.controller;

import dnd.franchise.dto.BaseValuesDto;
import dnd.franchise.dto.FranchiseUpdateDto;
import dnd.franchise.dto.SimulationRequest;
import dnd.franchise.dto.UniqueWorkerCreateDto;
import dnd.franchise.model.Franchise;
import dnd.franchise.repository.FranchiseRepository;
import dnd.franchise.repository.UserRepository;
import dnd.franchise.model.User;
import dnd.franchise.service.BusinessSimulationService.MarketingResult;
import dnd.franchise.service.BusinessSimulationService.MonthResult;
import dnd.franchise.service.BusinessSimulationService.RestructuringResult;
import dnd.franchise.service.FranchiseService;
import dnd.franchise.wrapper.SimulationResult;
import dnd.franchise.model.UniqueWorker;
import dnd.franchise.service.BusinessSimulationService;
import dnd.franchise.model.FranchiseLogEntry;
import dnd.franchise.repository.FranchiseLogRepository;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final BusinessSimulationService simulationService;
    private final FranchiseLogRepository logRepository;

    @GetMapping
    public List<Franchise> getAllFranchises(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getSubject();
        System.out.println("Franchise-Abfrage von: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Benutzer nicht gefunden"));

        return franchiseRepository.findByOwner(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Franchise> getFranchiseById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getSubject();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Benutzer nicht gefunden"));

        return franchiseRepository.findById(id)
                .filter(franchise -> franchise.getOwner().getId().equals(user.getId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    @PostMapping
    public Franchise createFranchise(@RequestBody Franchise franchise, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        franchise.setOwner(user);
        return franchiseRepository.save(franchise);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Franchise> updateFranchise(@PathVariable Long id, @RequestBody FranchiseUpdateDto dto) {
        return service.updateFranchise(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{franchiseId}/unique-workers")
    public ResponseEntity<Franchise> addUniqueWorker(
            @PathVariable Long franchiseId,
            @RequestBody UniqueWorkerCreateDto dto) {
        return service.addUniqueWorker(franchiseId, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{franchiseId}/unique-workers/{workerId}")
    public ResponseEntity<Franchise> updateUniqueWorker(
            @PathVariable Long franchiseId,
            @PathVariable Long workerId,
            @RequestBody UniqueWorkerCreateDto dto) {
        return service.updateUniqueWorker(franchiseId, workerId, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{franchiseId}/unique-workers/{workerId}")
    public ResponseEntity<Void> deleteUniqueWorker(
            @PathVariable Long franchiseId,
            @PathVariable Long workerId) {
        return service.deleteUniqueWorker(franchiseId, workerId)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/full-simulation")
    public ResponseEntity<SimulationResult> runFullSimulation(
            @PathVariable Long id,
            @RequestBody SimulationRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String username = jwt.getSubject();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        return franchiseRepository.findById(id)
                .filter(f -> f.getOwner().getId().equals(user.getId()))
                .map(franchise -> {
                    // Optionale Aktivitäten
                    MarketingResult marketingResult = null;
                    RestructuringResult restructuringResult = null;

                    if (request.marketingWorkerId != null) {
                        UniqueWorker w = findWorker(franchise, request.marketingWorkerId);
                        marketingResult = simulationService.runMarketingActivity(w, franchise);
                    }

                    if (request.restructuringWorkerId != null) {
                        UniqueWorker w = findWorker(franchise, request.restructuringWorkerId);
                        restructuringResult = simulationService.runRestructuringActivity(w, franchise);
                    }

                    int modifier = (request.accountingWorkerId != null &&
                            franchise.getUniqueWorkers().stream()
                                    .anyMatch(w -> w.getId().equals(request.accountingWorkerId))) ? 30 : 0;

                    MonthResult bookkeepingResult = simulationService.runMonthlyProfitCalculation(franchise, modifier);

                    FranchiseLogEntry log = new FranchiseLogEntry();
                    log.setFranchise(franchise);
                    log.setTimestamp(LocalDateTime.now());
                    log.setRoll(bookkeepingResult.getRawRoll());
                    log.setModifier(modifier);
                    log.setFinalRoll(bookkeepingResult.getFinalRoll());
                    log.setBookkeepingDescription(bookkeepingResult.getDescription());
                    log.setRevenue(bookkeepingResult.getBaseRevenue());
                    log.setUpkeep(bookkeepingResult.getBaseUpkeep());
                    log.setProfit(bookkeepingResult.getFinalProfit());

                    if (request.marketingWorkerId != null) {
                        UniqueWorker w = findWorker(franchise, request.marketingWorkerId);
                        log.setMarketingWorkerId(w.getId());
                        log.setMarketingWorkerName(w.getName());
                    }

                    // (analog für restructuring & accounting)
                    if (request.restructuringWorkerId != null) {
                        UniqueWorker w = findWorker(franchise, request.restructuringWorkerId);
                        log.setRestructuringWorkerId(w.getId());
                        log.setRestructuringWorkerName(w.getName());
                    }
                    if (request.accountingWorkerId != null) {
                        UniqueWorker w = findWorker(franchise, request.accountingWorkerId);
                        log.setAccountingWorkerId(w.getId());
                        log.setAccountingWorkerName(w.getName());
                    }
                    // Speichern des Log-Eintrags

                    logRepository.save(log);

                    franchiseRepository.save(franchise);
                    return ResponseEntity
                            .ok(new SimulationResult(marketingResult, restructuringResult, bookkeepingResult));
                })
                .orElse(ResponseEntity.notFound().build());

    }

    private UniqueWorker findWorker(Franchise franchise, Long workerId) {
        return franchise.getUniqueWorkers().stream()
                .filter(w -> w.getId().equals(workerId))
                .findFirst()
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Worker gehört nicht zum Franchise"));
    }

    @GetMapping("/{id}/log")
    public ResponseEntity<List<FranchiseLogEntry>> getLog(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getSubject();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        return franchiseRepository.findById(id)
                .filter(f -> f.getOwner().getId().equals(user.getId()))
                .map(f -> ResponseEntity.ok(logRepository.findByFranchiseIdOrderByTimestampDesc(f.getId())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/base-values")
    public ResponseEntity<BaseValuesDto> getBaseValues(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        String username = jwt.getSubject();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        return franchiseRepository.findById(id)
                .filter(f -> f.getOwner().getId().equals(user.getId()))
                .map(f -> {
                    int revenue = simulationService.calculateBaseRevenue(f);
                    int upkeep = simulationService.calculateBaseUpkeep(f);
                    return ResponseEntity.ok(new BaseValuesDto(revenue, upkeep));
                })
                .orElse(ResponseEntity.notFound().build());
    }

}