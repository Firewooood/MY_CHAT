package com.wxz.server.handler.message.impl;

import com.wxz.common.domain.Message;
import com.wxz.common.domain.Response;
import com.wxz.common.domain.ResponseHeader;
import com.wxz.common.enumeration.ResponseCode;
import com.wxz.common.enumeration.ResponseType;
import com.wxz.common.util.ProtoStuffUtil;
import com.wxz.server.handler.message.MessageHandler;
import com.wxz.server.property.PromptMsgProperty;
import com.wxz.server.user.UserManager;
import lombok.extern.slf4j.Slf4j;
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
 * @Description: 退出登录的处理器
 * @Date: Create in 2022/7/26
 */

@Component("MessageHandler.logout")
@Slf4j
public class LogoutMessageHandler extends MessageHandler {
    @Autowired
    private UserManager userManager;

    @Override
    public void handle(Message message, Selector server, SelectionKey client, AtomicInteger onlineUsers) {
        try {
            SocketChannel clientChannel = (SocketChannel) client.channel();
            userManager.logout(clientChannel);
            byte[] response = ProtoStuffUtil.serialize(
                    new Response(ResponseHeader.builder()
                            .type(ResponseType.PROMPT)
                            .responseCode(ResponseCode.LOGOUT_SUCCESS.getCode())
                            .sender(message.getHeader().getSender())
                            .timestamp(message.getHeader().getTimestamp()).build(),
                            PromptMsgProperty.LOGOUT_SUCCESS.getBytes(PromptMsgProperty.charset)));
            // 发送消息给客户端
            clientChannel.write(ByteBuffer.wrap(response));
            // eg: Response(header=ResponseHeader(sender=user1, type=PROMPT, responseCode=3, timestamp=1658806331779), body=[????])
            onlineUsers.decrementAndGet();  // 在线用户数减一

            log.info("客户端退出");
            // 必须要cancel,否则无法从keys中去除该客户端
            client.cancel();    // 取消SelectionKey及它选择器的注册
            clientChannel.close();
            clientChannel.socket().close();

            // 下线广播
            // 客户端关闭后
            byte[] logoutBroadcast = ProtoStuffUtil.serialize(
                    new Response(
                            ResponseHeader.builder()
                                    .type(ResponseType.NORMAL)
                                    .sender(SYSTEM_SENDER)
                                    .timestamp(message.getHeader().getTimestamp()).build(),
                            String.format(PromptMsgProperty.LOGOUT_BROADCAST, message.getHeader().getSender()).getBytes(PromptMsgProperty.charset)));
            super.broadcast(logoutBroadcast, server);
            // eg:Response(header=ResponseHeader(sender=系统提示, type=NORMAL, responseCode=null, timestamp=1658807835473), body=[????])

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
