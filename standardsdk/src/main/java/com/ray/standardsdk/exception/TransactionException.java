package com.ray.standardsdk.exception;

/**
 * @author ray
 * @date 2023/8/8 10:12
 */
public class TransactionException extends UnsupportedOperationException{
    public TransactionException(Throwable cause, String msg, Object... args){
        super(String.format(msg, args), cause);
    }
}
