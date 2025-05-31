package it.polito.extgol;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;

/**
 * Entity representing a cell in the Extended Game of Life.
 *
 * Serves as the base class for all cell types, embedding its board coordinates,
 * alive/dead state, energy budget (lifePoints), and interaction mood.
 * Each Cell is linked to a Board, Game, Tile, and a history of Generations.
 * Implements Evolvable to apply Conway’s rules plus energy checks each
 * generation,
 * and Interactable to model cell–cell energy exchanges.
 */
@Entity
public class Cell implements Evolvable, Interactable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * In-memory coordinates, persisted as two columns cell_x and cell_y.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "cell_x", nullable = false)),
            @AttributeOverride(name = "y", column = @Column(name = "cell_y", nullable = false))
    })
    private Coord cellCoord;

    /** Persisted alive/dead state */
    @Column(name = "is_alive", nullable = false)
    protected Boolean isAlive = false;

    /** Persisted lifepoints (default 0) */
    @Column(name = "lifepoints", nullable = false)
    protected Integer lifePoints = 0;

    /** The cell type determining specialized behavior rules */
    @Column(name = "cell_type", nullable = false)
    private CellType cellType = CellType.BASIC;

    /** The cell mood determining interaction behavior */
    @Column(name = "cell_mood", nullable = false)
    private CellMood cellMood = CellMood.NAIVE;

    /** The cell mood determining interaction behavior */
    @Column(name = "next_mood", nullable = true)
    private CellMood nextMood = null;

    /** Counter for tracking survival in death conditions (for HIGHLANDER) */
    @Transient
    private int deathGenerationCounter = 0;

    /** Reference to the parent board (read-only). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false, updatable = false)
    protected Board board;

    /** Reference to the owning game (read-only). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false, updatable = false)
    protected Game game;

    /** Transient list tracking generations this cell belongs to. */
    @Transient
    protected List<Generation> generations = new ArrayList<>();

    /** Back-reference: Tile owns the foreign key mapping. */
    @OneToOne(mappedBy = "cell", fetch = FetchType.LAZY)
    protected Tile tile;

    private boolean bloodMoon = false;

    /** Default constructor for JPA compliance. */
    public Cell() {
    }

    /**
     * Constructs a new Cell at given coordinates, defaulting to dead.
     * 
     * @param coord the cell's coordinates
     */
    public Cell(Coord tileCoord) {
        this.cellCoord = tileCoord;
        this.isAlive = false;
    }

    /**
     * Constructs a new Cell with its tile, board, and game context.
     * 
     * @param coord the cell's coordinates
     * @param tile  the owning Tile
     * @param board the Board context
     * @param game  the owning Game
     */
    public Cell(Coord tileCoord, Tile t, Board b, Game g) {
        this.cellCoord = tileCoord;
        this.isAlive = false;
        this.tile = t;
        this.board = b;
        this.game = g;
    }

    /**
     * Applies the classic Conway's Game of Life rules to calculate the cell's next
     * alive/dead state, extended with specialized cell behaviors.
     *
     * @param aliveNeighbors the count of alive neighboring cells
     * @return true if the cell will live, false otherwise
     */
    @Override
    public Boolean evolve(int aliveNeighbors) {
        // Start by assuming the cell retains its current state
        Boolean willLive = this.isAlive;
        boolean liveConditionsMet = false;

        // Apply specialized cell type behaviors using instanceof
        if (cellType == CellType.HIGHLANDER) {
            // Highlander can survive death conditions for 3 generations
            if (isAlive && (aliveNeighbors < 2 || aliveNeighbors > 3)) {
                if (deathGenerationCounter < 3) {
                    deathGenerationCounter++;
                    liveConditionsMet = true;
                } else {
                    deathGenerationCounter = 0;
                    liveConditionsMet = false;
                }
            } else {
                deathGenerationCounter = 0;
                liveConditionsMet = evaluateBasicRules(aliveNeighbors);
            }
        } else if (cellType == CellType.LONER) {
            // Loner can survive with as few as 1 neighbor
            if (isAlive) {
                liveConditionsMet = (aliveNeighbors >= 1 && aliveNeighbors <= 3);
            } else {
                // For dead cells, standard respawn rule applies
                liveConditionsMet = (aliveNeighbors == 3);
            }
        } else if (cellType == CellType.SOCIAL) {
            // Social can survive with up to 8 neighbors
            if (isAlive) {
                liveConditionsMet = (aliveNeighbors >= 2);
            } else {
                // For dead cells, standard respawn rule applies
                liveConditionsMet = (aliveNeighbors == 3);
            }
        } else {
            // Apply standard Conway rules (BASIC)
            liveConditionsMet = evaluateBasicRules(aliveNeighbors);
        }

        // Update life points based on cell's fate
        if (isAlive) {
            if (liveConditionsMet) {
                // Cell survives - gain a life point
                lifePoints++;
                willLive = true;
            } else {
                // Cell dies - lose a life point
                lifePoints--;
                willLive = false;
            }
        } else if (liveConditionsMet) {
            // Dead cell respawns - reset life points
            lifePoints = 0;
            willLive = true;
        }

        // A cell must have non-negative life points to be alive
        if (lifePoints < 0) {
            willLive = false;
        }

        // Set cell's new mood
        cellMood = (nextMood == null) ? cellMood : nextMood;

        return willLive;
    }

    /**
     * Helper method to evaluate basic Conway's Game of Life rules
     */
    private boolean evaluateBasicRules(int aliveNeighbors) {
        if (isAlive) {
            // Survival: 2-3 neighbors
            return (aliveNeighbors == 2 || aliveNeighbors == 3);
        } else {
            // Respawn: exactly 3 neighbors
            return (aliveNeighbors == 3);
        }
    }

    /**
     * Retrieves all tiles adjacent to this cell's tile.
     *
     * This method returns a copy of the underlying neighbor list to ensure
     * external code cannot modify the board topology.
     *
     * @return an immutable List of neighboring Tile instances
     */
    public List<Tile> getNeighbors() {
        return List.copyOf(tile.getNeighbors());
    }

    /**
     * Counts the number of live cells adjacent to this cell’s tile.
     *
     * Iterates over all neighboring tiles and increments the count for each
     * tile that hosts an alive Cell.
     *
     * @return the total number of alive neighboring cells
     */
    public int countAliveNeighbors() {
        int count = 0;
        for (Tile t : tile.getNeighbors()) {
            if (t.getCell() != null && t.getCell().isAlive())
                count++;
        }
        return count;
    }

    /**
     * Registers this cell in the specified generation’s back-reference list.
     *
     * Used internally by the ORM to maintain the relationship between
     * cells and the generations they belong to. Adds the given generation
     * to the cell’s internal history.
     *
     * @param gen the Generation instance to associate with this cell
     */
    void addGeneration(Generation gen) {
        generations.add(gen);
    }

    /**
     * Provides an unmodifiable history of all generations in which this cell has
     * appeared.
     *
     * Returns a copy of the internal list to prevent external modification
     * of the cell’s generation history.
     *
     * @return an immutable List of Generation instances tracking this cell’s
     *         lineage
     */
    public List<Generation> getGenerations() {
        return List.copyOf(generations);
    }

    /**
     * Returns the X coordinate of this cell on the board.
     *
     * @return the cell’s X position
     */
    public int getX() {
        return this.cellCoord.getX();
    }

    /**
     * Returns the Y coordinate of this cell on the board.
     *
     * @return the cell’s Y position
     */
    public int getY() {
        return this.cellCoord.getY();
    }

    /**
     * Retrieves the full coordinate object for this cell.
     *
     * @return a Coord instance representing this cell’s position
     */
    public Coord getCoordinates() {
        return this.cellCoord;
    }

    /**
     * Checks whether this cell is currently alive.
     *
     * @return true if the cell is alive; false if it is dead
     */
    public boolean isAlive() {
        return isAlive;
    }

    /**
     * Updates the alive/dead state of this cell.
     *
     * @param isAlive true to mark the cell as alive; false to mark it as dead
     */
    public void setAlive(boolean isAlive) {
        this.isAlive = isAlive;
    }

    /**
     * Returns a string representation of this cell’s position in the format "x,y".
     *
     * Overrides Object.toString() to provide a concise coordinate-based
     * representation.
     * 
     * @return a comma-separated string of the cell’s X and Y coordinates
     */
    @Override
    public String toString() {
        return getX() + "," + getY();
    }

    // EXTENDED BEHAVIORS implementations

    /**
     * Retrieves the current energy level of this cell.
     *
     * @return the number of life points the cell currently has
     */
    public int getLifePoints() {
        return lifePoints;
    }

    /**
     * Updates the energy level of this cell.
     *
     * @param lifePoints the new number of life points to assign to the cell
     */
    public void setLifePoints(int lifePoints) {
        this.lifePoints = lifePoints;
    }

    /**
     * Implements the interact() method of Interactable to
     * define the interaction between this cell and another cell.
     * Interactions adjust life points and potentially mood based on the
     * mood types of both cells.
     *
     * @param otherCell the Cell object to interact with
     */
    @Override
    public void interact(Cell otherCell) {
        InteractionLogic.processInteraction(this, otherCell);
    }

    /**
     * Inner class that handles the interaction logic between cells.
     * This separates the interaction business logic from the Cell entity.
     */
    private static class InteractionLogic {
        
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
            } else if (targetMood == CellMood.VAMPIRE && healer.getLifePoints() >= 0) {
                // VAMPIRE absorbs 1 lifePoint from HEALER
                healer.setLifePoints(healer.getLifePoints() - 1);
                target.setLifePoints(target.getLifePoints() + 1);
                if (target.bloodMoon) {
                    // VAMPIRE turns the HEALER into VAMPIRE
                    healer.setNextMood(CellMood.VAMPIRE);
                }
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
                target.setNextMood(CellMood.VAMPIRE);
            } // VAMPIRE + HEALER: handled in handleHealerInteraction
            // VAMPIRE + VAMPIRE: nothing happens
        }
    }

    /**
     * Assigns a specific cell type to this cell, influencing its behavior.
     *
     * @param t the CellType to set (e.g., BASIC, HIGHLANDER, LONER, SOCIAL)
     */
    public void setType(CellType t) {
        this.cellType = t;
    }

    /**
     * Gets the current type of this cell.
     * 
     * @return the CellType of this cell
     */
    public CellType getType() {
        return this.cellType;
    }

    /**
     * Sets the current mood of this cell, impacting how it interacts with others.
     *
     * @param mood the CellMood to assign (NAIVE, HEALER, or VAMPIRE)
     */
    public void setMood(CellMood mood) {
        this.cellMood = mood;
    }

    /**
     * Retrieves the current mood of this cell.
     *
     * @return the CellMood representing the cell's interaction style
     */
    public CellMood getMood() {
        return this.cellMood;
    }

    public Tile getTile() {
        return tile;
    }

    // Returns its cell type
    public CellType getCellType() {
        return this.cellType;
    }

    public void setBloodMoon(boolean bloodMoon) {
        this.bloodMoon = bloodMoon;
    }

    public void setNextMood(CellMood mood) {
        nextMood = mood;
    }
}
