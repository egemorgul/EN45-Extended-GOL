package it.polito.extgol;

import java.util.IntSummaryStatistics;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class BoardRepository extends GenericExtGOLRepository<Board, Integer> {

    public BoardRepository() {
        super(Board.class);
    }

    public Board load(Integer id) {
        return this.findById(id).get();
    }
}
