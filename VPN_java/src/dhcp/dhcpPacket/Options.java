/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dhcp.dhcpPacket;

import java.util.ArrayList;

/**
 *
 * @author root
 */
public class Options extends ArrayList<Option> {
    
    public Option findOptionByType(int type){
        for(Option o : this){
            if(o.type == type){
                return o;
            }
        }
        return null;
    }
    
    public int getMessageType(){
        Option o = findOptionByType(Option.type_message_type);
        if(o == null){
            return -1;
        }
        return ((Option.MessageType)o).getMessageType();
    }
    public byte[] getParamRequestList(){
        Option o = findOptionByType(Option.type_param_request_list);
        if(o == null){return new byte[0]; }
        return ((Option.ParamRequestList)o).getRequestList();
    }
    public byte[] toByteArray() {
        int length = 0;
        if(get(size()-1).type != Option.type_end){
            System.out.println("you forgot to add the end option!!");
            add( new Option.EndOption() );
        }
        for(Option o :this){
            length+=o.getByteArrayLength();
        }
        byte[] retu = new byte[length];
        int index = 0;
        for(Option o :this){
            retu[index] = (byte) o.type;
                index++;
            
            if(o.data.length>0){
                retu[index]= (byte) o.data.length;
                    index++;
                for(int i = 0; i<o.data.length; i++, index++){
                    retu[index] = o.data[i];
                }
            }
        }
        
        return retu;
    }
}
