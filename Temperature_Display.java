/*
 * Author: Rick Faszold
 * 
 * Comments: This portion of the code, display data from the controller board 
 * that has the Header Tag of "TEMP    ".  The Data is displayed in the same
 * order in which it was sent to make things easy.  In the initialization
 * of Viewer, all of the JLables are 'registered' with this Object.  Once the
 * JLabels are setup and the data comes in, then it's a simple / fast loop to
 * display all of the data.
 *
 * This method is a little more interesting because, the temperatures were
 * assembled 'on the fly', so there is more processing to display a temperature.
 * Also, a Status Flag is sent with the temperature denoting an issue.  The
 * The code is setup to change the background colors of the 3 fields depending
 * on the incoming Status Flag.
 */


package my.Viewer;

import java.awt.Color;
import java.util.logging.Level;
import javax.swing.JLabel;

import java.util.logging.Logger;
/**
 *
 * @author Solar
 */
public class Temperature_Display {
    
    // Status,C-Whole,C-Fraction,C-Flag,F-Whole,F-Fraction,F-Flag REPEAT 15 more times....
    // EEPROM  , 7 Fields, then repeat 15 more times...
    
    private final static Logger LOGGER = Logger.getLogger(Temperature_Display.class.getName());
    
    private Temperature_Display_Settings tds = new Temperature_Display_Settings();
    
    private static final int MAX_TEMPERATURE_DATA = 16;    // Rows of Data
    private static final int TOTAL_TEMPERATURE_DATA = 129; // total number of incoming fields 

    private String strTemperature_Element_Array[];

    private final JLabel TEMP_F_Array[] = new JLabel[MAX_TEMPERATURE_DATA];
    private final JLabel TEMP_C_Array[] = new JLabel[MAX_TEMPERATURE_DATA];
    private final JLabel TEMP_Flag_Array[] = new JLabel[MAX_TEMPERATURE_DATA];
    private final JLabel TEMP_ROM_Array[] = new JLabel[MAX_TEMPERATURE_DATA];
    
    private Update_GUI Update_The_GUI = new Update_GUI();
    
    
    private boolean Check_JLabel_Index(int iIndex, String strPlace) {
        
        if ((iIndex < 0) || (iIndex > MAX_TEMPERATURE_DATA - 1)) {
            String strMessage = strPlace + " Index Out Of Range.  Range: 0-" + Integer.toString(MAX_TEMPERATURE_DATA - 1) + " Passed Value: " + Integer.toString(iIndex);
            
            LOGGER.log(Level.INFO, strMessage);
            return false;
        }
        
        return true;
    }
    
    
    protected void Set_C_JLabel(int iIndex, JLabel labelControl) {
        
        if (Check_JLabel_Index(iIndex, "Set_C_JLabel") == false) {
            return;
        }
        
        TEMP_C_Array[iIndex] = labelControl;
    }
    
    protected void Set_F_JLabel(int iIndex, JLabel labelControl) {
        
        if (Check_JLabel_Index(iIndex, "Set_F_JLabel") == false) {
            return;
        }
        
        TEMP_F_Array[iIndex] = labelControl;
    }

    protected void Set_Flag_JLabel(int iIndex, JLabel labelControl) {
        
        if (Check_JLabel_Index(iIndex, "Set_Flag_JLabel") == false) {
            return;
        }
        
        TEMP_Flag_Array[iIndex] = labelControl;
    }

    protected void Set_ROM_JLabel(int iIndex, JLabel labelControl) {
        
        if (Check_JLabel_Index(iIndex, "Set_ROM_JLabel") == false) {
            return;
        }
        
        TEMP_ROM_Array[iIndex] = labelControl;
    }

    
    
    private String GetTemperature(int iIndex) {
        
        String strWhole = strTemperature_Element_Array[iIndex + 0];
        String strFraction = strTemperature_Element_Array[iIndex + 1];
        String strSign = strTemperature_Element_Array[iIndex + 2];
        
        String strTemp = "";
        if (strSign.equals("1")) {
            strTemp = "-";
        }
        
        strTemp = strTemp.trim() + strWhole.trim() + "." + strFraction.trim();
        
        return strTemp;
    }

    private String Format_ROM_Codes(String strOldROMCode) {

        String strSearchString = strOldROMCode + "xxxxxxxxxxxxxxxx";
        
        strSearchString = strSearchString.substring(0, 16);


        String strNewROMCode = "";
        for (int iIndex = 0; iIndex < 16; iIndex += 2) {  // 0, 2, 4, 6, 8, A, C, E
            if (iIndex != 0) {
                strNewROMCode += "-";
            }
            
            // 0 2 4 6 8 A C E
            // 0123456789ABCDEF
            // 0011223344556677
            strNewROMCode += strSearchString.subSequence(iIndex, iIndex + 2);
        }

        return strNewROMCode;
    }
    
    
    protected void Show_Data(String strData) {
        // Status,C-Whole,C-Fraction,C-Flag,F-Whole,F-Fraction,F-Flag REPEAT 15 more times....
        
        String strCWhole;
        String strCFraction;
        String strCSign;

        String strFWhole;
        String strFFraction;
        String strFSign;
        
        // by doing this, we will always get data populated in every element
        // the gui can handle blank fields...
        String strTemp = strData; 
        strTemperature_Element_Array = strTemp.split(",");


        while (strTemperature_Element_Array.length < TOTAL_TEMPERATURE_DATA) {
            strTemp += ",0";
            strTemperature_Element_Array = strTemp.split(",");
        }

        
        for (int iJLabelIndex = 0; iJLabelIndex < MAX_TEMPERATURE_DATA; iJLabelIndex++) {
            
            // the first section takes the status of the temperature and 
            // sets a color and status flag verbiage
            int iDataIndex = (iJLabelIndex * 8) + 1;  // need to skip the first element which is the TAG label.

            String strStatusFlag = strTemperature_Element_Array[iDataIndex + 0];
            int iIndex = tds.GetIndexOfStatusFlagAndBackGroundColor(strStatusFlag);

            String strFlag = tds.GetFlag(iIndex);           // get the verbiage for the flag
            Color colorBackGround = tds.GetColor(iIndex);   // color all of the fields based on flag
            
            //if (iJLabelIndex > 14) {
            //    int i = 0;
            //    i++;
            //}
            
            String strTempROMCodes = strTemperature_Element_Array[iDataIndex + 7];
            String strROMCodes = Format_ROM_Codes(strTempROMCodes);

            Update_The_GUI.Update(TEMP_F_Array[iJLabelIndex], GetTemperature(iDataIndex + 4),  colorBackGround);
            Update_The_GUI.Update(TEMP_C_Array[iJLabelIndex], GetTemperature(iDataIndex + 1),  colorBackGround);
            Update_The_GUI.Update(TEMP_Flag_Array[iJLabelIndex], strFlag,  colorBackGround);
            Update_The_GUI.Update(TEMP_ROM_Array[iJLabelIndex], strROMCodes,  colorBackGround);

        }
    }
    
}

