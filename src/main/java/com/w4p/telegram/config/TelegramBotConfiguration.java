package com.w4p.telegram.config;

import com.w4p.telegram.TelegramBeanProcessor;
import com.w4p.telegram.TelegramBotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;
import org.telegram.telegrambots.ApiContextInitializer;

@Slf4j
public class TelegramBotConfiguration implements ImportAware {

    @Bean
    public TelegramBeanProcessor telegramBeanProcessor(TelegramBotService telegramBotService) {
        return new TelegramBeanProcessor(telegramBotService);
    }

    @Bean
    public TelegramBotService telegramBotService(TelegramBotBuilder telegramBotBuilder) {
        ApiContextInitializer.init();
        return new TelegramBotService(telegramBotBuilder);
    }

    public void setImportMetadata(AnnotationMetadata importMetadata) {
    }
}
