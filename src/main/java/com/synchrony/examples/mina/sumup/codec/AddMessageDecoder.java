/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.synchrony.examples.mina.sumup.codec;
                                
import com.synchrony.examples.mina.sumup.message.AbstractMessage;
import com.synchrony.examples.mina.sumup.message.AddMessage;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;

/**
 * A {@link MessageDecoder} that decodes {@link AddMessage}.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class AddMessageDecoder extends AbstractMessageDecoder {

    public AddMessageDecoder() {
        super(Constants.ADD);
    }

    @Override
    protected AbstractMessage decodeBody(IoSession session, IoBuffer in) {
        if (in.remaining() < Constants.ADD_BODY_LEN) {
            return null;
        }

        AddMessage m = new AddMessage();
        m.setValue(in.getInt());
        return m;
    }

    public void finishDecode(IoSession session, ProtocolDecoderOutput out)
            throws Exception {
    }
}

