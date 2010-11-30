/*
 * Created by JFormDesigner on Tue Nov 02 16:50:40 CDT 2010
 */

package casserole.ui;

import javax.swing.*;
import com.jgoodies.forms.layout.*;

public class JFormPanel extends JPanel {
    public JFormPanel() {
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        label1 = new JLabel();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setLayout(new FormLayout(
            "default:grow",
            "fill:default:grow"));

        //---- label1 ----
        label1.setText("Nothing happening right now.");
        add(label1, cc.xy(1, 1));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel label1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
