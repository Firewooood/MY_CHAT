package com.wxz.client;

import com.wxz.common.domain.Message;
import com.wxz.common.domain.MessageHeader;
import com.wxz.common.domain.Response;
import com.wxz.common.domain.ResponseHeader;
import com.wxz.common.enumeration.MessageType;
import com.wxz.common.enumeration.ResponseCode;
import com.wxz.common.util.DateTimeUtil;
import com.wxz.common.util.ProtoStuffUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * @Author: WuXiangZhong
 * @Description:
 * @Date: Create in 2022/7/27
 */
public class ChatClient extends Frame {
    public static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;
    private Selector selector;
    private SocketChannel clientChannel;
    private ByteBuffer buf;
    private TextField tfText;   // 输入框
    private TextArea taContent;  // 文本区 多行文本输入框
    private ReceiverHandler listener;

    private String username;
    private boolean isLogin = false;
    private boolean isConnected = false;
    private Charset charset = StandardCharsets.UTF_8;

    /**
     * 用于接收信息的线程
     */
    private class ReceiverHandler implements Runnable{
        private boolean connected = true;

        @Override
        public void run() {
            try {
                while(connected){
                    int size = 0;
                    selector.select();
                    for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
                        SelectionKey selectionKey = it.next();
                        it.remove();
                        if (selectionKey.isReadable()) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            while ((size = clientChannel.read(buf)) > 0) {
                                buf.flip();
                                baos.write(buf.array(), 0, size);
                                buf.clear();
                            }
                            byte[] bytes = baos.toByteArray();
                            baos.close();
                            Response response = ProtoStuffUtil.deserialize(bytes, Response.class);
                            handleResponse(response);
                        }
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,"服务器关闭,请重新尝试连接");
                isLogin = false;
            }
        }

        // 处理接收到的信息
        private void handleResponse(Response response){
            System.out.println(response);
            ResponseHeader header = response.getHeader();
            switch (header.getType()){
                case PROMPT:
                    if(header.getResponseCode() != null){
                        ResponseCode code = ResponseCode.fromCode(header.getResponseCode());
                        if(code == ResponseCode.LOGIN_SUCCESS){
                            isLogin = true;
                            System.out.println("登录成功");
                        }else if(code == ResponseCode.LOGOUT_SUCCESS){
                            System.out.println("下线成功");
                            break;
                        }
                    }
                    String info = new String(response.getBody(),charset);
                    JOptionPane.showMessageDialog(ChatClient.this, info);
                    break;
                case NORMAL:
                    String content = formatMessage(taContent.getText(), response);  // 将原多行文本输入框的内容和 发送过来的response拼接在一起
                    taContent.setText(content);
                    taContent.setCaretPosition(content.length());
                    break;
                default:
                    break;
            }
        }

        /**
         * 将原多行文本输入框的内容和 发送过来的response拼接在一起
         * @param originalText  原多行文本输入框的内容
         * @param response      发送过来的response信息
         * @return  originalText
         *          user1: say goodbye    2022-07-27 11:00:33
         */
        private String formatMessage(String originalText, Response response){
            ResponseHeader header = response.getHeader();
            StringBuilder sb = new StringBuilder();
            sb.append(originalText)
                    .append(header.getSender())
                    .append(": ")
                    .append(new String(response.getBody(),charset))
                    .append("    ")
                    .append(DateTimeUtil.formatLocalDateTime(header.getTimestamp()))
                    .append("\n");
            return sb.toString();
        }

        public void shutdown(){
            connected = false;
        }

    }

    public ChatClient(String name, int x, int y, int w, int h){
        super(name);
        initFrame(x,y,w,h);
        initNetWork();
    }

    /**
     * 初始化窗体, 聊天框口框, 上方为TextArea, 下方为TextField输入框
     */
    private void initFrame(int x,int y,int w, int h){
        this.tfText = new TextField();
        this.taContent = new TextArea();
        this.setLayout(new BorderLayout());   // 两组件之间没有间隙

        // 重写关闭事件
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);  // 使当前窗口不可见
                disConnect();   // 断开client和server的连接
                System.exit(0);
            }
        });
        this.taContent.setEditable(false);
        this.add(tfText, BorderLayout.SOUTH);   // 输入框在组合框的下部
        this.add(taContent,BorderLayout.NORTH);

        this.tfText.addActionListener((actionEvent) -> {
            String str = tfText.getText().trim();
            tfText.setText("");
//            System.out.println(str);
            send(str);
        });
        this.pack();
        this.setVisible(true);
    }

    /**
     * 初始化网络模块
     */
    private void initNetWork(){
        try {
            selector  = Selector.open();
            clientChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8888));
            clientChannel.configureBlocking(false);
            clientChannel.register(selector,SelectionKey.OP_READ);
            buf = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
            login();
            isConnected = true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "连接服务器失败");
            e.printStackTrace();
        }
    }

    /**
     *  断开和服务器的连接
     */
    private void disConnect() {
        try {
            logout();
            if (!isConnected) {
                // 若已断开连接,则直接返回
                return;
            }
            listener.shutdown();
            //如果发送消息后马上断开连接，那么消息可能无法送达
            Thread.sleep(10);
            // 获取SelectionKey

            clientChannel.socket().close();
            clientChannel.close();
            isConnected = false;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 登录
     */
    private void login(){
        String username = JOptionPane.showInputDialog("请输入用户名");
        String password = JOptionPane.showInputDialog("请输入密码");
        Message message = new Message(
                MessageHeader.builder()
                            .type(MessageType.LOGIN)
                            .sender(username)
                            .timestamp(System.currentTimeMillis())
                            .build(),password.getBytes(charset));
        try {
            clientChannel.write(ByteBuffer.wrap(ProtoStuffUtil.serialize(message)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.username = username;
    }

    /**
     * 登出
     */
    private void logout() {
        if (!isLogin) {
            return;
        }
        System.out.println("客户端发送下线请求");
        Message message = new Message(
                MessageHeader.builder()
                        .type(MessageType.LOGOUT)
                        .sender(username)
                        .timestamp(System.currentTimeMillis())
                        .build(), null);
        try {
            // 向服务器发送下线消息
            clientChannel.write(ByteBuffer.wrap(ProtoStuffUtil.serialize(message)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  发送消息,监听 回车键
     * @param content
     */
    public void send(String content){
        if(!isLogin){
            JOptionPane.showMessageDialog(null,"尚未登录");
            return ;
        }
        try {
            Message message;
            // 1. 普通模式,两用户之间通信, eg: @user2:hello
            if(content.startsWith("@")){
                String[] slices = content.split(":");
                String receiver = slices[0].substring(1);  // 从下标@裁到:
                message = new Message(
                        MessageHeader.builder()
                                .type(MessageType.NORMAL)
                                .sender(username)
                                .receiver(receiver)
                                .timestamp(System.currentTimeMillis())
                                .build(), slices[1].getBytes(charset)); // 消息体为slice[1]
            }else{
            // 2. 广播模式, 向所有用户喊话
                message = new Message(
                        MessageHeader.builder()
                            .type(MessageType.BROADCAST)
                            .sender(username)
                            .timestamp(System.currentTimeMillis())
                            .build(), content.getBytes(charset));
            }
            System.out.println(message);
            clientChannel.write(ByteBuffer.wrap(ProtoStuffUtil.serialize(message)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void launch(){
        this.listener = new ReceiverHandler();
        // 启动接收消息的线程
        new Thread(listener).start();
    }

}
