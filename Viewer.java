/*
 * Rick Faszold
 * 
 * August 31st, 2017
 *
 * This program takes ASCII data from a Controller Board and displays the
 * data real time.  The data from the board is quick snap shots of the
 * current runtime state.  There are 118 pieces of data from the board.
 * Typically the board will output 20 full datasets a second while in a
 * production runtime state.  Removal of the sensors allow about 500
 * datasets to be captured per second.
 *
 * All of the controls on the main viewer are grouped into sections that
 * correcpond to an incoming message.  The data is ordered, thus the controls
 * were set in matching ordered arrays so that 'essentially', once the list of 
 * data arrives the control would be updated at the same time.  IE.  
 *    control[i].setText(data[i])...  This generally represents how GUI updates 
 * are made.  Each group of controls have their own processing display requirements.
 *
 * GUI updates are in a thread safe object using Swing.  Of course, GUI updates
 * are typically generated from the Threads.
 *
 * Data exchange is via UDP is in a thread.  In a seperate thread is the logic
 * that takes that data updates the screen
 * 
 * There is module that allows a user to interact with the board directly in order
 * to set - in real time - run time parameters.  The board will also respond 
 * to commands that will allow the dish to be moved via this client.  There is
 * absolutely no security in this data exchange... right now.  At some point, 
 * requests to make parameter changes will be protected.  
 *
 * The board can be set up to communicate with a specific server upon startup. 
 * At this point, the board firmware was designed to allow a hijack of that session 
 * from another device.  Again, this was by design.  The board obtains an IP
 * dynamically. 

 * To-Do Items
 * 1. The Starting and Ending the UDP session is not thread safe.
      One can easily close the UDP session while a read/write is taking place
      and hang the UDP session.
   2. Password Protect and challenge each transaction with a unique token
   3. Handle Port Changes during an up and running session.
   4. In the event of a DDoS, down grade the UDP Thread priority on the board
      to essentially reduce the impact of incoming data.  Thus, the 
      unit can remain fully operational while the attack is taking place.

*/

package my.Viewer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Solar
 */
public class Viewer extends javax.swing.JFrame {

    private final static Logger LOGGER = Logger.getLogger(Viewer.class.getName());
    
    protected UDP_Communications UDP_Comm = new UDP_Communications();
    protected EEPROM_Display EEPROM_Show = new EEPROM_Display();
    protected Temperature_Display Temp_Show = new Temperature_Display();
    protected RunTime_Display RunTime_Show = new RunTime_Display();
    protected Pump_Display Pump_Show = new Pump_Display();
    protected Movement_Flags_Display MoveFlags_Show = new Movement_Flags_Display();
    protected Dish_Display Dish_Show = new Dish_Display();
    EEPROM_Interface EEPROM_Setup = new EEPROM_Interface(this, false);
    MinorControlFields MinorControls = new MinorControlFields();
    
    protected Thread_Processing TProc = new Thread_Processing(UDP_Comm, 
                                                                EEPROM_Show, 
                                                                Temp_Show, 
                                                                RunTime_Show, 
                                                                Pump_Show,
                                                                MoveFlags_Show,
                                                                Dish_Show, 
                                                                EEPROM_Setup,
                                                                MinorControls);
    
    protected Thread_Setup TSet = null; // new Thread_Setup(UDP_Comm, TProc);
    
    /**
     * Creates new form Viewer
     */
    public Viewer() {
        initComponents();

        String strLocalIP = "Unknown";
        try {
            InetAddress ipAddr = InetAddress.getLocalHost();
            strLocalIP = ipAddr.getHostAddress();
        } catch (UnknownHostException ex) {
            LOGGER.log(Level.SEVERE, "Viewer()::Viewer() Unable to getLocalHost()", ex);
        }
        
        Label_Local_IP.setText(strLocalIP);
        Label_Local_UDP_Port.setText("55056");
        
        // all of the groups are paired with a different message type coming in from the UDP connection.
        // Thus, an EEPROM header from the UDP data is paired with the UDP section on the GUI.
        // to make processing performant, the Labels were all set to an array, we break up the
        // data into an array, the data and output is massged and then xGui[Index].setText(UDPData[Index]);
        // pretty easy and very performant.
        EEPROM_Show.Set_JLabel(0, Label_Status);
        EEPROM_Show.Set_JLabel(1, Label_EEPROM_Version);
        EEPROM_Show.Set_JLabel(2, Label_EEPROM_RebootCount);
        EEPROM_Show.Set_JLabel(3, Label_EEPROM_Processor_Speed);
        EEPROM_Show.Set_JLabel(4, Label_EEPROM_Temperature_Resolution);
        EEPROM_Show.Set_JLabel(5, Label_EEPROM_Horizontal_Adjustment);
        EEPROM_Show.Set_JLabel(6, Label_EEPROM_Verticall_Adjustment);
        EEPROM_Show.Set_JLabel(7, Label_EEPROM_Wind_Speed);
        EEPROM_Show.Set_JLabel(8, Label_EEPROM_Wind_Speed_Delay);
        EEPROM_Show.Set_JLabel(9, Label_EEPROM_UART_Telemetry);
        EEPROM_Show.Set_JLabel(10, Label_EEPROM_UDP_Telemetry);
        EEPROM_Show.Set_JLabel(11, Label_EEPROM_USE_DNS);
        EEPROM_Show.Set_JLabel(12, Label_EEPROM_UDP_Port);
        EEPROM_Show.Set_JLabel(13, Label_EEPROM_Server_Name);
        EEPROM_Show.Set_JLabel(14, Label_EEPROM_Dark_Threshold);
        EEPROM_Show.Set_JLabel(15, Label_EEPROM_Delay_Sudden_Moveback);
        EEPROM_Show.Set_JLabel(16, Label_EEPROM_Move_Threshold);
        
        
        Temp_Show.Set_C_JLabel(0, TEMP_C_0);
        Temp_Show.Set_C_JLabel(1, TEMP_C_1);
        Temp_Show.Set_C_JLabel(2, TEMP_C_2);
        Temp_Show.Set_C_JLabel(3, TEMP_C_3);
        Temp_Show.Set_C_JLabel(4, TEMP_C_4);
        Temp_Show.Set_C_JLabel(5, TEMP_C_5);
        Temp_Show.Set_C_JLabel(6, TEMP_C_6);
        Temp_Show.Set_C_JLabel(7, TEMP_C_7);
        Temp_Show.Set_C_JLabel(8, TEMP_C_8);
        Temp_Show.Set_C_JLabel(9, TEMP_C_9);
        Temp_Show.Set_C_JLabel(10, TEMP_C_10);
        Temp_Show.Set_C_JLabel(11, TEMP_C_11);
        Temp_Show.Set_C_JLabel(12, TEMP_C_12);
        Temp_Show.Set_C_JLabel(13, TEMP_C_13);
        Temp_Show.Set_C_JLabel(14, TEMP_C_14);
        Temp_Show.Set_C_JLabel(15, TEMP_C_15);
        
        Temp_Show.Set_F_JLabel(0, TEMP_F_0);
        Temp_Show.Set_F_JLabel(1, TEMP_F_1);
        Temp_Show.Set_F_JLabel(2, TEMP_F_2);
        Temp_Show.Set_F_JLabel(3, TEMP_F_3);
        Temp_Show.Set_F_JLabel(4, TEMP_F_4);
        Temp_Show.Set_F_JLabel(5, TEMP_F_5);
        Temp_Show.Set_F_JLabel(6, TEMP_F_6);
        Temp_Show.Set_F_JLabel(7, TEMP_F_7);
        Temp_Show.Set_F_JLabel(8, TEMP_F_8);
        Temp_Show.Set_F_JLabel(9, TEMP_F_9);
        Temp_Show.Set_F_JLabel(10, TEMP_F_10);
        Temp_Show.Set_F_JLabel(11, TEMP_F_11);
        Temp_Show.Set_F_JLabel(12, TEMP_F_12);
        Temp_Show.Set_F_JLabel(13, TEMP_F_13);
        Temp_Show.Set_F_JLabel(14, TEMP_F_14);
        Temp_Show.Set_F_JLabel(15, TEMP_F_15);

        Temp_Show.Set_Flag_JLabel(0, TEMP_Flag_0);
        Temp_Show.Set_Flag_JLabel(1, TEMP_Flag_1);
        Temp_Show.Set_Flag_JLabel(2, TEMP_Flag_2);
        Temp_Show.Set_Flag_JLabel(3, TEMP_Flag_3);
        Temp_Show.Set_Flag_JLabel(4, TEMP_Flag_4);
        Temp_Show.Set_Flag_JLabel(5, TEMP_Flag_5);
        Temp_Show.Set_Flag_JLabel(6, TEMP_Flag_6);
        Temp_Show.Set_Flag_JLabel(7, TEMP_Flag_7);
        Temp_Show.Set_Flag_JLabel(8, TEMP_Flag_8);
        Temp_Show.Set_Flag_JLabel(9, TEMP_Flag_9);
        Temp_Show.Set_Flag_JLabel(10, TEMP_Flag_10);
        Temp_Show.Set_Flag_JLabel(11, TEMP_Flag_11);
        Temp_Show.Set_Flag_JLabel(12, TEMP_Flag_12);
        Temp_Show.Set_Flag_JLabel(13, TEMP_Flag_13);
        Temp_Show.Set_Flag_JLabel(14, TEMP_Flag_14);
        Temp_Show.Set_Flag_JLabel(15, TEMP_Flag_15);
        
        Temp_Show.Set_ROM_JLabel(0, TEMP_ROM_0);
        Temp_Show.Set_ROM_JLabel(1, TEMP_ROM_1);
        Temp_Show.Set_ROM_JLabel(2, TEMP_ROM_2);
        Temp_Show.Set_ROM_JLabel(3, TEMP_ROM_3);
        Temp_Show.Set_ROM_JLabel(4, TEMP_ROM_4);
        Temp_Show.Set_ROM_JLabel(5, TEMP_ROM_5);
        Temp_Show.Set_ROM_JLabel(6, TEMP_ROM_6);
        Temp_Show.Set_ROM_JLabel(7, TEMP_ROM_7);
        Temp_Show.Set_ROM_JLabel(8, TEMP_ROM_8);
        Temp_Show.Set_ROM_JLabel(9, TEMP_ROM_9);
        Temp_Show.Set_ROM_JLabel(10, TEMP_ROM_10);
        Temp_Show.Set_ROM_JLabel(11, TEMP_ROM_11);
        Temp_Show.Set_ROM_JLabel(12, TEMP_ROM_12);
        Temp_Show.Set_ROM_JLabel(13, TEMP_ROM_13);
        Temp_Show.Set_ROM_JLabel(14, TEMP_ROM_14);
        Temp_Show.Set_ROM_JLabel(15, TEMP_ROM_15);
        

        RunTime_Show.Set_JLabel(0, Label_Status);
        RunTime_Show.Set_JLabel(1, Label_RunTime_UpTime);
        RunTime_Show.Set_JLabel(2, Label_RunTime_TemperatureCycles);
        RunTime_Show.Set_JLabel(3, Label_RunTime_PumpCycles);
        RunTime_Show.Set_JLabel(4, Label_RunTime_ADCCycles);
        RunTime_Show.Set_JLabel(5, Label_RunTime_DishCycles);
        RunTime_Show.Set_JLabel(6, Label_RunTime_QueryCount);
        RunTime_Show.Set_JLabel(7, Label_RunTime_ThermostatCycles);
        RunTime_Show.Set_JLabel(8, Label_RunTime_TotalHeatTime);
        RunTime_Show.Set_JLabel(9, Label_RunTime_EEPROMWrites);
        RunTime_Show.Set_JLabel(10, Label_RunTime_DataOutput_Count);
        RunTime_Show.Set_JLabel(11, Label_RunTime_Temps_Per_Second);
        RunTime_Show.Set_JLabel(12, Label_RunTime_ADC_Per_Second);

        Pump_Show.Set_JLabel(0, Label_Status);
        Pump_Show.Set_JLabel(1, Pump_Thermostat);
        Pump_Show.Set_JLabel(2, Pump_Dish_Percent);
        Pump_Show.Set_JLabel(3, Pump_Dish_Status);
        Pump_Show.Set_JLabel(4, Pump_Immdt_Percent);
        Pump_Show.Set_JLabel(5, Pump_Immdt_Status);
        Pump_Show.Set_JLabel(6, Pump_Hold_Percent);
        Pump_Show.Set_JLabel(7, Pump_Hold_Status);        
        Pump_Show.Set_JLabel(8, Pump_AUX_Percent);
        Pump_Show.Set_JLabel(9, Pump_AUX_Status);        

        
        MoveFlags_Show.Set_JLabel(0, Label_Status);
        MoveFlags_Show.Set_JLabel(1, Label_MF_Wind_Speed);
        MoveFlags_Show.Set_JLabel(2, Label_MF_Wind_Speed_Status);
        MoveFlags_Show.Set_JLabel(3, Label_MF_Failsafe_Check);
        MoveFlags_Show.Set_JLabel(4, Label_MF_Froximity_Right);
        MoveFlags_Show.Set_JLabel(5, Label_MF_Proximity_Left);
        MoveFlags_Show.Set_JLabel(6, Label_MF_Proximity_Up);
        MoveFlags_Show.Set_JLabel(7, Label_MF_Proximity_Down);


        Dish_Show.Set_JLabel(0, Label_Status);
        Dish_Show.Set_JLabel(1, Dish_H_R_Low);
        Dish_Show.Set_JLabel(2, Dish_H_L_Low);
        Dish_Show.Set_JLabel(3, Dish_H_R_High);
        Dish_Show.Set_JLabel(4, Dish_H_L_High);
        Dish_Show.Set_JLabel(5, Dish_H_Total);
        Dish_Show.Set_JLabel(6, Dish_H_Status);
        Dish_Show.Set_JLabel(7, Dish_V_U_Low);
        Dish_Show.Set_JLabel(8, Dish_V_D_Low);
        Dish_Show.Set_JLabel(9, Dish_V_U_High);
        Dish_Show.Set_JLabel(10, Dish_V_D_High);
        Dish_Show.Set_JLabel(11, Dish_V_Total);
        Dish_Show.Set_JLabel(12, Dish_V_Status);
        
        MinorControls.SetControlFields(Label_Remote_IP, Button_Change_EEPROM_Settings);
        
    }


               
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel17 = new javax.swing.JLabel();
        Panel_EEPROM = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        Label_EEPROM_Version = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        Label_EEPROM_RebootCount = new javax.swing.JLabel();
        Label_EEPROM_Processor_Speed = new javax.swing.JLabel();
        Label_EEPROM_Temperature_Resolution = new javax.swing.JLabel();
        Label_EEPROM_Horizontal_Adjustment = new javax.swing.JLabel();
        Label_EEPROM_Verticall_Adjustment = new javax.swing.JLabel();
        Label_EEPROM_Wind_Speed = new javax.swing.JLabel();
        Label_EEPROM_Wind_Speed_Delay = new javax.swing.JLabel();
        Label_EEPROM_UART_Telemetry = new javax.swing.JLabel();
        Label_EEPROM_UDP_Telemetry = new javax.swing.JLabel();
        Label_EEPROM_USE_DNS = new javax.swing.JLabel();
        Label_EEPROM_UDP_Port = new javax.swing.JLabel();
        Label_EEPROM_Server_Name = new javax.swing.JLabel();
        Label_EEPROM_Dark_Threshold = new javax.swing.JLabel();
        Label_EEPROM_Delay_Sudden_Moveback = new javax.swing.JLabel();
        Label_EEPROM_Move_Threshold = new javax.swing.JLabel();
        Button_Change_EEPROM_Settings = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        Label_Local_UDP_Port = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        Label_Local_IP = new javax.swing.JLabel();
        Button_Connect_To_Controller = new javax.swing.JToggleButton();
        jLabel22 = new javax.swing.JLabel();
        Label_Status = new javax.swing.JLabel();
        Panel_Temperatures = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        TEMP_Dev_0 = new javax.swing.JLabel();
        TEMP_Dev_1 = new javax.swing.JLabel();
        TEMP_Dev_2 = new javax.swing.JLabel();
        TEMP_Dev_3 = new javax.swing.JLabel();
        TEMP_Dev_4 = new javax.swing.JLabel();
        TEMP_Dev_5 = new javax.swing.JLabel();
        TEMP_Dev_6 = new javax.swing.JLabel();
        TEMP_Dev_7 = new javax.swing.JLabel();
        TEMP_Dev_8 = new javax.swing.JLabel();
        TEMP_Dev_9 = new javax.swing.JLabel();
        TEMP_Dev_10 = new javax.swing.JLabel();
        TEMP_Dev_11 = new javax.swing.JLabel();
        TEMP_Dev_12 = new javax.swing.JLabel();
        TEMP_Dev_13 = new javax.swing.JLabel();
        TEMP_Dev_14 = new javax.swing.JLabel();
        TEMP_Dev_15 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        TEMP_F_0 = new javax.swing.JLabel();
        TEMP_F_1 = new javax.swing.JLabel();
        TEMP_F_2 = new javax.swing.JLabel();
        TEMP_F_3 = new javax.swing.JLabel();
        TEMP_F_4 = new javax.swing.JLabel();
        TEMP_F_5 = new javax.swing.JLabel();
        TEMP_F_6 = new javax.swing.JLabel();
        TEMP_F_7 = new javax.swing.JLabel();
        TEMP_F_8 = new javax.swing.JLabel();
        TEMP_F_9 = new javax.swing.JLabel();
        TEMP_F_10 = new javax.swing.JLabel();
        TEMP_F_11 = new javax.swing.JLabel();
        TEMP_F_12 = new javax.swing.JLabel();
        TEMP_F_13 = new javax.swing.JLabel();
        TEMP_F_14 = new javax.swing.JLabel();
        TEMP_F_15 = new javax.swing.JLabel();
        jLabel55 = new javax.swing.JLabel();
        TEMP_C_0 = new javax.swing.JLabel();
        TEMP_C_1 = new javax.swing.JLabel();
        TEMP_C_2 = new javax.swing.JLabel();
        TEMP_C_3 = new javax.swing.JLabel();
        TEMP_C_4 = new javax.swing.JLabel();
        TEMP_C_5 = new javax.swing.JLabel();
        TEMP_C_6 = new javax.swing.JLabel();
        TEMP_C_7 = new javax.swing.JLabel();
        TEMP_C_8 = new javax.swing.JLabel();
        TEMP_C_9 = new javax.swing.JLabel();
        TEMP_C_10 = new javax.swing.JLabel();
        TEMP_C_11 = new javax.swing.JLabel();
        TEMP_C_12 = new javax.swing.JLabel();
        TEMP_C_13 = new javax.swing.JLabel();
        TEMP_C_14 = new javax.swing.JLabel();
        TEMP_C_15 = new javax.swing.JLabel();
        jLabel72 = new javax.swing.JLabel();
        TEMP_Flag_0 = new javax.swing.JLabel();
        TEMP_Flag_1 = new javax.swing.JLabel();
        TEMP_Flag_2 = new javax.swing.JLabel();
        TEMP_Flag_3 = new javax.swing.JLabel();
        TEMP_Flag_4 = new javax.swing.JLabel();
        TEMP_Flag_5 = new javax.swing.JLabel();
        TEMP_Flag_6 = new javax.swing.JLabel();
        TEMP_Flag_7 = new javax.swing.JLabel();
        TEMP_Flag_8 = new javax.swing.JLabel();
        TEMP_Flag_9 = new javax.swing.JLabel();
        TEMP_Flag_10 = new javax.swing.JLabel();
        TEMP_Flag_11 = new javax.swing.JLabel();
        TEMP_Flag_12 = new javax.swing.JLabel();
        TEMP_Flag_13 = new javax.swing.JLabel();
        TEMP_Flag_14 = new javax.swing.JLabel();
        TEMP_Flag_15 = new javax.swing.JLabel();
        jLabel89 = new javax.swing.JLabel();
        TEMP_ROM_0 = new javax.swing.JLabel();
        TEMP_ROM_1 = new javax.swing.JLabel();
        TEMP_ROM_2 = new javax.swing.JLabel();
        TEMP_ROM_3 = new javax.swing.JLabel();
        TEMP_ROM_4 = new javax.swing.JLabel();
        TEMP_ROM_5 = new javax.swing.JLabel();
        TEMP_ROM_6 = new javax.swing.JLabel();
        TEMP_ROM_7 = new javax.swing.JLabel();
        TEMP_ROM_8 = new javax.swing.JLabel();
        TEMP_ROM_9 = new javax.swing.JLabel();
        TEMP_ROM_10 = new javax.swing.JLabel();
        TEMP_ROM_11 = new javax.swing.JLabel();
        TEMP_ROM_12 = new javax.swing.JLabel();
        TEMP_ROM_13 = new javax.swing.JLabel();
        TEMP_ROM_14 = new javax.swing.JLabel();
        TEMP_ROM_15 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        Pump_Thermostat = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        Pump_Dish_Percent = new javax.swing.JLabel();
        Pump_Dish_Status = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        Pump_Immdt_Percent = new javax.swing.JLabel();
        Pump_Immdt_Status = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        Pump_Hold_Percent = new javax.swing.JLabel();
        Pump_Hold_Status = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        Pump_AUX_Percent = new javax.swing.JLabel();
        Pump_AUX_Status = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        Label_UpTime = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        Label_RunTime_UpTime = new javax.swing.JLabel();
        Label_RunTime_TemperatureCycles = new javax.swing.JLabel();
        Label_RunTime_PumpCycles = new javax.swing.JLabel();
        Label_RunTime_ADCCycles = new javax.swing.JLabel();
        Label_RunTime_DishCycles = new javax.swing.JLabel();
        Label_RunTime_QueryCount = new javax.swing.JLabel();
        Label_RunTime_ThermostatCycles = new javax.swing.JLabel();
        Label_RunTime_TotalHeatTime = new javax.swing.JLabel();
        Label_RunTime_EEPROMWrites = new javax.swing.JLabel();
        Label_RunTime_DataOutput_Count = new javax.swing.JLabel();
        Label_RunTime_Temps_Per_Second = new javax.swing.JLabel();
        Label_RunTime_ADC_Per_Second = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel52 = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        jLabel56 = new javax.swing.JLabel();
        jLabel57 = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        jLabel59 = new javax.swing.JLabel();
        Label_MF_Wind_Speed = new javax.swing.JLabel();
        Label_MF_Wind_Speed_Status = new javax.swing.JLabel();
        Label_MF_Failsafe_Check = new javax.swing.JLabel();
        Label_MF_Froximity_Right = new javax.swing.JLabel();
        Label_MF_Proximity_Left = new javax.swing.JLabel();
        Label_MF_Proximity_Up = new javax.swing.JLabel();
        Label_MF_Proximity_Down = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel68 = new javax.swing.JLabel();
        jLabel69 = new javax.swing.JLabel();
        jLabel70 = new javax.swing.JLabel();
        jLabel71 = new javax.swing.JLabel();
        Dish_H_Status = new javax.swing.JLabel();
        Dish_H_R_Low = new javax.swing.JLabel();
        Dish_H_L_Low = new javax.swing.JLabel();
        Dish_H_Total = new javax.swing.JLabel();
        Dish_H_R_High = new javax.swing.JLabel();
        Dish_H_L_High = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel74 = new javax.swing.JLabel();
        jLabel75 = new javax.swing.JLabel();
        jLabel76 = new javax.swing.JLabel();
        jLabel77 = new javax.swing.JLabel();
        Dish_V_Status = new javax.swing.JLabel();
        Dish_V_U_Low = new javax.swing.JLabel();
        Dish_V_D_Low = new javax.swing.JLabel();
        Dish_V_Total = new javax.swing.JLabel();
        Dish_V_U_High = new javax.swing.JLabel();
        Dish_V_D_High = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        Label_Remote_IP = new javax.swing.JLabel();

        jLabel17.setText("jLabel17");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Solar Thermal Data Viewer");

        Panel_EEPROM.setBackground(new java.awt.Color(255, 255, 204));
        Panel_EEPROM.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "EEPROM Settings", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.BELOW_TOP));
        Panel_EEPROM.setDoubleBuffered(false);
        Panel_EEPROM.setName(""); // NOI18N

        jLabel1.setBackground(new java.awt.Color(255, 255, 204));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel1.setText("Version:");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel1.setOpaque(true);

        Label_EEPROM_Version.setText("<Version>");
        Label_EEPROM_Version.setName(""); // NOI18N

        jLabel2.setBackground(new java.awt.Color(255, 255, 204));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel2.setText("Reboot Count:");
        jLabel2.setOpaque(true);

        jLabel3.setBackground(new java.awt.Color(255, 255, 204));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel3.setText("Processor Speed:");
        jLabel3.setOpaque(true);

        jLabel4.setBackground(new java.awt.Color(255, 255, 204));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel4.setText("Temperature Resolution:");
        jLabel4.setOpaque(true);

        jLabel5.setBackground(new java.awt.Color(255, 255, 204));
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel5.setText("Horizontal Adjustment:");
        jLabel5.setOpaque(true);

        jLabel6.setBackground(new java.awt.Color(255, 255, 204));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel6.setText("Vertical Adjustment:");
        jLabel6.setOpaque(true);

        jLabel7.setBackground(new java.awt.Color(255, 255, 204));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel7.setText("Max Wind Speed:");
        jLabel7.setOpaque(true);

        jLabel8.setBackground(new java.awt.Color(255, 255, 204));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel8.setText("Wind Speed Delay:");
        jLabel8.setOpaque(true);

        jLabel9.setBackground(new java.awt.Color(255, 255, 204));
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel9.setText("UART Telemetry:");
        jLabel9.setOpaque(true);

        jLabel10.setBackground(new java.awt.Color(255, 255, 204));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel10.setText("UDP Telemetry:");
        jLabel10.setOpaque(true);

        jLabel11.setBackground(new java.awt.Color(255, 255, 204));
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel11.setText("Use DNS:");
        jLabel11.setOpaque(true);

        jLabel12.setBackground(new java.awt.Color(255, 255, 204));
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel12.setText("UDP Port:");
        jLabel12.setOpaque(true);

        jLabel13.setBackground(new java.awt.Color(255, 255, 204));
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel13.setText("Server Name:");
        jLabel13.setOpaque(true);

        jLabel14.setBackground(new java.awt.Color(255, 255, 204));
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel14.setText("Dark Threshold:");
        jLabel14.setOpaque(true);

        jLabel15.setBackground(new java.awt.Color(255, 255, 204));
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel15.setText("Delay Sudden Moveback:");
        jLabel15.setOpaque(true);

        jLabel16.setBackground(new java.awt.Color(255, 255, 204));
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel16.setText("Move Threshold:");
        jLabel16.setOpaque(true);

        Label_EEPROM_RebootCount.setText("<0>");

        Label_EEPROM_Processor_Speed.setText("<0>");

        Label_EEPROM_Temperature_Resolution.setText("<9,10,11,12>");

        Label_EEPROM_Horizontal_Adjustment.setText("<0>");

        Label_EEPROM_Verticall_Adjustment.setText("<0>");

        Label_EEPROM_Wind_Speed.setText("<25>");

        Label_EEPROM_Wind_Speed_Delay.setText("<300>");

        Label_EEPROM_UART_Telemetry.setText("<True>");

        Label_EEPROM_UDP_Telemetry.setText("<True>");

        Label_EEPROM_USE_DNS.setText("<False>");

        Label_EEPROM_UDP_Port.setText("<55056>");

        Label_EEPROM_Server_Name.setText("<not defined>");

        Label_EEPROM_Dark_Threshold.setText("<1000>");

        Label_EEPROM_Delay_Sudden_Moveback.setText("<300>");

        Label_EEPROM_Move_Threshold.setText("<15>");

        javax.swing.GroupLayout Panel_EEPROMLayout = new javax.swing.GroupLayout(Panel_EEPROM);
        Panel_EEPROM.setLayout(Panel_EEPROMLayout);
        Panel_EEPROMLayout.setHorizontalGroup(
            Panel_EEPROMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Panel_EEPROMLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(Panel_EEPROMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_EEPROMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Label_EEPROM_Version, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Label_EEPROM_RebootCount, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Label_EEPROM_Processor_Speed, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Label_EEPROM_Temperature_Resolution)
                    .addComponent(Label_EEPROM_Horizontal_Adjustment, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(Panel_EEPROMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(Label_EEPROM_Wind_Speed, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                        .addComponent(Label_EEPROM_Verticall_Adjustment, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(Label_EEPROM_USE_DNS)
                    .addGroup(Panel_EEPROMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(Label_EEPROM_UDP_Telemetry, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(Label_EEPROM_UART_Telemetry, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(Label_EEPROM_Wind_Speed_Delay, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(Label_EEPROM_Server_Name)
                    .addGroup(Panel_EEPROMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(Label_EEPROM_Dark_Threshold, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                        .addComponent(Label_EEPROM_Delay_Sudden_Moveback, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(Label_EEPROM_Move_Threshold, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(Label_EEPROM_UDP_Port))
                .addGap(19, 19, 19))
        );

        Panel_EEPROMLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel1, jLabel10, jLabel11, jLabel12, jLabel13, jLabel14, jLabel15, jLabel16, jLabel2, jLabel3, jLabel4, jLabel5, jLabel6, jLabel7, jLabel8, jLabel9});

        Panel_EEPROMLayout.setVerticalGroup(
            Panel_EEPROMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Panel_EEPROMLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(Panel_EEPROMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(Label_EEPROM_Version))
                .addGap(6, 6, 6)
                .addGroup(Panel_EEPROMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(Label_EEPROM_RebootCount))
                .addGap(6, 6, 6)
                .addGroup(Panel_EEPROMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(Label_EEPROM_Processor_Speed))
                .addGap(6, 6, 6)
                .addGroup(Panel_EEPROMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(Label_EEPROM_Temperature_Resolution))
                .addGap(6, 6, 6)
                .addGroup(Panel_EEPROMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(Label_EEPROM_Horizontal_Adjustment))
                .addGap(6, 6, 6)
                .addGroup(Panel_EEPROMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(Label_EEPROM_Verticall_Adjustment))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_EEPROMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(Label_EEPROM_Wind_Speed))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_EEPROMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(Label_EEPROM_Wind_Speed_Delay))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_EEPROMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(Label_EEPROM_UART_Telemetry))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_EEPROMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(Label_EEPROM_UDP_Telemetry))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_EEPROMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(Label_EEPROM_USE_DNS))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_EEPROMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(Label_EEPROM_UDP_Port))
                .addGap(4, 4, 4)
                .addGroup(Panel_EEPROMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(Label_EEPROM_Server_Name))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_EEPROMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(Label_EEPROM_Dark_Threshold))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_EEPROMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(Label_EEPROM_Delay_Sudden_Moveback))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(Panel_EEPROMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(Label_EEPROM_Move_Threshold))
                .addContainerGap())
        );

        Panel_EEPROMLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel1, jLabel10, jLabel11, jLabel12, jLabel13, jLabel14, jLabel15, jLabel16, jLabel2, jLabel3, jLabel4, jLabel5, jLabel6, jLabel7, jLabel8, jLabel9});

        Button_Change_EEPROM_Settings.setBackground(new java.awt.Color(255, 255, 204));
        Button_Change_EEPROM_Settings.setText("Update EEPROM Settings");
        Button_Change_EEPROM_Settings.setEnabled(false);
        Button_Change_EEPROM_Settings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Button_Change_EEPROM_SettingsActionPerformed(evt);
            }
        });

        jLabel18.setBackground(new java.awt.Color(204, 255, 204));
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel18.setText("Local UDP Port:");

        Label_Local_UDP_Port.setBackground(new java.awt.Color(204, 255, 204));
        Label_Local_UDP_Port.setText("<55056>");

        jLabel20.setBackground(new java.awt.Color(204, 255, 204));
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel20.setText("Local IP:");

        Label_Local_IP.setBackground(new java.awt.Color(204, 255, 204));
        Label_Local_IP.setText("<0.0.0.0>");

        Button_Connect_To_Controller.setText("Connect To Contoller Board");
        Button_Connect_To_Controller.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Connect_Listener(evt);
            }
        });

        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel22.setText("Status:");

        Label_Status.setText("<Not Connected>");

        Panel_Temperatures.setBackground(new java.awt.Color(204, 255, 255));
        Panel_Temperatures.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Temperatures", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.BELOW_TOP));

        jLabel19.setText("Device");
        jLabel19.setPreferredSize(new java.awt.Dimension(50, 14));

        TEMP_Dev_0.setText("Dish");
        TEMP_Dev_0.setPreferredSize(new java.awt.Dimension(50, 14));

        TEMP_Dev_1.setText("Immediate Res.");
        TEMP_Dev_1.setPreferredSize(new java.awt.Dimension(50, 14));

        TEMP_Dev_2.setText("Hold Res.");
        TEMP_Dev_2.setPreferredSize(new java.awt.Dimension(50, 14));

        TEMP_Dev_3.setText("AUX Source");
        TEMP_Dev_3.setPreferredSize(new java.awt.Dimension(50, 14));

        TEMP_Dev_4.setText("Outside Air");
        TEMP_Dev_4.setPreferredSize(new java.awt.Dimension(50, 14));

        TEMP_Dev_5.setText("Ground");
        TEMP_Dev_5.setPreferredSize(new java.awt.Dimension(50, 14));

        TEMP_Dev_6.setText("Ctrl / Electronics");
        TEMP_Dev_6.setPreferredSize(new java.awt.Dimension(50, 14));

        TEMP_Dev_7.setText("Dish to Resevoir");
        TEMP_Dev_7.setPreferredSize(new java.awt.Dimension(50, 14));

        TEMP_Dev_8.setText("Inside Air");
        TEMP_Dev_8.setPreferredSize(new java.awt.Dimension(50, 14));

        TEMP_Dev_9.setText("User Defined - 2");
        TEMP_Dev_9.setPreferredSize(new java.awt.Dimension(50, 14));

        TEMP_Dev_10.setText("User Defined - 3");
        TEMP_Dev_10.setPreferredSize(new java.awt.Dimension(50, 14));

        TEMP_Dev_11.setText("User Defined - 4");
        TEMP_Dev_11.setPreferredSize(new java.awt.Dimension(50, 14));

        TEMP_Dev_12.setText("User Defined - 5");
        TEMP_Dev_12.setPreferredSize(new java.awt.Dimension(50, 14));

        TEMP_Dev_13.setText("User Defined - 6");
        TEMP_Dev_13.setPreferredSize(new java.awt.Dimension(50, 14));

        TEMP_Dev_14.setText("User Defined - 7");
        TEMP_Dev_14.setPreferredSize(new java.awt.Dimension(50, 14));

        TEMP_Dev_15.setText("User Defined - 8");
        TEMP_Dev_15.setPreferredSize(new java.awt.Dimension(50, 14));

        jLabel38.setText("Deg. (F)");

        TEMP_F_0.setText("<0>");

        TEMP_F_1.setText("<0>");

        TEMP_F_2.setText("<0>");

        TEMP_F_3.setText("<0>");

        TEMP_F_4.setText("<0>");

        TEMP_F_5.setText("<0>");

        TEMP_F_6.setText("<0>");

        TEMP_F_7.setText("<0>");

        TEMP_F_8.setText("<0>");

        TEMP_F_9.setText("<0>");

        TEMP_F_10.setText("<0>");

        TEMP_F_11.setText("<0>");

        TEMP_F_12.setText("<0>");

        TEMP_F_13.setText("<0>");

        TEMP_F_14.setText("<0>");

        TEMP_F_15.setText("<0>");

        jLabel55.setText("Deg. (C)");

        TEMP_C_0.setText("<0>");

        TEMP_C_1.setText("<0>");

        TEMP_C_2.setText("<0>");

        TEMP_C_3.setText("<0>");

        TEMP_C_4.setText("<0>");

        TEMP_C_5.setText("<0>");

        TEMP_C_6.setText("<0>");

        TEMP_C_7.setText("<0>");

        TEMP_C_8.setText("<0>");

        TEMP_C_9.setText("<0>");

        TEMP_C_10.setText("<0>");

        TEMP_C_11.setText("<0>");

        TEMP_C_12.setText("<0>");

        TEMP_C_13.setText("<0>");

        TEMP_C_14.setText("<0>");

        TEMP_C_15.setText("<0>");

        jLabel72.setText("Flag");

        TEMP_Flag_0.setText("<OK>");

        TEMP_Flag_1.setText("<OK>");

        TEMP_Flag_2.setText("<OK>");

        TEMP_Flag_3.setText("<OK>");

        TEMP_Flag_4.setText("<OK>");

        TEMP_Flag_5.setText("<OK>");

        TEMP_Flag_6.setText("<OK>");

        TEMP_Flag_7.setText("<OK>");

        TEMP_Flag_8.setText("<OK>");

        TEMP_Flag_9.setText("<OK>");

        TEMP_Flag_10.setText("<OK>");

        TEMP_Flag_11.setText("<OK>");

        TEMP_Flag_12.setText("<OK>");

        TEMP_Flag_13.setText("<OK>");

        TEMP_Flag_14.setText("<OK>");

        TEMP_Flag_15.setText("<OK>");

        jLabel89.setText("ROM Codes");

        TEMP_ROM_0.setText("<00-00-00-00-00-00-00-00>");

        TEMP_ROM_1.setText("<00-00-00-00-00-00-00-00>");

        TEMP_ROM_2.setText("<00-00-00-00-00-00-00-00>");

        TEMP_ROM_3.setText("<00-00-00-00-00-00-00-00>");

        TEMP_ROM_4.setText("<00-00-00-00-00-00-00-00>");

        TEMP_ROM_5.setText("<00-00-00-00-00-00-00-00>");

        TEMP_ROM_6.setText("<00-00-00-00-00-00-00-00>");

        TEMP_ROM_7.setText("<00-00-00-00-00-00-00-00>");

        TEMP_ROM_8.setText("<00-00-00-00-00-00-00-00>");

        TEMP_ROM_9.setText("<00-00-00-00-00-00-00-00>");

        TEMP_ROM_10.setText("<00-00-00-00-00-00-00-00>");

        TEMP_ROM_11.setText("<00-00-00-00-00-00-00-00>");

        TEMP_ROM_12.setText("<00-00-00-00-00-00-00-00>");

        TEMP_ROM_13.setText("<00-00-00-00-00-00-00-00>");

        TEMP_ROM_14.setText("<00-00-00-00-00-00-00-00>");

        TEMP_ROM_15.setText("<00-00-00-00-00-00-00-00>");

        javax.swing.GroupLayout Panel_TemperaturesLayout = new javax.swing.GroupLayout(Panel_Temperatures);
        Panel_Temperatures.setLayout(Panel_TemperaturesLayout);
        Panel_TemperaturesLayout.setHorizontalGroup(
            Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Panel_TemperaturesLayout.createSequentialGroup()
                .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_Dev_0, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                    .addComponent(TEMP_Dev_1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_Dev_2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_Dev_3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_Dev_4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_Dev_5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_Dev_6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_Dev_7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_Dev_8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_Dev_9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_Dev_10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_Dev_11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_Dev_12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_Dev_13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_Dev_14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_Dev_15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(10, 10, 10)
                .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Panel_TemperaturesLayout.createSequentialGroup()
                        .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(TEMP_F_0, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel38, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel55)
                            .addComponent(TEMP_C_0, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                            .addComponent(TEMP_C_1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(TEMP_C_2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(TEMP_C_3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(TEMP_C_4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(TEMP_C_5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(TEMP_C_6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(TEMP_C_7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(TEMP_C_8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(TEMP_C_9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(TEMP_C_10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(TEMP_C_11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(TEMP_C_12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(TEMP_C_13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(TEMP_C_14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(TEMP_C_15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(TEMP_F_1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_4, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_5, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_6, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_7, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_8, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_10, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_11, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_12, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_13, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_14, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_15, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_9, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Panel_TemperaturesLayout.createSequentialGroup()
                        .addComponent(jLabel72, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26))
                    .addGroup(Panel_TemperaturesLayout.createSequentialGroup()
                        .addComponent(TEMP_Flag_0, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(Panel_TemperaturesLayout.createSequentialGroup()
                        .addComponent(TEMP_Flag_1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(Panel_TemperaturesLayout.createSequentialGroup()
                        .addComponent(TEMP_Flag_2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(Panel_TemperaturesLayout.createSequentialGroup()
                        .addComponent(TEMP_Flag_3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(Panel_TemperaturesLayout.createSequentialGroup()
                        .addComponent(TEMP_Flag_4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(Panel_TemperaturesLayout.createSequentialGroup()
                        .addComponent(TEMP_Flag_5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(Panel_TemperaturesLayout.createSequentialGroup()
                        .addComponent(TEMP_Flag_6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(Panel_TemperaturesLayout.createSequentialGroup()
                        .addComponent(TEMP_Flag_7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(Panel_TemperaturesLayout.createSequentialGroup()
                        .addComponent(TEMP_Flag_8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(Panel_TemperaturesLayout.createSequentialGroup()
                        .addComponent(TEMP_Flag_9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(Panel_TemperaturesLayout.createSequentialGroup()
                        .addComponent(TEMP_Flag_10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(Panel_TemperaturesLayout.createSequentialGroup()
                        .addComponent(TEMP_Flag_11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(Panel_TemperaturesLayout.createSequentialGroup()
                        .addComponent(TEMP_Flag_12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(Panel_TemperaturesLayout.createSequentialGroup()
                        .addComponent(TEMP_Flag_13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(Panel_TemperaturesLayout.createSequentialGroup()
                        .addComponent(TEMP_Flag_14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(Panel_TemperaturesLayout.createSequentialGroup()
                        .addComponent(TEMP_Flag_15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel89, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_ROM_0, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                    .addComponent(TEMP_ROM_1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_ROM_2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_ROM_3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_ROM_4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_ROM_5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_ROM_6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_ROM_7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_ROM_8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_ROM_9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_ROM_10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_ROM_11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_ROM_12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_ROM_13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_ROM_14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TEMP_ROM_15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 6, Short.MAX_VALUE))
        );
        Panel_TemperaturesLayout.setVerticalGroup(
            Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Panel_TemperaturesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel38)
                    .addComponent(jLabel55)
                    .addComponent(jLabel72)
                    .addComponent(jLabel89))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TEMP_Dev_0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_0)
                    .addComponent(TEMP_C_0)
                    .addComponent(TEMP_Flag_0)
                    .addComponent(TEMP_ROM_0))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TEMP_Dev_1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_1)
                    .addComponent(TEMP_C_1)
                    .addComponent(TEMP_Flag_1)
                    .addComponent(TEMP_ROM_1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TEMP_Dev_2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_2)
                    .addComponent(TEMP_C_2)
                    .addComponent(TEMP_Flag_2)
                    .addComponent(TEMP_ROM_2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(TEMP_F_3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(TEMP_Dev_3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(TEMP_C_3)
                        .addComponent(TEMP_Flag_3)
                        .addComponent(TEMP_ROM_3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TEMP_Dev_4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_4)
                    .addComponent(TEMP_C_4)
                    .addComponent(TEMP_ROM_4)
                    .addComponent(TEMP_Flag_4, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TEMP_Dev_5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_5)
                    .addComponent(TEMP_C_5)
                    .addComponent(TEMP_Flag_5)
                    .addComponent(TEMP_ROM_5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TEMP_Dev_6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_6)
                    .addComponent(TEMP_C_6)
                    .addComponent(TEMP_Flag_6)
                    .addComponent(TEMP_ROM_6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TEMP_Dev_7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_7)
                    .addComponent(TEMP_C_7)
                    .addComponent(TEMP_Flag_7)
                    .addComponent(TEMP_ROM_7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TEMP_Dev_8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_8)
                    .addComponent(TEMP_C_8)
                    .addComponent(TEMP_Flag_8)
                    .addComponent(TEMP_ROM_8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TEMP_Dev_9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_9)
                    .addComponent(TEMP_C_9)
                    .addComponent(TEMP_Flag_9)
                    .addComponent(TEMP_ROM_9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TEMP_Dev_10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_10)
                    .addComponent(TEMP_C_10)
                    .addComponent(TEMP_Flag_10)
                    .addComponent(TEMP_ROM_10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TEMP_Dev_11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_11)
                    .addComponent(TEMP_C_11)
                    .addComponent(TEMP_Flag_11)
                    .addComponent(TEMP_ROM_11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TEMP_Dev_12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_12)
                    .addComponent(TEMP_C_12)
                    .addComponent(TEMP_Flag_12)
                    .addComponent(TEMP_ROM_12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TEMP_Dev_13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_13)
                    .addComponent(TEMP_C_13)
                    .addComponent(TEMP_Flag_13)
                    .addComponent(TEMP_ROM_13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TEMP_Dev_14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TEMP_F_14)
                    .addComponent(TEMP_C_14)
                    .addComponent(TEMP_Flag_14)
                    .addComponent(TEMP_ROM_14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(TEMP_C_15, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(Panel_TemperaturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(TEMP_Dev_15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(TEMP_F_15)
                        .addComponent(TEMP_ROM_15)
                        .addComponent(TEMP_Flag_15, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jPanel1.setBackground(new java.awt.Color(255, 204, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Pump Data", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.BELOW_TOP));

        jLabel21.setText("Thermostat");

        Pump_Thermostat.setText("<Off>");

        jLabel24.setText("Dish Pump %");

        Pump_Dish_Percent.setText("<0>");

        Pump_Dish_Status.setText("<OK>");

        jLabel27.setText("Imdte Pump %");

        Pump_Immdt_Percent.setText("<0>");

        Pump_Immdt_Status.setText("<OK>");

        jLabel30.setText("Hold Pump %");

        Pump_Hold_Percent.setText("<0>");

        Pump_Hold_Status.setText("<OK>");

        jLabel33.setText("AUX Pump %");

        Pump_AUX_Percent.setText("<0>");

        Pump_AUX_Status.setText("<OK>");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel30)
                    .addComponent(jLabel33)
                    .addComponent(jLabel27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(Pump_AUX_Percent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Pump_Hold_Percent, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Pump_Immdt_Percent, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Pump_Dish_Percent, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Pump_Thermostat, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(8, 8, 8)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Pump_Dish_Status, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                    .addComponent(Pump_Immdt_Status, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Pump_Hold_Status, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Pump_AUX_Status, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(Pump_Thermostat))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(Pump_Dish_Percent)
                    .addComponent(Pump_Dish_Status))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(Pump_Immdt_Percent)
                    .addComponent(Pump_Immdt_Status))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel30)
                    .addComponent(Pump_Hold_Percent)
                    .addComponent(Pump_Hold_Status))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel33)
                    .addComponent(Pump_AUX_Percent)
                    .addComponent(Pump_AUX_Status))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Runtime Status", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.BELOW_TOP));

        Label_UpTime.setText("Uptime:");

        jLabel25.setText("Temperature Cycles");
        jLabel25.setMaximumSize(new java.awt.Dimension(37, 14));
        jLabel25.setMinimumSize(new java.awt.Dimension(37, 14));

        jLabel26.setText("Pump Cycles");
        jLabel26.setMaximumSize(new java.awt.Dimension(37, 14));
        jLabel26.setMinimumSize(new java.awt.Dimension(37, 14));

        jLabel28.setText("ADC Cycles");
        jLabel28.setMaximumSize(new java.awt.Dimension(37, 14));
        jLabel28.setMinimumSize(new java.awt.Dimension(37, 14));

        jLabel29.setText("Dish Cycles");
        jLabel29.setMaximumSize(new java.awt.Dimension(37, 14));
        jLabel29.setMinimumSize(new java.awt.Dimension(37, 14));

        jLabel31.setText("Query Count");
        jLabel31.setMaximumSize(new java.awt.Dimension(37, 14));
        jLabel31.setMinimumSize(new java.awt.Dimension(37, 14));

        jLabel32.setText("Thermostat Cycles");
        jLabel32.setMaximumSize(new java.awt.Dimension(37, 14));
        jLabel32.setMinimumSize(new java.awt.Dimension(37, 14));

        jLabel34.setText("Total Time Heat Req'd");
        jLabel34.setMaximumSize(new java.awt.Dimension(37, 14));
        jLabel34.setMinimumSize(new java.awt.Dimension(37, 14));

        jLabel35.setText("EEPROM Writes");
        jLabel35.setMaximumSize(new java.awt.Dimension(37, 14));
        jLabel35.setMinimumSize(new java.awt.Dimension(37, 14));

        jLabel36.setText("Data Output Count");
        jLabel36.setMaximumSize(new java.awt.Dimension(37, 14));
        jLabel36.setMinimumSize(new java.awt.Dimension(37, 14));

        jLabel37.setText("Temps & Pumps / Second");
        jLabel37.setMaximumSize(new java.awt.Dimension(37, 14));
        jLabel37.setMinimumSize(new java.awt.Dimension(37, 14));

        jLabel39.setText("ADC & Dish / Second");
        jLabel39.setMaximumSize(new java.awt.Dimension(37, 14));
        jLabel39.setMinimumSize(new java.awt.Dimension(37, 14));

        Label_RunTime_UpTime.setText("<0>");

        Label_RunTime_TemperatureCycles.setText("<0>");

        Label_RunTime_PumpCycles.setText("<0>");

        Label_RunTime_ADCCycles.setText("<0>");

        Label_RunTime_DishCycles.setText("<0>");

        Label_RunTime_QueryCount.setText("<0>");

        Label_RunTime_ThermostatCycles.setText("<0>");

        Label_RunTime_TotalHeatTime.setText("<0>");

        Label_RunTime_EEPROMWrites.setText("<0>");

        Label_RunTime_DataOutput_Count.setText("<0>");

        Label_RunTime_Temps_Per_Second.setText("<0>");

        Label_RunTime_ADC_Per_Second.setText("<0>");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(Label_UpTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel25, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                    .addComponent(jLabel26, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel28, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel29, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel31, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(Label_RunTime_QueryCount, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE)
                    .addComponent(Label_RunTime_DishCycles, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Label_RunTime_ADCCycles, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Label_RunTime_PumpCycles, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Label_RunTime_TemperatureCycles, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Label_RunTime_UpTime, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(Label_RunTime_ThermostatCycles, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(Label_RunTime_TotalHeatTime, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(Label_RunTime_EEPROMWrites, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(Label_RunTime_ADC_Per_Second, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(Label_RunTime_DataOutput_Count, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel37, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(Label_RunTime_Temps_Per_Second, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Label_UpTime)
                    .addComponent(Label_RunTime_UpTime)
                    .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Label_RunTime_ThermostatCycles))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Label_RunTime_TemperatureCycles)
                    .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Label_RunTime_TotalHeatTime))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Label_RunTime_PumpCycles)
                    .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Label_RunTime_EEPROMWrites))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Label_RunTime_ADCCycles)
                    .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Label_RunTime_DataOutput_Count))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Label_RunTime_DishCycles)
                    .addComponent(jLabel37, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Label_RunTime_Temps_Per_Second))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Label_RunTime_QueryCount)
                    .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Label_RunTime_ADC_Per_Second)))
        );

        jPanel3.setBackground(new java.awt.Color(255, 204, 204));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Movement Flags", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.BELOW_TOP));
        jPanel3.setMaximumSize(new java.awt.Dimension(194, 186));
        jPanel3.setMinimumSize(new java.awt.Dimension(194, 186));

        jLabel52.setText("Wind Speed");

        jLabel53.setText("Wind Speed Satus");

        jLabel54.setText("Failsafe Check");

        jLabel56.setText("Proximity Right");

        jLabel57.setText("Proximity Left");

        jLabel58.setText("Proximity Up");

        jLabel59.setText("Proximity Down");

        Label_MF_Wind_Speed.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        Label_MF_Wind_Speed.setText("<0>");

        Label_MF_Wind_Speed_Status.setText("<OK>");

        Label_MF_Failsafe_Check.setText("<OK>");

        Label_MF_Froximity_Right.setText("<OK>");

        Label_MF_Proximity_Left.setText("<OK>");

        Label_MF_Proximity_Up.setText("<OK>");

        Label_MF_Proximity_Down.setText("<OK>");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel53, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel52, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel54, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel56, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel57, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel58, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel59, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(Label_MF_Proximity_Up, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                    .addComponent(Label_MF_Proximity_Left, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Label_MF_Froximity_Right, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Label_MF_Failsafe_Check, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Label_MF_Wind_Speed_Status, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Label_MF_Wind_Speed, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Label_MF_Proximity_Down, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Label_MF_Wind_Speed, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel52, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Label_MF_Wind_Speed_Status)
                    .addComponent(jLabel53))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel54)
                    .addComponent(Label_MF_Failsafe_Check))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel56)
                    .addComponent(Label_MF_Froximity_Right))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel57)
                    .addComponent(Label_MF_Proximity_Left))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel58)
                    .addComponent(Label_MF_Proximity_Up))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel59)
                    .addComponent(Label_MF_Proximity_Down)))
        );

        jPanel4.setBackground(new java.awt.Color(204, 255, 204));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Horizontal Movement", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.BELOW_TOP));

        jLabel68.setText("            Right   /   Left");

        jLabel69.setText("Low");

        jLabel70.setText("High");

        jLabel71.setText("Total");

        Dish_H_Status.setText("<No Movement>");

        Dish_H_R_Low.setText("<0>");

        Dish_H_L_Low.setText("<0>");

        Dish_H_Total.setText("<0>");

        Dish_H_R_High.setText("<0>");

        Dish_H_L_High.setText("<0>");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel69, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel70, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(Dish_H_R_Low, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                            .addComponent(Dish_H_R_High, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(Dish_H_L_Low, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(Dish_H_L_High, javax.swing.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Dish_H_Total, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel68, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel71, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(97, Short.MAX_VALUE))
                    .addComponent(Dish_H_Status, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel68)
                    .addComponent(jLabel71))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel69)
                    .addComponent(Dish_H_R_Low)
                    .addComponent(Dish_H_L_Low)
                    .addComponent(Dish_H_Total))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel70)
                    .addComponent(Dish_H_R_High)
                    .addComponent(Dish_H_L_High))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Dish_H_Status)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBackground(new java.awt.Color(153, 153, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Vertical Movement", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.BELOW_TOP));

        jLabel74.setText("             Up    /    Down");

        jLabel75.setText("Low");

        jLabel76.setText("High");

        jLabel77.setText("Total");

        Dish_V_Status.setText("<No Movement>");

        Dish_V_U_Low.setText("<0>");

        Dish_V_D_Low.setText("<0>");

        Dish_V_Total.setText("<0>");

        Dish_V_U_High.setText("<0>");

        Dish_V_D_High.setText("<0>");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Dish_V_Status, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel74, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel75, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel76, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(Dish_V_U_High, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                                    .addComponent(Dish_V_U_Low, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(Dish_V_D_High)
                                    .addComponent(Dish_V_D_Low, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel77, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(Dish_V_Total, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel74)
                    .addComponent(jLabel77))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel75)
                    .addComponent(Dish_V_U_Low)
                    .addComponent(Dish_V_D_Low)
                    .addComponent(Dish_V_Total))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel76)
                    .addComponent(Dish_V_U_High)
                    .addComponent(Dish_V_D_High))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Dish_V_Status)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel23.setBackground(new java.awt.Color(204, 255, 204));
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel23.setText("Remote IP:");

        Label_Remote_IP.setBackground(new java.awt.Color(204, 255, 204));
        Label_Remote_IP.setText("<0.0.0.0>");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(Panel_EEPROM, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(Button_Connect_To_Controller, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(Label_Status, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(Button_Change_EEPROM_Settings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(Label_Local_IP, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(Label_Local_UDP_Port, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 12, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(Label_Remote_IP, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(Panel_Temperatures, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(0, 21, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(Panel_EEPROM, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(7, 7, 7)
                                .addComponent(Button_Change_EEPROM_Settings))
                            .addComponent(Panel_Temperatures, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(11, 11, 11)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel20)
                                    .addComponent(Label_Local_IP))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel18)
                                    .addComponent(Label_Local_UDP_Port))
                                .addGap(18, 18, 18)
                                .addComponent(Button_Connect_To_Controller)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel23)
                                    .addComponent(Label_Remote_IP))
                                .addGap(50, 50, 50)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel22)
                                    .addComponent(Label_Status)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        Panel_EEPROM.getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void Button_Change_EEPROM_SettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Button_Change_EEPROM_SettingsActionPerformed

        //EEPROM_Interface EEPROM_GUI = new EEPROM_Interface(this, false);
        
        EEPROM_Setup.setVisible(true);

        // allows commands to be sent to the Controller Board.
        EEPROM_Setup.setUDPCommunications(UDP_Comm);

        EEPROM_Setup.setTitle("EEPROM Configuration");
        
        //JDialog dialog1 = new JDialog(parent, "Dialog1 - Modeless Dialog");
        //dialog1.setBounds(200, 200, 300, 200);
        //dialog1.setVisible(true);
        
        //ControllerBoard jpControllerBoard = new ControllerBoard("EEPROM Settings");

        //jpControllerBoard.setVisible(true);
        //add (jpControllerBoard);
    }//GEN-LAST:event_Button_Change_EEPROM_SettingsActionPerformed

    private void Connect_Listener(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Connect_Listener
        
        // ok, right here we need to start doing our thing...
        String strButtonText = Button_Connect_To_Controller.getText();
        if (strButtonText.compareTo("Connect To Contoller Board") == 0) {
        
            int iPort = Integer.parseInt(Label_Local_UDP_Port.getText());
        
            TSet = new Thread_Setup(TProc, iPort);
        
            Thread_Setup.Thread_Setup_Threads();

            if ((TSet.Check_UDP_Thread()) && (TSet.Check_Screen_Thread())) {
                Button_Connect_To_Controller.setText("Stop Communications With Controller Board");
            }
            else {
                if (TSet.Check_UDP_Thread()) {
                    Label_Status.setText("UDP Thread is OK.  Screen Update Thread is Not");
                }
                else if (TSet.Check_Screen_Thread()) {
                    Label_Status.setText("Screen Update Thread is OK.  UDP Thread is No");
                }
                else {
                    Label_Status.setText("UDP Thread and Screen Update Thread Failed.");
                }
            }
        }
        else {
            if (strButtonText.compareTo("Stop Communications With Controller Board") == 0) {
                UDP_Comm.Stop_Communicating();
                Button_Connect_To_Controller.setText("Connect To Contoller Board");
            }
            
        }
            
            
    }//GEN-LAST:event_Connect_Listener

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Viewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Viewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Viewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Viewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Viewer().setVisible(true);

            }
        });
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Button_Change_EEPROM_Settings;
    private javax.swing.JToggleButton Button_Connect_To_Controller;
    private javax.swing.JLabel Dish_H_L_High;
    private javax.swing.JLabel Dish_H_L_Low;
    private javax.swing.JLabel Dish_H_R_High;
    private javax.swing.JLabel Dish_H_R_Low;
    private javax.swing.JLabel Dish_H_Status;
    private javax.swing.JLabel Dish_H_Total;
    private javax.swing.JLabel Dish_V_D_High;
    private javax.swing.JLabel Dish_V_D_Low;
    private javax.swing.JLabel Dish_V_Status;
    private javax.swing.JLabel Dish_V_Total;
    private javax.swing.JLabel Dish_V_U_High;
    private javax.swing.JLabel Dish_V_U_Low;
    private javax.swing.JLabel Label_EEPROM_Dark_Threshold;
    private javax.swing.JLabel Label_EEPROM_Delay_Sudden_Moveback;
    private javax.swing.JLabel Label_EEPROM_Horizontal_Adjustment;
    private javax.swing.JLabel Label_EEPROM_Move_Threshold;
    private javax.swing.JLabel Label_EEPROM_Processor_Speed;
    private javax.swing.JLabel Label_EEPROM_RebootCount;
    private javax.swing.JLabel Label_EEPROM_Server_Name;
    private javax.swing.JLabel Label_EEPROM_Temperature_Resolution;
    private javax.swing.JLabel Label_EEPROM_UART_Telemetry;
    private javax.swing.JLabel Label_EEPROM_UDP_Port;
    private javax.swing.JLabel Label_EEPROM_UDP_Telemetry;
    private javax.swing.JLabel Label_EEPROM_USE_DNS;
    private javax.swing.JLabel Label_EEPROM_Version;
    private javax.swing.JLabel Label_EEPROM_Verticall_Adjustment;
    private javax.swing.JLabel Label_EEPROM_Wind_Speed;
    private javax.swing.JLabel Label_EEPROM_Wind_Speed_Delay;
    private javax.swing.JLabel Label_Local_IP;
    private javax.swing.JLabel Label_Local_UDP_Port;
    private javax.swing.JLabel Label_MF_Failsafe_Check;
    private javax.swing.JLabel Label_MF_Froximity_Right;
    private javax.swing.JLabel Label_MF_Proximity_Down;
    private javax.swing.JLabel Label_MF_Proximity_Left;
    private javax.swing.JLabel Label_MF_Proximity_Up;
    private javax.swing.JLabel Label_MF_Wind_Speed;
    private javax.swing.JLabel Label_MF_Wind_Speed_Status;
    private javax.swing.JLabel Label_Remote_IP;
    private javax.swing.JLabel Label_RunTime_ADCCycles;
    private javax.swing.JLabel Label_RunTime_ADC_Per_Second;
    private javax.swing.JLabel Label_RunTime_DataOutput_Count;
    private javax.swing.JLabel Label_RunTime_DishCycles;
    private javax.swing.JLabel Label_RunTime_EEPROMWrites;
    private javax.swing.JLabel Label_RunTime_PumpCycles;
    private javax.swing.JLabel Label_RunTime_QueryCount;
    private javax.swing.JLabel Label_RunTime_TemperatureCycles;
    private javax.swing.JLabel Label_RunTime_Temps_Per_Second;
    private javax.swing.JLabel Label_RunTime_ThermostatCycles;
    private javax.swing.JLabel Label_RunTime_TotalHeatTime;
    private javax.swing.JLabel Label_RunTime_UpTime;
    private javax.swing.JLabel Label_Status;
    private javax.swing.JLabel Label_UpTime;
    private javax.swing.JPanel Panel_EEPROM;
    private javax.swing.JPanel Panel_Temperatures;
    private javax.swing.JLabel Pump_AUX_Percent;
    private javax.swing.JLabel Pump_AUX_Status;
    private javax.swing.JLabel Pump_Dish_Percent;
    private javax.swing.JLabel Pump_Dish_Status;
    private javax.swing.JLabel Pump_Hold_Percent;
    private javax.swing.JLabel Pump_Hold_Status;
    private javax.swing.JLabel Pump_Immdt_Percent;
    private javax.swing.JLabel Pump_Immdt_Status;
    private javax.swing.JLabel Pump_Thermostat;
    private javax.swing.JLabel TEMP_C_0;
    private javax.swing.JLabel TEMP_C_1;
    private javax.swing.JLabel TEMP_C_10;
    private javax.swing.JLabel TEMP_C_11;
    private javax.swing.JLabel TEMP_C_12;
    private javax.swing.JLabel TEMP_C_13;
    private javax.swing.JLabel TEMP_C_14;
    private javax.swing.JLabel TEMP_C_15;
    private javax.swing.JLabel TEMP_C_2;
    private javax.swing.JLabel TEMP_C_3;
    private javax.swing.JLabel TEMP_C_4;
    private javax.swing.JLabel TEMP_C_5;
    private javax.swing.JLabel TEMP_C_6;
    private javax.swing.JLabel TEMP_C_7;
    private javax.swing.JLabel TEMP_C_8;
    private javax.swing.JLabel TEMP_C_9;
    private javax.swing.JLabel TEMP_Dev_0;
    private javax.swing.JLabel TEMP_Dev_1;
    private javax.swing.JLabel TEMP_Dev_10;
    private javax.swing.JLabel TEMP_Dev_11;
    private javax.swing.JLabel TEMP_Dev_12;
    private javax.swing.JLabel TEMP_Dev_13;
    private javax.swing.JLabel TEMP_Dev_14;
    private javax.swing.JLabel TEMP_Dev_15;
    private javax.swing.JLabel TEMP_Dev_2;
    private javax.swing.JLabel TEMP_Dev_3;
    private javax.swing.JLabel TEMP_Dev_4;
    private javax.swing.JLabel TEMP_Dev_5;
    private javax.swing.JLabel TEMP_Dev_6;
    private javax.swing.JLabel TEMP_Dev_7;
    private javax.swing.JLabel TEMP_Dev_8;
    private javax.swing.JLabel TEMP_Dev_9;
    private javax.swing.JLabel TEMP_F_0;
    private javax.swing.JLabel TEMP_F_1;
    private javax.swing.JLabel TEMP_F_10;
    private javax.swing.JLabel TEMP_F_11;
    private javax.swing.JLabel TEMP_F_12;
    private javax.swing.JLabel TEMP_F_13;
    private javax.swing.JLabel TEMP_F_14;
    private javax.swing.JLabel TEMP_F_15;
    private javax.swing.JLabel TEMP_F_2;
    private javax.swing.JLabel TEMP_F_3;
    private javax.swing.JLabel TEMP_F_4;
    private javax.swing.JLabel TEMP_F_5;
    private javax.swing.JLabel TEMP_F_6;
    private javax.swing.JLabel TEMP_F_7;
    private javax.swing.JLabel TEMP_F_8;
    private javax.swing.JLabel TEMP_F_9;
    private javax.swing.JLabel TEMP_Flag_0;
    private javax.swing.JLabel TEMP_Flag_1;
    private javax.swing.JLabel TEMP_Flag_10;
    private javax.swing.JLabel TEMP_Flag_11;
    private javax.swing.JLabel TEMP_Flag_12;
    private javax.swing.JLabel TEMP_Flag_13;
    private javax.swing.JLabel TEMP_Flag_14;
    private javax.swing.JLabel TEMP_Flag_15;
    private javax.swing.JLabel TEMP_Flag_2;
    private javax.swing.JLabel TEMP_Flag_3;
    private javax.swing.JLabel TEMP_Flag_4;
    private javax.swing.JLabel TEMP_Flag_5;
    private javax.swing.JLabel TEMP_Flag_6;
    private javax.swing.JLabel TEMP_Flag_7;
    private javax.swing.JLabel TEMP_Flag_8;
    private javax.swing.JLabel TEMP_Flag_9;
    private javax.swing.JLabel TEMP_ROM_0;
    private javax.swing.JLabel TEMP_ROM_1;
    private javax.swing.JLabel TEMP_ROM_10;
    private javax.swing.JLabel TEMP_ROM_11;
    private javax.swing.JLabel TEMP_ROM_12;
    private javax.swing.JLabel TEMP_ROM_13;
    private javax.swing.JLabel TEMP_ROM_14;
    private javax.swing.JLabel TEMP_ROM_15;
    private javax.swing.JLabel TEMP_ROM_2;
    private javax.swing.JLabel TEMP_ROM_3;
    private javax.swing.JLabel TEMP_ROM_4;
    private javax.swing.JLabel TEMP_ROM_5;
    private javax.swing.JLabel TEMP_ROM_6;
    private javax.swing.JLabel TEMP_ROM_7;
    private javax.swing.JLabel TEMP_ROM_8;
    private javax.swing.JLabel TEMP_ROM_9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JLabel jLabel77;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel89;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    // End of variables declaration//GEN-END:variables
}
