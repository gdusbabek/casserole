package org.apache.cassandra.casserole.ui;

import org.apache.cassandra.casserole.Connection;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Future;

public abstract class RefreshingPanel extends JPanel {
    protected final Timer timer = new Timer(0, new ActionListener() {
        Future lastUpdate = null;
        public void actionPerformed(ActionEvent e) {
            if (lastUpdate != null)
                lastUpdate.cancel(true);
            lastUpdate = OffSwingWorker.submit(getRefresher(), null);
        }
    });
    
    public RefreshingPanel() {
        super();
        timer.setDelay(5000);
        timer.start();
    }
    
    protected abstract Runnable getRefresher();
    public abstract void setConnection(Connection con);
}
