# Java-Telemetry-Client

This Java Client accepts real-time data from a micro-controller board.  


The micro-controller board outputs 80+ sensor readings in one group of data.  In test mode, 
the board is capable of outputting 1,200 groups per second.   In production mode, the 
board produces less than 50 groups of data per second.


The current hardware / sofware configuration of the Java client, can process 500 groups
of data.  Perfromance was measured on worst case scenario hardware platforms to ensure 
high levels of performance in production mode.  


The salient features of the program are:

    1. UDP communications to/from board

    2. Actively or passively acquire board data
    
    3. Multithreading implemented for 
    
       a. UDP Communications
       
       b. Screen Processing
       
       c. Use of standard Java Swing for threadsafe GUI updates
       
   4. Netbeans 8.2

The .png listing provides an understanding of the client.

