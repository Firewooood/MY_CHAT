package com.wxz.common.domain;

import com.wxz.common.enumeration.TaskType;
import lombok.AllArgsConstructor;
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
public class Task {
    private SocketChannel receiver;
    private TaskType type;
    private String desc;
    private Message message;
}
