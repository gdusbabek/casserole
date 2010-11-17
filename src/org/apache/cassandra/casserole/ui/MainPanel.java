package org.apache.cassandra.casserole.ui;

import com.jgoodies.forms.layout.CellConstraints;
import org.apache.cassandra.casserole.Connection;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class MainPanel extends JFormPanel
{
    private static final String LAST = "LAST";
    private Connection con = new Connection();
    
    public MainPanel()
    {
        super();
    }

    public void refresh()
    {
        CellConstraints cc = new CellConstraints();
        
        // need to login.
        if (!con.isConnected())
        {
            this.removeAll();
            final ConnectionPanel cp = new ConnectionPanel();
            cp.putClientProperty(ComponentProperties.CONNECTION, con);
            PropertyChangeListener pcl = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if ((Boolean)evt.getNewValue()) {
                        cp.removePropertyChangeListener(this);  
                        refresh();
                    }
                }
            };
            cp.addPropertyChangeListener(ComponentProperties.CONNECTION, pcl);
            this.add(cp, cc.xywh(1, 1, 1, 1));
        }
        
        // logged in, draw initial screen.
        else if (con.isConnected())
        {
            if (getClientProperty(LAST) != null)
                remove((Component)getClientProperty(LAST));
            
            final RingPanel rp = new RingPanel(con);
            rp.putClientProperty(ComponentProperties.CONNECTION, con);
            PropertyChangeListener pcl = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (!(Boolean)evt.getNewValue()) {
                        rp.removePropertyChangeListener(this);
                        refresh();
                    }
                }
            };
            rp.addPropertyChangeListener(ComponentProperties.CONNECTION, pcl);
            this.add(rp, cc.xywh(1, 1, 1, 1));
        }
        
        revalidate();
    }
    
    public void add(Component comp, Object constraints) 
    {
        putClientProperty(LAST, comp);
        super.add(comp, constraints);
    }
}
