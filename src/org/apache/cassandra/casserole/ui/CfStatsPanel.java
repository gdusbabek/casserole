/*
 * Created by JFormDesigner on Wed Nov 03 16:42:19 CDT 2010
 */

package org.apache.cassandra.casserole.ui;

import org.apache.cassandra.casserole.Connection;
import org.apache.cassandra.casserole.model.CfStat;
import org.apache.cassandra.casserole.model.RowData;
import org.apache.cassandra.casserole.util.SizeFormatter;

import java.rmi.RemoteException;
import java.util.Collection;

class CfStatsPanel extends StatsPanel {
    private static int[] minWidths = new int[] {
            150, 80, 70, 70, 70,
            70, 70, 70, 70, 70,
            70, 70, 70, 70, 70,
            70, 70, 70, 100, 100,
            100
    };
    
    private final boolean index;
    
    public CfStatsPanel() {
        this(false);
    }

    public CfStatsPanel(boolean index) {
        super(CfStat.COLS);
        this.index = index;
        statsTable.setModel(model);
        statsTable.getColumnModel().getColumn(2).setCellRenderer(new BriefNumberRenderer(SizeFormatter.Type.Bytes));
        statsTable.getColumnModel().getColumn(3).setCellRenderer(new BriefNumberRenderer(SizeFormatter.Type.Bytes));
        statsTable.getColumnModel().getColumn(4).setCellRenderer(new BriefNumberRenderer(SizeFormatter.Type.DecimalSmall));
        statsTable.getColumnModel().getColumn(5).setCellRenderer(new BriefNumberRenderer(SizeFormatter.Type.Bytes));
        statsTable.getColumnModel().getColumn(6).setCellRenderer(new BriefNumberRenderer(SizeFormatter.Type.DecimalSmall));
        statsTable.getColumnModel().getColumn(7).setCellRenderer(new BriefNumberRenderer(SizeFormatter.Type.DecimalSmall));
        statsTable.getColumnModel().getColumn(8).setCellRenderer(new BriefNumberRenderer(SizeFormatter.Type.Milliseconds));
        statsTable.getColumnModel().getColumn(9).setCellRenderer(new BriefNumberRenderer(SizeFormatter.Type.DecimalSmall));
        statsTable.getColumnModel().getColumn(10).setCellRenderer(new BriefNumberRenderer(SizeFormatter.Type.Milliseconds));
        statsTable.getColumnModel().getColumn(11).setCellRenderer(new BriefNumberRenderer(SizeFormatter.Type.DecimalSmall));
        statsTable.getColumnModel().getColumn(12).setCellRenderer(new BriefNumberRenderer(SizeFormatter.Type.Bytes));
        statsTable.getColumnModel().getColumn(13).setCellRenderer(new BriefNumberRenderer(SizeFormatter.Type.Bytes));
        statsTable.getColumnModel().getColumn(14).setCellRenderer(new BriefNumberRenderer(SizeFormatter.Type.Bytes));
        for (int i = 0; i < statsTable.getColumnCount(); i++)
            statsTable.getColumnModel().getColumn(i).setMinWidth(minWidths[i]);
    }

    protected Collection<? extends RowData> getUpdatedRowData(Connection c) throws RemoteException {
        if (index)
            return c.getIndexCfStats();
        else
            return c.getsCfStats();
    }
}
