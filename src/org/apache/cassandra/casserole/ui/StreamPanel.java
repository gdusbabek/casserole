/*
 * Created by JFormDesigner on Tue Nov 16 15:35:06 CST 2010
 */

package org.apache.cassandra.casserole.ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;

import com.jgoodies.forms.layout.*;
import org.apache.cassandra.casserole.Connection;
import org.apache.cassandra.casserole.util.ParseHelp;
import org.apache.cassandra.streaming.StreamingServiceMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class StreamPanel extends RefreshingPanel {
    private static final Logger logger = LoggerFactory.getLogger(StreamPanel.class.getPackage().getName());
    
    private Connection con;
    private List<FilesModel> files = new ArrayList<FilesModel>();
    DefaultComboBoxModel dstModel = new DefaultComboBoxModel();
    DefaultComboBoxModel srcModel = new DefaultComboBoxModel();
    private DefaultComboBoxModel sectionsModel = new DefaultComboBoxModel();
    private FileTableModel fileTableModel = new FileTableModel();
    private Runnable refresher = new Runnable() { public void run() {
        if (destList.getSelectedIndex() >= 0)
                destChanged();
            else if (srcList.getSelectedIndex() >= 0)
                srcChanged();
    }};
    
    public StreamPanel() {
        initComponents();
        
        destList.setModel(dstModel);
        srcList.setModel(srcModel);
        filesTable.setModel(fileTableModel);
        sectionList.setModel(sectionsModel);
        filesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                int row = filesTable.getSelectedRow();
                if (row < 0) return;
                sectionsModel.removeAllElements();
                for (String section : files.get(row).sections)
                    sectionsModel.addElement(section);
            }
        });
    }

    protected Runnable getRefresher() {
        return refresher;
    }

    public void setConnection(Connection con) {
        this.con = con;
        
        try {
            StreamingServiceMBean ss = con.getStreamingService();
            srcModel.removeAllElements();
            for (InetAddress src : ss.getStreamSources())
                srcModel.addElement(src.getHostAddress());
            dstModel.removeAllElements();
            for (InetAddress dst : ss.getStreamDestinations())
                dstModel.addElement(dst.getHostAddress());
            // clear table model, section model.
            files.clear();
            fileTableModel.fireTableDataChanged();
            sectionsModel.removeAllElements();
        } catch (RemoteException ex) {
            logger.info("Selected node likely dead.");
        } catch (UndeclaredThrowableException ex) {
            logger.debug(ex.getMessage(), ex);
            con.disconnect();
        }
        
    }

    private void populateTable(List<String> fileDescriptions) {
        files.clear();
        for (String desc : fileDescriptions)
            files.add(new FilesModel(ParseHelp.parsePendingFile(desc)));
        sectionsModel.removeAllElements();
        fileTableModel.fireTableDataChanged();
    }

    private void destChanged() {
        if (destList.getSelectedValue() == null) return;
        srcList.getSelectionModel().removeSelectionInterval(0, srcList.getModel().getSize());
        String dst = destList.getSelectedValue().toString();
        try {
            populateTable(con.getStreamingService().getOutgoingFiles(dst));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void srcChanged() {
        if (srcList.getSelectedValue() == null) return;
        destList.getSelectionModel().removeSelectionInterval(0, destList.getModel().getSize());
        String src = srcList.getSelectedValue().toString();
        try {
            populateTable(con.getStreamingService().getIncomingFiles(src));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private class FileTableModel extends AbstractTableModel {
        private final String[] COLS = {"Path", "Bytes Transferred", "Bytes Total"};
        public int getRowCount() {
            return files == null ? 0 : files.size();
        }

        public int getColumnCount() {
            return COLS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLS[column];
        }

        public Object getValueAt(int rowIndex, int columnIndex)
        {
            FilesModel file = files.get(rowIndex);
            switch (columnIndex) {
                case 0: return file.file;
                case 1: return file.bytesTransferred;
                case 2: return file.totalBytes;
                default: return "eh?";
            }
        }
    }
    
    private class FilesModel {
        private String file;
        private String bytesTransferred;
        private String totalBytes;
        private List<String> sections;
        
        FilesModel(List<String> pieces) {
            assert pieces.size() >= 3;
            file = pieces.get(0);
            bytesTransferred = pieces.get(pieces.size()-2);
            totalBytes = pieces.get(pieces.size()-1);
            sections = new ArrayList<String>();
            for (int i = 1; i < pieces.size()-2; i++)
                sections.add(String.format("%s, %s", pieces.get(i), pieces.get(i+1)));
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        splitPane1 = new JSplitPane();
        panel2 = new JPanel();
        panel3 = new JPanel();
        scrollPane2 = new JScrollPane();
        destList = new JList();
        panel4 = new JPanel();
        scrollPane4 = new JScrollPane();
        srcList = new JList();
        panel6 = new JPanel();
        panel7 = new JPanel();
        filesTableScroller = new JScrollPane();
        filesTable = new JTable();
        panel8 = new JPanel();
        scrollPane5 = new JScrollPane();
        sectionList = new JList();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setLayout(new FormLayout(
            "default:grow",
            "fill:default:grow"));

        //======== splitPane1 ========
        {
            splitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);

            //======== panel2 ========
            {
                panel2.setLayout(new FormLayout(
                    "default:grow, $lcgap, default:grow",
                    "fill:default:grow"));

                //======== panel3 ========
                {
                    panel3.setBorder(new TitledBorder("Destinations"));
                    panel3.setLayout(new FormLayout(
                        "default:grow",
                        "fill:default:grow"));

                    //======== scrollPane2 ========
                    {

                        //---- destList ----
                        destList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        destList.setVisibleRowCount(5);
                        destList.addListSelectionListener(new ListSelectionListener() {
                            public void valueChanged(ListSelectionEvent e) {
                                destChanged();
                            }
                        });
                        scrollPane2.setViewportView(destList);
                    }
                    panel3.add(scrollPane2, cc.xy(1, 1));
                }
                panel2.add(panel3, cc.xy(1, 1));

                //======== panel4 ========
                {
                    panel4.setBorder(new TitledBorder("Sources"));
                    panel4.setLayout(new FormLayout(
                        "default:grow",
                        "fill:default:grow"));

                    //======== scrollPane4 ========
                    {

                        //---- srcList ----
                        srcList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        srcList.setVisibleRowCount(5);
                        srcList.addListSelectionListener(new ListSelectionListener() {
                            public void valueChanged(ListSelectionEvent e) {
                                srcChanged();
                            }
                        });
                        scrollPane4.setViewportView(srcList);
                    }
                    panel4.add(scrollPane4, cc.xy(1, 1));
                }
                panel2.add(panel4, cc.xy(3, 1));
            }
            splitPane1.setTopComponent(panel2);

            //======== panel6 ========
            {
                panel6.setLayout(new FormLayout(
                    "default:grow",
                    "fill:default:grow, $lgap, fill:default:grow"));

                //======== panel7 ========
                {
                    panel7.setBorder(new TitledBorder("File Information"));
                    panel7.setLayout(new FormLayout(
                        "default:grow",
                        "fill:default:grow"));

                    //======== filesTableScroller ========
                    {

                        //---- filesTable ----
                        filesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        filesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                        filesTableScroller.setViewportView(filesTable);
                    }
                    panel7.add(filesTableScroller, cc.xy(1, 1));
                }
                panel6.add(panel7, cc.xy(1, 1));

                //======== panel8 ========
                {
                    panel8.setBorder(new TitledBorder("Sections Information"));
                    panel8.setLayout(new FormLayout(
                        "default:grow",
                        "fill:default:grow"));

                    //======== scrollPane5 ========
                    {
                        scrollPane5.setViewportView(sectionList);
                    }
                    panel8.add(scrollPane5, cc.xy(1, 1));
                }
                panel6.add(panel8, cc.xy(1, 3));
            }
            splitPane1.setBottomComponent(panel6);
        }
        add(splitPane1, cc.xy(1, 1));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JSplitPane splitPane1;
    private JPanel panel2;
    private JPanel panel3;
    private JScrollPane scrollPane2;
    private JList destList;
    private JPanel panel4;
    private JScrollPane scrollPane4;
    private JList srcList;
    private JPanel panel6;
    private JPanel panel7;
    private JScrollPane filesTableScroller;
    private JTable filesTable;
    private JPanel panel8;
    private JScrollPane scrollPane5;
    private JList sectionList;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
