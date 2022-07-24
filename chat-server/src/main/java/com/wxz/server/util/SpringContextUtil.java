package com.wxz.server.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jsoup.select.Evaluator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Author:WuXiangZhong
 * @Description:
 * @Date: Create in 2022/7/24
 */
@Slf4j
public final class SpringContextUtil {
    // Spring应用上下文环境
    private static ApplicationContext applicationContext;

    // 调用SpringContextUtil 方法 或 生成SpringContextUtil对象时,会初始化applicationContext
    static{
        applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
    }

    private SpringContextUtil(){

    }

    public static <T> T getBean(String beanId){
        T bean = null;
        try {
            if(StringUtils.isNotEmpty(StringUtils.trim(beanId))){
                bean = (T)applicationContext.getBean(beanId);
            }
        } catch (NoSuchBeanDefinitionException e) {
            log.error("获取bean失败");
            return null;
        }
        return bean;
    }


    // 形参为可变长度参数
    public static <T> T getBean(String... partName){
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < partName.length; i++){
            sb.append(partName[i]);
            if( i != partName.length - 1){
                sb.append(".");
            }
        }
        return getBean(sb.toString());

    }
}
