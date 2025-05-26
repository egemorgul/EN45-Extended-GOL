package it.polito.extgol;

import jakarta.persistence.EntityManager;

public class BoardRepository extends GenericExtGOLRepository<Board, Long> {

    public BoardRepository() {
        super(Board.class);
    }
}
