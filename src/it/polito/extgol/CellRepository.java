package it.polito.extgol;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

/**
 * Repository for Cell entities, providing persistence operations
 * and custom queries for Cell-related data.
 */
public class CellRepository extends GenericExtGOLRepository<Cell, Long> {
    
    /**
     * Creates a new CellRepository instance.
     */
    public CellRepository() {
        super(Cell.class);
    }
    
    /**
     * Loads a Cell by its ID.
     *
     * @param id the unique identifier of the Cell to load
     * @return the loaded Cell entity, or null if not found
     */
    public Cell load(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Cell.class, id);
        } finally {
            em.close();
        }
    }
    
    /**
     * Loads all Cells with a specific CellType.
     * 
     * @param type the CellType to filter by
     * @return a List of Cell entities of the specified type
     */
    public List<Cell> findByType(CellType type) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Cell> cq = cb.createQuery(Cell.class);
            Root<Cell> root = cq.from(Cell.class);
            
            cq.select(root)
              .where(cb.equal(root.get(Cell_.cellType), type));
            
            return em.createQuery(cq).getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Loads all alive Cells with a specific CellMood.
     * 
     * @param mood the CellMood to filter by
     * @return a List of alive Cell entities with the specified mood
     */
    public List<Cell> findAliveByMood(CellMood mood) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Cell> cq = cb.createQuery(Cell.class);
            Root<Cell> root = cq.from(Cell.class);
            
            cq.select(root)
              .where(cb.and(
                  cb.equal(root.get(Cell_.cellMood), mood),
                  cb.equal(root.get(Cell_.isAlive), true)
              ));
            
            return em.createQuery(cq).getResultList();
        } finally {
            em.close();
        }
    }
}
