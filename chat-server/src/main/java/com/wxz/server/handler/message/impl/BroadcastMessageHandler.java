package com.wxz.server.handler.message.impl;

import com.wxz.common.domain.Message;
import com.wxz.common.domain.Response;
import com.wxz.common.domain.ResponseHeader;
import com.wxz.common.enumeration.ResponseType;
import com.wxz.common.util.ProtoStuffUtil;
import com.wxz.server.handler.message.MessageHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: WuXiangZhong
 * @Description: 处理 在聊天室中未@user, 群发的消息
 * @Date: Create in 2022/7/26
 */
@Component("MessageHandler.broadcast")
public class BroadcastMessageHandler extends MessageHandler {
    @Override
    public void handle(Message message, Selector server, SelectionKey client, AtomicInteger onlineUsers) {
        try {
            byte[] response = ProtoStuffUtil.serialize(
                    new Response(
                            ResponseHeader.builder()
                                    .type(ResponseType.NORMAL)
                                    .sender(message.getHeader().getSender())
                                    .timestamp(message.getHeader().getTimestamp()).build(),
                            message.getBody()));
            super.broadcast(response,server);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
