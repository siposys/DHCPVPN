/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gateway.server;

import java.io.IOException;
import java.util.ArrayList;
import sockets.RawSocket;
import sockets.IpPacket_deprecated;
import sockets.TcpPacket;
import sockets.editable.TcpPacketBuilder;

/**
 *
 * @author root
 */
public class ConnectedDevice {
    private final int clientIp, gatewayIp;
    private ArrayList<IpPacketHolder> requests = new ArrayList<IpPacketHolder>();
    private RawSocket rawOut;
    
    public ConnectedDevice(int ClientIp, int gatewayIp, RawSocket outputStream){
        this.clientIp = ClientIp;
        this.gatewayIp = gatewayIp;
        this.rawOut = outputStream;
    }
    
    public boolean accept(byte[] s) throws IOException{
        
        for(IpPacketHolder p : requests){
            if(p.accept(s)){
                return true;
            }
        }
        
        int sourceIpAddress = TcpPacketBuilder.getInt(12, s);
        int destinationIp = TcpPacketBuilder.getInt(16, s);
        
        if(sourceIpAddress == clientIp){
            System.out.println("making new packet");
            IpPacket_deprecated sock = new IpPacket_deprecated(s, rawOut);
            IpPacketHolder req = new IpPacketHolder(sock, rawOut, gatewayIp);
            requests.add(req);
            
            if(!req.accept(s)){ throw new IOException(); }
            return true;
        }
            return false;
    }
    
}
