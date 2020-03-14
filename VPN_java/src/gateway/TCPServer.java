/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gateway;

import dhcp.DHCPServer;
import gateway.server.ConnectedDevice;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import sockets.RawSocket;
import sockets.IpPacket;
import sockets.IpPacket_deprecated;
import udp.PrivateIpHandler;

/**
 *
 * @author root
 */
public class TCPServer implements Runnable {
    private RawSocket TCP_serverSocket;// = RawSocket.initialize_TCP("wlan0");
    private int clientIp = IpPacket.ipStringToInt("192.168.1.11");
    private int serverIp = IpPacket.ipStringToInt("192.168.1.12"), forwardingIp = IpPacket.ipStringToInt("72.188.192.147");
    public final DHCPServer dhcp;
    public final PrivateIpHandler privateIp;
    
    private ArrayList<ConnectedDevice> clients = new ArrayList<ConnectedDevice>();
    
    public TCPServer(DHCPServer dhcp, PrivateIpHandler priv){
        this.dhcp = dhcp;
        this.privateIp = priv;
        this.TCP_serverSocket = priv.outTCP;
        //TCP
    }
    
    public Thread t = null;
    public void start(){
        if(t == null){
            t = new Thread(this);
            t.start();
        }
    }
    
    /**
     * runs the server on the current thread
     */
    public void run(){
        while(true){
            System.out.println("\n\nTCP read loopp");
            byte[] s = null;
            try {
                s = TCP_serverSocket.accept();
                if(s == null){ throw new IOException("socket is null"); }
                
                
                if(privateIp.accept(s)){ continue; }
                
                else{
                    logUnknownPacket(s);
                }
            
            } catch (IOException ex) {
                        Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
                        if(s !=null){
                            System.out.println(s.toString());
                        }
                       break;
            }
            
           
        }
        t = null;
        
    }
    private void logUnknownPacket(byte[] b){
        System.out.println("regular TCP packet Recieved:"+
                IpPacket.ipIntToString(IpPacket.getSourceIp(b))
                +":"+
                IpPacket.UDPPacket.getSourcePort(b)+
                "=>"+
                IpPacket.ipIntToString(IpPacket.getDestIp(b))
                +":"+
                IpPacket.UDPPacket.getDestPort(b)

        );
                
        try{
            System.out.println("unknonwn ip packet:"+IpPacket.toString(b));
            System.out.println("unknown UDP packet:"+IpPacket.TCPPacket.toString(b));
            System.out.println("array"+new String(Arrays.copyOfRange(b, IpPacket.TCPPacket.getPayloadStartIndex(b), b.length))
            );
        }catch (IndexOutOfBoundsException e){
            throw e;
        }
    }
    
}
