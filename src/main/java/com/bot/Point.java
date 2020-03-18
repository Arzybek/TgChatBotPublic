package com.bot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Point {

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            botsApi.registerBot(new BotTask("javaTaskBot", "src/main/resources/cities.txt", "src/main/resources/aboutme.txt"));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
