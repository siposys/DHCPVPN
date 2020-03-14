/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gateway.routers;

import java.io.IOException;

/**
 *
 * @author root
 */
public interface PortConnectionBuilder{
    public ConnectionForwarder bind( Client.UDP conn) throws IOException;
    public ConnectionForwarder bind(  Client.TCP conn) throws IOException;
    public interface ConnectionForwarder{
        /**
         * 
         * @param b [ip Header] + [UDP/TCP heade] + [Payload] 
         * @throws IOException 
         */
        public void newClientMessage(byte[] b) throws IOException;
    } 
}