package dnd.franchise.wrapper;

import dnd.franchise.service.BusinessSimulationService;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SimulationResult {
    private final BusinessSimulationService.MarketingResult marketing;
    private final BusinessSimulationService.RestructuringResult restructuring;
    private final BusinessSimulationService.MonthResult bookkeeping;
}
