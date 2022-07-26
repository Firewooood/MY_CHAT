package com.wxz.server.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @Author: WuXiangZhong
 * @Description:
 * @Date: Create in 2022/7/26
 */
public class ServerTest {

    public static void main(String[] args) {
        try {
            //获取服务端通道
            ServerSocketChannel ssc = ServerSocketChannel.open();
            //绑定服务端ip端口号
            ssc.socket().bind(new InetSocketAddress("127.0.0.1", 8000));
            //切换非阻塞模式
            ssc.configureBlocking(false);

            //获取selector选择器
            Selector selector = Selector.open();
            // 将服务端channel注册到选择器上，并且指定感兴趣的事件是 Accept
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            //创建读/写缓冲区
            ByteBuffer readBuff = ByteBuffer.allocate(1024);
            ByteBuffer writeBuff = ByteBuffer.allocate(128);

            //写入数据
            writeBuff.put("received".getBytes());
            //切换读写模式
            writeBuff.flip();

            while (true) {
                int nReady = selector.select();
                //获取就绪状态集合
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                while (it.hasNext()) {
                    //获取到就绪状态
                    SelectionKey key = it.next();
                    it.remove();

                    //判断是什么状态，对对应操作进行对应处理
                    if (key.isAcceptable()) {
                        // 创建新的连接，并且把连接注册到 selector 上，而且，声明这个 channel 只对读操作感兴趣。
                        SocketChannel socketChannel = ssc.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        readBuff.clear();
                        socketChannel.read(readBuff);
                        readBuff.flip();
                        System.out.println("received : " + new String(readBuff.array()));
                        key.interestOps(SelectionKey.OP_WRITE);
                    } else if (key.isWritable()) {
                        writeBuff.rewind();
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        socketChannel.write(writeBuff);
                        key.interestOps(SelectionKey.OP_READ);
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

