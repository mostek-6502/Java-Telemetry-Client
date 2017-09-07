/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


package my.Viewer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.logging.Level;

import java.util.logging.Logger;

/**
 *
 * @author Solar
 */
public class UDP_Communications {

    private final static Logger LOGGER = Logger.getLogger(UDP_Communications.class.getName());

    @SuppressWarnings("FieldMayBeFinal")
    private DatagramSocket socket = null;

    InetAddress RemoteClientIP  = null;
    int RemoteControllerPort = -1;
    
    int iReadTimeOut = 500;
    
    boolean bDataReady = false;
    
    public UDP_Communications() {
        // stub, just in case we need to use it later....
        
    }
    
    private int UDP_Setup_Datagram_Socket(int iPort) {

        if ((RemoteControllerPort == iPort) && (socket != null)) {
            LOGGER.log(Level.INFO, "UDP_Setup_And_Start()::UDP_Setup_Datagram_Socket()  Port Number Match.  Socket Open.  Exit.");
            return 0;
        }
        
        // was the port already open?  If is this a change?
        if (RemoteControllerPort != iPort) {
            if (socket != null) {
                if (socket.isConnected()) {
                    socket.disconnect();
                }
            
                if (socket.isClosed() == false) {
                    socket.close();
                }
                socket = null;
            }
        } 
        
        
        RemoteControllerPort = iPort;
        
        try {
            // this constructor BINDS the Local machine IP and the given Port to the Socket.
            // there does not appear to be a need to specifically call bind()
            socket = new DatagramSocket(RemoteControllerPort);
        }
        catch (SocketException eSocEx) {
            LOGGER.log(Level.SEVERE, "UDP_Setup_And_Start() SocketException in UDP_Setup_Datagram_Socket()", eSocEx);
            socket = null;
            return -1;
        }
        catch (SecurityException eSecEx) {
            LOGGER.log(Level.INFO, "UDP_Setup_And_Start()  SecurityException in UDP_Setup_Datagram_Socket()", eSecEx);
        }
        
        return 1;
    }
    
    
    public boolean UDP_Send_Packet(String strSendPacket) {

        if (RemoteClientIP == null) {
            LOGGER.log(Level.SEVERE, "UDP_Send_Packet()::Controller IP is Null!");
            
            bDataReady = false;
            
            return false;
        }

        if (RemoteControllerPort == -1) {
            LOGGER.log(Level.SEVERE, "UDP_Send_Packet()::Controller Port is -1!");
            
            bDataReady = false;
            
            return false;
        }

        // this needs to be in a multi-threaded pardigm...
        if (socket == null) {
            LOGGER.log(Level.SEVERE, "UDP_Send_Packet()::socket is NULL");
            return false;
        }

        
        byte bSendPacket[] = strSendPacket.getBytes();

        String strRemoteAddress = RemoteClientIP.getHostAddress();
        DatagramPacket dgP = new DatagramPacket(bSendPacket, bSendPacket.length, RemoteClientIP, RemoteControllerPort);
                
        try {
            socket.send(dgP);
                    
            String sTemp = "UDP_Run()::Send_Beacon() Sent->" + 
                            strSendPacket + 
                            "<-  To(" + 
                            RemoteClientIP +
                            ")  From(" + 
                            RemoteControllerPort +
                            ")";
                    
            LOGGER.log(Level.INFO, sTemp);
        } catch (IOException ioE) {
            LOGGER.log(Level.SEVERE, "UDP_Setup_And_Start()::Send_Beacon() Unable to Send DataGram." , ioE);
            
            bDataReady = false;
            
            return false;
        }
        
        return true;
    }
    
    private String getLocalIP() {

        // this is just getting the Local IP of this Client....
        String strIPAddress;
        // InetAddress iLocalIPAddress;
                
        try {
            strIPAddress = Inet4Address.getLocalHost().getHostAddress();
            //iLocalIPAddress = InetAddress.getByName(strIPAddress);
        }
        catch (UnknownHostException uhe) {
            LOGGER.log(Level.SEVERE, "UDP_Send_Beacon()::getLocalHost() or getByName().  Exception. ", uhe);
            return "0.0.0.0";
        }
       
        String strLocalIPAddress;
        if (strIPAddress.startsWith("/")) {
            strLocalIPAddress = strIPAddress.split("/")[1];
        }
        else {
            strLocalIPAddress = strIPAddress;
        }
        
        return strLocalIPAddress;
    }

    
    public boolean UDP_Send_Beacon() {
        
        
        // this simply takes an IPV4 address )of the local machine), loops through all of the 
        // possibilities from xxx.xxx.xxx.001 to xxx.xxx.xxx.255
        // This sends "SYNCH_1 " to the Controller Board and waits for a "SYNCH_2 "
        // Once the Jave Client Receives "SYNCH_2 " from the Controller Board, the Client
        // sends "SYNCH_3 " and the data starts flowing.
        
        if (RemoteControllerPort == -1) {
            LOGGER.log(Level.SEVERE, "UDP_Send_Beacon()::Remote Port Not Set. ");
            return false;
        }

        
        String strLocalIP = getLocalIP();
        
        InetAddress IPLocalAddress;
        try {
            IPLocalAddress = InetAddress.getByName(strLocalIP);
        } catch (UnknownHostException uhe) {
            LOGGER.log(Level.SEVERE, "UDP_Send_Beacon()::getLocalHost() or getByName().  Exception. ", uhe);
            return false;
        }

        
        @SuppressWarnings("UnusedAssignment")
        int i = 0;
        for (i = 1; i < 256; i++) {

            // this is the magic right here!!  This changes the end octet....
            byte[] bIPAddress = IPLocalAddress.getAddress();
            bIPAddress[3] = (byte) i;
            
            try {
                RemoteClientIP = InetAddress.getByAddress(bIPAddress);
            }
            catch (UnknownHostException uhe) {
                LOGGER.log(Level.SEVERE, "UDP_Send_Beacon()::getByAddress() Exception. ", uhe);
                return false;
            }
            
            
            if (RemoteClientIP.getHostAddress().equals(IPLocalAddress.getHostAddress())) {
                LOGGER.log(Level.INFO, "UDP_Send_Beacon()::Send_Beacon() Skipping Beacon IP.  Same as Local Machine");
            }
            else {
                // send out "SYNCH_1 "
                String strSend = "SYNCH_1 ";
                boolean b = UDP_Send_Packet(strSend);
                
                if (b == true) {
                    String sTemp = "UDP_Send_Beacon() Sent->" + 
                            strSend + 
                            "<-  To(" + 
                            RemoteClientIP.getHostAddress() +
                            ")  From(" + 
                            IPLocalAddress.getHostAddress() +
                            ")";
                    
                    LOGGER.log(Level.INFO, sTemp);
                } else {
                    LOGGER.log(Level.SEVERE, "UDP_Send_Beacon()::Send_Beacon() Unable to Send SYNCH_1.");
                    return false;
                }
            }
        }
          
        return true;
    }
    
    
    public boolean UDP_Setup_And_Start(int iPort) {
        
        int iRtn = UDP_Setup_Datagram_Socket(iPort);

        // Return Codes
        // -1 = Error, stop processing
        //  0 = No Changes, Nothing Happened, All OK
        // +1 = Some sort of change, fire up the Beacon
        
        if (iRtn == -1) {
            LOGGER.log(Level.SEVERE, "UDP_Setup_And_Start()  Return From:UDP_Setup_Datagram_Socket() Invalid Return.");
            return false;
        }

        if (iRtn == 0) {
            LOGGER.log(Level.INFO, "UDP_Setup_And_Start()  UDP Running.  No Port Changes.  No State Change.");
            return true;
        }
        
        // there was a new socket, go ahead and scan it...
        if (iRtn != 1) {
            String strError = "UDP_Setup_And_Start()  Invalid Return From: UDP_Setup_Datagram_Socket()  Code: " + Integer.toString(iRtn);
            LOGGER.log(Level.INFO, "UDP_Setup_And_Start()  ");
            
            return false;
        }

        //LOGGER.log(Level.INFO, "UDP_Setup_And_Start()  Successful Beacon!");
        
        return true;
    }
    
    
    
    public String UDP_Data_Read() {

        // Return Codes
        // -1 = Error, stop processing
        //  0 = No Changes, Nothing Happened, All OK
        // +1 = Some sort of change, fire up the Beacon
        
        byte byteSocketBuffer[] = new byte[2048];
        
        int iLen;
        
        String strRemoteIP;
        String strRemoteInfo;
        
        
        DatagramPacket dgPacket = new DatagramPacket(byteSocketBuffer, byteSocketBuffer.length);

        // the very first time a UDP read is initiated, the Timeout is set to 500ms.
        // this is primarily there to give enough time for the app to start receiving 
        // data from the controller board.  If the timeout occurs, a beacon is sent.
        // data should arrive from the board at least 15 time a second, thereafter.
        // thus, timeout is reset to 75ms, which should be ample time for data to arrive.

        // ok this needs to in a multi-threaded paradigm ... 
        // a close can be issued while still trying to read...
        if (socket == null) {
            LOGGER.log(Level.SEVERE, "UDP_Data_Read()::socket is NULL");
            return "ERROR-socket is null";
        }

        try {
            socket.setSoTimeout(iReadTimeOut);
        } catch (SocketException e) {
            LOGGER.log(Level.SEVERE, "UDP_Data_Read()::socket.setSoTimeout() Invalid Return. Exception.", e);
            return "ERROR-set_timeout";
        }
        iReadTimeOut = 75;
            

        
        try {
            socket.receive(dgPacket);     
        }
        catch (SocketTimeoutException eTO) {
            LOGGER.log(Level.SEVERE, "UDP_Data_Read()::socket.receive() Read Timeout.");
            return "TIMEOUT";
        }
        catch (IOException ioE) {
            LOGGER.log(Level.SEVERE, "UDP_Data_Read()::socket.receive() Invalid Return. Exception.", ioE);
            
            bDataReady = false;
            
            return "ERROR-receive";
        } 
         
        
        // this really should never happen; but, handle it anyway....
        iLen = dgPacket.getLength();
        if (iLen == 0) {
            return "NO DATA";  // this really should never be 0, because we checked the return before calling this.
        }
        
        byte bData[] = dgPacket.getData();
        String strData = new String(bData);
        
        //strData = strData.substring(0, iLen);
        
        if ("SYNCH_1 ".equals(strData)) {
            // this is just blow back from the Beacon Command, throw it away
            return "SYNCH_1 ";
        }

        
        // ok, we probably got something good here...
        // set the Remote IP & Port on Every Receive...  It may be something different...
        strRemoteIP = dgPacket.getAddress().toString().split("/")[1];
        try {
            RemoteClientIP = InetAddress.getByName(strRemoteIP);
        }
        catch (UnknownHostException uhe) {
            LOGGER.log(Level.SEVERE, "Process_REMOTE_Command()::Unable to convert IP.  Exception.", uhe);
            return "ERROR-address";
        }
        RemoteControllerPort = dgPacket.getPort();
        
        bDataReady = true;
        
        return strData;
    }
    
    public boolean DataReady() {
        return bDataReady;
    }
             
    public String getRemoteIP() {
        
        String strIP = "";
        
        if (bDataReady) {
            strIP = RemoteClientIP.toString();
            
            if (strIP.startsWith("/")) {
                strIP = strIP.substring(1);
            }
        }
        
        return strIP;
    }
    
    
    public void Stop_Communicating() {
        socket.disconnect();
        socket.close();
        socket = null;

        bDataReady = false;
        
        RemoteClientIP  = null;
        RemoteControllerPort = -1;
    }
    
}
    
