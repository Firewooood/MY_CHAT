package com.wxz.server.user;

import com.wxz.common.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author:WuXiangZhong
 * @Description: 服务器管理 用户功能
 * @Date: Create in 2022/7/24
 */

@Component("userManager")
@Slf4j
public class UserManager {
    // 可登录的用户表
    private Map<String, User> users;
    // key 是ip和端口号,value是用户名  在线用户
    private Map<SocketChannel,String> onlineUsers;

    public UserManager(){
        users = new ConcurrentHashMap<>();
        users.put("user1", User.builder().username("user1").password("pwd1").build());
        users.put("user2", User.builder().username("user2").password("pwd2").build());
        users.put("user3", User.builder().username("user3").password("pwd3").build());
        users.put("user4", User.builder().username("user4").password("pwd4").build());
        users.put("user5", User.builder().username("user5").password("pwd5").build());
        onlineUsers = new ConcurrentHashMap<>();
    }

    public synchronized boolean login(SocketChannel channel, String username, String password){
        // 1.未存储该用户名时,登陆失败
        if(!users.containsKey(username)){
            return false;
        }
        User user = users.get(username);
        // 2.当输入的password 和存储的password不一致时,登录失败
        if(!user.getPassword().equals(password)){
            return false;
        }
        // 3.以登录过的用户,再次登录会失败
        if(user.getChannel() != null){
            log.info("重复登录,拒绝");
            return false;
        }
        // 正常登录
        user.setChannel(channel);
        onlineUsers.put(channel, username);
        return true;
    }

    public synchronized void logout(SocketChannel channel){
        String username = onlineUsers.get(channel);
        log.info("{}下线",username);
        // 将下线用户的channel置空
        users.get(username).setChannel(null);
        onlineUsers.remove(channel);
    }

    public synchronized SocketChannel getUserChannel(String username){
        User user = users.get(username);
        if(user == null)return null;
        SocketChannel channel = user.getChannel();
        if(onlineUsers.containsKey(channel)){
            return channel;
        }
        return null;
    }
}
