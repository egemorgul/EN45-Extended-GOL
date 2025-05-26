package it.polito.extgol;

import jakarta.persistence.EntityManager;

public class BoardRepository extends GenericExtGOLRepository<Board, Long> {

    public BoardRepository() {
        super(Board.class);
    }

    /**
     * Loads a board and its complete state, including all tiles and cells.
     * Uses fetch joins to eagerly load tiles and their cells in a single query.
     *
     * @param id the ID of the board to load
     * @return the loaded board with its tiles and cells initialized
     */
    public Board load(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // Use join fetch to get tiles and cells in one query
            Board board = em.createQuery(
                    "SELECT DISTINCT b FROM Board b " +
                            "LEFT JOIN FETCH b.tiles t " +
                            "LEFT JOIN FETCH t.cell " +
                            "WHERE b.id = :id",
                    Board.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return board;
        } finally {
            em.close();
        }
    }
}
