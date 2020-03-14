/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sockets.tcp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import sockets.editable.TcpPacketBuilder;
import sockets.tcp.Options.Option;

/**
 *
 * @author root
 */
public class Options extends ArrayList<Option> {
    
    public static class Option{
        public static final int 
                type_padding_flag = 1,
                type_max_seg_size= 2,//"maximum segment size",
                type_wind_scale = 3,//"Window scale",
                type_selec_ack_permit = 4,//"Selective Acknowledgement permitted",
                type_time = 8;//"timestamp";
        int type;
        byte[] data;
        public Option(int type, byte[] b , int start, int length){
            this.type = type;
            this.data = Arrays.copyOfRange(b, start, start+length);
        }
        public String toString(){
            String retu = type+"=";
            switch(type){
                case type_padding_flag:
                    retu="Padding_Flag;";
                    break;
                case type_max_seg_size:
                    retu="Max_Seg_size="+( (data[0]<<8) + (data[1]&0xff) );
                    break;
                case type_wind_scale:
                    retu = "Window_Scale:e^"+data[0]+" = "+ Math.pow(2, data[0]);
                    break;
                case type_selec_ack_permit:
                    retu ="Selec_Ack_permit="+"true";
                    break;
                default: 
                    retu = type+"="+Arrays.toString(data);
                    break;
                        
            }
            return retu;
        }
        public byte[] toArray(){
            if(type == type_padding_flag){
                byte[]retu=new byte[1];
                retu[0] = (byte) type;
                return retu;
            }
            byte[] retu = new byte[2+data.length];
            retu[0] = (byte) type;
            retu[1] = (byte) retu.length;
            for(int i = 0; i<data.length; i++){
                retu[2+i] = data[i];
            }
            return retu;
        }
    }
    public static class TimeStamp extends Option{
        
        public TimeStamp( byte[] b, int start) {
            super(Option.type_time, b, start, 8);
        }
        public TimeStamp(int type, byte[] b, int start, int length) { super(type, b, start, length); }
        
        public String toString(){ return "Time="+getTsVal()+"(TsVal):"+getTsecr()+"(Tsecr)"; }
        public int getTsVal(){ return TcpPacketBuilder.getInt(0, data); }
        public int getTsecr(){ return TcpPacketBuilder.getInt(4, data); }
        
        
    }
    
    public Options(){}
    public Options(byte[] b, int start, int length) throws IOException{
        int offset = 0 ;
        while(offset < length){
            switch(b[start+offset]){
                
                case 0://end flag
                    if(offset +1 != length){
                        throw new IOException("malformed options list:"+(offset-length) );
                    }
                    offset++;
                    break;
                    
                case 1://padding flag
                    this.add(new Option(Option.type_padding_flag,b, 0,0));
                    offset++;
                    break;
                    
                case 2://maximum segment size
                    if(b[start+1+offset] != 4){ 
                        throw new IOException("malformed options list" );
                    }
                    offset+=2;
                    this.add(new Option(Option.type_max_seg_size, b,start+offset, 2));
                    offset+=2;
                    break;
                    
                case 3://window scale
                    if(b[start+1+offset] != 3){ 
                        throw new IOException("malformed options list:"+Arrays.toString(Arrays.copyOfRange(b, start, start+length)) );
                    }
                    offset+=2;
                    this.add(new Option(Option.type_wind_scale, b,start+offset, 1));
                    offset++;
                    break;
                    
                case 4: // Selective Acknowledgement permitted
                    if(b[start+1+offset] != 2){ 
                        throw new IOException("malformed options list" );
                    }
                    offset+=2;
                    this.add(new Option(Option.type_selec_ack_permit, b,start+offset, 0));
                    break;
                    
                case 5: //Selective ACKnowledgement
                    offset+=2;
                    this.add( new Option(5, b,start+offset, b[start+offset-1]) );
                    offset+=  b[start+offset-1]-2;
                    break;
                    
                case 8: //Timestamp
                    if(b[start+1+offset] != 10){ 
                        throw new IOException("malformed options list" );
                    }
                    offset+=2;
                    this.add( new TimeStamp( b,start+offset) );
                    offset+=  8;
                    break;
                    
                default: throw new IOException("unknown option:"+b[start+offset]);
                    
                    
            }
        }
    }
    
    public byte[] toByteArray(){
        ArrayList<Byte> fil = new ArrayList<Byte>();
        for(Option o : this){
            byte[] buf = o.toArray();
            for(byte byt : buf){
                fil.add(byt);
            }
        }
        byte[] retu = new byte[fil.size()];
        for(int i =0; i<retu.length; i++){
            retu[i] = fil.get(i);
        }
        return retu;
    }
    
    
    
public void addMaxSegmentSize(int size){
                    this.add(new Option(Option.type_max_seg_size, new byte[]{ (byte)((size>>8)&0xff), (byte)(size&0xff) },0, 2));
}
public void addPadding(){
                    this.add(new Option(Option.type_padding_flag,new byte[]{}, 0,0));
}
/**
takes in a number and converts it to the exponential form
    scale = 2^{pow}
    the value is encoded using pow, therefore the number you choose must be an exact power of 2
    <a href="https://cloudshark.io/articles/tcp-window-scaling-examples/"> source</a>
**/
public void addWindowScale(int scale){
        scale  = (int)(Math.log(scale)/Math.log(2));
                    this.add(new Option(Option.type_wind_scale, new byte[]{(byte)scale},0, 1));
}
public void addSelectiveAcknowlegementPermitted(){
                    this.add(new Option(Option.type_selec_ack_permit, new byte[]{},0, 0));
}
    
    /**
     * http://www.networksorcery.com/enp/protocol/tcp/option008.htm
     * @param timestampEcho 
     */
    public void addTimeStamp(int ourTimeStamp, int timestampEcho){
        byte[] b = new byte[8];
        int ourTime = (int) System.currentTimeMillis();
        TcpPacketBuilder.setInt(ourTime, 0, b);
        TcpPacketBuilder.setInt(timestampEcho, 4, b);
        System.out.println("addind timestamp:"+ourTimeStamp+":"+timestampEcho);
        add(new TimeStamp(b, 0));
    }

    public TimeStamp getTimeStamp(){
        for(Option o : this){
            if(o.type == Option.type_time){
                return (TimeStamp)o;
            }
        }
        return null;
    }
}
