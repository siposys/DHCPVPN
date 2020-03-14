/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dhcp;

import dhcp.dhcpPacket.Options;
import dhcp.dhcpPacket.Option;
import java.io.IOException;
import java.util.ArrayList;
import sockets.RawSocket;
import sockets.IpPacket;
import sockets.IpPacket_deprecated;
import sockets.UdpPacketBuilder;
import sockets.editable.IpPacketBuilder;

/**
 *
 * @author root
 */
public class DHCPServer {
    private static final int subnetMask = IpPacket.ipStringToInt("255.255.255.0"),
            subnetPrefix = IpPacket.ipStringToInt("192.168.1.0"),
            serverIpAddress = IpPacket.ipStringToInt("192.168.1.1"),
            serverIdentifier = IpPacket.ipStringToInt("192.168.1.1"),
            gatewayIpAddress = IpPacket.ipStringToInt("0.0.0.0"),
            broadcastAddress = IpPacket.ipStringToInt("192.168.1.101"),
            addressLeaseTime = 60*60*24,
            renewalLeaseTime = 60*60*12,
            rebindingLeaseTime = 60*60*21;
    
    public final RawSocket out;
    ArrayList<Integer> usedSubnets = new ArrayList<Integer>();
    
    public DHCPServer(RawSocket outStream_UDP){
        this.out = outStream_UDP;
    }
    
    public boolean accept(byte[] b) throws IOException{
        if(IpPacket.UDPPacket.getSourcePort(b) != 68 || IpPacket.UDPPacket.getDestPort(b) != 67){
            return false;
        }
        Options opts = DHCPPacket.getOptions(b);
        switch(opts.getMessageType() ){
            case Option.MessageType.DHCPDISCOVER:
                sendDiscover(b, opts);
                return true;
            case Option.MessageType.DHCPREQUEST:
                sendRequestAck(b,opts);
                return true;
            default: 
                System.out.println("unable to procss message type:"+opts.getMessageType());
                System.out.println("ip packet:"+IpPacket.toString(b));
                System.out.println("udp packet:"+IpPacket.UDPPacket.toString(b));
                System.out.println("dhcp packet:"+DHCPPacket.toString(b));
                throw new IOException("unable to process message");
        }
    }
    private int getAvailableIp(){
        for(int i = 2; i<100; i++){
            boolean used = false;
            for(int clientSubnet: usedSubnets){
                if(clientSubnet == i){
                    used = true;
                    break;
                }
            }
            if(!used){ return i; }
        }
        throw new IndexOutOfBoundsException("no available clients");
    }
    private void grabIp(int ip) throws IOException{
        int requestedSubnet = (~subnetMask) & ip;
            for(int clientSubnet: usedSubnets){
                if(requestedSubnet == clientSubnet){
                    throw new IOException("address already in use:"+IpPacket.ipIntToString(ip));
                }
            }
            usedSubnets.add(requestedSubnet);
    }
    public boolean isConnectedClient(int ip){
        if( (ip & subnetMask) != subnetPrefix){ return false; }//if its not on our subnet
        int ourSubnet = (~subnetMask) & ip;
        for(int clientSubnet : usedSubnets){
            if(clientSubnet == ourSubnet){ return true; }
        }
        return false;
        
    }
    private void sendDiscover(byte[] b, Options opts) throws IOException{
        /*Sys.out.println(
                "new Discover request:"+
                IpPacket.getSourceIp(b)+":"+IpPacket.UDPPacket.getSourcePort(b)+
                "=>"+IpPacket.getDestIp(b)+":"+IpPacket.UDPPacket.getDestPort(b)+"\nDCHP details:"+DHCPPacket.toString(b)
        );
        */
        DhcpPacketBuilder builder = new DhcpPacketBuilder();
        
        builder.initializeFromCopy(b);
        builder.setOP((byte)2);
        builder.setServer_IP_address(serverIpAddress);
        builder.setYour_IP_address(subnetPrefix+getAvailableIp());
        builder.options = buildResponseOptions(opts);
        
        //add the mandatory options
        
        //configure message type 
        Option o = builder.options.findOptionByType(Option.type_message_type);
        if(o == null){
            o= new Option.MessageType(Option.MessageType.DHCPOFFER);
            builder.options.add(o);
        }
        
        //subnetMask
        o = builder.options.findOptionByType(Option.type_subnet_mask);
        if(o == null){
            o= new Option.SubnetMask(subnetMask);
            builder.options.add(o);
        }
        
        //router
        o = builder.options.findOptionByType(Option.type_router);
        if(o == null){
            o = buildResponseByType((byte)Option.type_router);
            builder.options.add(o);
        }
        
        //lease time
        o = builder.options.findOptionByType(Option.type_ip_address_lease_time);
        if(o == null){
            o = buildResponseByType((byte)Option.type_ip_address_lease_time);
            builder.options.add(o);
        }
        
        //server identifier
        o = builder.options.findOptionByType(Option.type_server_identifier);
        if(o == null){
            o = buildResponseByType((byte)Option.type_server_identifier);
            builder.options.add(o);
        }
        
        //dns servers
        o = builder.options.findOptionByType(Option.type_dns);
        if(o == null){
            o = buildResponseByType((byte)Option.type_dns);
            builder.options.add(o);
        }
        
        
        //build final packets
        //ip packet
        IpPacketBuilder ipe = new IpPacketBuilder(b);
            ipe.setSourceIp(serverIpAddress)
                    .setDestIp(IpPacket.getDestIp(b));
        //udp packet
        UdpPacketBuilder udpe = new UdpPacketBuilder(b, IpPacket.getIPHeaderLength(b));
        int destPort = udpe.getDestPort();
        int srcPort = udpe.getSourcePort();
            udpe.setDestPort(srcPort)
                .setSourcePort(destPort);
            
        byte[] retu = builder.build();//dhcp packet
        retu = udpe.build(ipe.getSourceIp(), ipe.getDestIp(), IpPacket_deprecated.payloadStartIndex+UdpPacketBuilder.payload_start_index+retu.length,retu );
        retu = ipe.build(retu);
        out.write(retu, udpe.getDestPort(), ipe.getDestIp());
        
        
    }
    private void sendRequestAck(byte[] b, Options opts) throws IOException, IOException{
        int requestedIp = ((Option.RequestedIp)opts.findOptionByType(Option.type_requested_ip_address)).getIpAddress();
        grabIp(requestedIp);
        
        
        DhcpPacketBuilder builder = new DhcpPacketBuilder();
        
        builder.initializeFromCopy(b);
        builder.setOP((byte)2);
        builder.setServer_IP_address(serverIpAddress);
        builder.setYour_IP_address(subnetPrefix+getAvailableIp());
        builder.options = buildResponseOptions(opts);
        
        
        //add manditory options:
        
        
        //configure message type 
        Option o = builder.options.findOptionByType(Option.type_message_type);
        if(o == null){
            o= new Option.MessageType(Option.MessageType.DHCPACK);
            builder.options.add(o);
        }
        
        //subnetMask
        o = builder.options.findOptionByType(Option.type_subnet_mask);
        if(o == null){
            o= new Option.SubnetMask(subnetMask);
            builder.options.add(o);
        }
        
        //router
        o = builder.options.findOptionByType(Option.type_router);
        if(o == null){
            o = buildResponseByType((byte)Option.type_router);
            builder.options.add(o);
        }
        
        //lease time
        o = builder.options.findOptionByType(Option.type_ip_address_lease_time);
        if(o == null){
            o = buildResponseByType((byte)Option.type_ip_address_lease_time);
            builder.options.add(o);
        }
        
        //server identifier
        o = builder.options.findOptionByType(Option.type_server_identifier);
        if(o == null){
            o = buildResponseByType((byte)Option.type_server_identifier);
            builder.options.add(o);
        }
        
        //dns servers
        o = builder.options.findOptionByType(Option.type_dns);
        if(o == null){
            o = buildResponseByType((byte)Option.type_dns);
            builder.options.add(o);
        }
        
        
        
        //----------------------------------
        //build final packets
        //ip packet
        IpPacketBuilder ipe = new IpPacketBuilder(b);
            ipe.setSourceIp(serverIpAddress)
                    .setDestIp(IpPacket.getDestIp(b));
        //udp packet
        UdpPacketBuilder udpe = new UdpPacketBuilder(b, IpPacket.getIPHeaderLength(b));
        int destPort = udpe.getDestPort();
        int srcPort = udpe.getSourcePort();
            udpe.setDestPort(srcPort)
                .setSourcePort(destPort);
            
        byte[] retu = builder.build();//dhcp packet
        //udp packet
        retu = udpe.build(ipe.getSourceIp(), ipe.getDestIp(), IpPacket_deprecated.payloadStartIndex+UdpPacketBuilder.payload_start_index+retu.length,retu );
        //ip packet
        retu = ipe.build(retu);
        out.write(retu, udpe.getDestPort(), ipe.getDestIp());
    }
    private Options buildResponseOptions(Options request){
        Options retu = new Options();
        byte[] list = request.getParamRequestList();
        for(byte i : list){
            Option o = buildResponseByType(i);
            if(o != null){ retu.add(o); }
        }
        return retu;
    }
    private Option buildResponseByType(byte type){
        switch(type&0xff){
            case Option.type_subnet_mask:
                return new Option.SubnetMask( subnetMask);
                
            case Option.type_classless_static_route:
                //Sys.out.println("skipping classless static route because its confusing");
                return null;
                
            case Option.type_static_route:
                return new Option.StaticRoute( 
                        new int[]{
                            gatewayIpAddress, serverIpAddress
                        } 
                );
                
            case Option.type_router:
                return new Option.Router(serverIpAddress);
                
            case Option.type_dns:
                return new Option.DNS(
                        IpPacket.ipStringToInt("8.8.8.8"),
                        IpPacket.ipStringToInt("8.8.4.4")
                );
                
            case Option.type_host_name:
                return new Option.HostName("192.168.1.1");
                
            case Option.type_domain_name:
                //Sys.out.println("skipping domain name because its confusing, and appears to be unneccessary");
                return null;
                
            case Option.type_interface_mtu:
                return new Option.InterfaceMTU(1500);
                
            case Option.type_ip_address_lease_time:
                return new Option.IpAddressLeaseTime(addressLeaseTime);
                
            case Option.type_server_identifier:
                return new Option.ServerID(this.serverIdentifier);
            
            case Option.type_broadcast_address:
                return new Option.BroadcastAddress(broadcastAddress);
                
            case Option.type_renewal_time_val:
                return new Option.RenewalTimeValue(renewalLeaseTime);
            
            case Option.type_rebinding_time_value:
                return new Option.RebindingTimeValue(rebindingLeaseTime);
                
            case Option.type_domain_search:
                //Sys.out.println("DNS search options were removed since it requires domain_name to be used");
                return null;
                
            case Option.type_WPAD:
                //Sys.out.println("WPAD(web proxy auto discovery) is removed since ther is no proxy");
                return null;
                
            default: 
                
                throw new IndexOutOfBoundsException("cannot respond to:"+type+"="+Option.typeToString(type));
        }
    }
    
}
