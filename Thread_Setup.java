/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *
 */

 
package my.Viewer;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Thread_Setup {

    private final static Logger LOGGER = Logger.getLogger(Thread_Setup.class.getName());
    
    static int iUDPPort;
    
    static Thread_Processing TProc = null;
    
    static private Thread_Retreive_UDP_Data thread_Retreive_UDP_Data = null;
    static private Thread_Update_Screen thread_Update_Screen = null;
    
    
    public Thread_Setup(Thread_Processing TProc, int iUDPPort) {
        Thread_Setup.iUDPPort = iUDPPort;

        Thread_Setup.TProc = TProc;
    }

    
    public static void Thread_Setup_Threads() {
        
        thread_Retreive_UDP_Data = new Thread_Retreive_UDP_Data("Thread_Receive_UDP_Data");
        thread_Retreive_UDP_Data.setDaemon(true);
        thread_Retreive_UDP_Data.start();
       
        thread_Update_Screen = new Thread_Update_Screen("Thread_Update_Screen");
        thread_Update_Screen.setDaemon(true);
        thread_Update_Screen.start();
    } 

    
    public boolean Check_UDP_Thread() {

        return thread_Retreive_UDP_Data.isAlive();
        
    }
    
    public boolean Check_Screen_Thread() {
        
        return thread_Update_Screen.isAlive();

    }

    
    static class Thread_Retreive_UDP_Data extends Thread implements Runnable {
        
        Thread_Retreive_UDP_Data(String strThreadName) {
            super(strThreadName);
        }
        
        @Override
        public void run() {
            try {
                TProc.Thread_Retreive_UDP_Data(iUDPPort);
            }
            catch(InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Thread_Retreive_UDP_Data() Error! ", e);
            }
        }
    }


    static class Thread_Update_Screen extends Thread implements Runnable {
        
        Thread_Update_Screen(String strThreadName) {
            super(strThreadName);
        }
        
        @Override
        public void run()
        {
            try
            {
                TProc.Thread_Update_Screen();
            }
            catch(InterruptedException e)
            {
                LOGGER.log(Level.SEVERE, "Thread_Update_Screen() Error! ", e);
            }
        }
    }

    
}
