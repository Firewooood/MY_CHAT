package com.wxz.server.handler.message.impl;

import com.wxz.common.domain.*;
import com.wxz.common.enumeration.ResponseCode;
import com.wxz.common.enumeration.ResponseType;
import com.wxz.common.util.ProtoStuffUtil;
import com.wxz.server.handler.message.MessageHandler;
import com.wxz.server.property.PromptMsgProperty;
import com.wxz.server.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: WuXiangZhong
 * @Description:
 * @Date: Create in 2022/7/25
 */
@Component("MessageHandler.login")
public class LoginMessageHandler extends MessageHandler {
    @Autowired
    private UserManager userManager;

    @Override
    public void handle(Message message, Selector server, SelectionKey client, BlockingQueue<Task> queue, AtomicInteger onlineUsers) throws InterruptedException {
        // 获取和客户端连接的SocketChannel
        SocketChannel clientChannel = (SocketChannel) client.channel();
        MessageHeader header = message.getHeader();
        String username = header.getSender();
        String password = new String(message.getBody(), PromptMsgProperty.charset);
        try{
            // 调用login处理逻辑
            if(userManager.login(clientChannel, username, password)){
                // 登录成功
                byte[] response = ProtoStuffUtil.serialize(
                        new Response(
                                ResponseHeader.builder()
                                    .type(ResponseType.PROMPT)
                                    .sender(username)
                                    .timestamp(header.getTimestamp())
                                    .responseCode(ResponseCode.LOGIN_SUCCESS.getCode()).build(),
                                String.format(PromptMsgProperty.LOGIN_SUCCESS,onlineUsers.incrementAndGet()).getBytes(PromptMsgProperty.charset)));
                // 发送给客户端
                // eg: Response(header=ResponseHeader(sender=user1, type=PROMPT, responseCode=1, timestamp=1658805322066), body=[-25, -103, -69, -27, -67, -107, -26, -120, -112, -27, -118, -97, -17, -68, -116, -27, -67, -109, -27, -119, -115, -27, -123, -79, -26, -100, -119, 49, -28, -67, -115, -27, -100, -88, -25, -70, -65, -25, -108, -88, -26, -120, -73]) 登录成功
                clientChannel.write(ByteBuffer.wrap(response));
                // 连续发送信息不可行,需要暂时中断
                // 粘包问题
                Thread.sleep(10);
                // 登录提示 广播给所有的onlineUsers
                byte[] loginBroadcast = ProtoStuffUtil.serialize(
                        new Response(
                                ResponseHeader.builder()
                                        .type(ResponseType.NORMAL)
                                        .sender(SYSTEM_SENDER)
                                        .timestamp(message.getHeader().getTimestamp()).build(),
                                String.format(PromptMsgProperty.LOGIN_BROADCAST, message.getHeader().getSender()).getBytes(PromptMsgProperty.charset)));
                // 广播给selector 上的所有用户,即onlineUsers
                super.broadcast(loginBroadcast, server);
            }else{ //登录失败
                byte[] response = ProtoStuffUtil.serialize(
                        new Response(
                                ResponseHeader.builder()
                                        .type(ResponseType.PROMPT)
                                        .responseCode(ResponseCode.LOGIN_FAILURE.getCode())
                                        .sender(message.getHeader().getSender())
                                        .timestamp(message.getHeader().getTimestamp()).build(),
                                PromptMsgProperty.LOGIN_FAILURE.getBytes(PromptMsgProperty.charset)));
                clientChannel.write(ByteBuffer.wrap(response));
                // eg: Response(header=ResponseHeader(sender=user1, type=PROMPT, responseCode=2, timestamp=1658805931557), body=[???]
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }
}
