package com.resms.lightsentinel.common;

/**
 * LightSentinel异常类
 *
 * @author sam
 */
public class LightSentinelException extends RuntimeException{
    public LightSentinelException(String message) {
        super(message);
    }

    public LightSentinelException(Throwable cause) {
        super(cause);
    }
}