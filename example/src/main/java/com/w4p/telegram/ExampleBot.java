package com.w4p.telegram;

import com.w4p.telegram.annotation.EnableW4TelegramBot;
import com.w4p.telegram.config.TelegramBotBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableW4TelegramBot
public class ExampleBot extends SpringBootServletInitializer {

    @Bean
    public TelegramBotBuilder telegramBotBuilder() {
        return new TelegramBotBuilder()
                .username("SpringTeleBot")
                .token("473338456:AAG8Dy9XgRRDbzu7kCjzmXzL3LpL7ZN6d6M");
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ExampleBot.class);
    }


    public static void main(String[] args) {
        SpringApplication.run(ExampleBot.class, args);
    }
}
