package com.wxz.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author:WuXiangZhong
 * @Description:
 * @Date: Create in 2022/7/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private MessageHeader header;
    private byte[] body;
}
