/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my.Viewer;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Rick Faszold
 * 
 */
public class Thread_Processing {
    
    private final static Logger LOGGER = Logger.getLogger(Thread_Processing.class.getName());
    
    // Create a list shared by producer and consumer Size of list is 2.
    // might declare this as volitile...
    LinkedList<String> list = new LinkedList<>();
    int capacity = 50;

    static UDP_Communications UDP_Comm = null;
    static EEPROM_Display EEPROM_Show = null;
    static Temperature_Display Temp_Show = null;
    static RunTime_Display RunTime_Show = null;
    static Pump_Display Pump_Show = null;
    static Movement_Flags_Display MoveFlags_Show = null;
    static Dish_Display Dish_Show = null;    
    static EEPROM_Interface EEPROM_Setup = null;
    static MinorControlFields Minor_Controls = null;
    
    Thread_Processing(UDP_Communications UDP_Comm, 
                        EEPROM_Display EEPROM_Show, 
                        Temperature_Display Temp_Show, 
                        RunTime_Display RunTime_Show,
                        Pump_Display Pump_Show,
                        Movement_Flags_Display MoveFlags_Show,
                        Dish_Display Dish_Show,
                        EEPROM_Interface EEPROM_Setup,
                        MinorControlFields Minor_Controls) {

        Thread_Processing.UDP_Comm = UDP_Comm;
        Thread_Processing.EEPROM_Show = EEPROM_Show;
        Thread_Processing.Temp_Show = Temp_Show;
        Thread_Processing.RunTime_Show = RunTime_Show;
        Thread_Processing.Pump_Show = Pump_Show;
        Thread_Processing.MoveFlags_Show = MoveFlags_Show;
        Thread_Processing.Dish_Show = Dish_Show;
        Thread_Processing.EEPROM_Setup = EEPROM_Setup;
        Thread_Processing.Minor_Controls = Minor_Controls;
    }
        
        // Function called by producer thread
    public void Thread_Retreive_UDP_Data(int iUDPPort) throws InterruptedException {

        boolean bBeaconSentFlag = false;
        
        int iRemoteIPFlag = 0;
        
        String strData;
        String strMessage;
        
        
        if (UDP_Comm.UDP_Setup_And_Start(iUDPPort) == false) {
            LOGGER.log(Level.SEVERE, "Thread_Setup_And_UDP_Start()::Invalid Return .");
            return;
        }

        
        while (true)
        {
            synchronized (this)
            {
                // producer thread waits while list is full
                while (list.size() >= capacity) {
                    wait();
                }
                
                
                strData = UDP_Comm.UDP_Data_Read();
                
                if (strData.startsWith("ERROR-")) {

                    strMessage = "Thread_Retreive_UDP_Data()::UDP_Data_Read() Invalid Return: " + strData;
                    
                    LOGGER.log(Level.SEVERE, strMessage);
                    return;
                }
                else if (strData.startsWith("TIMEOUT")) {

                    // The controller board can be configured to send data to a specific IP / Port.
                    // If preconfigured, the board starts sending data to the configured IP.
                    // Thus, upon start up, the client application can receive data immediately.
                    // However, if the IP is not configured and/or no data is coming to this client
                    // then, you can tell the controller board where you to get data are or steal the 
                    // stream from another IP.

                    //strMessage = "Thread_Retreive_UDP_Data()::UDP_Data_Read() Timeout!  Beacon_Sent_Flag: " + bBeaconSentFlag;
                    //LOGGER.log(Level.SEVERE, strMessage);

                    if (bBeaconSentFlag == false) {
                        if (UDP_Comm.UDP_Send_Beacon() == false) {
                            LOGGER.log(Level.SEVERE, "Thread_Retreive_UDP_Data()::UDP_Send_Beacon() Invalid Return.");
                            return;
                        }
                    
                        bBeaconSentFlag = true;
                    }
                    
                }
                else if ("NO DATA".equals(strData)) {
                    strMessage = "Thread_Retreive_UDP_Data()::UDP_Data_Read() NO DATA was Returned." + String.valueOf(list.size());

                    LOGGER.log(Level.SEVERE, strMessage);
                }
                else if ("SYNCH_1 ".equals(strData)) {
                    strMessage = "Thread_Retreive_UDP_Data()::UDP_Data_Read() 'SYNCH_1 ' was Returned.  Ignored." + String.valueOf(list.size());

                    LOGGER.log(Level.SEVERE, strMessage);

                }
                else {
                    
                    list.add(strData);

                    notify();

                    wait();
                }

            }
        }
    }
    
        // Function called by consumer thread
    public void Thread_Update_Screen() throws InterruptedException {

        int iRtn;
        String sData;
        String sTemp;
        
        while (true) {

            synchronized (this) {

                // debugging purposes only!
                
                while (list.isEmpty()) {
                    //LOGGER.log(Level.SEVERE, "Screen()   In Check Queue  Check Queue - Wait");
                    wait();
                }
                    
                sData = list.removeFirst();

                
                if (sData.startsWith("SYNCH_2")) {
                    if (UDP_Comm.UDP_Send_Packet("SYNCH_3 ") == false) {
                        LOGGER.log(Level.SEVERE, "Thread_Update_Screen() Process SYNCH_2 Invalid Return! ");
                        return;
                    }
                }
                else if (sData.startsWith("SEN-WSFP,")) {
                    // SEN = Sensor
                    // W = Wind, S - Wind Status
                    // F = Failsafe
                    // P = Proximity
                    MoveFlags_Show.Show_Data(sData);
                }
                else if (sData.startsWith("RUNTIME ,")) {
                    RunTime_Show.Show_Data(sData);
                }
                else if (sData.startsWith("DISH    ,")) {
                    Dish_Show.Show_Data(sData);
                }
                else if (sData.startsWith("EEPROM  ,")) {
                    EEPROM_Show.Show_Data(sData);
                }
                else if (sData.startsWith("TEMPS   ,")) {
                    Temp_Show.Show_Data(sData);
                }
                else if (sData.startsWith("PUMPS   ,")) {
                    Pump_Show.Show_Data(sData);
                }
                else if (sData.startsWith("COMREPLY,")) {
                    // is the Window Active?  Is it Visible?  If so, add the data....
                    if ((EEPROM_Setup.isVisible()) && (EEPROM_Setup.isActive())) {
                        EEPROM_Setup.Show_Data(sData);
                    }
                }

                String strRemoteIP = "";
                boolean bUDPDataReady= UDP_Comm.DataReady();
                if (bUDPDataReady) {
                    strRemoteIP = UDP_Comm.getRemoteIP();
                }
                Minor_Controls.Update_Minor_Controls(bUDPDataReady, strRemoteIP);

                
                
                // Wake up producer thread
                notify();
 
                // and sleep 
                wait();
            }
        }
    }
}
