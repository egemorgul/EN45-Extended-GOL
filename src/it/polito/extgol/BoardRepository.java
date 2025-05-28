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

    public IntSummaryStatistics getEnergyStatistics(Generation generation) {
        EntityManager em = JPAUtil.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Generation> cq = cb.createQuery(Generation.class);
        Root<Generation> root = cq.from(Generation.class);
        cq.select(root)
                .where(cb.equal(root.get("id"), generation.getId()));

        Generation res = em.createQuery(cq).getSingleResult();

        IntSummaryStatistics stats = res.getEnergyStates()
                .entrySet().stream()
                .filter(e -> res.getCellAlivenessStates().get(e.getKey()))
                .mapToInt(e -> e.getValue())
                .summaryStatistics();

        em.close();
        return stats;
    }
}
