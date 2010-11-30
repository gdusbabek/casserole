package casserole.ui;

import casserole.model.RowData;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class UpdateTableModel<C extends Comparable, T extends RowData> extends AbstractTableModel {
    SortedMap<C, T> rowMap = new TreeMap<C, T>();
    List<T> rowData = new ArrayList<T>();
    
    
    private final String[] cols;
    
    public UpdateTableModel(String[] cols) {
        this.cols = cols;    
    }

    public int getRowCount() {
        return rowData == null ? 0 : rowData.size();
    }

    public int getColumnCount() {
        return cols.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        T row = rowData.get(rowIndex);
        return row.getCol(columnIndex);
    }

    @Override
    public String getColumnName(int column) {
        return cols[column];
    }
    
    public T getRow(int index) {
        return rowData.get(index);
    }
    
    public Set<C> getKeys() {
        return rowMap.keySet();
    }
    
    public void insert(C key, T data) {
        rowMap.put(key, data);
        final int index = indexOf(key);
        rowData.add(index, data);
        SwingUtilities.invokeLater(new Runnable() { public void run() {
            fireTableRowsInserted(index, index);  
        }});
    }
    
    public void update(C key, T data) {
        final int row = indexOf(key);
        for (int c : rowData.get(row).update(data)) {
            final int col = c;
            SwingUtilities.invokeLater(new Runnable() { public void run() {
                fireTableCellUpdated(row, col);       
            }});
        }
    }
    
    public void remove(C key) {
        final int row = indexOf(key);
        rowMap.remove(key);
        rowData.remove(row);
        SwingUtilities.invokeLater(new Runnable() { public void run() {
            fireTableRowsDeleted(row, row);
        }});
    }
    
    public void clear() {
        rowMap.clear();
        rowData.clear();
        SwingUtilities.invokeLater(new Runnable() { public void run() {
            fireTableDataChanged();
        }});
    }
    
    // todo: need a better way of doing this.
    private int indexOf(C key) {
        int pos = 0;
        for (C c : rowMap.keySet()) {
            if (c.equals(key))
                return pos;
            pos++;
        }
        throw new IllegalStateException("WTF?");
    }
}
