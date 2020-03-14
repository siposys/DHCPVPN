/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockets.editable;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import sockets.IpPacket;
import sockets.IpPacket_deprecated;
import sockets.TcpPacket;
import sockets.tcp.Options;

/**
 *
 * @author root
 */
public class TcpPacketBuilder {
    private byte[] b;
    private Options options;
    public byte[] payload;
    public TcpPacketBuilder(byte[] IpBuffer) throws IOException{
        this(Arrays.copyOfRange(IpBuffer, IpPacket.getIPHeaderLength(IpBuffer), IpBuffer.length), IpPacket.getSourceIp(IpBuffer), IpPacket.getDestIp(IpBuffer));
        
    }
    public TcpPacketBuilder(byte[] buffer, int sourceIp, int destIp) throws IOException{
        this.b = buffer;
            options = new Options(b, 20, getOptionsLength());
        payload =Arrays.copyOfRange(b, TcpPacket.optionsStartIndex+getOptionsLength(), b.length);
        b = Arrays.copyOfRange(b, 0, TcpPacket.optionsStartIndex);
        
        int oldChecksum = getShort(16);
        int checksum = getChecksum(sourceIp, destIp);
        if(checksum!= 0){
            throw new IOException("checksu invalid:"+checksum+"!="+oldChecksum+":"+0xFFFF);
        }
    }
    
    public static int getShort(int index, byte[] b){
        return  ((b[index]&0xff)<<8) | ((b[index+1]&0xff)); 
    }
    private int getShort(int index){
        return getShort(index, b);
    }
    public static void setShort(int value, int index, byte[] b){
        b[index] = (byte) ((value>>8) & 0xff);
        b[index+1] = (byte) (value&0xff);
    }
    private  void setShort(int value,int index){
        setShort(value, index, b);
    }
    
    public static int getInt(int index, byte[] b){
        return (getShort(index,b)<<16) + getShort(index+2,b);
    }
    private int getInt(int index){
        return getInt(index,b);
    }
    public static void setInt(int val, int index, byte[] b){
        setShort(val>>16, index, b);
        setShort(val&0xffff, index+2, b);
    }
    private void setInt(int val, int index){
        setInt(val, index, b);
    }
    
    
    public int getSourcePort(){ return getShort(0); }
    public TcpPacketBuilder setSourcePort(int val){ setShort(val,0); return this; }
    
    public int getDestPort(){ return getShort(2); }
    public TcpPacketBuilder setDestPort(int val){ setShort(val,2); return this; }
    
    public int getSequenceNumber(){ return getInt(4); }
    public TcpPacketBuilder setSequenceNumber(int val){ setInt(val,4); return this; }
    
    public int getAckNumber(){ return getInt(8); }
    public TcpPacketBuilder setAckNumber(int val){ setInt(val,8); return this; }
    
    public int getOptionsLength() { 
        return (((b[12]&0xff)>>4)-5)*4; }
    
    public TcpPacketBuilder setOptionsLength() { 
       int length = options.toByteArray().length;
       length/=4;
       length += 5;
       length = length<<4;
       b[12] = (byte)length;
       return this;
    }
    
    public int getFlags(){ return b[13]; }
    public TcpPacketBuilder setFlags(int val){ b[13] = (byte) val; return this; }
    
    private void setFlag(boolean on, int flag){
        int flags = getFlags() & (~flag);
        if(on){
            flags |= flag;
        }
        setFlags(flags);
    }
    public boolean getACK(){   return (getFlags() & TcpPacket.ACK_flag) != 0;  }
    public TcpPacketBuilder setACK(boolean ACK){ setFlag(ACK,TcpPacket.ACK_flag); return this; }
    
    public boolean getPSH(){   return (getFlags() & TcpPacket.PSH_flag) != 0;  }
    public TcpPacketBuilder setPSH(boolean ACK){ setFlag(ACK,TcpPacket.PSH_flag); return this; }
    
    public boolean getRST(){   return (getFlags() & TcpPacket.RST_flag) != 0;  }
    public TcpPacketBuilder setRST(boolean ACK){ setFlag(ACK,TcpPacket.RST_flag); return this; }
    
    public boolean getSYN(){   return (getFlags() & TcpPacket.SYN_flag) != 0;  }
    public TcpPacketBuilder setSYN(boolean ACK){ setFlag(ACK,TcpPacket.SYN_flag); return this; }
    
    public boolean getFIN(){   return (getFlags() & TcpPacket.FIN_flag) != 0;  }
    public TcpPacketBuilder setFIN(boolean ACK){ setFlag(ACK,TcpPacket.FIN_flag); return this; }
    
    public boolean getURG(){   return (getFlags() & TcpPacket.URG_flag) != 0;  }
    public TcpPacketBuilder setURG(boolean ACK){ setFlag(ACK,TcpPacket.URG_flag); return this; }
    
    
    public int getWindowSize(){ return getShort(14); }
    /**
    @param val the number of shorts(16 bit) that can be sent to the reciever before they need to ack(send a read reciept)
            <br/>
            this value defaults to the number of shorts, but can be changed using the 
            <a href="https://en.wikipedia.org/wiki/TCP_window_scale_option">window size scaling option</a>
            <br/>
            see #getOptions()
    **/
    public void setWindowSize(int val){ setShort(val,14); }
    
    //checksum = 16(short)
    private int getChecksum(int sourceIp,int destIp) throws IOException{
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
        
        //buffer checksums
        sum+=(~IpPacket_deprecated.checksum(b, 0, b.length))&0xffff;
            byte[] options = getOptions().toByteArray();
        sum+=(~IpPacket_deprecated.checksum(options, 0, options.length)) & 0xffff;
        sum+=(~IpPacket_deprecated.checksum(payload, 0, payload.length)) & 0xffff;
        
        //tcp length
        sum+= b.length+options.length+payload.length;
        
        if(sum<0){ throw new IOException("failed to add checksum"); }
        while(sum>0xFFFF){
            sum = (sum&0xffff) + (sum>>16);
        }
        long checksum = (~sum)&0xffff;
        return (int) checksum;
        
    }
    public int setChecksum(int sourceIp, int destIp) throws IOException{
        b[16] = b[17] = 0;
        int sum = getChecksum(sourceIp, destIp);
        setShort(sum,16);
        return sum;
    }
    
    public int getUrgentPointer(){ return getShort(18); }
    public void set(int val){ setShort(val,18); }
    
    public Options getOptions(){ 
        setOptionsLength(); 
        return options;
    }
    public void setOptions( Options val) throws IOException{
        if(val == null){ throw new NullPointerException(); }
        this.options = val; 
        setOptionsLength(); 
    }
    
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("sourcePort:").append(this.getSourcePort());
        sb.append("\ndestinationPort:").append(this.getDestPort());
        sb.append("\nsequence number:").append(this.getSequenceNumber());
        sb.append("\nackNumber:").append(this.getAckNumber());
        sb.append("\nwindow size:").append(this.getWindowSize());
        sb.append("\nurgent pointer:").append(this.getUrgentPointer());
        sb.append("\noptions length:").append(this.getOptionsLength());
        if(getACK())sb.append("\n ACK:").append(true);
        if(getPSH())sb.append("\n PSH:").append(true);
        if(getRST())sb.append("\n RST:").append(true);
        if(getSYN())sb.append("\n SYN:").append(true);
        if(getFIN())sb.append("\n FIN:").append(true);
        if(getURG())sb.append("\n URG:").append(true);
        sb.append("\noptions:").append(this.getOptions().toString());
        sb.append("\npayload length:").append(this.payload.length);
        sb.append("\npayload:").append(Arrays.toString(payload));
        return sb.toString();
    }
    public byte[] buildWholePacket(IpPacketBuilder ip) throws IOException{
        byte[] tcp = build(ip.getSourceIp(), ip.getDestIp());
        return ip.build(tcp);
    }
    public byte[] build(int sourceIp, int destIp) throws IOException{
        setChecksum(sourceIp, destIp);
        byte[] options = getOptions().toByteArray();
        
        byte[] retu = new byte[ b.length + options.length + payload.length ];
        for(int i = 0; i<b.length; i++){ retu[i] = b[i]; }
        
        for(int i = 0; i<options.length; i++){ retu[i+b.length] = options[i]; }
        
        for(int i = 0; i<payload.length; i++){ retu[i+b.length+options.length] = payload[i]; }
        return retu;
    }
    
}
