package com.wxz.server.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author:WuXiangZhong
 * @Description: UncaughtExceptionHandler异常处理器可以处理ExecutorService通过execute方法提交的线程中抛出的RuntimeException
 *
 * @Date: Create in 2022/7/24
 */

@Component("taskExceptionHandler")
@Slf4j
public class TaskExceptionHandler implements Thread.UncaughtExceptionHandler{
    @Override
    public void uncaughtException(Thread t, Throwable e) {

    }
}