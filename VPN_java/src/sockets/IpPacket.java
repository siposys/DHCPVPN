/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockets;

import java.util.Arrays;
import static sockets.editable.TcpPacketBuilder.getInt;
import static sockets.editable.TcpPacketBuilder.getShort;


/**
 *
 * @author root
 */
public class IpPacket {
    public static final int  ipv4HeaderVersion = 4, TCP_protocol = 6, UDP_protocol = 17; 
    
    public static class TCPPacket{
        public static final int optionsStartIndex = 20;
        public static int getSourcePort(byte[] b){
            return getShort(getIPHeaderLength(b), b);
        }
        public static int getDestPort(byte[] b){
            return getShort(getIPHeaderLength(b)+2, b);
        }
        public static int getSequenceNumber(byte[] b){
            return getInt(getIPHeaderLength(b)+4, b);
        }
        public static int getAckNumber(byte[] b){
            return getInt(getIPHeaderLength(b)+8, b);
        }
        public static int getOptionsLength(byte[] b){
            return (((b[getIPHeaderLength(b)+12]&0xff)>>4)-5)*4;
        }
        public static boolean isURG(byte[] b){
            return (b[getIPHeaderLength(b)+13]& TcpPacket.URG_flag) != 0;
        }
        public static boolean isACK(byte[] b){
            return (b[getIPHeaderLength(b)+13]& TcpPacket.ACK_flag) != 0;
        }
        public static boolean isPSH(byte[] b){
            return (b[getIPHeaderLength(b)+13]& TcpPacket.PSH_flag) != 0;
        }
        public static boolean isRST(byte[] b){
            return (b[getIPHeaderLength(b)+13]& TcpPacket.RST_flag) != 0;
        }
        public static boolean isSYN(byte[] b){
            return (b[getIPHeaderLength(b)+13]& TcpPacket.SYN_flag) != 0;
        }
        public static boolean isFIN(byte[] b){
            return (b[getIPHeaderLength(b)+13]& TcpPacket.FIN_flag) != 0;
        }
        public static int getWindowSize(byte[] b){
            return getShort(getIPHeaderLength(b)+14, b);
        }
        public static int getTCPHeaderLength(byte[] b){
            return getOptionsLength(b)+optionsStartIndex;
        }
        public static int getPayloadStartIndex(byte [] b){
            return getIPHeaderLength(b)+getTCPHeaderLength(b);
        }
        public static String toString(byte[]b){
            StringBuilder sb= new StringBuilder("TCP Packet:\n");
            sb.append("sourcePort:").append(getSourcePort(b));
            sb.append("\nDestination Port:").append(getDestPort(b));
            sb.append("\nSequence Number:").append(getSequenceNumber(b));
            sb.append("\nAck Number:").append(getAckNumber(b));
            sb.append("\nWindow Size:").append(getWindowSize(b));
            if(isURG(b)){ sb.append("\nURG:true"); }
            if(isACK(b)){ sb.append("\nACK:true"); }
            if(isPSH(b)){ sb.append("\nPSH:true"); }
            if(isRST(b)){ sb.append("\nRST:true"); }
            if(isSYN(b)){ sb.append("\nSYN:true"); }
            if(isFIN(b)){ sb.append("\nFIN:true"); }
            return sb.toString();
        }
    }
    
    public static class UDPPacket{
        private static final int UDP_header_length  = 8;
        public static int getSourcePort(byte[]b){
            return getShort(getIPHeaderLength(b), b);
        }
        public static int getDestPort(byte[]b){
            return getShort(getIPHeaderLength(b)+2, b);
        }
        /**
         * payload length+udpHeader length = {@link #getPayload(byte[])}.length+ 8
         * @param b
         * @return 
         */
        public static int getLength(byte[]b){
            return getShort(getIPHeaderLength(b)+4, b);
        }
        public static int getChecksum(byte[]b){
            return getShort(getIPHeaderLength(b)+6, b);
        }
        public static int getPayloadStartIndex(byte[]b){
            return getIPHeaderLength(b)+UDP_header_length;
        }
        public static byte[] getPayload(byte[]b){
            int start = getPayloadStartIndex(b);
            return Arrays.copyOfRange(b, start, start+getLength(b)-8);
        }
        public static String toString(byte[] b){
            StringBuilder sb = new StringBuilder("UDP packet:");
            sb.append("\nsource port:").append(getSourcePort(b));
            sb.append("\ndest port:").append(getDestPort(b));
            sb.append("\nlength:").append(getLength(b));
            sb.append("\ncheck sum:").append(getChecksum(b));
            return sb.toString();
        }
    }
    
    public static int getVersion(byte[]b){
        return (b[0]&0xf0)>>4;
    }
    /**@return  length of IP header in bytes**/
    public static int getIPHeaderLength(byte[] b){
        return ((b[0]&0x0f))*4;
    }
    public static int getTypeOfService(byte[] b){ return b[1]; }
    
    public static int getTotalLength(byte[] b){
        return getShort(2,b);
    }
     public static int getId(byte[] b){ return getShort(4,b); }
     
    public static int getFragOffset(byte[]b){ return getShort(6,b); }
    
    public static int getTTL(byte[]b){ return b[8];}
    
    public static int getProtocol(byte[] b){
        return b[9];
    }
    
    public static int getCheckSum(byte[]b){ return getShort(10,b); }
    
    public static int getSourceIp(byte[] b){
        return getInt(12, b);
    }
    public static int getDestIp(byte[] b){
        return getInt(16, b);
    }
    
    public static String ipIntToString(int ip){
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
    
    public static int ipStringToInt(String ipAddress){
        int retu = 0;
        String[] pieces = ipAddress.split("\\.");
        for(String byt : pieces){
            retu = retu<<8;
            retu = retu | (Integer.parseInt(byt) & 0xff);
        }
        return retu;
    }
    
    
    public static  String toString(byte[] b){
        StringBuilder sb = new StringBuilder("IP packet:");
        sb.append("\nversion:").append(getVersion(b));
        sb.append("\nip header length:").append(getIPHeaderLength(b));
        sb.append("\ntype of service:").append(getTypeOfService(b));
        sb.append("\n total length:").append(getTotalLength(b));
        sb.append("\nID:").append(getId(b));
        sb.append("\nFrag ofset:").append(getFragOffset(b));
        sb.append("\nTTL:").append(getTTL(b));
        sb.append("\nprotocol:").append(getProtocol(b));
        sb.append("\ncheck sum:").append(getCheckSum(b));
        sb.append("\nsource Ip:").append(sockets.IpPacket_deprecated.ipIntToString(getSourceIp(b)));
        sb.append("\ndest ip:").append(sockets.IpPacket_deprecated.ipIntToString(getDestIp(b)));
        return sb.toString();
    }
    
    
}
