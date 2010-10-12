/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.synchrony.examples.mina.sumup.codec;



import com.synchrony.examples.mina.sumup.message.AddMessage;
import com.synchrony.examples.mina.sumup.message.ResultMessage;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;

/**
 * A {@link ProtocolCodecFactory} that provides a protocol codec for
 * SumUp protocol.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class SumUpProtocolCodecFactory extends DemuxingProtocolCodecFactory {

    public SumUpProtocolCodecFactory(boolean server) {
        if (server) {
            super.addMessageDecoder(AddMessageDecoder.class);
            super.addMessageEncoder(ResultMessage.class, ResultMessageEncoder.class);
        } else // Client
        {
            super.addMessageEncoder(AddMessage.class, AddMessageEncoder.class);
            super.addMessageDecoder(ResultMessageDecoder.class);
        }
    }
}


