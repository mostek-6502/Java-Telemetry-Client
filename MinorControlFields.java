/**
 *
 * Author: Rick Faszold
 */

package my.Viewer;

import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;


public class MinorControlFields {
    
    private final static Logger LOGGER = Logger.getLogger(MinorControlFields.class.getName());
    
    private JLabel Label_Remote_IP = null;
    private JButton Button_Change_EEPROM_Settings = null;
    
    private final Update_GUI Update_The_GUI = new Update_GUI();
    
    private boolean bCurrentState = false;
    
    public MinorControlFields() {
    }
    
    public void SetControlFields(JLabel Label_Remote_IP, JButton Button_Change_EEPROM_Settings) {
        this.Label_Remote_IP = Label_Remote_IP;
        this.Button_Change_EEPROM_Settings = Button_Change_EEPROM_Settings;
    }
    
    public void Update_Minor_Controls(boolean bUDPReady, String strRemoteIP) {
        
        if (Label_Remote_IP == null) {
            return;
        }

        if (Button_Change_EEPROM_Settings == null) {
            return;
        }

        
        if (bUDPReady) {
            if (bCurrentState == true) return;
            bCurrentState = true;
            
            Update_The_GUI.Enable(Button_Change_EEPROM_Settings, true);
            Update_The_GUI.Update(Label_Remote_IP, strRemoteIP);
        } else {
            if (bCurrentState == false) return;
            bCurrentState = false;

            Update_The_GUI.Enable(Button_Change_EEPROM_Settings, false);
            Update_The_GUI.Update(Label_Remote_IP, "");
        }
        
    }
    
}
