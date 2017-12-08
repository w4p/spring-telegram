package com.w4p.telegram;

import com.w4p.telegram.annotation.W4TelegramBot;
import com.w4p.telegram.annotation.W4TelegramCommand;
import com.w4p.telegram.annotation.W4TelegramMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TelegramBeanProcessor implements BeanPostProcessor, Ordered {

    private TelegramBotService telegramBotService;

    private Map<String, Class> botControllerMap = new HashMap<>();

    public TelegramBeanProcessor(TelegramBotService telegramBotService) {
        this.telegramBotService = telegramBotService;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(W4TelegramBot.class)) {
            log.info("Init W4TelegramBot controller: {}", bean.getClass().getName());
            botControllerMap.put(beanName, bean.getClass());
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(!botControllerMap.containsKey(beanName)) return bean;

        Class original = botControllerMap.get(beanName);

        Arrays.stream(original.getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(W4TelegramCommand.class) || method.isAnnotationPresent(W4TelegramMessage.class))
            .forEach((Method method) -> bindController(bean, method));

        return bean;
    }

    private void bindController(Object bean, Method method) {
        if (method.getAnnotation(W4TelegramCommand.class) != null) {
            log.info("Init W4TelegramBot command: {}:{}", method.getAnnotation(W4TelegramCommand.class), method.getName());
            this.telegramBotService.addHandler(bean, method);
        } else if (method.getAnnotation(W4TelegramMessage.class) != null) {
            log.info("Init W4TelegramBot default message handler: {}:{}", method.getAnnotation(W4TelegramMessage.class), method.getName());
            this.telegramBotService.addMessageHandler(bean, method);
        }
        this.telegramBotService.addHelpMethod();
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
