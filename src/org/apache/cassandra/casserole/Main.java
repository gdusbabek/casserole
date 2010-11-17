package org.apache.cassandra.casserole;

import org.apache.cassandra.casserole.ui.MainPanel;

import javax.swing.*;

public class Main
{
    public static void main(String args[])
    {
        Runnable r = new Runnable()
        {
            public void run()
            {
                MainPanel mainPanel = new MainPanel();
                JFrame frame = new JFrame("Casserole");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setContentPane(mainPanel);
                frame.setSize(800, 600);
                frame.setVisible(true);
                mainPanel.refresh();
            }
        };
        SwingUtilities.invokeLater(r);
    }
}
