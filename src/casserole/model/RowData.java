package casserole.model;

public interface RowData {    
    public Object getCol(int c);
    public Iterable<Integer> update(RowData rd);
    public String getName(); // unique key.
}
