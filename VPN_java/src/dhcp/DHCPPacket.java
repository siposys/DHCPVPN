/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dhcp;

import dhcp.dhcpPacket.Option;
import dhcp.dhcpPacket.Options;
import static sockets.IpPacket.UDPPacket.getPayloadStartIndex;
import java.util.ArrayList;
import java.util.Arrays;
import sockets.IpPacket_deprecated;
import static sockets.editable.TcpPacketBuilder.getInt;
import static sockets.editable.TcpPacketBuilder.getShort;

/**
 *<a href="https://tools.ietf.org/html/rfc2131">source</a>
 * @author root
 */
public class DHCPPacket {
    
    public static byte getOP(byte[] b){  return b[getPayloadStartIndex(b)+0]; }
    
    public static byte getHTYPE(byte[] b){  return b[getPayloadStartIndex(b)+1]; }
    
    public static byte getHLEN(byte[] b){  return b[getPayloadStartIndex(b)+2]; }
    
    public static byte getHOPS(byte[] b){  return b[getPayloadStartIndex(b)+3]; }
    
    public static int getXID(byte[] b){  return getInt(getPayloadStartIndex(b)+4,b); }
    
    public static int getSECS(byte[] b){  return getShort(getPayloadStartIndex(b)+8,b); }
    
    public static int getFLAGS(byte[] b){  return getShort(getPayloadStartIndex(b)+10,b); }
    
    
    public static int getClient_IP_address(byte[] b){  return getInt(getPayloadStartIndex(b)+12,b); }
    
    public static int getYour_IP_address(byte[] b){  return getInt(getPayloadStartIndex(b)+16,b); }
    
    public static int getServer_IP_address(byte[] b){  return getInt(getPayloadStartIndex(b)+20,b); }
    
    public static int getGateway_IP_address(byte[] b){  return getInt(getPayloadStartIndex(b)+24,b); }
    
    public static int[] getClient_hardware_address(byte[] b){ 
        int[] retu = new int[4];
        for(int i = 0; i<retu.length; i++){
            retu[i] = getInt(getPayloadStartIndex(b)+28+(i*4),b);
        }
        return retu; 
    }
    public static String getServer_host_name(byte[] b){ 
        byte[] retu = new byte[64];
        int length = 0;
        for(int i = 0; i<(retu.length); i++){
            retu[i] = b[getPayloadStartIndex(b)+44+i];
            if(retu[i] == 0){
                length = 0;
                break;
            }
        }
        return new String(retu,0,length); 
    }
    public static String getBoot_file_name(byte[] b){ 
        byte[] retu = new byte[128];
        int length = 0;
        for(int i = 0; i<(retu.length); i++){
            retu[i] = b[getPayloadStartIndex(b)+108+i];
            if(retu[i] == 0){
                length = 0;
                break;
            }
        }
        return new String(retu,0,length); 
    }
    public static int getOptionsStartIndex(byte[] b){
        int optionsStart = 236+getPayloadStartIndex(b);
        if(b[optionsStart] != 99 || b[optionsStart+1] != -126 || b[optionsStart+2] != 83 || b[optionsStart+3] != 99 ){
            System.out.println(
                    "magic cookies b["+getOptionsStartIndex(b)+"]:"+
                    Arrays.toString(
                        Arrays.copyOfRange(b, getOptionsStartIndex(b), b.length)
                    )
            );
            throw new IndexOutOfBoundsException("magic cokies failed");
        }
        
        return optionsStart;
    } 
    public static Options getOptions(byte[] b){
        Options retu = new Options();
        int index = getOptionsStartIndex(b);
        
        index+=4;//for the magic cookie
        while(index<b.length){
            Option o = Option.initialize(b, index);
            index+=o.getByteArrayLength();
            retu.add(o);
        }
        return retu;
    }
    public static String toString(byte[]b){
        StringBuilder sb = new StringBuilder();
        try{
            sb.append("start index:").append(getPayloadStartIndex(b));
            sb.append("\nOP:").append(getOP(b));
            sb.append("\nHTYPE:").append(getHTYPE(b));
            sb.append("\nHLEN:").append(getHLEN(b));
            sb.append("\nHOPS:").append(getHOPS(b));
            sb.append("\nXID:").append(getXID(b));
            sb.append("\nSECS:").append(getSECS(b));
            sb.append("\nFLAGS:").append(getFLAGS(b));
            sb.append("\nClient_IP_address:").append(IpPacket_deprecated.ipIntToString(getClient_IP_address(b)));
            sb.append("\nYour_IP_address:").append(IpPacket_deprecated.ipIntToString(getYour_IP_address(b)));
            sb.append("\nServer_IP_address:").append(IpPacket_deprecated.ipIntToString(getServer_IP_address(b)));
            sb.append("\nGateway_IP_address:").append(IpPacket_deprecated.ipIntToString(getGateway_IP_address(b)));
            sb.append("\nClient_hardware_address:").append(Arrays.toString(getClient_hardware_address(b)));
            sb.append("\nServer_host_name:").append(getServer_host_name(b));
            sb.append("\nBoot_file_name:").append(getBoot_file_name(b));
            ArrayList<Option> options = getOptions(b);
             sb.append("\nOPTIONS:");
            for(Option o : options){
                 sb.append("\n\t\t").append(o.toString());
            }
            return sb.toString();
        }catch(Exception e){
            System.out.println("failed to make string of packet \n "+sb.toString());
            throw e;
        }
        
    }
    
    
    
}
