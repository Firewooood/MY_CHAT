package com.wxz.server.test;

import com.wxz.common.domain.Message;
import com.wxz.common.domain.MessageHeader;
import com.wxz.common.enumeration.MessageType;
import com.wxz.common.util.ProtoStuffUtil;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * @Author: WuXiangZhong
 * @Description:
 * @Date: Create in 2022/7/26
 */
@Slf4j
public class ClientTest {
    public static void main(String[] args) throws IOException {
        try {
            //获取通道
            SocketChannel socketChannel = SocketChannel.open();
            //绑定主机的ip端口号
            socketChannel.connect(new InetSocketAddress("127.0.0.1", 8000));
            //设置非阻塞模式
            socketChannel.configureBlocking(false);
            //创建buffer
            ByteBuffer writeBuffer = ByteBuffer.allocate(32);
            ByteBuffer readBuffer = ByteBuffer.allocate(32);
            //给buffer写入数据
            writeBuffer.put("hello".getBytes());
            //模式切换
            writeBuffer.flip();
            while (true) {
                writeBuffer.rewind();
                //写入通道数据
                socketChannel.write(writeBuffer);
                //关闭
                readBuffer.clear();
                socketChannel.read(readBuffer);
            }
        } catch (IOException e) {

        }
    }
}
