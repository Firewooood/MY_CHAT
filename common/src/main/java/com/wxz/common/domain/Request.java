package com.wxz.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @Author:WuXiangZhong
 * @Description:
 * @Date: Create in 2022/7/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    private String url;
    private Map<String,String> params;
}
