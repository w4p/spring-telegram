package com.w4p.telegram.model;

import lombok.Getter;

@Getter
public class TelegramBotCommand {
    private String command;
    private String description;

    public TelegramBotCommand(String command, String description) {
        this.command = command;
        this.description = description;
    }
}
