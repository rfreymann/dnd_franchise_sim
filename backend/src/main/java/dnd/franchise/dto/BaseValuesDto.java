package dnd.franchise.dto;

public class BaseValuesDto {
    public int baseRevenue;
    public int baseUpkeep;

    public BaseValuesDto(int revenue, int upkeep) {
        this.baseRevenue = revenue;
        this.baseUpkeep = upkeep;
    }
}

