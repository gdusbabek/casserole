/*
 * Created by JFormDesigner on Thu Nov 04 08:33:30 CDT 2010
 */

package casserole.ui;

import casserole.Connection;
import casserole.model.RowData;
import casserole.model.TpStat;

import java.rmi.RemoteException;
import java.util.Collection;

class TpStatsPanel extends StatsPanel {
    
    public TpStatsPanel() {
        super(TpStat.COLS);
        statsTable.setModel(model);
        statsTable.getColumnModel().getColumn(0).setPreferredWidth(180);
        statsTable.getColumnModel().getColumn(3).setPreferredWidth(180);
    }

    protected Collection<? extends RowData> getUpdatedRowData(Connection c) throws RemoteException {
        return c.getTpStats();
    }
}
