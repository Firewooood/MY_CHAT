package com.wxz.server;

import java.util.Scanner;

/**
 * @Author: WuXiangZhong
 * @Description:    服务器启动主函数
 * @Date: Create in 2022/7/24
 */
public class ChatServerMain {
    public static final String QUIT = "EXIT"; // 键入exit后退出
    public static void main(String[] args) {
        System.out.println("Initialing...");
        ChatServer chatServer = new ChatServer();
        chatServer.launch();
        Scanner scanner = new Scanner(System.in, "UTF-8");
        while(scanner.hasNext()){
            String next = scanner.next();
            if(next.equalsIgnoreCase(QUIT)){
                System.out.println("服务器准备关闭");
                chatServer.shutdownServer();
                System.out.println("服务器已关闭");
            }
        }
    }
}
