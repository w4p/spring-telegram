# spring-telegram
TelegramBot for Spring Framework with multithread async support.

## Repo
```xml
<dependency>
    <groupId>com.github.w4p</groupId>
    <artifactId>spring-telegram</artifactId>
    <version>0.9.1</version>
</dependency>
```

## Setup project

### 1) Create TelegramBot
Please create one Telegram Bot by the [link](https://telegram.me/botfather)  

### 2) Init Spring project

Init SpringBootApplication and enable TelegramBot support **`@EnableW4TelegramBot`**. 
Setup you **`"[botusername]"`** and **`[token]`** . 
```java
@SpringBootApplication
@EnableW4TelegramBot
public class ExampleBot extends SpringBootServletInitializer {

    @Bean
    public TelegramBotBuilder telegramBotBuilder() {
        return new TelegramBotBuilder()
                .username("[botusername]")
                .token("[token]");
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ExampleBot.class);
    }


    public static void main(String[] args) {
        SpringApplication.run(ExampleBot.class, args);
    }
}
```

### 3) Add Telegram controller to project

```java
@W4TelegramBot
public class TelegramBotController {

    @W4TelegramCommand(value = "/test", description = "It is a test method with response")
    public SendMessage test(User user, String argument) {
        return new SendMessage()
                .setChatId(user.getId().longValue())
                .setText("Hi "+user.getFirstName() +"! Your send me data: \"" + argument + "\"");
    }
}
```
Well done! We ready to send & recieve commands from Telegram. Full example you can see by the [link](https://github.com/w4p/spring-telegram-example)

## Open source
Spring-Telegram is an open source project distributed under the liberal MIT license. 

## Status
Spring-Telegram now under active development and has pre-release status.
