/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockets.editable;

import java.io.IOException;
import java.util.Arrays;
import sockets.IpPacket_deprecated;

/**
 *
 * @author root
 */
public class IpPacketBuilder {
    private byte[] b;
    //public byte[] payload;
    
    
    public IpPacketBuilder(){
        this.b = new byte[IpPacket_deprecated.payloadStartIndex];
        b[0] = ((4<<4) + 5);
                
    }
    public IpPacketBuilder(byte[] b) throws IOException{
        this.b = Arrays.copyOfRange(b, 0, IpPacket_deprecated.payloadStartIndex
        );
        //this.payload = Arrays.copyOfRange(
        //        b, Socket.payloadStartIndex, b.length
        //);
        if(
                b.length != getShort(2) //packet length
                && 
                b[0] != ((4<<4) + 5)//4 = version & 5 = ihl
                &&
                b[9] != IpPacket_deprecated.TCP_protocol //tcp protocol
                & 
                getShort(10) != IpPacket_deprecated.checksum(b, 0, IpPacket_deprecated.payloadStartIndex)
        ){
            throw new IOException("failed to parse packet correctly");
        }
        
    }
    
    private int getShort(int index){
        return  ((b[index]&0xff)<<8) | ((b[index+1]&0xff)); 
    }
    private  void setShort(int value,int index){
        b[index] = (byte) ((value>>8) & 0xff);
        b[index+1] = (byte) (value&0xff);
    }
    
    private int getInt(int index){
        return (getShort(index)<<16) + getShort(index+2);
    }
    private void setInt(int val, int index){
        setShort(val>>16, index);
        setShort(val&0xFFFF, index+2);
    }
    
    
    
    public int getTypeOfService() { return b[1]; }
    public IpPacketBuilder setTypeOfService(int type){ b[1] = (byte)( type & 0xff ); return this; } 
    
    public int setLength(byte[] payload){ 
        setShort(b.length+payload.length, 2);
        return b.length+payload.length;
    }
    
    public int getId(){ return getShort(4); }
    public IpPacketBuilder setId(int id){  setShort(id, 4); return this; }
    
    public int getFragOffset(){ return getShort(6); }
    public IpPacketBuilder setFragOffset(int offset){ setShort(offset, 6); return this; }
    
    public int getTTL(){ return b[8];}
    public IpPacketBuilder setTTL(int ttl){ b[8] = (byte) ttl; return this; }
    
    public int getProtocol(){ return b[9]; }
    public IpPacketBuilder setProtocol(int protocol){ b[9] = (byte)protocol; return this; }
    public int setCheckSum(){
        b[10] = b[11] = 0;
        int check = IpPacket_deprecated.checksum(b, 0, IpPacket_deprecated.payloadStartIndex);
        setShort(check, 10);
        return check;
    }
    
    public int getSourceIp(){ return getInt(12); }
    public IpPacketBuilder setSourceIp(int ip){ setInt(ip, 12); return this; }
    
    public int getDestIp(){ return getInt(16); }
    public IpPacketBuilder setDestIp(int ip){ setInt(ip, 16); return this; }
    
    
    public byte[] build(byte[] payload){
        setLength(payload);
        setCheckSum();
        byte[] retu = new byte[b.length+payload.length];
        
        for(int i =0; i<b.length; i++){ retu[i] = b[i]; }
        
        for(int i = 0; i<payload.length; i++){ retu[i+b.length] = payload[i]; }
        
        return retu;
    }
}
