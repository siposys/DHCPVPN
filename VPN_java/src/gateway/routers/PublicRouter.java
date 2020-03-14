/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gateway.routers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import sockets.IpPacket;
import sockets.IpPacket_deprecated;
import sockets.RawSocket;
import sockets.Tcp3WayHandshake;
import sockets.UdpPacketBuilder;
import sockets.editable.IpPacketBuilder;

/**
 *
 * @author root
 */
public class PublicRouter implements PortConnectionBuilder {
    private RawSocket outTCP, outUDP;
    public PublicRouter(RawSocket outTCP, RawSocket outUDP){ 
        this.outTCP = outTCP; 
        this.outUDP = outUDP; 
    }

    @Override
    public PortConnectionBuilder.ConnectionForwarder bind(Client.UDP conn) throws IOException {
        return new LocalUDP(conn);
    }

    @Override
    public PortConnectionBuilder.ConnectionForwarder bind(Client.TCP conn) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    private void sentToVPN(byte[] payload, int protocol){
        throw new UnsupportedOperationException("still working on this");
        
    }
    private class TCP implements PortConnectionBuilder.ConnectionForwarder{

        @Override
        public void newClientMessage(byte[] b) throws IOException {
            System.out.println("new client message tcp:\n"+IpPacket.toString(b)+"/n"+IpPacket.TCPPacket.toString(b));
            byte[] response = Tcp3WayHandshake.isHandshake(b);
            if(response != null){
                outTCP.write(response);
                return;
            }
            
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    /**
     * udp timeout 30 seconds
     */
    private class LocalUDP implements PortConnectionBuilder.ConnectionForwarder, Runnable{
        private static final int socketTimeout = 30*1000;
        
        private DatagramSocket socket;
        private Client.UDP conn;
        private Thread readThread = new Thread(this);
            private boolean running = false;
        private InetAddress outAddress;
        private long lastSendTime = 0;
        private byte[] firstpacket;
        
        
        private LocalUDP(Client.UDP conn) throws SocketException, UnknownHostException{
            this.conn = conn;
            this.socket = new DatagramSocket();
            this.socket.setSoTimeout(socketTimeout);//30 seconds
            outAddress = InetAddress.getByName(IpPacket.ipIntToString(conn.serverIp));
        }

        @Override
        public void newClientMessage(byte[] buf) throws IOException{
            if(buf == null){ firstpacket = buf; }
            byte[] payload = IpPacket.UDPPacket.getPayload(buf);
            System.out.println("sending client message to server:"+new String(payload));
            DatagramPacket packet;
            packet = new DatagramPacket(payload, payload.length, outAddress, conn.serverPort);
            socket.send(packet);
            lastSendTime = System.currentTimeMillis();
            if(!running){
                running = true;
                readThread.start();
            }
        }

        private byte[] read = new byte[65535];
        @Override
        public void run() {//read for response packets for 30 seconds
            running = true;
            while(true){
                try{
                    DatagramPacket packet = new DatagramPacket(read, read.length);
                    socket.receive(packet);
                    sendPacketToClient(packet);
                }catch(SocketTimeoutException ste){
                    if(System.currentTimeMillis()-lastSendTime > socketTimeout){
                        break;
                    }

                } catch (IOException ex) {
                    Logger.getLogger(LocalRouter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            running = false;
        }
        private void sendPacketToClient(DatagramPacket packet) throws IOException{
            IpPacketBuilder ip = new IpPacketBuilder(firstpacket);
            ip.setDestIp(conn.clientIp);
            ip.setSourceIp(conn.serverIp);
           
            UdpPacketBuilder udp = new UdpPacketBuilder(firstpacket, IpPacket_deprecated.payloadStartIndex);
            udp.setDestPort(conn.clientPort);
            udp.setSourcePort(conn.serverPort);
            
            byte[] b = Arrays.copyOfRange(packet.getData(), socketTimeout, socketTimeout);
            System.out.println("sending packet to client:"+new String(b));
            byte[] send = udp.buildWholePacket(ip,b );
            outUDP.write(b);
            
        }
        
    }
    
}
