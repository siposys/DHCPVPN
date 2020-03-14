/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gateway.routers;

import gateway.routers.PortConnectionBuilder.ConnectionForwarder;
import java.io.IOException;
import java.util.ArrayList;
import sockets.IpPacket;

/**
 *
 * @author root
 */
public class Client {
    private final int clientIp;
    private ArrayList<OutBoundIp> outboundIp = new ArrayList<OutBoundIp>();
    private PortConnectionBuilder cb;
    
    public Client(byte[] b, PortConnectionBuilder pcb) throws IOException{
        this.cb = pcb;
        this.clientIp = IpPacket.getSourceIp(b);
        if(!this.accept(b)){
            throw new IOException("failed to build client");
        }
    }
    
    public boolean accept(byte[] b) throws IOException{
        int sourceIp = IpPacket.getSourceIp(b);
        
        if(sourceIp != clientIp ){//not sending to or from client
            return false;
        }
        
        for(int i = 0; i<outboundIp.size(); i++){
            if(outboundIp.get(i).accept(b, sourceIp )){//this packet belongs to one of our clients connections
                return true;
            }
        }
        //if there are no destinations open for this client
        outboundIp.add(new OutBoundIp(b));
        return false;
    }
    
    
    private class OutBoundIp {
        private final int srcIp, destIp;
        private ArrayList<PortConnection> tcpConnections = new ArrayList<PortConnection>();
        private ArrayList<PortConnection> udpConnections = new ArrayList<PortConnection>();

        public OutBoundIp(byte[]b ) throws IOException{
            this.srcIp  = IpPacket.getSourceIp(b);
            this.destIp = IpPacket.getDestIp(b);
            this.accept(b, srcIp);
        }

        public boolean accept(byte[] b, int sourceIp) throws IOException{
            int destIp = IpPacket.getDestIp(b);
            if(
                    sourceIp != this.srcIp || destIp != this.destIp
            ){
                return false;//not coming to or from out clients
            }

            int protocol = IpPacket.getProtocol(b);
            if(protocol == IpPacket.TCP_protocol){
                for(PortConnection conn: tcpConnections){
                    if(conn.accept(b,sourceIp, destIp, protocol)){
                        return true;
                    }
                }
                tcpConnections.add(new TCP(b));
            }
            else if(protocol == IpPacket.UDP_protocol){
                for(PortConnection conn: udpConnections){
                    if(conn.accept(b,sourceIp, destIp, protocol)){
                        return true;
                    }
                }
                udpConnections.add(new UDP(b));
            }
            return false;

        }
    }
    public  class UDP extends PortConnection{
        private ConnectionForwarder forwarder= cb.bind( this);
        public UDP(byte[] b) throws IOException{  
            super(b); 
            this.accept(b, this.clientIp, this.serverIp, this.protocol);
        }
        
        int getSrcPort(byte[] b){
            return IpPacket.UDPPacket.getSourcePort(b);
        }
        int getDestPort(byte[] b){
            return IpPacket.UDPPacket.getDestPort(b);
        }
        void forwardPacket(byte[] b) throws IOException{
            forwarder.newClientMessage(b);
        }
    }
    public class TCP extends PortConnection{
        private final ConnectionForwarder forwarder= cb.bind( this);
        public TCP(byte[] b) throws IOException{  
            super(b); 
            this.accept(b, this.clientIp, this.serverIp, this.protocol);
        }
        
        int getSrcPort(byte[] b){
            return IpPacket.TCPPacket.getSourcePort(b);
        }
        int getDestPort(byte[] b){
            return IpPacket.TCPPacket.getDestPort(b);
        }
        void forwardPacket(byte[] b) throws IOException{
            forwarder.newClientMessage(b);
        }
    }

    private abstract class PortConnection {
        public final int protocol, clientPort, serverPort, clientIp, serverIp;
        public PortConnection(byte[] b) throws IOException{
            this.protocol = IpPacket.getProtocol(b);
            this.clientPort = getSrcPort(b);
            this.serverPort = getDestPort(b);
            this.clientIp = IpPacket.getSourceIp(b);
            this.serverIp = IpPacket.getDestIp(b);
        }
        abstract void forwardPacket(byte[] b) throws IOException;

        abstract int getSrcPort(byte[] b);
        abstract int getDestPort(byte[] b);

        public boolean accept(byte[] b, int srcIp, int destIp, int protocol) throws IOException{
            if(
                    srcIp != this.clientIp || destIp != this.serverIp || protocol != this.protocol
                    ||
                    this.getSrcPort(b) != this.clientPort || this.getDestPort(b) != this.serverPort
            ){
                return false;
            }
            forwardPacket(b);
            return true;
        }

    }

    
}
