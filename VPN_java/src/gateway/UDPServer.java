/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gateway;

import dhcp.DHCPPacket;
import dhcp.DHCPServer;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import sockets.RawSocket;
import sockets.IpPacket;
import udp.PrivateIpHandler;

/**
 *
 * @author root
 */
public class UDPServer implements Runnable{
    public final RawSocket sock;// = RawSocket.initialize_UDP("eth0");
    private int clientIp = IpPacket.ipStringToInt("192.168.1.11");
    private int serverIp = IpPacket.ipStringToInt("192.168.1.12"), forwardingIp = IpPacket.ipStringToInt("72.188.192.147");
    public final DHCPServer dhcp ;
    public final PrivateIpHandler privateIp;
    
    public UDPServer(DHCPServer dhcp, PrivateIpHandler priv){
        this.dhcp = dhcp;
        this.privateIp = priv;
        sock = dhcp.out;
    }
    
    private Thread t;
    public void start(){
        if(t == null){
            t = new Thread(this);
            t.start();
        }
    }
    public void run(){
                dhcp.DHCPServer dhcp = new dhcp.DHCPServer(sock);
        while(true){
            try {
                System.out.println("\n\n\n\nUDP read loop:");
                
                byte[] b = sock.accept();
                
                if(dhcp.accept(b)){ continue; }//check if its a dhcp packet
                
                if(privateIp.accept(b)){ continue; }
                
                System.out.println("regular udp packetRecieved:"+
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
                        System.out.println("unknown UDP packet:"+IpPacket.UDPPacket.toString(b));
                        System.out.println("array"+new String(Arrays.copyOfRange(b, IpPacket.UDPPacket.getPayloadStartIndex(b), b.length))
                        );
                    }catch (IndexOutOfBoundsException e){
                        throw e;
                    }
                
                
            } catch (IOException ex) {
                Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
