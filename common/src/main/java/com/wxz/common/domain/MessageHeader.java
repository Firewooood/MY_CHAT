package com.wxz.common.domain;

import com.wxz.common.enumeration.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
public class MessageHeader {
    private String sender;
    private String receiver;
    private MessageType type;
    private Long timestamp;
}
