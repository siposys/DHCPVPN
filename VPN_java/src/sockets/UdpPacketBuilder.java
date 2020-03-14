/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockets;

import java.util.Arrays;
import sockets.editable.IpPacketBuilder;
import sockets.editable.TcpPacketBuilder;

/**
 *
 * @author root
 */
public class UdpPacketBuilder {
    public static final int UDP_protocol = 17, payload_start_index= 8;
    
    private byte[]b ;
    public UdpPacketBuilder(){
        b = new byte[8];
    }
    public UdpPacketBuilder(byte[] b, int offset){
        this.b = Arrays.copyOfRange(b, offset, offset+payload_start_index);
    }
    
    public  int getSourcePort(){
        return TcpPacketBuilder.getShort(0, b);
    }
    public UdpPacketBuilder setSourcePort(int port){
        TcpPacketBuilder.setShort(port, 0, b);
        return this;
    }
    public int getDestPort(){
        return TcpPacketBuilder.getShort(2, b);
    }
    public UdpPacketBuilder setDestPort(int port){
        TcpPacketBuilder.setShort(port, 2, b);
        return this;
    }
    public int getLength(){
        return TcpPacketBuilder.getShort(4, b);
    }
    public UdpPacketBuilder setLength(int length){
        TcpPacketBuilder.setShort(length, 4, b);
        return this;
    }
    /**
     * 
     * @return true if the checksum passes, or false if the checksum fails
     */
    public boolean checksum(int sourceIp, int destIp, int totalLength){
        int checksum = TcpPacketBuilder.getShort(6,b);
        if(checksum == 0){
            return true;
        }
        
        long sum = 0;
        //source ip
        sum+= (sourceIp>>16 )& 0xFFFF;
        sum+= sourceIp& 0xffff;
              
        //dest ip
        sum+= (destIp>>16)& 0xFFFF;
        sum+= destIp & 0xffff;
        
        // 8 bit 0
        
        //protocol
        sum+=  (IpPacket_deprecated.TCP_protocol);
        
        sum+=(~IpPacket_deprecated.checksum(b, 0, b.length))&0xffff;
        
        while(sum>0xFFFF){
            sum = (sum&0xffff) + (sum>>16);
        }
        
        return ((~sum)&0xffff) == 0;
    }
    public UdpPacketBuilder setChecksum(int sourceIp, int destIp, int totalLength){
        TcpPacketBuilder.setShort(0,6,b);
        return this;
    }
    /**
     * 
     * @param ip
     * @param payload
     * @return  [ip packet] + [udp packet] + [payload]
     */
    public byte[] buildWholePacket(IpPacketBuilder ip, byte[] payload){
        byte[] b = build(
                    ip.getSourceIp(), 
                    ip.getDestIp(), 
                    IpPacket_deprecated.payloadStartIndex+payload.length+payload_start_index,
                    payload
                );
        return ip.build(b);
    }
    /**
     * 
     * @param sourceIp
     * @param destIp
     * @param totalLength
     * @param payload
     * @return  [udp packet] + [payload] 
     */
    public byte[] build(int sourceIp, int destIp, int totalLength, byte[] payload){
        setLength(payload_start_index+payload.length);
        setChecksum(sourceIp, destIp, totalLength);
        byte[] retu = new byte[getLength()];
        for(int i = 0; i<this.b.length; i++){
            retu[i] = this.b[i];
        }
        for(int i = 0; i<payload.length; i++){
            retu[i+b.length] = payload[i];
        }
        return retu;
    }
}
