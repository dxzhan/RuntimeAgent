package com.resms.rta.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RTA自定义线程工厂类
 *
 * @author sam
 */
public class RTAThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolCounter = new AtomicInteger(1);
    private final AtomicInteger threadCounter = new AtomicInteger(1);
    private final String prefix;
    private final boolean daemon;
    private final ThreadGroup threadGroup;

    public RTAThreadFactory() {
        this("RTAPool-" + poolCounter.getAndIncrement(),false);
    }

    public RTAThreadFactory(String prefix) {
        this(prefix,false);
    }

    public RTAThreadFactory(String prefix, boolean daemon) {
        this.prefix = prefix;
        this.daemon = daemon;
        SecurityManager s = System.getSecurityManager();
        this.threadGroup = ( s == null ) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(threadGroup,r, prefix + threadCounter.getAndIncrement(),0);
        thread.setDaemon(daemon);
        return thread;
    }

    public ThreadGroup getThreadGroup()
    {
        return threadGroup;
    }
}