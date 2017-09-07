/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my.Viewer;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;


/**
 *
 * @author Solar
 */
public class Pump_Display {

    private final static Logger LOGGER = Logger.getLogger(Pump_Display.class.getName());
    
    private String strPumpData_Element_Array[];
    
    private static final int MAX_PUMP_DATA = 10;
    
    private JLabel PUMP_Label_Array[] = new JLabel[MAX_PUMP_DATA];
                                               //             0         1         2            3           4           5              6                  7                  8                9
    private static final String strArrayPumpStatus[]    = {"Running",  "Off",  "Temp. Err", "Too Cold", "Too Hot",  "Therm. Off", "Failsafe Error", "House Temp. Err", "House Temp. High", "Unknown" };
    
    private final Update_GUI Update_The_GUI = new Update_GUI();

    private final Background_Colors bgColors = new Background_Colors();
   
    
    protected void Set_JLabel(int iIndex, JLabel labelControl) {
        
        if ((iIndex < 0) || (iIndex > MAX_PUMP_DATA - 1)) {
            String strMessage = "Set_JLabel()  Index Out Of Range.  Range: 0-" + Integer.toString(MAX_PUMP_DATA - 1) + " Passed Value: " + Integer.toString(iIndex);
            
            LOGGER.log(Level.SEVERE, strMessage);
            return;
        }
        
        PUMP_Label_Array[iIndex] = labelControl;
    }
    

    private Color GetPumpColor(int iPumpStatus) {
        
        if (iPumpStatus == 0) {
            return bgColors.getPaleGreen();
        } else if ((iPumpStatus == 1) || (iPumpStatus == 5)) {
            return bgColors.getYellow();
        } 
        
        return bgColors.getOrangeRed();
        
    }
    
    
    private String GetPumpStatus(int iPumpIndex) {
        
        if (iPumpIndex < strArrayPumpStatus.length)
            return strArrayPumpStatus[iPumpIndex];              
        
        // gets the last element in the list....
        return strArrayPumpStatus[strArrayPumpStatus.length - 1];  // the last element.
        
    }
    
    
    private int CvtPumpStatusToInt(String strNumber) {

        // the last byte of the stream may come back corrupted as far as length goes.
        // therefore, you have to know the potential lenght of the filed and adjust it
        // if not, the parseInt routine gets whacked out....

        String strTemp = strNumber + "\n";  // this will stop it.
        String strReturn = "";
        
        for(char c : strTemp.toCharArray()) {
            if ((c >= 48) && (c <= 57)) {
                strReturn += c;
            }
            else {
                break;
            }
        }        
        
        
        int iIndex;
        
        try {
            iIndex = Integer.parseInt(strReturn);
        } catch (NumberFormatException e) {
            iIndex = 99;
        }

        return iIndex;
    }

    
    protected void Show_Data(String strData) {

        
        // if the data is lacking, it's easire to fill it in with NULL and process empty fields.
        String strTemp = strData;  
        strPumpData_Element_Array = strTemp.split(",");

        // we are adding lenght to the array just so that we can properly process it.
        while (strPumpData_Element_Array.length < MAX_PUMP_DATA) {
            strTemp += ",0";
            strPumpData_Element_Array = strTemp.split(",");
        }
        
        for (int iIndex = 0; iIndex < MAX_PUMP_DATA; iIndex++) {
            

            if (iIndex == 0) {

                Update_The_GUI.Update(PUMP_Label_Array[iIndex], strPumpData_Element_Array[iIndex]);
                
            }
            else if (iIndex == 1) {

                int iOnOff = Integer.parseInt(strPumpData_Element_Array[iIndex]);
                
                String strThermostat = "On";
                Color colorBackGround = bgColors.getPaleGreen();
                if (iOnOff == 0) {
                    strThermostat = "Off";
                    colorBackGround = bgColors.getYellow();
                }
                
                Update_The_GUI.Update(PUMP_Label_Array[iIndex], strThermostat, colorBackGround);
            }
            else if ((iIndex == 2) || (iIndex == 4) || (iIndex == 6) || (iIndex == 8)) {
                // this section of code works with:
                //                  pump percent (2, 4, 6, 8)
                //                  pump status  (3, 5, 7, 9)
                //                  the background color of both....
                
                String strPumpPercent = strPumpData_Element_Array[iIndex + 0];
                int iPumpStatus = CvtPumpStatusToInt(strPumpData_Element_Array[iIndex + 1]);
                String strPumpStatus = GetPumpStatus(iPumpStatus);
                
                // determine colors
                Color colorBackGround = bgColors.getPaleGreen();
                if (iPumpStatus == 0) {
                    if (strPumpPercent.equals("0")) {
                        colorBackGround = bgColors.getYellow();
                    }
                }
                else {
                    colorBackGround = GetPumpColor(iPumpStatus);
                }
                
                Update_The_GUI.Update(PUMP_Label_Array[iIndex + 0], strPumpPercent, colorBackGround);
                Update_The_GUI.Update(PUMP_Label_Array[iIndex + 1], strPumpStatus, colorBackGround);
            }
        }
    }
    
}

