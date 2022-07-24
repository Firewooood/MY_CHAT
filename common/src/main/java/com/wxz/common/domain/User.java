package com.wxz.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.channels.SocketChannel;

/**
 * @Author:WuXiangZhong
 * @Description:
 * @Date: Create in 2022/7/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder  // 使用该注解后,可以链式书写 为对象赋值.
public class User {
    private String username;
    private String password;
    private SocketChannel channel;
}
