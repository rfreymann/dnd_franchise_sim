package dnd.franchise.service;

import dnd.franchise.model.Franchise;
import dnd.franchise.model.UniqueWorker;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class BusinessSimulationService {

    private final Random random = new Random();

    public MarketingResult runMarketingActivity(UniqueWorker workers, Franchise franchise) {
        int baseRevenue = calculateBaseRevenue(franchise) + franchise.getRevenueModifier();
        int dc = 13 + Math.min(baseRevenue / 5000, 4);
        int[] mods = new int[3];
        mods[0] = workers.getModifierMarketing();
        mods[1] = workers.getModifierRestructuring();
        mods[2] = workers.getModifierAccounting();
        System.out.println(mods[0] + " " + mods[1] + " " + mods[2]);

        int successes = 0;
        int[] rolls = new int[3];
        for (int i = 0; i < 3; i++) {
            int roll = d20() + mods[i];
            rolls[i] = roll;
            if (roll >= dc) {
                successes++;
            }
        }

        double revenueMod = switch (successes) {
            case 0 -> -0.02;
            case 1 -> 0.02;
            default -> 0.05;
        };

        int oldRevenueMod = franchise.getRevenueModifier();
        int newRevenueMod = (int)(oldRevenueMod + revenueMod * baseRevenue);
        franchise.setRevenueModifier(newRevenueMod);
        return new MarketingResult(newRevenueMod, dc, rolls, successes);
    }

    public RestructuringResult runRestructuringActivity(UniqueWorker workers, Franchise franchise) {
        int baseUpkeep = calculateBaseUpkeep(franchise) + franchise.getUpkeepModifier();
        int dc = 13 + Math.min(baseUpkeep / 5000, 4);
        int[] mods = new int[3];
        mods[0] = workers.getModifierMarketing();
        mods[1] = workers.getModifierRestructuring();
        mods[2] = workers.getModifierAccounting();
        System.out.println(mods[0] + " " + mods[1] + " " + mods[2]);

        int successes = 0;
        int[] rolls = new int[3];
        for (int i = 0; i < 3; i++) {
            int roll = d20() + mods[i];
            rolls[i] = roll;
            if (roll >= dc) {
                successes++;
            }
        }

        double upkeepMod = switch (successes) {
            case 0 -> 0.06;
            case 1 -> -0.03;
            default -> -0.06;
        };

        int oldUpkeepMod = franchise.getUpkeepModifier();
        int newUpkeepMod = (int)(oldUpkeepMod + upkeepMod * baseUpkeep);
        franchise.setUpkeepModifier(newUpkeepMod);
        return new RestructuringResult(newUpkeepMod, dc, rolls, successes);
    }

    public MonthResult runMonthlyProfitCalculation(Franchise franchise, int modifier) {
        int roll = d100();
        int modifiedRoll = Math.min(roll + modifier, 100);

        int revenueMod = franchise.getRevenueModifier();
        int upkeepMod = franchise.getUpkeepModifier();
        int baseRevenue = calculateBaseRevenue(franchise) + revenueMod;
        int baseUpkeep = calculateBaseUpkeep(franchise) + upkeepMod;

        // Ränge: [low, high, Beschreibung, Multiplikator]
        Object[][] ranges = {
            {1, 10,  "Katastrophaler Monat", 0.00},
            {11, 20, "Großer Verlust",       0.50},
            {21, 30, "Kleiner Verlust",      0.75},
            {31, 40, "Unter den Erwartungen",1.00},
            {41, 60, "Durchschnittlich",     1.25},
            {61, 70, "Guter Monat",          1.50},
            {71, 80, "Sehr guter Monat",     1.75},
            {81, 90, "Fantastischer Monat",  2.00},
            {91,100, "Legendärer Erfolg",    2.25}
        };

        for (Object[] row : ranges) {
            int low = (int) row[0];
            int high = (int) row[1];
            if (modifiedRoll >= low && modifiedRoll <= high) {
                String description = (String) row[2];
                double multiplier = (double) row[3];
                int profit = (int) Math.round(baseRevenue * multiplier - baseUpkeep);
                franchise.setFunds(franchise.getFunds() + profit);
                franchise.setRevenueModifier((int)(revenueMod * 0.9));
                franchise.setUpkeepModifier((int)(upkeepMod * 0.9));
                return new MonthResult(
                    roll, modifier, modifiedRoll,
                    baseRevenue, baseUpkeep,
                    description, multiplier, profit
                );
            }
        }

        throw new IllegalStateException("Roll did not match any profit tier.");
    }


    private int d100() {
        return random.nextInt(100) + 1;
    }


    private int d20() {
        return random.nextInt(20) + 1;
    }

    public int calculateBaseRevenue(Franchise f) {
        return (int)(f.getPropertyValue() *.15 + calculateWagesWOUnique(f));
    }

    public int calculateBaseUpkeep(Franchise f) {
        return (int)(f.getPropertyValue() * .1 + calculateWages(f));
    }

    private int calculateWages(Franchise f) {
        return f.getUniqueWorkers().stream()
                .mapToInt(UniqueWorker::getMonthlyCost)
                .sum() 
                + f.getCostHighskilledWorkers() * f.getHighskilledWorkers()
                + f.getCostLowskilledWorkers() * f.getLowskilledWorkers()
                + f.getCostUnskilledWorkers() * f.getUnskilledWorkers();
    }

    private int calculateWagesWOUnique(Franchise f) {
        return f.getCostHighskilledWorkers() * f.getHighskilledWorkers()
                + f.getCostLowskilledWorkers() * f.getLowskilledWorkers()
                + f.getCostUnskilledWorkers() * f.getUnskilledWorkers();
    }

    @Getter
    @RequiredArgsConstructor
    public static class MarketingResult {
        private final int revenueMod;
        private final int dc;
        private final int[] rolls;
        private final int successes;
    }

        @Getter
    @RequiredArgsConstructor
    public static class BookkeepingResult {
        private final int roll;
        private final int revenue;
        private final int upkeep;
        private final int profit;
        private final boolean boostUsed;
    }


    @Getter
    @RequiredArgsConstructor
    public static class RestructuringResult {
        private final int upkeepMod;
        private final int dc;
        private final int[] rolls;
        private final int successes;
    }

    @Getter
    @RequiredArgsConstructor
    public class MonthResult {
        private final int rawRoll;
        private final int modifier;
        private final int finalRoll;
        private final int baseRevenue;
        private final int baseUpkeep;
        private final String description;
        private final double multiplier;
        private final int finalProfit;
    }

}
