package com.w4p.telegram.controller;

import com.w4p.telegram.annotation.W4TelegramBot;
import com.w4p.telegram.annotation.W4TelegramCommand;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.User;

@W4TelegramBot
public class TelegramBotController {

    @W4TelegramCommand(value = "/test", description = "It is a test method")
    public SendMessage test(User user, String argument) {
        System.out.println("Incoming message from: " + user.getId()
                + " data: " + argument);


        return new SendMessage()
                .setChatId(user.getId().longValue())
                .setText("Hi "+user.getFirstName() +"! Your send me data: \"" + argument + "\"");
    }
}
