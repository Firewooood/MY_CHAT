package com.wxz.server.handler.message.impl;

import com.wxz.common.domain.*;
import com.wxz.common.enumeration.ResponseType;
import com.wxz.common.util.ProtoStuffUtil;
import com.wxz.server.handler.message.MessageHandler;
import com.wxz.server.property.PromptMsgProperty;
import com.wxz.server.user.UserManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: WuXiangZhong
 * @Description: 聊天室内给用户发送消息  eg: @user1:hello
 * @Date: Create in 2022/7/26
 */
@Component("MessageHandler.normal")
@Slf4j
public class NormalMessageHandler extends MessageHandler {

    @Autowired
    private UserManager userManager;

    @Override
    public void handle(Message message, Selector server, SelectionKey client, AtomicInteger onlineUsers) throws InterruptedException {
        try {
            SocketChannel clientChannel = (SocketChannel) client.channel();
            MessageHeader header = message.getHeader();
            SocketChannel receiverChannel = userManager.getUserChannel(header.getReceiver());   // 获取接收方的socketChannel
            if(receiverChannel == null){
                // 接收者不在线, 向发送信息的用户端回复消息
                byte[] response = ProtoStuffUtil.serialize(
                        new Response(
                                ResponseHeader.builder()
                                        .type(ResponseType.PROMPT) // 系统提示信息
                                        .sender(message.getHeader().getSender())
                                        .timestamp(message.getHeader().getTimestamp()).build(),
                                PromptMsgProperty.RECEIVER_LOGGED_OFF.getBytes(PromptMsgProperty.charset)));
                clientChannel.write(ByteBuffer.wrap(response));
            }else{
                // 接收者在线的情况
                byte[] response = ProtoStuffUtil.serialize(
                        new Response(
                                ResponseHeader.builder()
                                        .type(ResponseType.NORMAL) // 正常发送消息
                                        .sender(message.getHeader().getSender())
                                        .timestamp(message.getHeader().getTimestamp()).build(),
                                message.getBody()));
                log.info("已转发给{}",receiverChannel);
                receiverChannel.write(ByteBuffer.wrap(response));
                // 给自己也发一份
                clientChannel.write(ByteBuffer.wrap(response));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
