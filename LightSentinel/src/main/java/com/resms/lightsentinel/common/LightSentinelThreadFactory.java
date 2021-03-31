package com.resms.lightsentinel.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.LongAdder;

/**
 * LightSentinel自定义线程工厂类
 *
 * @author sam
 */
public class LightSentinelThreadFactory implements ThreadFactory {
    private static final LongAdder poolCounter = new LongAdder();
    private final LongAdder nextId = new LongAdder();
    private static final String DEFAULT_PREFIX = "LightSentinelPool-";
    private final String prefix;
    private final boolean daemon;
    private final ThreadGroup threadGroup;
    public LightSentinelThreadFactory() {
        this(DEFAULT_PREFIX + poolCounter.intValue(),false);
    }

    public LightSentinelThreadFactory(String prefix) {
        this(prefix,false);
    }

    public LightSentinelThreadFactory(String prefix, boolean daemon) {
        this.prefix = prefix;
        this.daemon = daemon;
        SecurityManager s = System.getSecurityManager();
        this.threadGroup = ( s == null ) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable r) {
        nextId.increment();
        Thread thread = new Thread(threadGroup,
                r,
                prefix + nextId.intValue(),
                0);
        thread.setDaemon(daemon);
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        return thread;
    }

    public ThreadGroup getThreadGroup()
    {
        return threadGroup;
    }
}