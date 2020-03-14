/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp;

import dhcp.DHCPServer;
import java.util.ArrayList;
import java.util.Arrays;
import sockets.IpPacket;
import sockets.RawSocket;
import gateway.routers.Client;
import gateway.routers.LocalRouter;
import java.io.IOException;

/**
 *
 * @author root
 */
public class PrivateIpHandler {
    private static final int priv10min = IpPacket.ipStringToInt("10.0.0.0"), priv10max = IpPacket.ipStringToInt("10.255.255.255"),
            priv172min = IpPacket.ipStringToInt("172.16.0.0"), priv172max = IpPacket.ipStringToInt("172.31.255.255"),
            priv192min = IpPacket.ipStringToInt("192.168.0.0"), priv192max = IpPacket.ipStringToInt("192.168.255.255"),
            priv224min = IpPacket.ipStringToInt("224.0.0.0"), priv224max = IpPacket.ipStringToInt("224.0.0.255")
            ;
    
    private ArrayList<Client> clients = new ArrayList<Client>();
    private LocalRouter router;
    private DHCPServer clientManager;
    
    public final RawSocket outTCP, outUDP;
    
    public PrivateIpHandler(RawSocket outTCP, RawSocket outUDP, DHCPServer clientManager){
        this.outUDP = outUDP;
        this.outTCP = outTCP;
        this.clientManager = clientManager;
        this.router = new LocalRouter(outTCP,outUDP);
    }
    public static boolean isPrivateIp(int ip){
             if(ip>=priv10min && ip<=priv10max){ return true; }
        else if(ip>=priv172min && ip<=priv172max){ return true; }
        else if(ip>=priv192min && ip<=priv192max){ return true; }
        else if(ip>=priv224min && ip<=priv224max){ return true; }
        return false;
    }
    public boolean accept(byte[] b) throws IOException{
        if(!isPrivateIp(IpPacket.getDestIp(b))){
            return false;
        }
        if(
                IpPacket.getProtocol(b) == IpPacket.TCP_protocol
                ||
                IpPacket.getProtocol(b) == IpPacket.UDP_protocol
        ){
            
            
            int srcIp = IpPacket.getSourceIp(b);
            for(Client c : clients){
                if(c.accept(b)){
                    return true;
                }
            }
            if(clientManager.isConnectedClient(srcIp)){//if its one of the clients we care about
                clients.add(new Client(b, router));
                return true;
            }
            else{
                System.out.println("refused packet:private ip handler:"+srcIp);
                return false;
            }
        }
        else{
            throw new UnsupportedOperationException("unknown protocol:"+IpPacket.getProtocol(b));
        }
    }
    
}
