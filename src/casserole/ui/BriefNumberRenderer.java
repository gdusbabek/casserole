package casserole.ui;

import casserole.util.SizeFormatter;

import javax.swing.table.DefaultTableCellRenderer;

public class BriefNumberRenderer extends DefaultTableCellRenderer {
    private final SizeFormatter fmt;
    
    public BriefNumberRenderer(SizeFormatter.Type type) {
        fmt = new SizeFormatter(type);
    }
    
    @Override
    protected void setValue(Object value) {
        super.setValue(fmt.format(value));    
    }
}
