package com.wxz.server.exception.handler;

import com.wxz.common.domain.Message;
import com.wxz.common.domain.Response;
import com.wxz.common.domain.ResponseHeader;
import com.wxz.common.enumeration.ResponseType;
import com.wxz.common.util.ProtoStuffUtil;
import com.wxz.server.property.PromptMsgProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @Author:WuXiangZhong
 * @Description: 线程被打断时的处理函数, 返回response
 * @Date: Create in 2022/7/24
 */

@Component("interruptedExceptionHandler")
public class InterruptedExceptionHandler {
    public void handle(SocketChannel channel, Message message) {
        try {
            byte[] response = ProtoStuffUtil.serialize(
                    new Response(
                            ResponseHeader.builder()
                                    .type(ResponseType.PROMPT)
                                    .sender(message.getHeader().getSender())
                                    .timestamp(message.getHeader().getTimestamp()).build(),
                            PromptMsgProperty.SERVER_ERROR.getBytes(PromptMsgProperty.charset)));
            channel.write(ByteBuffer.wrap(response));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
