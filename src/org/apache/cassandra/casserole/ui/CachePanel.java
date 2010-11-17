/*
 * Created by JFormDesigner on Tue Nov 16 09:44:37 CST 2010
 */

package org.apache.cassandra.casserole.ui;

import org.apache.cassandra.casserole.Connection;
import org.apache.cassandra.casserole.model.CacheData;
import org.apache.cassandra.casserole.model.RowData;
import org.apache.cassandra.casserole.util.SizeFormatter;

import java.rmi.RemoteException;
import java.util.Collection;

class CachePanel extends StatsPanel {
    
    public CachePanel() {
        super(CacheData.COLS);
        statsTable.setModel(model);
        statsTable.getColumnModel().getColumn(3).setCellRenderer(new BriefNumberRenderer(SizeFormatter.Type.DecimalSmall));
        statsTable.getColumnModel().getColumn(4).setCellRenderer(new BriefNumberRenderer(SizeFormatter.Type.Percent));
        statsTable.getColumnModel().getColumn(5).setCellRenderer(new BriefNumberRenderer(SizeFormatter.Type.DecimalSmall));
        statsTable.getColumnModel().getColumn(6).setCellRenderer(new BriefNumberRenderer(SizeFormatter.Type.Bytes));
        statsTable.getColumnModel().getColumn(0).setMinWidth(150);
    }

    protected Collection<? extends RowData> getUpdatedRowData(Connection c) throws RemoteException {
        return c.getCacheStats();
    }
}
