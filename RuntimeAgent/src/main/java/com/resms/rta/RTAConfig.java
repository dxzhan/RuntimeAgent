package com.resms.rta;

import org.springframework.context.annotation.Configuration;

/**
 * RTA配置类
 *
 * @author sam 
 */
@Configuration
//@EnableAsync
//public class RTAConfig implements AsyncConfigurer {
public class RTAConfig {
    /**
     * 获取异步线程池执行对象
     * @return
     */
//    @Override
//    public Executor getAsyncExecutor() {
//        //使用Spring内置线程池任务对象
//        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
//        //设置线程池参数
//        taskExecutor.setCorePoolSize(1);
//        taskExecutor.setMaxPoolSize(1);
//        taskExecutor.setQueueCapacity(25);
//        taskExecutor.initialize();
//        return taskExecutor;
//    }
//
//    @Override
//    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
//        return null;
//    }
}