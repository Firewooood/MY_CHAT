package com.wxz.server.handler.message.impl;

import com.wxz.common.domain.Message;
import com.wxz.common.domain.Task;
import com.wxz.server.handler.message.MessageHandler;
import com.wxz.server.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
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

    }
}
