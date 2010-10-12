/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.synchrony.examples.mina.sumup.codec;


import com.synchrony.examples.mina.sumup.message.ResultMessage;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.demux.MessageEncoder;

/**
 * A {@link MessageEncoder} that encodes {@link ResultMessage}.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class ResultMessageEncoder<T extends ResultMessage> extends AbstractMessageEncoder<T> {
    public ResultMessageEncoder() {
        super(Constants.RESULT);
    }

    @Override
    protected void encodeBody(IoSession session, T message, IoBuffer out) {
        if (message.isOk()) {
            out.putShort((short) Constants.RESULT_OK);
            out.putInt(message.getValue());
        } else {
            out.putShort((short) Constants.RESULT_ERROR);
        }
    }

    public void dispose() throws Exception {
    }
}



