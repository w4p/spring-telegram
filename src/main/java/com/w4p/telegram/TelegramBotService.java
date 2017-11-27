package com.w4p.telegram;

import com.w4p.telegram.annotation.TelegramCommand;
import com.w4p.telegram.config.TelegramBotBuilder;
import com.w4p.telegram.model.TelegramBotCommand;
import com.w4p.telegram.model.TelegramHandler;
import com.w4p.telegram.model.TelegramMessageCommand;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.generics.LongPollingBot;
import org.telegram.telegrambots.generics.WebhookBot;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
public class TelegramBotService {

    private String username;
    private String token;
    private String path;

    private TelegramBotsApi api;

    private final Map<String, TelegramHandler> commandList = new LinkedHashMap<>();
    private Executor botExecutor;
    private TelegramHandler defaultMessageHandler;

    private DefaultAbsSender client;

    public TelegramBotService(TelegramBotBuilder botBuilder) {
        log.info("Build TelegramBot: {}", botBuilder);

        this.username = botBuilder.getUsername();
        this.token = botBuilder.getToken();
        this.path = botBuilder.getPath();

        this.api = new TelegramBotsApi();
        try {
            if (botBuilder.getType() == TelegramBotBuilder.BotType.LONG_POLLING) {
                this.botExecutor = Executors.newFixedThreadPool(
                    (botBuilder.getMaxThreads() > 0) ? botBuilder.getMaxThreads():100);

                this.client = new TelegramBotLongPollingImpl();
                api.registerBot((LongPollingBot) this.client);
            } else if (botBuilder.getType() == TelegramBotBuilder.BotType.WEBHOOK) {
                this.client = new TelegramBotWebHooksImpl();
                api.registerBot((WebhookBot) this.client);
            }
        } catch (TelegramApiException e) {
            log.error("Error while creating TelegramBotsApi: {}", e.getMessage());
        }
    }

    public void updateLongPolling(Update update) {
        CompletableFuture.runAsync(() -> {
            BotApiMethod result = updateProcess(update);
            if (result != null) {
                try {
                    this.client.execute(result);
                } catch (TelegramApiException e) {
                    log.error("TelegramBotService: {}", e.getMessage());
                }
            }
        }, botExecutor);
    }

    public BotApiMethod updateProcess(Update update) {
        log.debug("Update received: {}", update);
        try {
            if (update.getMessage() != null) {
                TelegramMessageCommand command = getCommand(update.getMessage().getText());
                TelegramHandler commandHandler = this.defaultMessageHandler;
                if (command.isCommand()) {
                    commandHandler = this.commandList.get(command.getCmd());
                }

                if (commandHandler != null) {
                    Object[] arguments = makeArgumentList(commandHandler.getMethod(), command, update);
                    if (commandHandler.getTelegramCommand() != null && commandHandler.getTelegramCommand().isHelp()) {
                        sendHelpList(update);
                    } else if (commandHandler.getMethod().getGenericReturnType().equals(Void.TYPE)) {
                        //Void method
                        commandHandler.getMethod().invoke(commandHandler.getBean(), arguments);
                    } else if (commandHandler.getMethod().getGenericReturnType().equals(SendMessage.class)) {
                        //SendMessage
                        SendMessage sendMessage = (SendMessage) commandHandler.getMethod()
                            .invoke(commandHandler.getBean(), arguments);
                        return sendMessage;
                    }
                }
            }
        } catch (IllegalAccessException e) {
            log.error("TelegramBotService: {}", e.getMessage());
        } catch (InvocationTargetException e) {
            log.error("TelegramBotService: {}",e.getMessage());
        } catch (TelegramApiException e) {
            log.error("TelegramBotService: {}", e.getMessage());
        }
        return null;
    }

    private Object[] makeArgumentList(Method method, TelegramMessageCommand telegramMessageCommand, Update update) {
        Type[] commandArguments = method.getGenericParameterTypes();
        List<Object> arguments = new ArrayList<>();
        for (Type type : commandArguments) {
            if (type.equals(Update.class)) {
                arguments.add(update);
            } else if (type.equals(TelegramMessageCommand.class)) {
                arguments.add(telegramMessageCommand);
            } else if (type.equals(String.class)) {
                arguments.add(telegramMessageCommand.getArgument());
            } else if (type.equals(TelegramBotsApi.class)) {
                arguments.add(this.api);
            } else if (type.equals(TelegramBotService.class)) {
                arguments.add(this);
            } else if (type.equals(DefaultAbsSender.class)) {
                arguments.add(this.client);
            }
        }

        return arguments.toArray(new Object[arguments.size()]);
    }

    private void sendHelpList(Update update) throws TelegramApiException {
        this.client.execute(new SendMessage()
            .setChatId(update.getMessage().getChatId())
            .setText(buildHelpMessage())
        );
    }

    private String buildHelpMessage() {
        StringBuilder sb = new StringBuilder();
        getCommandList().stream().forEach((method) -> {
            sb.append(method.getCommand())
                    .append(" ")
                    .append(method.getDescription())
                    .append("\n");
        });
        return sb.toString();
    }

    public List<TelegramBotCommand> getCommandList() {
        return this.commandList.entrySet().stream()
            .filter(entry -> !entry.getValue().getTelegramCommand().hidden())
            .map((entry) -> new TelegramBotCommand(entry.getKey(), entry.getValue().getTelegramCommand().description()))
            .collect(Collectors.toList());
    }

    public DefaultAbsSender getClient() {
        return this.client;
    }

    private TelegramMessageCommand getCommand(String msg) {
        return new TelegramMessageCommand(msg);
    }

    public void addHandler(Object bean, Method method) {
        TelegramCommand command = method.getAnnotation(TelegramCommand.class);
        for (String cmd: command.value()) {
            this.commandList.put(cmd, new TelegramHandler(bean, method, command));
        }
    }

    public void addMessageHandler(Object bean, Method method) {
        this.defaultMessageHandler = new TelegramHandler(bean, method, null);
    }



    public class TelegramBotLongPollingImpl extends TelegramLongPollingBot {

        @Override
        public void onUpdateReceived(Update update) {
            TelegramBotService.this.updateLongPolling(update);
        }

        @Override
        public String getBotUsername() {
            return username;
        }

        @Override
        public String getBotToken() {
            return token;
        }
    }

    public class TelegramBotWebHooksImpl extends TelegramWebhookBot {

        @Override
        public BotApiMethod onWebhookUpdateReceived(Update update) {
            return TelegramBotService.this.updateProcess(update);
        }

        @Override
        public String getBotUsername() {
            return username;
        }

        @Override
        public String getBotToken() {
            return token;
        }

        @Override
        public String getBotPath() {
            return path;
        }
    }

}
