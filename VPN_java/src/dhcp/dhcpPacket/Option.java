/*
 * <a href="https://tools.ietf.org/html/rfc1533"> source </a>
 */
package dhcp.dhcpPacket;

import java.util.Arrays;
import sockets.IpPacket_deprecated;
import sockets.editable.TcpPacketBuilder;

/**
 * <a href="https://tools.ietf.org/html/rfc1533"> source </a>
 * <br/>
 * <a href="http://www.networksorcery.com/enp/protocol/bootp/options.htm">nicer reference</a>
 * @author root
 */
public class Option {
    public static final int 
    type_subnet_mask = 1,type_time_offset = 2,type_router =3,type_time_server = 4,type_name_server = 5,
    type_dns = 6,type_log_server = 7,type_cookie_server = 8,type_lpr_server = 9,type_impress_server = 10,
    type_resource_location_server = 11,type_host_name = 12,type_boot_file_size = 13,type_merit_dump_file = 14,type_domain_name = 15,
            type_interface_mtu = 26,
            type_broadcast_address = 28,
            type_static_route = 33,
            type_requested_ip_address = 50,
            type_ip_address_lease_time = 51,
            type_message_type = 53,
            type_server_identifier = 54,
            type_renewal_time_val = 58,
            type_rebinding_time_value = 59,
            type_client_identifier = 61,
            type_param_request_list = 55,
            type_max_message_size = 57,
            type_class_identifier = 60,
            type_domain_search = 119,
            type_classless_static_route = 121,
            type_WPAD = 252,
            type_end = 255;
    public int type;
    public byte[] data;
    
    public static Option initialize(byte[] b, int start){
        switch(b[start]&0xff){
            case type_subnet_mask:  return new SubnetMask(b,start);
            case type_router:  return new Router(b,start);
            case type_dns:  return new DNS(b,start);
            case type_host_name:  return new HostName(b,start);
            case type_static_route:  return new StaticRoute(b,start);
            case type_message_type:  return new MessageType(b,start);
            case type_client_identifier:  return new ClientIdentifier(b,start);
            case type_param_request_list:  return new ParamRequestList(b,start);
            case type_max_message_size:  return new MaxMessageSize(b,start);
            case type_class_identifier:  return new ClassIdentifier(b,start);
            case type_end:  return new EndOption(b,start);
            case type_interface_mtu: return new InterfaceMTU(b,start);
            case type_broadcast_address: return new BroadcastAddress(b,start);
            case type_requested_ip_address: return new RequestedIp(b,start);
            case type_ip_address_lease_time: return new IpAddressLeaseTime(b,start);
            case type_server_identifier: return new ServerID(b,start);
            case type_renewal_time_val: return new RenewalTimeValue(b,start);
            case type_rebinding_time_value: return new RebindingTimeValue(b,start);
        }
        return new Option(b,start);
    }
    private  Option(byte[]b, int start){
            type = b[start]&0xff;
        if(type == 0 || type == 0xff){
            data = new byte[0];
            return;
        }
        else{
            if(b[start+1]<0){
                System.out.println("array[start=>length]:"+Arrays.toString(Arrays.copyOfRange(b, start, b.length)));
                throw new IndexOutOfBoundsException("at index:"+start);
            }
            data = Arrays.copyOfRange(b, start+2, b[start+1]+start+2);
        }
    }
    public int getByteArrayLength(){
        if(data.length == 0){
            return 1;
        }
        return 2+data.length;
    }
    public String toString(){
        return typeToString(type)+":"+Arrays.toString(data);
    }
    public static String typeToString(int type){
        switch(type){
            case type_subnet_mask:  return  "subnetMask";
            case type_time_offset:  return  "timeOffset";
            case type_router:  return  "router";
            case type_time_server:  return  "timeServer";
            case type_name_server:  return  "nameServer";
            case type_dns:  return  "dns";
            case type_log_server:  return  "logServer";
            case type_cookie_server:  return  "cookieServer";
            case type_lpr_server:  return  "lprServer";
            case type_impress_server:  return  "impressServer";
            case type_resource_location_server:  return  "resourceLocationServer";
            case type_boot_file_size:  return  "bootFileSize";
            case type_merit_dump_file:  return  "meritDumpFile";
            case type_host_name:  return  "hostName";
            case type_domain_name:  return  "domainName";
            case type_interface_mtu:  return  "interfaceMTU";
            case type_broadcast_address:  return  "broadcastAddress";
            case type_static_route:  return  "staticRoute";
            case type_message_type:  return  "MessageType";
            case type_requested_ip_address: return "RequestedIp";
            case type_ip_address_lease_time:  return  "ipAddrLeaseTime";
            case type_server_identifier:  return "serverId";
            case type_renewal_time_val:  return "renewalTimeVal";
            case type_rebinding_time_value:  return "rebindingTimeVal";
            case type_client_identifier:  return "ClientIdentifier";
            case type_param_request_list:  return "ParamRequestList";
            case type_max_message_size:  return "MaxMessageSize";
            case type_class_identifier:  return "ClassIdentifier";
            case type_end:  return "EndOption";
        }
        return "unknown("+(type&0xff)+")";
    }
    
    //----------------------------------------------------------------------------------------------------
    //extended classes
    
    public static class MessageType extends Option{
         public static final int DHCPDISCOVER = 1,DHCPOFFER = 2, DHCPREQUEST = 3, 
                 DHCPDECLINE = 4, DHCPACK = 5, DHCPNAK = 6, DHCPRELEASE = 7;
         public MessageType(int type){
             super(new byte[]{(byte)type_message_type, (byte)0},0);
             data = new byte[]{(byte)type};
         }
        public MessageType(byte[] b, int start) {
            super(b, start);
            if(type!= type_message_type){
                throw new IndexOutOfBoundsException("incorrect Type:"+type);
            }
        }
        public void setMessageType(int type){ data[0] = (byte)type; }
        public int getMessageType(){ return data[0]; }
        public String toString(){
            switch(data[0]){
                case DHCPDISCOVER: return "MessageType:Discover";
                case DHCPOFFER: return "MessageType:Offer";
                case DHCPREQUEST: return "MessageType:Request";
                case DHCPDECLINE: return "MessageType:Decline";
                case DHCPACK: return "MessageType:Ack";
                case DHCPNAK: return "MessageType:Nak";
                case DHCPRELEASE: return "MessageType:Release";
            }
            return "MessageType:unknown("+data[0]+")";
        }
    }
    
    public static class RequestedIp extends Option{
        public RequestedIp(int mask){
            this(
                    new byte[]{
                        Option.type_requested_ip_address, 
                        4, 
                        (byte) ((mask>>24) &0xff), 
                        (byte)((mask>>16) & 0xff), 
                        (byte)((mask>>8) & 0xff), 
                        (byte)(mask&0xff)
                    },
                    0
            );
        }
        public RequestedIp(byte[] b, int start) {
            super(b, start);
            
            if(type!= type_requested_ip_address){
                throw new IndexOutOfBoundsException("incorrect Type:"+type);
            }
        }
        public String toString(){
           return "RequestedIp:"+IpPacket_deprecated.ipIntToString(TcpPacketBuilder.getInt(0, data));
        }

        public int getIpAddress() {
            return TcpPacketBuilder.getInt(0, data);
        }
        
    }
    
    public static class SubnetMask extends Option{
        public SubnetMask(int mask){
            this(
                    new byte[]{
                        Option.type_subnet_mask, 
                        4, 
                        (byte) ((mask>>24) &0xff), 
                        (byte)((mask>>16) & 0xff), 
                        (byte)((mask>>8) & 0xff), 
                        (byte)(mask&0xff)
                    },
                    0
            );
        }
        public SubnetMask(byte[] b, int start) {
            super(b, start);
            
            if(type!= type_subnet_mask){
                throw new IndexOutOfBoundsException("incorrect Type:"+type);
            }
        }
        public String toString(){
           return "SubnetMask:"+IpPacket_deprecated.ipIntToString(TcpPacketBuilder.getInt(0, data));
        }
        
    }
    
    public static class ClientIdentifier extends Option{
        
        public ClientIdentifier(byte[] b, int start) {
            super(b, start);
            
            if(type!= type_client_identifier){
                throw new IndexOutOfBoundsException("incorrect Type:"+type);
            }
        }
        public String toString(){
            return "CliendIdentifier:"+Arrays.toString(data);
        }
        
    }
    
    public static class MaxMessageSize extends Option{
        
        public MaxMessageSize(byte[] b, int start) {
            super(b, start);
            
            if(type!= type_max_message_size){
                throw new IndexOutOfBoundsException("incorrect Type:"+type);
            }
        }
        public String toString(){
            return "maxMessageSize:"+TcpPacketBuilder.getShort(0,data);
        }
        
    }
    
    public static class ClassIdentifier extends Option{
        
        public ClassIdentifier(byte[] b, int start) {
            super(b, start);
            
            if(type!= type_class_identifier){
                throw new IndexOutOfBoundsException("incorrect Type:"+type);
            }
        }
        public String toString(){
            return "classIdentifier:"+new String(data,0,data.length);
        }
        
    }
    
    public static class EndOption extends Option{
        public EndOption(){
            super(new byte[]{(byte)type_end, 0},0);
        }
        public EndOption(byte[] b, int start) {
            super(b, start);
            
            if(type!= type_end){
                throw new IndexOutOfBoundsException("incorrect Type:"+type);
            }
        }
        public String toString(){
            return "EndOption";
        }
        
    }
    public static class ParamRequestList extends Option{
        
        public ParamRequestList(byte[] b, int start) {
            super(b, start);
            
            if(type!= type_param_request_list){
                throw new IndexOutOfBoundsException("incorrect Type:"+type);
            }
        }
        public byte[] getRequestList(){ return data; }
        public String toString(){
            StringBuilder sb = new StringBuilder("ParamRequestList:[");
            for(int i = 0; i<data.length; i++){
                if(i>0){sb.append(", "); }
                sb.append(typeToString(data[i]));
            }
            sb.append("]");
            return sb.toString();
        }
        
    }
    /**
     * <a href="http://www.networksorcery.com/enp/protocol/bootp/option033.htm">source</a>
     */
    public static class StaticRoute extends Option{
        public StaticRoute(int[] ipAddressPairs){
            super(new byte[]{type_static_route, 0},0);
            if(ipAddressPairs.length%2 != 0){
                throw new IndexOutOfBoundsException("static routes must come in pairs of Ip addresses");
            }
            byte[] b = new byte[(ipAddressPairs.length*4)];
            for(int i = 0; i<ipAddressPairs.length; i++){
                TcpPacketBuilder.setInt(ipAddressPairs[i], (i*4), b);
            }
            this.data = b;
        }
        public StaticRoute(byte[] b, int start) {
            super(b, start);
            
            if(type!= type_static_route){
                throw new IndexOutOfBoundsException("incorrect Type:"+type);
            }
        }
        public String toString(){
            StringBuilder sb = new StringBuilder("static route option:");
            for(int i = 0; i<data.length; i+=8){
                sb.append("\n")
                        .append(IpPacket_deprecated.ipIntToString(TcpPacketBuilder.getInt(i, data)))
                        .append("=>")
                        .append(IpPacket_deprecated.ipIntToString(TcpPacketBuilder.getInt(i+4, data)));
            }
           return sb.toString();
        }
        
    }
    
    public static class Router extends Option{
        public Router(int... routerIps){
            super(new byte[]{type_router, 0}, 0);
            this.data= new byte[routerIps.length*4];
            for(int i = 0; i<routerIps.length; i++){
                TcpPacketBuilder.setInt(routerIps[i], i*4, data);
            }
        }
        public Router(byte[] b, int start) {
            super(b, start);
            
            if(type!= type_router){
                throw new IndexOutOfBoundsException("incorrect Type:"+type);
            }
        }
        public String toString(){
            StringBuilder sb = new StringBuilder("router option:");
            for(int i = 0; i<data.length; i+=4){
                sb.append("\n")
                        .append(IpPacket_deprecated.ipIntToString(TcpPacketBuilder.getInt(i, data)));
            }
           return sb.toString();
        }
        
    }
    
    public static class DNS extends Option{
        public DNS(int... dnsIPs){
            super(new byte[]{type_dns, 0}, 0);
            this.data= new byte[dnsIPs.length*4];
            for(int i = 0; i<dnsIPs.length; i++){
                TcpPacketBuilder.setInt(dnsIPs[i], i*4, data);
            }
        }
        public DNS(byte[] b, int start) {
            super(b, start);
            
            if(type!= type_dns){
                throw new IndexOutOfBoundsException("incorrect Type:"+type);
            }
        }
        public String toString(){
            StringBuilder sb = new StringBuilder("DNS option:");
            for(int i = 0; i<data.length; i+=4){
                sb.append("\n")
                        .append(IpPacket_deprecated.ipIntToString(TcpPacketBuilder.getInt(i, data)));
            }
           return sb.toString();
        }
        
    }
    
    public static class HostName extends Option{
        public HostName(String hostname){
            super(new byte[]{type_host_name, 0}, 0);
            data = hostname.getBytes();
            
        }
        
        public HostName(byte[] b, int start) {
            super(b, start);
            
            if(type!= type_host_name){
                throw new IndexOutOfBoundsException("incorrect Type:"+type);
            }
        }
        public String toString(){
            return "host name:"+new String(data,0,data.length);
        }
        
    }
    
    public static class DomainName extends Option{
        
        public DomainName(byte[] b, int start) {
            super(b, start);
            
            if(type!= type_domain_name){
                throw new IndexOutOfBoundsException("incorrect Type:"+type);
            }
        }
        public String toString(){
            return "Domain name:"+new String(data,0,data.length);
        }
        
    }
    
    public static class InterfaceMTU extends Option{
        public InterfaceMTU(int maxFrameSize){
            super(new byte[]{type_interface_mtu, 0}, 0);
            data = new byte[2];
            TcpPacketBuilder.setShort(maxFrameSize, 0, data);
            
        }
        public InterfaceMTU(byte[] b, int start) {
            super(b, start);
            
            if(type!= type_interface_mtu){
                throw new IndexOutOfBoundsException("incorrect Type:"+type);
            }
        }
        public String toString(){
            return "interfaceMtu:"+TcpPacketBuilder.getShort(0,data);
        }
        
    }
    
    public static class BroadcastAddress extends Option{
        public BroadcastAddress(int ipAddress){
            super(new byte[]{type_broadcast_address, 0}, 0);
            data = new byte[4];
            TcpPacketBuilder.setInt(ipAddress, 0, data);
        }
        public BroadcastAddress(byte[] b, int start) {
            super(b, start);
            
            if(type!= type_broadcast_address){
                throw new IndexOutOfBoundsException("incorrect Type:"+type);
            }
        }
        public String toString(){
            return "broadcast address:"+IpPacket_deprecated.ipIntToString(TcpPacketBuilder.getInt(0,data));
        }
        
    }
    public static class IpAddressLeaseTime extends Option{
        public IpAddressLeaseTime(int timeInSeconds){
            super(new byte[]{type_ip_address_lease_time, 0}, 0);
            data = new byte[4];
            TcpPacketBuilder.setInt(timeInSeconds, 0, data);
        }
        public IpAddressLeaseTime(byte[] b, int start) {
            super(b, start);
            
            if(type!= type_ip_address_lease_time){
                throw new IndexOutOfBoundsException("incorrect Type:"+type);
            }
        }
        public String toString(){
            return "IP address lease in seconds:"+TcpPacketBuilder.getInt(0,data);
        }
        
    }
    
    public static class RenewalTimeValue extends Option{
        public RenewalTimeValue(int timeInSeconds){
            super(new byte[]{type_renewal_time_val, 0}, 0);
            data = new byte[4];
            TcpPacketBuilder.setInt(timeInSeconds, 0, data);
        }
        public RenewalTimeValue(byte[] b, int start) {
            super(b, start);
            
            if(type!= type_renewal_time_val){
                throw new IndexOutOfBoundsException("incorrect Type:"+type);
            }
        }
        public String toString(){
            return "IP address lease in seconds:"+TcpPacketBuilder.getInt(0,data);
        }
        
    }
    
    public static class RebindingTimeValue extends Option{
        public RebindingTimeValue(int timeInSeconds){
            super(new byte[]{type_rebinding_time_value, 0}, 0);
            data = new byte[4];
            TcpPacketBuilder.setInt(timeInSeconds, 0, data);
        }
        public RebindingTimeValue(byte[] b, int start) {
            super(b, start);
            
            if(type!= type_rebinding_time_value){
                throw new IndexOutOfBoundsException("incorrect Type:"+type);
            }
        }
        public String toString(){
            return "IP address reattempt:"+TcpPacketBuilder.getInt(0,data);
        }
        
    }
    
    public static class ServerID extends Option{
        public ServerID(int identifier){
            super(new byte[]{type_server_identifier, 0}, 0);
            data = new byte[4];
            TcpPacketBuilder.setInt(identifier, 0, data);
        }
        public ServerID(byte[] b, int start) {
            super(b, start);
            
            if(type!= type_server_identifier){
                throw new IndexOutOfBoundsException("incorrect Type:"+type);
            }
        }
        public String toString(){
            return "ServerID:"+TcpPacketBuilder.getInt(0,data);
        }
        
    }
    
    
    
    
}
