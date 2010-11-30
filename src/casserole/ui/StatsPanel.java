/*
 * Created by JFormDesigner on Tue Nov 16 10:59:20 CST 2010
 */

package casserole.ui;

import javax.swing.*;
import com.jgoodies.forms.layout.*;
import casserole.Connection;
import casserole.model.RowData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.UndeclaredThrowableException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class StatsPanel extends RefreshingPanel {
    protected static final Logger logger = LoggerFactory.getLogger(StatsPanel.class.getPackage().getName());
    
    private Connection con;
    protected final UpdateTableModel<String, RowData> model;
    private final Runnable refresher;
    
    public StatsPanel() {
        this(new String[] {});
    }
    
    public StatsPanel(String[] cols) {
        super();
        initComponents();
        
        model = new UpdateTableModel<String, RowData>(cols);
        refresher = new Runnable() {
            private Connection curCon = con;
            private Lock lock = new ReentrantLock(true);
            public void run() {
                long start = System.currentTimeMillis();
                if (!lock.tryLock() && curCon == con) 
                    return;
                try {
                    curCon = con;
                    if (curCon.isUnstable()) return; 
                    if (isVisible() || model.getRowCount() == 0) {
                        Set<String> existing = model.getKeys();
                        Map<String, RowData> newStats = new HashMap<String, RowData>();
                        try {
                            for (RowData stat : getUpdatedRowData(curCon))
                                newStats.put(stat.getName(), stat);
                            
                            if (curCon != con)
                                return;
                            
                            Set<String> leaving = new HashSet<String>(existing);
                            leaving.removeAll(newStats.keySet());
                            for (String key : leaving)
                                model.remove(key);
                            
                            Set<String> arriving = new HashSet<String>(newStats.keySet());
                            arriving.removeAll(existing);
                            for (String key : arriving)
                                model.insert(key, newStats.get(key));
                            
                            Set<String> staying = new HashSet<String>(existing);
                            staying.removeAll(leaving);
                            for (String key : staying)
                                model.update(key, newStats.get(key));
                        } catch (RemoteException ex) {
                            logger.info("Selected node likely dead. " + ex.getMessage());
                        } catch (UndeclaredThrowableException ex) {
                            logger.debug(ex.getMessage(), ex);
                            curCon.disconnect();
                        }
                    }
                } finally {
                    lock.unlock();
                }
                logger.trace(StatsPanel.this.getClass().getSimpleName() + " refresh - " + (System.currentTimeMillis() - start) + " " + isVisible());
            }
        };
    }
    
    protected abstract Collection<? extends RowData> getUpdatedRowData(Connection c) throws RemoteException;
            
    protected Runnable getRefresher() {
        return refresher;
    }

    public void setConnection(Connection con)
    {
        timer.stop();
        this.con = con;
        model.clear();
        timer.start();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        scrollPane1 = new JScrollPane();
        statsTable = new JTable();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setLayout(new FormLayout(
            "default:grow",
            "fill:default:grow"));

        //======== scrollPane1 ========
        {

            //---- statsTable ----
            statsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            statsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            scrollPane1.setViewportView(statsTable);
        }
        add(scrollPane1, cc.xy(1, 1));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JScrollPane scrollPane1;
    protected JTable statsTable;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
