/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my.Viewer;


import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;

/**
 *
 * @author Solar
 */
public class Dish_Display {
    
    private final static Logger LOGGER = Logger.getLogger(Dish_Display.class.getName());
    
    private String strDish_Element_Array[];
    
    private static final int MAX_DISH_DATA = 13;
    
    private JLabel Dish_Label_Array[] = new JLabel[MAX_DISH_DATA];

    private Update_GUI Update_The_GUI = new Update_GUI();
    
    private final String[] strMovementStatus = {
        "MOVE_OK_TO_MOVE",
        "MOVE_H_RIGHT",
        "MOVE_H_LEFT",
        "MOVE_V_UP",
        "MOVE_V_DOWN",
        "MOVE_TRANSITION",
        "MOVE_ERROR_WIND_SPEED",
        "MOVE_H_ERROR_DUAL_PROXIMITY",
        "MOVE_V_ERROR_DUAL_PROXIMITY",
        "NO_H_MOVEMENT_NEEDED",
        "NO_V_MOVEMENT_NEEDED",
        "NO_MOVE_PROXIMITY_DETECT_RIGHT",
        "NO_MOVE_PROXIMITY_DETECT_LEFT",
        "NO_MOVE_PROXIMITY_DETECT_UP",
        "NO_MOVE_PROXIMITY_DETECT_DOWN",
        "NO_H_MOVEMENT_PERCENT_RANGE",
        "NO_V_MOVEMENT_PERCENT_RANGE",
        "NO_MOVEMENT_FAILSAFE",
        "NO_MOVEMENT_MOTORS_OFF",
        "FORCE_MOVE_H_RIGHT",
        "FORCE_MOVE_H_LEFT",
        "FORCE_MOVE_V_UP",
        "FORCE_MOVE_V_DOWN",
        "FORCE_MOVE_H_PAUSE",
        "FORCE_MOVE_V_PAUSE",
        "TEMP_TOO_HIGH_MOVE_AWAY_FROM_SUN_H",
        "TEMP_TOO_HIGH_MOVE_AWAY_FROM_SUN_V",
        "ERROR_HORIZONTAL_MOVEMENT_FLAG",
        "ERROR_VERTICAL_MOVEMENT_FLAG",
        "ERROR_PHOTO_RESISTOR_OUT_OF_RANGE",
        "Unknown"};

    
    private String GetMovementStatus(int iMovementIndex) {
        
        if (iMovementIndex < strMovementStatus.length)
            return strMovementStatus[iMovementIndex];              
            
        return strMovementStatus[strMovementStatus.length - 1];  // the last element.
    }
    
    protected void Set_JLabel(int iIndex, JLabel labelControl) {
        
        if ((iIndex < 0) || (iIndex > MAX_DISH_DATA - 1)) {
            String strMessage = "Set_JLabel()  Index Out Of Range.  Range: 0-" + Integer.toString(MAX_DISH_DATA - 1) + " Passed Value: " + Integer.toString(iIndex);
            
            LOGGER.log(Level.SEVERE, strMessage);
            return;
        }
        
        Dish_Label_Array[iIndex] = labelControl;
        
    }
 
  
    private int CvtDishStatusToInt(String strNumber) {

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
        strDish_Element_Array = strTemp.split(",");

        while (strDish_Element_Array.length < MAX_DISH_DATA) {
            strTemp += ",0";
            strDish_Element_Array = strTemp.split(",");
        }

        for (int iIndex = 0; iIndex < MAX_DISH_DATA; iIndex++) {
            
            if ((iIndex == 6) || (iIndex == 12)) {

                int iStatus = CvtDishStatusToInt(strDish_Element_Array[iIndex]);
                
                Update_The_GUI.Update(Dish_Label_Array[iIndex], GetMovementStatus(iStatus));
            } else {
                Update_The_GUI.Update(Dish_Label_Array[iIndex], strDish_Element_Array[iIndex]);
            }
        }

    }
}

