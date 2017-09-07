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
public class RunTime_Display {
 
    private final static Logger LOGGER = Logger.getLogger(RunTime_Display.class.getName());
    
    protected static final int MAX_RUNTIME_DATA = 13;
    
    private final JLabel RunTime_Label_Array[] = new JLabel[MAX_RUNTIME_DATA];
    
    private String strRunTime_Element_Array[];

    private final Update_GUI Update_The_GUI = new Update_GUI();
    
    
    
    protected void Set_JLabel(int iIndex, JLabel labelControl) {
        
        if ((iIndex < 0) || (iIndex > MAX_RUNTIME_DATA - 1)) {
            String strMessage = "Set_JLabel() Index Out Of Range.  Range: 0-" + Integer.toString(MAX_RUNTIME_DATA - 1) + " Passed Value: " + Integer.toString(iIndex);
            
            LOGGER.log(Level.SEVERE, strMessage);
            return;
        }
        
        RunTime_Label_Array[iIndex] = labelControl;
    }
    
    
    protected void Show_Data(String strData) {
        
        int iClockTime = 0;
        int iUpTime = 0;
        int iTempCycles = 0;
        int iADCCycles = 0;

        String strSplit = strData;  
        strRunTime_Element_Array = strSplit.split(",");

        // the last two elements are created here and are not a part of the data stream
        while (strRunTime_Element_Array.length < MAX_RUNTIME_DATA - 2) {
            strSplit += ",0";
            strRunTime_Element_Array = strSplit.split(",");
        }

        
        // the reason this is -2 is because the last two fields are computed.
        // those files are not included in the list of data sent.
        for (int iIndex = 0; iIndex < MAX_RUNTIME_DATA - 2; iIndex++) {
            
            // OK, what for two values and do the math at the end....
            String strDataElement = strRunTime_Element_Array[iIndex];
            
            // when board is running with sensors again, change this to 2 & 4
            if (iIndex == 1) iUpTime = Integer.parseInt(strDataElement);
            if (iIndex == 3) iTempCycles = Integer.parseInt(strDataElement);
            if (iIndex == 5) iADCCycles = Integer.parseInt(strDataElement);
            
            // these are time fields
            if ((iIndex == 1) || (iIndex == 8)) {
                
                int iTime =  Integer.parseInt(strDataElement);

                int iDays = 0;
                if (iTime > 86400) {
                    iDays = iTime / 86400;  // number of days.
                    iTime = iTime % 86400;  // remainder after "subtracting" out days
                }

                String strLapsedTime = String.format("%d %02d:%02d:%02d", iDays, iTime / 3600, (iTime / 60) % 60, iTime % 60);
                
                Update_The_GUI.Update(RunTime_Label_Array[iIndex], strLapsedTime);
            }
            else { // 0, 2, 3, 4, 5, 6, 7  or everything else...
                Update_The_GUI.Update(RunTime_Label_Array[iIndex], strDataElement);
            }
        }
        
        // and now to display the computed fields...
        // setup Temperatures Cycles Per Second
        float fTempCyclesPerSec = (float) iTempCycles / iUpTime;
        String strDataElement = String.format("%.02f", fTempCyclesPerSec);
        Update_The_GUI.Update(RunTime_Label_Array[MAX_RUNTIME_DATA - 2], strDataElement);
        
        // setup ADC Cycles Per Second
        float fADCCyclesPerSec = (float) iADCCycles / iUpTime;
        strDataElement = String.format("%.02f", fADCCyclesPerSec);
        Update_The_GUI.Update(RunTime_Label_Array[MAX_RUNTIME_DATA - 1], strDataElement);
        
    }
}


