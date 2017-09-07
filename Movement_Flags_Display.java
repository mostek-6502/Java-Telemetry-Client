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
public class Movement_Flags_Display {
    
    private final static Logger LOGGER = Logger.getLogger(Movement_Flags_Display.class.getName());
    
    private String strMovementFlags_Element_Array[];
    
    private static final int MAX_FLAG_DATA = 8;
    
    private JLabel Movement_Flags_Label_Array[] = new JLabel[MAX_FLAG_DATA];

    private Update_GUI Update_The_GUI = new Update_GUI();

    private Background_Colors bgColors = new Background_Colors();
    
    
    protected void Set_JLabel(int iIndex, JLabel labelControl) {
        
        if ((iIndex < 0) || (iIndex > MAX_FLAG_DATA - 1)) {
            String strMessage = "Set_JLabel()  Index Out Of Range.  Range: 0-" + Integer.toString(MAX_FLAG_DATA - 1) + " Passed Value: " + Integer.toString(iIndex);
            
            LOGGER.log(Level.SEVERE, strMessage);
            return;
        }
        
        Movement_Flags_Label_Array[iIndex] = labelControl;
    }
    
    private int CvtStatusToInt(String strNumber) {

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
    
        String strTemp = strData;  
        
        strMovementFlags_Element_Array = strTemp.split(",");
        while (strMovementFlags_Element_Array.length < MAX_FLAG_DATA) {
            strTemp += ",0";
            strMovementFlags_Element_Array = strTemp.split(",");
        }

        
        for (int iIndex = 0; iIndex < MAX_FLAG_DATA; iIndex++) {

            if (iIndex == 0) {

                Update_The_GUI.Update(Movement_Flags_Label_Array[iIndex], strMovementFlags_Element_Array[iIndex]);
                
            }
            else if ((iIndex == 1) || (iIndex == 2)) {
                if (iIndex == 1) {
                    String strWindSpeed = strMovementFlags_Element_Array[iIndex + 0];
                    int iWindSpeedStatus = Integer.parseInt(strMovementFlags_Element_Array[iIndex + 1]);

                    String strWindSpeedStatus = "OK";
                    Color colorBackGround = bgColors.getPaleGreen();
                    if (iWindSpeedStatus == 1)
                    {
                        strWindSpeedStatus = "Caution";
                        colorBackGround = bgColors.getYellow();
                    }
                    else if (iWindSpeedStatus == 2)
                    {
                        strWindSpeedStatus = "Alert";
                        colorBackGround = bgColors.getOrangeRed();
                    }

                    Update_The_GUI.Update(Movement_Flags_Label_Array[iIndex + 0], strWindSpeed, colorBackGround);
                    Update_The_GUI.Update(Movement_Flags_Label_Array[iIndex + 1], strWindSpeedStatus,  colorBackGround);
                }
            }
            else if (iIndex == 3) {

                int iWindFaiLSafeStatus = Integer.parseInt(strMovementFlags_Element_Array[iIndex]);
                
                String strFailSafeStatus = "Connected";
                Color colorBackGround = bgColors.getPaleGreen();
                
                if (iWindFaiLSafeStatus == 0) {
                    strFailSafeStatus = "Disconnected";
                    colorBackGround = bgColors.getOrangeRed();
                }

                Update_The_GUI.Update(Movement_Flags_Label_Array[iIndex], strFailSafeStatus,  colorBackGround);
            }
            else {
                
                String strIndex = strMovementFlags_Element_Array[iIndex];
                int iProximityStatus = CvtStatusToInt(strIndex);
                
                
                Color colorBackGround = bgColors.getPaleGreen();
                String strProximityLimit = "OK";

                if (iProximityStatus > 0) {
                    colorBackGround = bgColors.getYellow();
                    strProximityLimit = "Limit";
                }

                Update_The_GUI.Update(Movement_Flags_Label_Array[iIndex], strProximityLimit,  colorBackGround);
            }
        }
    }
    
}

