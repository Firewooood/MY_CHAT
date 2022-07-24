package com.wxz.server;

import com.wxz.common.domain.Task;
import com.wxz.server.task.TaskManagerThread;
import com.wxz.server.exception.handler.InterruptedExceptionHandler;
import com.wxz.server.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author:WuXiangZhong
 * @Description: 聊天服务器类
 *      使用NIO, 非阻塞式IO, 一个线程请求写入一些数据到某通道，但不需要等待它完全写入，这个线程同时可以去做别的事情
 * @Date: Create in 2022/7/24
 */

@Slf4j
public class ChatServer {
    public static final int DEFAULT_BUFFER_SIZE = 1024*1024;
    public static final int PORT = 8888;

    private AtomicInteger onlineUsers;

    private ServerSocketChannel serverSocketChannel;    // 可从channel中读写数据,但是读写要先经过一个buffer
    private Selector selector;  // 单线程使用selector可以处理多个channel

    private ExecutorService readPool;   //线程池
    private BlockingQueue<Task> downloadTaskQueue;
    private TaskManagerThread taskManagerThread;
    private ListenerThread listenerThread;
    private InterruptedExceptionHandler exceptionHandler;

    /**
     * 推荐的结束线程的方式是使用中断
     * 在while循环处开始检查是否中断,并提供一个方法将自己中断,不在外部将线程中断
     * 若中断一个阻塞在某个地方的线程,最好继承自Thread,先关闭所依赖的资源,再关闭当前线程.
     */
    private class ListenerThread extends Thread{
        @Override
        public void interrupt(){
            try {
                try {// selector.close(); 关闭Selector本身,并将所有的SelectionKey失效,但不会关闭Channel
                    selector.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } finally {
                super.interrupt();
            }
        }

        @Override
        public void run(){
            // 若有一个及以上的客户端数据准备就绪
            try {
                while (!Thread.currentThread().isInterrupted()){
                    // 当注册的事件到达时,方法返回,否则该方法会一直阻塞

                    // 获取当前选择其中所有注册的监听事件
                    selector.select();
                    for(Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ){
                        SelectionKey key = it.next();
                        it.remove(); // 删除已选的key,防止重复处理
                        if(key.isAcceptable()){     // 如果"接收" 事件已就绪
                            handleAcceptRequest();  // 交由接收事件的处理器处理
                        }else if(key.isReadable()){ // 如果"读取事件已就绪"
                            // 取消可读触发标记,本次处理完后打开读取事件标记   key.interestOps() & (~SelectionKey.OP_READ)操作将OP_READ取消
                            key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
                            readPool.execute(new ReadEventHandler(key));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void shutdown(){
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 处于线程池中的线程会随着线程池的shutdown方法而关闭
     */
    private class ReadEventHandler implements Runnable {
        private ByteBuffer buf;
        private SocketChannel client;
        private ByteArrayOutputStream baos;
        private SelectionKey key;

        public ReadEventHandler(SelectionKey key) {
            this.key = key;
            this.client = (SocketChannel) key.channel();
            this.buf = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
            this.baos = new ByteArrayOutputStream();
        }

        @Override
        public void run() {

        }
    }

    public ChatServer(){
        log.info("服务器启动");
        initServer();
    }

    private void initServer(){
        try {
            serverSocketChannel = ServerSocketChannel.open();   // ServerSocketChannel对象类是抽象的,不能直接new实例化,使用静态方法open() 实例化该类
            // 切换为非阻塞模式, 和selector一起使用时,channel必须处于非阻塞模式

            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(PORT));

            // 获得选择器
            selector = Selector.open();
            // 将channel注册到selector上            interest集合，意思是在通过Selector监听Channel时对什么事件感兴趣,OP_ACCEPT 表示接受就绪
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            // 初始化类变量
            //           核心线程数为5,最大线程数为10,救急线程生存时间为1000ms,阻塞队列大小为10, 拒绝策略为 (如果任务被拒绝了,由调用线程直接执行该任务)
            this.readPool = new ThreadPoolExecutor(5,10,1000, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(10), new ThreadPoolExecutor.CallerRunsPolicy());
            this.downloadTaskQueue = new ArrayBlockingQueue<>(20);
            this.taskManagerThread = new TaskManagerThread(downloadTaskQueue);
            this.taskManagerThread.setUncaughtExceptionHandler(SpringContextUtil.getBean("taskExceptionHandler"));
            this.listenerThread = new ListenerThread();
            this.onlineUsers = new AtomicInteger(0);
            this.exceptionHandler = SpringContextUtil.getBean("interruptedExceptionHandler");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动方法，线程最好不要在构造函数中启动，应该作为一个单独方法，或者使用工厂方法来创建实例
     * 避免构造未完成就使用成员变量
     */
    public void launch(){
        new Thread(listenerThread).start();
        new Thread(taskManagerThread).start();
    }

    /**
     * 关闭服务器
     */
    public void shutdownServer(){
        try {
            taskManagerThread.shutdown();
            listenerThread.shutdown();
            readPool.shutdown();
            serverSocketChannel.close();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理客户端的连接请求
     */
    private void handleAcceptRequest(){

    }
}
