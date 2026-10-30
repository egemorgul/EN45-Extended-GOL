# Extended Game of Life

A Java extension of [Conway's Game of Life](https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life). It adds specialized cell types, energy mechanics, cell-to-cell interactions, global board events, analytics, and full state persistence with JPA/Hibernate.

Developed as a group project for the Object-Oriented Programming course at Politecnico di Torino.

## Features

### Cells
- **Classic GOL rules**: survival, underpopulation, overpopulation and respawn on a finite board.
- **Life points**: each cell has an energy level that rises when it survives, falls when it dies and resets when it respawns. A cell needs at least 0 life points to stay alive.
- **Cell types**
  - `Highlander`: survives up to 3 consecutive generations in death-inducing conditions
  - `Loner`: survives with only 1 neighbor
  - `Social`: survives with up to 8 neighbors
- **Moods**: `NAIVE`, `HEALER` and `VAMPIRE`.
  - Healers give life points to other cells.
  - Vampires drain life points and turn naive cells into vampires.

### Board
- An `M×N` grid of tiles. Each tile holds one cell.
- Tiles have a `lifePointModifier` that adds energy to their cell or takes it away.
- Text visualization (`C`, `H`, `L`, `S`, `0`).
- Analytics:
  - cell counts, per generation and per type
  - highest-energy and top-N cells
  - cells grouped by energy level or by number of alive neighbors
  - energy statistics, including a time series across generations

### Game events
Each generation can have at most one global event:

| Event | Effect |
|---|---|
| **Cataclysm** | Resets all cells' life points to 0 |
| **Famine** | Every cell loses 1 life point |
| **Bloom** | Every cell gains 2 life points |
| **Blood Moon** | Vampires can also turn healers into vampires |
| **Sanctuary** | Healers gain 1 life point; all vampires become naive |

### Persistence
- All entities (`Cell` hierarchy, `Tile`, `Board`, `Game`, `Generation`) are mapped with JPA.
- State is stored in an in-memory H2 database through Hibernate.
- Each entity has its own repository built on a generic base class.
- A saved game can be reloaded at any generation, with its events re-attached, so the simulation can be replayed or inspected.

## Tech Stack

- **Java**
- **Maven**
- **JPA / Hibernate ORM**
- **H2** (in-memory database)
- **JUnit 4**

## Project Structure

```
├── resources/META-INF/
│   └── persistence.xml        # Hibernate + H2 configuration
├── src/it/polito/extgol/      # Source code (entities, game logic, repositories)
├── test/it/polito/extgol/test # JUnit tests
└── pom.xml
```

## Getting Started

**Requirements:** Java 17+ and Maven

```bash
git clone https://github.com/egemorgul/EN45-Extended-GOL.git
cd EN45-Extended-GOL
mvn test
```

## Example Board Output

```
0C00H
L00CS
0H000
C0000
```

`C` = basic cell, `H` = Highlander, `L` = Loner, `S` = Social, `0` = dead
