package it.polito.extgol;
public class BoardRepository extends GenericExtGOLRepository<Board, Integer> {

    public BoardRepository() {
        super(Board.class);
    }

    public Board load(Integer id) {
        return this.findById(id).get();
    }
}
