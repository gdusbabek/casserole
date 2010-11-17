/*
 * Created by JFormDesigner on Wed Nov 03 10:38:42 CDT 2010
 */

package org.apache.cassandra.casserole.ui;

import java.awt.event.*;
import java.lang.reflect.UndeclaredThrowableException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.layout.*;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.casserole.Connection;
import org.apache.cassandra.casserole.ConnectionPool;
import org.apache.cassandra.casserole.model.RingData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RingPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(RingPanel.class.getPackage().getName());
    
    private Timer timer;
    private ActionListener updateListener;
    
    public RingPanel(final Connection con)
    {
        initComponents();
        ConnectionPool _pool = null;
        final UpdateTableModel<Token, RingData> tableModel = new UpdateTableModel<Token, RingData>(RingData.COLS);
        try { _pool = new ConnectionPool(con); } catch (RemoteException ex) { throw new RuntimeException(ex); }
        final ConnectionPool pool = _pool;
        
        ringTable.setModel(tableModel);
        ringTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ringTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                RingData node = tableModel.getRow(ringTable.getSelectedRow());
                Connection con = pool.getConnection(node.getHost());
                cfStatsPanel.setConnection(con);
                tpStatsPanel.setConnection(con);
                cacheStatsPanel.setConnection(con);
                indexStatsPanel.setConnection(con);
                msgStatsPanel.setConnection(con);
                streamPanel.setConnection(con);
                logger.trace("Node selection changed");
            }
        });
        updateListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (con == null) return;
                if (RingPanel.this.isVisible()) {
                    try {
                        if (!con.isConnected())
                            con.connect();
                        // todo: should happen off the edt.
                        Set<Token> existingData = tableModel.getKeys();
                        SortedMap<Token, RingData> updatedData = pool.getRingData();
                        
                        Set<Token> leaving = new HashSet<Token>(existingData);
                        leaving.removeAll(updatedData.keySet());
                        for (Token t : leaving)
                            tableModel.remove(t);
                        
                        Set<Token> arriving = new HashSet<Token>(updatedData.keySet());
                        arriving.removeAll(existingData);
                        for (Token t : arriving)
                            tableModel.insert(t, updatedData.get(t));
                        
                        Set<Token> staying = new HashSet<Token>(existingData);
                        staying.removeAll(leaving);
                        for (Token t : staying)
                            tableModel.update(t, updatedData.get(t));
                        
                        status.setText("Connected");
                    } catch (RemoteException ex) {
                        status.setText(ex.getMessage());
                        con.disconnect();
                    } catch (UndeclaredThrowableException ex) {
                        // indicate there are problems updating the data.
                        logger.info(ex.getMessage(), ex);
                        status.setText("RMI Error");
                        con.disconnect();
                    } catch (RuntimeException ex) {
                        // almost certainly jmx related.
                        logger.info(ex.getMessage(), ex);
                        status.setText("RMI Error");
                        con.disconnect();
                    }
                }
            }
        };
        timer = new Timer(2000, updateListener);
        timer.start(); 
    }
    
    public RingPanel() {
        this(null);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        panel2 = new JSplitPane();
        scrollPane1 = new JScrollPane();
        ringTable = new JTable();
        tabbedPane1 = new JTabbedPane();
        cfStatsPanel = new CfStatsPanel(false);
        tpStatsPanel = new TpStatsPanel();
        cacheStatsPanel = new CachePanel();
        indexStatsPanel = new CfStatsPanel(true);
        msgStatsPanel = new MsgStatsPanel();
        streamPanel = new StreamPanel();
        panel1 = new JPanel();
        status = new JLabel();
        disconnectBtn = new JButton();
        disconnectAction = new DisconnectAction();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setLayout(new FormLayout(
            "default:grow",
            "fill:default:grow, $lgap, default"));

        //======== panel2 ========
        {
            panel2.setOrientation(JSplitPane.VERTICAL_SPLIT);

            //======== scrollPane1 ========
            {

                //---- ringTable ----
                ringTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                ringTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                scrollPane1.setViewportView(ringTable);
            }
            panel2.setTopComponent(scrollPane1);

            //======== tabbedPane1 ========
            {
                tabbedPane1.addTab("Column Families", cfStatsPanel);

                tabbedPane1.addTab("Threadpools", tpStatsPanel);

                tabbedPane1.addTab("Caches", cacheStatsPanel);

                tabbedPane1.addTab("Indexes", indexStatsPanel);

                tabbedPane1.addTab("Messages", msgStatsPanel);

                tabbedPane1.addTab("Streams", streamPanel);

            }
            panel2.setBottomComponent(tabbedPane1);
        }
        add(panel2, cc.xy(1, 1));

        //======== panel1 ========
        {
            panel1.setLayout(new FormLayout(
                "2*(default, $lcgap), default:grow, $lcgap, right:default",
                "default"));

            //---- status ----
            status.setText("Status");
            status.setHorizontalAlignment(SwingConstants.CENTER);
            panel1.add(status, cc.xy(5, 1));

            //---- disconnectBtn ----
            disconnectBtn.setAction(disconnectAction);
            panel1.add(disconnectBtn, cc.xy(7, 1));
        }
        add(panel1, cc.xywh(1, 3, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JSplitPane panel2;
    private JScrollPane scrollPane1;
    private JTable ringTable;
    private JTabbedPane tabbedPane1;
    private CfStatsPanel cfStatsPanel;
    private TpStatsPanel tpStatsPanel;
    private CachePanel cacheStatsPanel;
    private CfStatsPanel indexStatsPanel;
    private MsgStatsPanel msgStatsPanel;
    private StreamPanel streamPanel;
    private JPanel panel1;
    private JLabel status;
    private JButton disconnectBtn;
    private DisconnectAction disconnectAction;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private class DisconnectAction extends AbstractAction {
        private DisconnectAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Disconnect");
            putValue(SHORT_DESCRIPTION, "Close all connections");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_MASK));
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            // disconnect button was clicked.
            Connection con = (Connection)RingPanel.this.getClientProperty(ComponentProperties.CONNECTION);
            con.disconnect();
            RingPanel.this.firePropertyChange(ComponentProperties.CONNECTION, true, false);
        }
    }
}
