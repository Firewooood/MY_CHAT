package com.wxz.client;

/**
 * @Author: WuXiangZhong
 * @Description: 客户端启动程序
 * @Date: Create in 2022/7/27
 */

public class ChatClientMain {
    public static void main(String[] args) {
        System.out.println("Client Initialing");
        ChatClient client = new ChatClient("Client", 200, 200, 300, 200);
        client.launch();
    }
}
