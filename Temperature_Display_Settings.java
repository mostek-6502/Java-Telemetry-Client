/**
 *
 * @author Solar
 */

package my.Viewer;


import java.awt.Color;

public class Temperature_Display_Settings {
    
    private static final int MAX_TEMPERATURE_DATA = 16;
    
    private final Background_Colors bgColors = new Background_Colors();
    
    private static final String strFlagArray[]  = {"0",      "20",       "30",        "40",       "50",     "60",        "85",        "90"       , "99"      };
    private static final String strFlagStatus[] = {"OK",     "Chip Rst", "Channel-1", "ROM Rtrv", "No Cfg", "Temp Strt", "Channel-2", "Temp Retv", "Unknown" };
    
    
    protected int GetIndexOfStatusFlagAndBackGroundColor(String strStatusFlag) {
        
        for (int iStatusIndex = 0; iStatusIndex < strFlagArray.length; iStatusIndex++) {

            if (strStatusFlag.equals(strFlagArray[iStatusIndex])) {
                    
                return iStatusIndex;
            }
        }

        // this should be the last element in the array.
        // this is an error designation.
        return strFlagArray.length - 1;  
    }

    protected String GetFlag(int iIndex) {
        return strFlagStatus[iIndex];   // the code in GetIndexOfStatusFlagAndBackGroundColor should control this index
    }
    
    protected Color GetColor(int iIndex) {
        
        if (iIndex == 0) { 
            return bgColors.getPaleGreen();
        }
            
        return bgColors.getOrangeRed();
    }

    protected int GetMaxTemperatures() {
        return MAX_TEMPERATURE_DATA;
    }
    
}
