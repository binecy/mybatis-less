package com.binecy.util;

public class MybatisLessException extends RuntimeException {
    public MybatisLessException(String msg) {
        super(msg);
    }

    public MybatisLessException(String msg, Throwable e) {
        super(msg, e);
    }
}
