/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockets;
import java.io.IOException;
import sockets.IpPacket.TCPPacket;
import sockets.editable.IpPacketBuilder;
import sockets.editable.TcpPacketBuilder;

/**
 *
 * @author root
 */
public class Tcp3WayHandshake {
    /**
     * 
     * @param request
     * @return response byte if this is a handshake request, and null if it is not
     */
    public static byte[] isHandshake(byte[] request) throws IOException{
        if(isStep1Request(request)){
            return sendStep2(request);
        }
        return null;
    }
    
    public static boolean isStep1Request(byte[] request){
        if(
                IpPacket.getProtocol(request) != IpPacket.TCP_protocol//not correct protocol
                ||
                !TCPPacket.isSYN(request) // no sync flag
                ||
                TCPPacket.isACK(request)//should not have ack flag
                
        ){
            return false;
        }
        return true;
    }
    /**
     * 
     * @param synRequest the recieved syn request
     * @return the response [syn+ack] request
     */
    public static byte[] sendStep2(byte[] synRequest ) throws IOException{
        if(!isStep1Request(synRequest)){
            throw new IOException("request is not a syn request");
        }
        IpPacketBuilder ip = new IpPacketBuilder(synRequest);
            int srcIp = ip.getSourceIp();
            ip.setSourceIp(ip.getDestIp());
            ip.setDestIp(srcIp);
            
        TcpPacketBuilder tcp = new TcpPacketBuilder(synRequest);
            int srcPort = tcp.getSourcePort();
            tcp.setSourcePort(tcp.getDestPort());
            tcp.setDestPort(srcPort);
            tcp.setACK(true);
            tcp.setSYN(true);
            tcp.setAckNumber(tcp.getSequenceNumber()+1);
            tcp.setSequenceNumber((int)(Math.random()*Short.MAX_VALUE));
            
        return tcp.buildWholePacket(ip);
    }
    
}
