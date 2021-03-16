package com.resms.rta.common;

/**
 * RTA异常类
 *
 * @author sam
 */
public class RTAException extends RuntimeException{
    public RTAException(String message) {
        super(message);
    }

    public RTAException(Throwable cause) {
        super(cause);
    }
}