package com.wxz.server.task;

import com.wxz.common.domain.Task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

/**
 * @Author: WuXiangZhong
 * @Description: 消费者,负责从阻塞队列中取出任务并交给线程池
 * @Date: Create in 2022/7/24
 */
public class TaskManagerThread extends Thread{
    private ExecutorService taskPool;
    private BlockingQueue<Task> taskBlockingQueue;
    //private HttpConnectionManager httpConnectionManager;

    private ExecutorService crawlerPool;

    public TaskManagerThread(BlockingQueue<Task> downloadTaskQueue) {

    }

    public void shutdown() {
        taskPool.shutdown();
        crawlerPool.shutdown();
        Thread.currentThread().interrupt();
    }
}
