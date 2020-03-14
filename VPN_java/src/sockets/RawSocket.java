/*
 *
 */
package sockets;

import java.io.File;
import java.io.IOException;
import static java.lang.System.out;
import java.util.ArrayList;

/**
 *
 <a href="https://www.binarytides.com/raw-sockets-c-code-linux/">tcp headers</a>
 <br/>
 <a href="http://squidarth.com/networking/systems/rc/2018/05/28/using-raw-sockets.html">sample code</a>
 <br/>
 <a href="https://linux.die.net/man/7/raw">linux raw sockets</a>
 * @author joshuarabanal
 */
public class RawSocket {
  private long socketPointer = 0;
  private static final int TYPE_TCP = 6, TYPE_UDP = 17;
  private int type;
  /**
   * list of all sockets that are waiting for responses
   */
  private ArrayList<IpPacket_deprecated> boundSockets = new ArrayList<IpPacket_deprecated>();
  
    public static RawSocket initialize_TCP(String interfaceName){
        return new RawSocket(TYPE_TCP, interfaceName);
    }
    public static RawSocket initialize_UDP(String interfaceName){
        return new RawSocket(TYPE_UDP, interfaceName);
    }
    //private String interfaceAddress;
    private RawSocket(int protocol, String interfaceName){
        File f = new File(System.getProperty("user.dir"));
        f = f.getParentFile();
        f = new File(f, "librawSocket_native.so");
        System.load(f.toString());
        //this.interfaceAddress = interface_address;
        this.type = protocol;
        if(interfaceName == null){ interfaceName = "wlan4"; }
        initialize(protocol, interfaceName);
        System.out.println("socketPointer:"+socketPointer);
        
        
    }
    public byte[] accept() throws IOException{
        byte[] b = readNextPacket();
            if(b == null){
                throw new NullPointerException("failed to get packet");
            }
        for(IpPacket_deprecated s: boundSockets){
            if(s.packetIsResponse(b)){
                b = null;
                break;
            }
        }
        if(b == null){//this socket was already taken, waiting for next socket
            System.out.println("accepting response packet");
            return accept();
        }
        return b;
    }
    void bindForResponses(IpPacket_deprecated s){
        if(boundSockets.contains(s)){
            return;
        }
        boundSockets.add(s);
    }
    public native byte[] readNextPacket();
    private native void initialize(int type, String interfaceName);
    private native int writePacket(byte[] b, int port, int ipAddress);
    
    public void write(byte[]b ) throws IOException{
        switch(IpPacket.getProtocol(b)){
            case IpPacket.TCP_protocol:
                write(b, IpPacket.TCPPacket.getDestPort(b), IpPacket.getDestIp(b));
                return;
            case IpPacket.UDP_protocol:
                write(b, IpPacket.UDPPacket.getDestPort(b), IpPacket.getDestIp(b));
                return;
            default:
                throw new IOException("unsupported protocol:"+IpPacket.getProtocol(b));
        }
    }
    public void write(byte[] b, int destinationPort, int destinationIp) throws IOException{
        int howmany = writePacket(b, destinationPort, destinationIp);
        if(howmany != b.length){
            String errorMessage = "unknown("+howmany+")";
            switch(howmany){
                case 11:  errorMessage = "try again";
                break;
                case 13: errorMessage = "permission denied";
                break;
                case 89: errorMessage = "Destination address required";
                break;
            }
            throw new IOException("failed to send full packet:"+howmany+":"+errorMessage);
        }
    }
    /**
     * uses struct iphdr for this
     * @param b
     */
private void logPacket(byte[] b){
      int index = 0;
    int ilh = (b[index]&0xf0)>>4;//unsigned int ihl:4;
    out.println("ilh:"+ilh);
    
    int version = ((b[index]&0x0f));//unsigned int version:4;
        index++;
    out.println("version:"+version);
    
    byte tos = b[index];//u_int8_t tos;
        index++;
    out.println("tos:"+tos);
    
    int totalLength = ((b[index]&0xff)<<8) | ((b[index+1]&0xff));//u_int16_t tot_len;
        index+=2;
    out.println("totalLength:"+totalLength);
    
    int id = ((b[index]&0xff)<<8) | ((b[index+1]&0xff)); //u_int16_t id;
        index+=2;
    out.println("id:"+id);
    
    int frag_off//u_int16_t frag_off;
        = ((b[index]&0xff)<<8) | ((b[index+1]&0xff));
        index+=2;
    out.println("frag_off:"+frag_off);
    
    int ttl//u_int8_t ttl;
        = b[index];
        index++;
    out.println("ttl:"+ttl);
    
    int protocol//u_int8_t protocol;
        = b[index];
        index++;
        if(protocol != 6 && type == TYPE_TCP){
            throw  new IndexOutOfBoundsException("protocol found to be:"+protocol+" instead of 6");
        }
    out.println("protocol:"+protocol);
    
    int check//u_int16_t check;
        = ((b[index]&0xff)<<8) | ((b[index+1]&0xff));
        index+=2;
    out.println("check:"+check);
        
        
    String sourceAddress//u_int32_t saddr;
        = ( 0xff& b[index]) +"."+( 0xff& b[index+1])+"."+( 0xff& b[index+2]) + "."+ ( 0xff& b[index+3]);
        index+=4;
    String destinationAddress//u_int32_t daddr;
        = (0xff & b[index]) +"."+(0xff & b[index+1])+"."+(0xff & b[index+2]) + "."+ (0xff & b[index+3]);
        index+=4;
System.out.println("source address:"+sourceAddress+"\n destination address:"+destinationAddress);
     
System.out.println("rest of packet:"+ new String(b,index,b.length-index));
     
     StringBuilder sb = new StringBuilder();
     for(byte bite : b){
         sb.append(","+(0xff&bite));
     }
     System.out.println("full packet:\n"+sb.substring(1));
}
    
}
