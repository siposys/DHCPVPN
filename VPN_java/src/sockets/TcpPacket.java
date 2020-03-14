/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
    <a href="https://www.cloudshark.org/captures/7e751e01085a">usefull link</a>
 * and open the template in the editor.
 */

package sockets;

import java.io.IOException;
import java.util.Arrays;
import sockets.editable.IpPacketBuilder;
import sockets.editable.TcpPacketBuilder;
import sockets.tcp.Options;
import sockets.tcp.Options.TimeStamp;

/**
 *
 * @author root
 */
public class TcpPacket {
    public static final int
            URG_flag = 0x20,  
            ACK_flag = 0x10, 
            PSH_flag = 0x08,
            RST_flag = 0x04,
            SYN_flag = 0x02,
            FIN_flag = 0x01;
    
    public static final int optionsStartIndex = 20;
   
    private IpPacket_deprecated source;
    private byte[] buffer;
    
    /**
     * inbound data in the headers
     */
       public  int sourcePort, destinationPort, sequenceNumber, ackNumber, windowSize, urgentPointer, optionsLength;
     /**
      * inbound flags
      */
        boolean /*NS,*/ /*CWR,*/ /*ECE,*/URG, ACK, PSH, RST, SYN, FIN;
        Options options;
        
     /**
      * outbound data in headers
      */
      private int 
              outboundSequenceNumber = (int) (Math.random()*50000);
   
        
    public TcpPacket(byte[] b) throws IOException{
        this(b,null);
        
    }
    public TcpPacket(IpPacket_deprecated s) throws IOException{
        this(s.buffer, s);
        
    }
    public TcpPacket(byte[] b, IpPacket_deprecated source) throws IOException{
        this.source = source;
        this.buffer = b;
        int index = IpPacket_deprecated.payloadStartIndex;
        
        
        sourcePort = ((b[index]&0xff)<<8) | ((b[index+1]&0xff)); 
            index+=2;//index= 20+2
        destinationPort = ((b[index]&0xff)<<8) | ((b[index+1]&0xff));
            index+=2;//index = 20+4
            
        sequenceNumber = ((b[index]&0xff)<<24) | ((b[index+1]&0xff)<<16) | ((b[index+2]&0xff)<<8) | ((b[index+3]&0xff));
        index+=4;//index = 20+8
        ackNumber = ((b[index]&0xff)<<24) | ((b[index+1]&0xff)<<16) | ((b[index+2]&0xff)<<8) | ((b[index+3]&0xff));
        index+=4;//index = 20+12
        
        optionsLength = (((b[index]&0xff)>>4)-5)*4;
        if( (b[index]&0x0E) != 0){
            throw new IndexOutOfBoundsException("tcp checksum not 0:"+(b[index]&0x0f) );
        }
        //NS = (b[index]&0x01) == 1;
            index++;//index = 20+13
        //CWR = (b[index] & 0x80) !=0;
        //ECE = (b[index] & 0x40) != 0;
        URG = (b[index] & URG_flag) != 0;
        ACK = (b[index] & ACK_flag) != 0;
        PSH = (b[index] & PSH_flag) != 0;
        RST = (b[index] & RST_flag) != 0;
        SYN = (b[index] & SYN_flag) != 0;
        FIN = (b[index] & FIN_flag) != 0;
        if(index-IpPacket_deprecated.payloadStartIndex != 13){
            throw new IOException("flags wrong:"+index);
        }
        index++;//index = 20+14
        windowSize = ((b[index]&0xff)<<8) | ((b[index+1]&0xff)); 
            index+=2;//index = 20+16
        if(index != 16+IpPacket_deprecated.payloadStartIndex){
                throw new IOException("checksum index found to be:"+index);
        }
        
        int checksum = ((b[index]&0xff)<<8) | ((b[index+1]&0xff)); 
        if(index!= IpPacket_deprecated.payloadStartIndex+16){
            throw new IOException("checksum index is:"+index);
        }
            index+=2;//index = 20+18
        
        
        urgentPointer = ((b[index]&0xff)<<8) | ((b[index+1]&0xff)); 
            index+=2;
        if(getOptionsStartIndex() != index){
            throw new IndexOutOfBoundsException("wrong options index:"+index);
        }
        options = new Options(b, getOptionsStartIndex(), optionsLength);
        index+=optionsLength;
        
        
        if(source != null){
            int calcChecksum = checksum();
            if(calcChecksum != 0){
                System.out.println("warning checksum is wrong:"+calcChecksum);
                //System.out.println(this.toString());
                //System.out.println("raw data:"+Options.arrayToString(source.buffer, Socket.payloadStartIndex, source.buffer.length-Socket.payloadStartIndex));
                throw new IOException("checksum found to be:"+calcChecksum+", when it actuall is:"+checksum+" difference:"+(calcChecksum-checksum));
            }
        }
        
        
    }
    private int getOptionsStartIndex(){ return IpPacket_deprecated.payloadStartIndex+optionsStartIndex; }
    private int getPayloadStartIndex(){ return getOptionsStartIndex()+optionsLength; }
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("sourcePort:").append(sourcePort)
                .append("\ndestinationPort:").append(destinationPort)
                .append("\n sequence number:").append(sequenceNumber)
                .append("\n ackNumber:").append(ackNumber)
                .append("\n window size:").append(windowSize)
                .append("\n urgent pointer:").append(urgentPointer)
                .append("\n options length:").append(optionsLength);
                //.append("\n NS:").append(NS)
                //.append("\n CWR:").append(CWR)
                //.append("\n ECE:").append(ECE)
                if(ACK)sb.append("\n ACK:").append(ACK);
                if(PSH)sb.append("\n PSH:").append(PSH);
                if(RST)sb.append("\n RST:").append(RST);
                if(SYN)sb.append("\n SYN:").append(SYN);
                if(FIN)sb.append("\n FIN:").append(FIN);
                if(URG)sb.append("\n URG:").append(URG);
                if(options!=null){
                    sb.append("\n options:").append(options.toString());
                }
                    sb.append("\n payload length:").append(buffer.length-getPayloadStartIndex());
                    sb.append("\n payload:").append(new String(buffer, getPayloadStartIndex(), buffer.length-getPayloadStartIndex()));
                
        return sb.toString();
    }
    
    /**
     * <a href="https://www.slashroot.in/how-is-tcp-and-udp-checksum-calculated"></a>
     * <br/>
     * <a href="http://www.tcpipguide.com/free/t_TCPChecksumCalculationandtheTCPPseudoHeader-2.htm"> calculation steps</a>
     * <br/>
     * <a href="https://en.wikipedia.org/wiki/Transmission_Control_Protocol#TCP_checksum_for_IPv4">wikipedia</a>
     * @param b 
     */
   
   
    private int checksum(byte[] tcpPacket, int sourceIp, int destIp){
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
        
        //tcp length
        sum+= tcpPacket.length;
        
        for(int i = 0; i<tcpPacket.length; i+=2){//since the ip header adds to FFFF it will not effect the summation since FFFF+FFFF=1FFFE
             //if(i == Socket.payloadStartIndex + 16){ continue; }//skip over the checksum value
             int s = 
                     ((tcpPacket[i] & 0xff )<<8) 
                     +
                     (tcpPacket[i+1] & 0xff)
                     ;
             sum+= s;
         }
        if( (tcpPacket.length%2) == 1){
            sum+= (tcpPacket[tcpPacket.length-1]&0xff)<<16;
        }
        
        long retu =  (sum & 0xffff);
         retu += sum>>16;
        
         
         return (int) ((~retu) & 0xffff) ;
         
    }
    private int checksum() throws IOException{
        //(32 bit source address + 32 bit destination address + 8 bit reserved + 8 bit protocol type+ 16 bit tcp length 
        long sum = 0;
        //source ip
        sum+= (source.sourceIpAddress>>16 )& 0xFFFF;
        sum+= source.sourceIpAddress& 0xffff;
              
        //dest ip
        sum+= (source.destinationIpAddress>>16)& 0xFFFF;
        sum+= source.destinationIpAddress& 0xffff;
        
        // 8 bit 0
        
        //protocol
        sum+=  (IpPacket_deprecated.TCP_protocol);
        
        //tcp length
        int length = (source.buffer.length- IpPacket_deprecated.payloadStartIndex);
        sum+= length;

        for(int i = IpPacket_deprecated.payloadStartIndex; i<source.buffer.length; i+=2){//since the ip header adds to FFFF it will not effect the summation since FFFF+FFFF=1FFFE
             //if(i == Socket.payloadStartIndex + 16){ continue; }//skip over the checksum value
             int s = 
                     ((source.buffer[i] & 0xff )<<8) 
                     +
                     (source.buffer[i+1] & 0xff)
                     ;
             sum+= s;
         }
        
        long retu =  (sum & 0xffff);
         retu += sum>>16;
        
         
         retu = ((~retu) & 0xffff) ;
         
         byte[] tcp = Arrays.copyOfRange(source.buffer, IpPacket_deprecated.payloadStartIndex, source.buffer.length);
         int check2 = checksum(tcp, source.destinationIpAddress, source.sourceIpAddress);
         if(retu != check2){
             throw new IndexOutOfBoundsException(retu +"!="+check2);
         }
         return (int) retu;
        
    }
   
    
    private int currentClientSequenceNumber;
    /**
     * http://www.omnisecu.com/tcpip/tcp-three-way-handshake.php
     * @throws IOException 
     */
    public void start_threeWayHandshake(IpPacket_deprecated message) throws IOException{
        
        
        
        if( !ACK && !PSH && !RST && SYN && !FIN && !URG ){//first sync message
            //acknowlege the initialization and send the response initialization params
            System.out.println("starting 3 way handshake with:"+IpPacket_deprecated.ipIntToString(message.sourceIpAddress)+":"+message.getTCP().sourcePort);
            
            IpPacketBuilder se = new IpPacketBuilder(Arrays.copyOf(message.buffer, message.buffer.length));
            TcpPacketBuilder tcpe = new TcpPacketBuilder(
                    Arrays.copyOfRange(message.buffer, IpPacket_deprecated.payloadStartIndex, message.buffer.length),
                    se.getSourceIp(),
                    se.getDestIp()
            );
            se.setDestIp(message.sourceIpAddress);
            se.setSourceIp(message.destinationIpAddress);
            
            TcpPacket tcp = message.getTCP();
            tcpe.setSourcePort(tcp.destinationPort);
            tcpe.setDestPort(tcp.sourcePort);
            
            tcpe.setSequenceNumber(0);
            tcpe.setAckNumber(tcp.sequenceNumber+1);
            tcpe.setACK(true);
            tcpe.setSYN(true);
            Options inOpt = tcp.options;
            Options opte = new Options();
                opte.addMaxSegmentSize(1460);
                opte.addPadding();
                opte.addPadding();
                TimeStamp ts = inOpt.getTimeStamp();
                if(ts!= null){
                    opte.addTimeStamp((int) System.currentTimeMillis(), ts.getTsVal());
                }
                opte.addPadding();
                opte.addWindowScale(8);
                //opte.addSelectiveAcknowlegementPermitted();
            tcpe.setOptions(opte);
            
            System.out.println("recieved message:\n"+message.getTCP().toString());
            System.out.println("\n\nsent response:\n"+tcpe.toString());
            
            source.socket.write(se.build(tcpe.build(se.getSourceIp(), se.getDestIp())),  tcpe.getDestPort(), se.getDestIp());
            /**
             * PacketBuilder p = new PacketBuilder(this.sourcePort, this.destinationPort);

            p.setSYN(true).setACK(true);
            this.sequenceNumber++;//increment the sequence number since we are replying
            System.out.println("sent handshake step 1\n"+source.toString());
            source.sendPacket(p.build(), sourcePort);
            *
            **/
        }
        else if(RST && !ACK && !PSH && !SYN && !FIN && !URG){
            throw new IOException("client rejected 3 way handshake:\n"+message.toString());
        }
        
        
        else{
            System.out.println(this.toString());
            throw new IOException("tcp does not appear to be in three way handshake state");
        }
        
        
        
       
        
    }
    private void threeWayHandshake_step2(TcpPacket response) throws IOException{
        if(response.RST){
            throw new IOException("connection closed unexpectedly");
            //return;//handshake was cancelled
        }
       System.out.println("outbound sequence:"+this.outboundSequenceNumber);
    }
    boolean packetIsResponse(byte[] b) throws IOException {
        //t //To change body of generated methods, choose Tools | Templates.
        
        TcpPacket neu = new TcpPacket(b);
        int sourcePort = TcpPacketBuilder.getShort(IpPacket_deprecated.payloadStartIndex, b);
        int destinationPort = TcpPacketBuilder.getShort(IpPacket_deprecated.payloadStartIndex+2, b);
        if(sourcePort != neu.sourcePort || destinationPort != neu.destinationPort){
            throw new IOException("failed:"+sourcePort+" != "+neu.sourcePort+" || "+destinationPort+" != "+neu.destinationPort);
        }
        
        if(
                neu.destinationPort == this.sourcePort &&
                neu.sourcePort == this.sourcePort
                //&& this.outboundSequenceNumber == neu.outboundSequenceNumber
                //&& neu.ackNumber == this.sequenceNumber
                //&&neu.sequenceNumber == this.ackNumber
        ){
            threeWayHandshake_step2(neu);
            throw new UnsupportedOperationException("Not finished yet.");
            //return true;
        }
        
        return false;
    }
    
    /**
     * automatically:
     * <br/>
     * uses {@link #sequenceNumber Sequence number} as ACKnumber
     * <br/>
     * uses {@link #outboundSequenceNumber outbound sequence number} as SequenceNumber
     */
    private class PacketBuilder{
        int packetdestinationPort, packetsourcePort;
        byte[] options = new byte[0], payload= new byte[0];
        int flags = 0;
        PacketBuilder(int destinationPort, int sourcePort){
            this.packetdestinationPort = destinationPort;
            this.packetsourcePort = sourcePort;
        }
        private void setFlag(int mask){   flags |= mask; }
        private void removeFlag(int mask){    flags &= ~mask; }
        
        PacketBuilder setSYN(boolean set){
            if(set){ setFlag(SYN_flag); }
            else{ removeFlag(SYN_flag); }
            return this;
        }
        PacketBuilder setACK(boolean set){
            if(set){ setFlag(ACK_flag); }
            else{ removeFlag(ACK_flag); }
            return this;
        }
        
        byte[] build() throws IOException{
            byte[] retu= new byte[optionsStartIndex+options.length+payload.length];
            
            //source port
            retu[0] = (byte) ((packetsourcePort>>8)&0xff);
                retu[1] = (byte) (packetsourcePort&0xff);
           //destination port
           retu[2] = (byte) ((packetdestinationPort>>8)&0xff);
                retu[3] = (byte) (packetdestinationPort&0xff);
            
            //sequenceNumber
            retu[4] = (byte) ((outboundSequenceNumber>>24) &0xff);
                retu[5] = (byte) ((outboundSequenceNumber>>16) &0xff);
                retu[6] = (byte) ((outboundSequenceNumber>>8) &0xff);
                retu[7] = (byte) (outboundSequenceNumber &0xff);
            //acknowlegementNumber
            retu[8] = (byte) ((sequenceNumber>>24) &0xff);
                retu[9] = (byte) ((sequenceNumber>>16) &0xff);
                retu[10] = (byte) ((sequenceNumber>>8) &0xff);
                retu[11] = (byte) (sequenceNumber &0xff);
            //header length
            retu[12] = (byte) ((retu.length/4)<<4);
            //set bitwise flags
            retu[13] = (byte) flags;
            //window size
            retu[14] = (byte) ((43690>>8) & 0xff);
                retu[15] = (byte) (43690 & 0xff);
            //tcp checksum
            retu[16] = (byte) ((43690>>8) & 0xff);
                retu[17] = (byte) (43690 & 0xff);
            //urgent pointer
            retu[18] = 0;
                retu[19] = 0;
            
            //fill options
            for(int i = 0; i<options.length; i++){
                retu[optionsStartIndex+i] = options[i];
            }
            int start = optionsStartIndex+options.length;
            //fill payload
            for(int i = 0; i<payload.length; i++){
                retu[start+i] = payload[i];
            }
            
            fillChecksum(retu, source.destinationIpAddress, source.sourceIpAddress);
            return retu;
        }
         
        private void fillChecksum(byte[] tcpBuffer, int srcIp, int destIp) throws IOException{
           int checksum = checksum(tcpBuffer, srcIp, destIp);
           tcpBuffer[16] = (byte) ((checksum>>8) & 0xff);
           tcpBuffer[17] = (byte) (checksum & 0xff);
       
        }
    
    }
}
