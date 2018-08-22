package com.jasonren.seckill.result;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    private Result(T data) {
        this.code = 0;
        this.msg = "success";
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(data);
    }

}
