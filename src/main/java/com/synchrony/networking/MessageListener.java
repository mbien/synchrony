/*
 * Created on Monday, January 03 2011 20:56
 */

package com.synchrony.networking;

import java.net.Socket;

/**
 *
 * @author mbien
 */
public interface MessageListener {
    
    public void onMessage(MsgHeader message, Socket connection);
    
}
