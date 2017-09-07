/*
 * Rick Faszold
 * 
 * This is a simple call to update a Client field embedded within a Thread.
 * Since everything being updated is a JLabel, there is not a huge need for
 * polymorphism.  Although, I did add a method for Color.
 *
 * Also, the calls from within the routines are all reduced to one line.
 */
package my.Viewer;

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 *
 * @author Solar
 */
public class Update_GUI {
 
    void Update(JLabel jLabel, String strText) {

        final JLabel jlFinalLabel = jLabel;
        final String strFinalText = strText;
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                jlFinalLabel.setText(strFinalText);
            }
        });
    }

    void Update(JLabel jLabel, String strText, Color cColor) {

        // jLabel.getContentPane().setBackground(Color.CYAN);
        
        final Color cFinalColor = cColor;
        final JLabel jlFinalLabel = jLabel;
        final String strFinalText = strText;
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                jlFinalLabel.setText(strFinalText);
                
                jlFinalLabel.setOpaque(true);
                jlFinalLabel.setBackground(cColor);
            }
        });
    }

    
    void Enable(JButton jButton, boolean bEnable) {

        final JButton jlFinalButton = jButton;
        final boolean bFinalEnable = bEnable;
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                jlFinalButton.setEnabled(bFinalEnable);
            }
        });
    }

    
}
