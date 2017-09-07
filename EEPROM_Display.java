/*
 * Author: Rick Faszold
 * 
 * Comments: This portion of the code, display data from the controller board 
 * that has the Header Tag of "EEPROM  ".  The Data is displayed in the same
 * order in which it was sent to make things easy.  In the initialization
 * of Viewer, all of the JLables are 'registered' with this Object.  Once the
 * JLabels are setup and the data comes in, then it's a simple / fast loop to
 * display all of the data.
 *
 */

package my.Viewer;

import java.util.logging.Level;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.util.logging.Logger;
import javax.swing.SwingUtilities;



public class EEPROM_Display extends JPanel {
    
    private final static Logger LOGGER = Logger.getLogger(EEPROM_Display.class.getName());

    private String strEEPROM_Element_Array[];
    
    private static final int MAX_EEPROM_DATA = 17;
    
    private JLabel EEPROM_Label_Array[] = new JLabel[MAX_EEPROM_DATA];

    private Update_GUI Update_The_GUI = new Update_GUI();
    
    protected void Set_JLabel(int iIndex, JLabel labelControl) {
        
        if ((iIndex < 0) || (iIndex > MAX_EEPROM_DATA - 1)) {
            String strMessage = "Set_JLabel()  Index Out Of Range.  Range: 0-" + Integer.toString(MAX_EEPROM_DATA - 1) + " Passed Value: " + Integer.toString(iIndex);
            
            LOGGER.log(Level.SEVERE, strMessage);
            return;
        }
        
        EEPROM_Label_Array[iIndex] = labelControl;
    }
    

    protected void Show_Data(String strData) {

        // if the data is lacking, it's easier to fill it with zero fields...
        String strTemp = strData;  
        strEEPROM_Element_Array = strTemp.split(",");

        while (strEEPROM_Element_Array.length < MAX_EEPROM_DATA) {
            strTemp += ",0";
            strEEPROM_Element_Array = strTemp.split(",");
        }

        
        for (int iIndex = 0; iIndex < MAX_EEPROM_DATA; iIndex++) {
            
            Update_The_GUI.Update(EEPROM_Label_Array[iIndex], strEEPROM_Element_Array[iIndex]);
        }
        
    }
    
}
