/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sockets;

import java.io.IOException;
import java.util.Arrays;
import sockets.editable.TcpPacketBuilder;

/**
 *
 * @author root
 */
public class IpPacket_deprecated {
    
    public static final int payloadStartIndex = 20, ipHeaderVersion = 4, TCP_protocol = 6; 
    public RawSocket socket;
    
    
    //all of the inbound data
    //general packet data
    private int  typeOfService, id, frag_off, ttl;
    public int sourceIpAddress, destinationIpAddress;
    /**
    //tcp specific data
    int sourcePort, destinationPort, sequenceNumber, ackNumber;
    boolean NS,CWR, ECE, URG, ACK, PSH, RST, SYN, FIN;
    int windowSize, urgentPointer;
    byte[] options;
    byte[] data;
    
    //all of the outbound data
    byte[] outboundData;
    * **/
    
    public final byte[] buffer;
    private TcpPacket tcp;
    private byte[] backup;


    public IpPacket_deprecated(byte[] source, RawSocket socket) throws IOException {
        this.backup = Arrays.copyOfRange(source, 0, source.length);
        this.buffer = source;
        this.socket = socket;
        int offset = setPacketHeaderFields(source);
        if(offset!= payloadStartIndex){
            throw new IndexOutOfBoundsException("payload start index wrong:"+offset);
        }
        //System.out.println(this.toString());
        
    }
    
   
    
    //getter and setter functions
    /**
     * @deprecated use {@link IpPacket#ipIntToString(java.lang.String)  }
     * @param ip
     * @return
     * @deprecated
     */
    @Deprecated public static String ipIntToString(int ip){
        String retu = "";
        retu += (ip>>24)&0xff;
        retu += ".";
        retu += (ip>>16)&0xff;
        retu+=".";
        retu += (ip>>8)&0xff;
        retu+=".";
        retu += ip&0xff;
        return retu;
       
    }
    /**
     * @deprecated use {@Link IpPacket#ipStringToInt(String)} instead
     * @param ipAddress
     * @return
     * @deprecated
     */
    @Deprecated public static int ipStringToInt(String ipAddress){
        int retu = 0;
        String[] pieces = ipAddress.split("\\.");
        for(String byt : pieces){
            retu = retu<<8;
            retu = retu | (Integer.parseInt(byt) & 0xff);
        }
        return retu;
    }
    public String toString(){
        StringBuilder sb = new StringBuilder();
        //sb.append("ilh:"+ilh+"\n").append("version:"+version+"\n")
        sb.append("tos:"+typeOfService+"\n")
        .append("totalLength:"+buffer.length+"\n").append("id:"+id+"\n")
        .append("frag_off:"+frag_off+"\n").append("ttl:"+ttl+"\n")
        .append("source address="+ipIntToString(sourceIpAddress)+"\n")
        .append("destination address:"+ipIntToString(destinationIpAddress)+"\n");
         if(tcp!=null){
             sb.append("tcp packet:\n").append(tcp.toString());
         }
        //.append("sequence numeber:"+sequenceNumber+"\n")
        //.append("ackNumber:"+ackNumber+"\n")
        //.append("NS:"+NS+", ").append("CWR:"+CWR+", ").append("ECE:"+ECE+", ").append("URG:"+URG+", ").append("ACK:"+ACK+", ")
        //.append("PSH:"+PSH+", ").append("RST:"+RST+", ").append("SYN:"+SYN+", ").append("FIN:"+FIN+", ")
        //.append("windowSize:"+windowSize+"\n")
        //.append("urgentPointer:"+urgentPointer+"\n"); 
        
        //if(options!= null){  sb.append("options:"+new String(options)+"\n"); }
        
        //if(data!=null){ sb.append("data:"+new String(data)); }

        return sb.toString();
    }
    public TcpPacket getTCP() throws IOException{ 
        if(tcp == null){
            tcp = new TcpPacket(this);
        }
        return tcp;
    }
     private int setPacketHeaderFields(byte[] b) throws IOException{
         int index = 0;
     int version = (b[index]&0xf0)>>4;//unsigned int ihl:4;
     int ihl = ((b[index]&0x0f));//unsigned int version:4;
     if(version!= 4 || ihl != 5){
         throw new IOException("ip header error:"+version+","+ihl);
     }
     
        index++;//index = 1
     typeOfService = b[index];//u_int8_t tos;
     if(index!=1){ throw new IOException("type of service");}
        index++;//index = 2
        
     int totalLength = ((b[index]&0xff)<<8) | ((b[index+1]&0xff));//u_int16_t tot_len;
     if(totalLength != b.length ){
         throw new IOException("improper length:"+totalLength+"!="+b.length);
     }
        index+=2;//index = 4;
        
     id = ((b[4]&0xff)<<8) | ((b[5]&0xff)); //u_int16_t id;
        index+=2;//index = 6
     frag_off//u_int16_t frag_off;
        = ((b[index]&0xff)<<8) | ((b[index+1]&0xff));
        index+=2;//index = 8
     ttl//u_int8_t ttl;
        = b[index];
        index++;//index = 9
    int protocol//u_int8_t protocol;
        = b[9];
        if(protocol != TCP_protocol ){
            System.out.println(this.toString());
            throw  new IndexOutOfBoundsException("protocol found to be:"+protocol+" instead of 6 at index:"+index);
        }
        index++;//index= 10
    int check//u_int16_t check;
        = 
            ((b[10] )<<8) | 
            (b[11]&0xff)
            ;
        index+=2;//index= 12
     
     sourceIpAddress//u_int32_t saddr;
        = (( 0xff& b[index])<<24) 
             |
             ((0xff& b[index+1])<<16)
             |
             (( 0xff& b[index+2])<<8) 
             |
             ( 0xff& b[index+3]);
        index+=4;// index = 16
    destinationIpAddress//u_int32_t daddr;
         = (( 0xff& b[index])<<24) 
             |
             ((0xff& b[index+1])<<16)
             |
             (( 0xff& b[index+2])<<8) 
             |
             ( 0xff& b[index+3]);
        index+=4;
       
        
     int calcChecksum = checksum(b, 0, IpPacket_deprecated.payloadStartIndex);
     if(calcChecksum != 0){
         System.out.println(this.toString());
         System.out.println("checksum:"+check+"{"+b[10]+","+b[11]);
         throw new IOException("chacksum incorrect:"+(calcChecksum)+", "+check);
     }
        
       return index;
        
    }
     
     public static int checksum(byte[] b, int startIndex, int length){
         if(startIndex+length > b.length){
             throw new IndexOutOfBoundsException("lengths too long for index:"+startIndex+"+length:"+length+">b.length:"+b.length); 
         }
         int sum = 0;
         for(int i = 0; i+1<length ; i+=2){
             //if(i == 10){ continue; }//skip over the checksum value
             int s = 
                     ((b[startIndex+i] & 0xff )<<8) 
                     |
                     (b[startIndex+i+1] & 0xff)
                     ;
             sum+= s;
         }
         
         if(length%2 == 1){
             sum+=((b[startIndex+length-1]& 0xff)<<16);
         }
         
         int retu =  (sum & 0xffff);
         retu += sum>>16;
         
         return (short)(~retu);
     }
    public TcpPacket getTcp() throws IOException{
        if(tcp == null){
            tcp = new TcpPacket(this);
        }
        return tcp;
    }
    //communication functions
     /** 
      * calling this function means you are interested in seeing what the responses are.
      * <br/>
      * upon calling this function, you are also initiating the tcp 3 way handshake
      * @throws IOException 
      */
    public void bindPacket() throws IOException{
            //System.out.println("recieved buffer:\n"+Options.arrayToString(buffer, 0, buffer.length));
            TcpPacket tcp = getTcp();
            socket.bindForResponses(this);
            tcp.start_threeWayHandshake(this);
      
    }
    boolean packetIsResponse(byte[] b) throws IOException{
        
        
     int sourceIp = IpPacket.getSourceIp(b);
            /** (b[12]&0xff)<<24
             |
             (b[13]&0xff)<<16
             |
             (b[14]&0xff)<<8
             | 
             (b[15]&0xff);
             * **/
     int destinationIp = IpPacket.getDestIp( b);
             /**(b[16]&0xff)<<24
             |
             (b[17]&0xff)<<16
             |
             (b[18]&0xff)<<8
             | 
             (b[19]&0xff);
             * **/
     //int id = (b[4]&0xff)<<8 | (b[5] & 0xff);
     
      if(
              b[9] == TCP_protocol &&//protocol is tcp
              //this.id == id &&
              sourceIp == sourceIpAddress && 
              destinationIp == destinationIpAddress &&
              tcp.packetIsResponse(b)
        ){
          return true;
      }
      return false;
    }
    void sendPacket(byte[] data, int port) throws IOException{
      byte[] retu = new byte[payloadStartIndex+data.length];  
        
      //ihl and version
      retu[0] = (byte)( ( ((payloadStartIndex/4)<<4) & 0xf0) | (ipHeaderVersion & 0x0f) );
      retu[1] = (byte) typeOfService;
      //totalLength
      retu[2] = (byte) ((retu.length>>8)&0xff);  
        retu[3] = (byte) (retu.length&0xff);
        //id
     retu[4] = (byte) ((id>>8)&0xff);
        retu[5] = (byte) (id&0xff);
        //fragment offset
     retu[6] = (byte) ((frag_off>>8) &0xff);
        retu[7] = (byte) (frag_off&0xff);
        //time to live
     retu[8] = (byte) ttl;
     //protocol
     retu[9] = TCP_protocol;
     //checksum
     retu[10] = 0;
        retu[11] = 0;
    //sourceIpAddress
     retu[12] = (byte) ((destinationIpAddress>>24)&0xff);
        retu[13] = (byte) ((destinationIpAddress>>16) & 0xff);
        retu[14] = (byte) ((destinationIpAddress>>8) & 0xff);
        retu[15] = (byte) (destinationIpAddress& 0xff);
    //destinationIpAddress
     retu[16] = (byte) ((sourceIpAddress>>24)&0xff);
        retu[17] = (byte) ((sourceIpAddress>>16) & 0xff);
        retu[18] = (byte) ((sourceIpAddress>>8) & 0xff);
        retu[19] = (byte) (sourceIpAddress& 0xff);
        
     //get checksum
     int checksum = checksum(retu,0, IpPacket_deprecated.payloadStartIndex);
        retu[10] = (byte) ((checksum>>8)&0xff);
        retu[11] = (byte) (checksum & 0xff);
     
            
        for(int i = 0; i<data.length; i++){
            retu[payloadStartIndex+i] = data[i];
        }
       
        socket.write(data,port,sourceIpAddress);
    }
    
    
   }
