package it.polito.extgol;

/**
 * Handles interactions between cells based on their moods.
 * This class encapsulates the business logic for cell-to-cell interactions,
 * separating it from the Cell entity class for better maintainability.
 */
public class InteractionHandler {
    
    /**
     * Processes the interaction between two cells based on their moods.
     * Only alive cells can interact with each other.
     * 
     * @param cell the initiating cell
     * @param otherCell the target cell to interact with
     */
    public static void processInteraction(Cell cell, Cell otherCell) {
        // Only alive cells can interact
        if (!cell.isAlive() || !otherCell.isAlive()) {
            return;
        }
        
        CellMood cellMood = cell.getMood();
        
        // Interaction rules based on moods
        if (cellMood == CellMood.HEALER) {
            handleHealerInteraction(cell, otherCell);
        } else if (cellMood == CellMood.VAMPIRE) {
            handleVampireInteraction(cell, otherCell);
        }
        // NAIVE + NAIVE: nothing happens (no explicit handling needed)
    }
    
    /**
     * Handles interactions when the initiating cell is a HEALER.
     * 
     * @param healer the healer cell
     * @param target the target cell
     */
    private static void handleHealerInteraction(Cell healer, Cell target) {
        CellMood targetMood = target.getMood();
        
        if (targetMood == CellMood.NAIVE) {
            // HEALER grants 1 lifePoint to NAIVE
            target.setLifePoints(target.getLifePoints() + 1);
        } else if (targetMood == CellMood.VAMPIRE) {
            // VAMPIRE absorbs 1 lifePoint from HEALER
            healer.setLifePoints(healer.getLifePoints() - 1);
            target.setLifePoints(target.getLifePoints() + 1);
        }
        // HEALER + HEALER: nothing happens
    }
    
    /**
     * Handles interactions when the initiating cell is a VAMPIRE.
     * 
     * @param vampire the vampire cell
     * @param target the target cell
     */
    private static void handleVampireInteraction(Cell vampire, Cell target) {
        CellMood targetMood = target.getMood();
        
        if (targetMood == CellMood.NAIVE && target.getLifePoints() >= 0) {
            // VAMPIRE absorbs 1 lifePoint from NAIVE and turns them into VAMPIRE
            vampire.setLifePoints(vampire.getLifePoints() + 1);
            target.setLifePoints(target.getLifePoints() - 1);
            target.setMood(CellMood.VAMPIRE);
        } else if (targetMood == CellMood.HEALER) {
            // VAMPIRE absorbs 1 lifePoint from HEALER
            vampire.setLifePoints(vampire.getLifePoints() + 1);
            target.setLifePoints(target.getLifePoints() - 1);
        }
        // VAMPIRE + VAMPIRE: nothing happens
    }
}
