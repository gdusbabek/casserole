package casserole.ui;

import casserole.Connection;
import casserole.model.MsgData;
import casserole.model.RowData;

import java.rmi.RemoteException;
import java.util.Collection;

class MsgStatsPanel extends StatsPanel {
    public MsgStatsPanel() {
        super(MsgData.COLS);
        statsTable.setModel(model);
        statsTable.getColumnModel().getColumn(1).setMinWidth(150);
        statsTable.getColumnModel().getColumn(2).setMinWidth(150);
        statsTable.getColumnModel().getColumn(3).setMinWidth(150);
        statsTable.getColumnModel().getColumn(4).setMinWidth(150);
    }

    protected Collection<? extends RowData> getUpdatedRowData(Connection c) throws RemoteException {
        return c.getMessageStats();
    }
}
