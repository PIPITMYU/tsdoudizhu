package com.up72.server.mina.tcp;

import com.up72.server.mina.utils.WebSocketUtil;
import com.up72.server.mina.bean.ProtocolData;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.DemuxingProtocolEncoder;
import org.apache.mina.filter.codec.demux.MessageEncoder;

//编码器
public class MinaEncoder extends DemuxingProtocolEncoder {
	public MinaEncoder() {
		addMessageEncoder(ProtocolData.class, new BaseSocketBeanEncoder());
	}

	class BaseSocketBeanEncoder implements MessageEncoder<ProtocolData> {
		public void encode(IoSession session, ProtocolData message,
						   ProtocolEncoderOutput out) throws Exception {
			byte[] _protocol = null;
			if (message.isWebAccept()) {
				_protocol = message.getJsonString().getBytes("UTF-8");
                                
			} else {
				_protocol = WebSocketUtil.encode(message.getJsonString());//----0322
                                //_protocol = WebSocketUtil.encode(JSON.toJSONString(message));//----0323
			}
			int length = _protocol.length;
			IoBuffer buffer = IoBuffer.allocate(length);
			buffer.put(_protocol);
			buffer.flip();
			out.write(buffer);
		}
	}
}