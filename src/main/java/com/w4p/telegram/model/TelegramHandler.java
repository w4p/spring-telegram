package com.w4p.telegram.model;

import com.w4p.telegram.annotation.W4TelegramCommand;
import lombok.Getter;

import java.lang.reflect.Method;

@Getter
public class TelegramHandler {
    private Object bean;
    private Method method;
    private W4TelegramCommand w4TelegramCommand;

    public TelegramHandler(Object bean, Method method, W4TelegramCommand w4TelegramCommand) {
        this.bean = bean;
        this.method = method;
        this.w4TelegramCommand = w4TelegramCommand;
    }
}
