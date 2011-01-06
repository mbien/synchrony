/*
 * Created on Thursday, January 06 2011 03:54
 */
package com.synchrony.networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author mbien
 */
public enum MsgHeader {
    
    STATUS_REQUEST, SYNC_UPLOAD;

    public static MsgHeader load(InputStream is) throws IOException {
        int code = is.read();
        if(code >= values().length) {
            throw new IOException("invalid header code:"+code+".");
        }
        return values()[code];
    }

    public void write(OutputStream os) throws IOException {
        os.write(ordinal());
    }

}
