/*
 * Created by JFormDesigner on Wed Nov 03 08:18:16 CDT 2010
 */

package org.apache.cassandra.casserole.ui;

import java.awt.event.*;
import javax.swing.*;
import com.jgoodies.forms.layout.*;
import org.apache.cassandra.casserole.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPanel.class.getPackage().getName());
    
    public ConnectionPanel() {
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        label1 = new JLabel();
        host = new JTextField();
        label2 = new JLabel();
        jmxPort = new JTextField();
        label3 = new JLabel();
        thriftPort = new JTextField();
        panel1 = new JPanel();
        button1 = new JButton();
        button2 = new JButton();
        connectAction = new ConnectAction();
        testAction = new TestAction();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setLayout(new FormLayout(
            "default, $lcgap, 110dlu",
            "3*(default, $lgap), default"));

        //---- label1 ----
        label1.setText("Host:");
        add(label1, cc.xy(1, 1));

        //---- host ----
        host.setText("127.0.0.1");
        add(host, cc.xy(3, 1));

        //---- label2 ----
        label2.setText("JMX Port:");
        add(label2, cc.xy(1, 3));

        //---- jmxPort ----
        jmxPort.setText("8081");
        add(jmxPort, cc.xy(3, 3));

        //---- label3 ----
        label3.setText("Thrift Port");
        add(label3, cc.xy(1, 5));

        //---- thriftPort ----
        thriftPort.setText("9160");
        add(thriftPort, cc.xy(3, 5));

        //======== panel1 ========
        {
            panel1.setLayout(new FormLayout(
                "default, $lcgap, default",
                "default"));

            //---- button1 ----
            button1.setAction(testAction);
            panel1.add(button1, cc.xy(1, 1));

            //---- button2 ----
            button2.setAction(connectAction);
            panel1.add(button2, cc.xy(3, 1));
        }
        add(panel1, cc.xy(3, 7));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel label1;
    private JTextField host;
    private JLabel label2;
    private JTextField jmxPort;
    private JLabel label3;
    private JTextField thriftPort;
    private JPanel panel1;
    private JButton button1;
    private JButton button2;
    private ConnectAction connectAction;
    private TestAction testAction;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private class ConnectAction extends AbstractAction {
        private ConnectAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Connect");
            putValue(SHORT_DESCRIPTION, "Connect to Cassandra");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_MASK));
            putValue(ACTION_COMMAND_KEY, "connect");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            Connection con = (Connection)getClientProperty(ComponentProperties.CONNECTION);
            con.setHost(host.getText());
            con.setJmxPort(Integer.parseInt(jmxPort.getText()));
            con.setThriftPort(Integer.parseInt(thriftPort.getText()));
            try {
                con.connect();
                ConnectionPanel.this.firePropertyChange(ComponentProperties.CONNECTION, false, true);
            }
            catch (Exception ex)
            {
                logger.error(ex.getMessage(), ex);
                JOptionPane.showMessageDialog(ConnectionPanel.this, "Connection failed.");
            }
        }
    }

    private class TestAction extends AbstractAction {
        private TestAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Test");
            putValue(SHORT_DESCRIPTION, "Tests connection properties");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_MASK));
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            Connection con = new Connection();
            con.setHost(host.getText());
            con.setJmxPort(Integer.parseInt(jmxPort.getText()));
            con.setThriftPort(Integer.parseInt(thriftPort.getText()));
            try {
                con.connect();
                JOptionPane.showMessageDialog(ConnectionPanel.this, "Success!");
                con.disconnect();
            }
            catch (Exception ex) {
                logger.debug(ex.getMessage(), ex);
                JOptionPane.showMessageDialog(ConnectionPanel.this, "Connection failed.");
            }
        }
    }
}
