package com.bot;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class BotTask extends TelegramLongPollingBot {

    private String name;
    private String about;
    private long currentChatId;
    public User currUser;
    private HashMap<String, User> dictUsers = new HashMap<>();
    private List<String> cities;
    private HashMap<String, ArrayList<String>> citiesDict = new HashMap<>();

    public BotTask(String name, String cities, String about) {
        this.name = name;
        this.about = ReadFile.readFileInString(new File(about));
        this.cities = ReadFile.readFileInList(new File(cities));
        this.initializeCities();
    }

    public List<String> getCities() {
        return this.cities;
    }

    public HashMap<String, ArrayList<String>> getCitiesDict() {
        return this.citiesDict;
    }

    private void sendMsg(Message msg, String text) {
        SendMessage s = new SendMessage();
        s.setChatId(msg.getChatId());
        s.setText(text);
        try {
            execute(s);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkTownOnMap(String town) {
        String check = "";
        try {
            URL url = new URL("https://geo.koltyrin.ru/goroda_poisk.php?city=" + town);
            URLConnection conn = url.openConnection();
            conn.connect();

            Document doc = Jsoup.connect(String.valueOf(url)).get();
            Elements scriptElements = doc.getElementsByTag("b");
            for (Element element : scriptElements) {
                check = element.toString();
            }
            return check.equals("");
        } catch (MalformedURLException e) {
            System.out.println("Invalid url");
        } catch (IOException e) {
            System.out.println("Error connection");
        }
        return false;
    }

    public void initializeCities() {
        for (char symb = 'а'; symb <= 'я'; symb++) {
            String convert = String.valueOf(symb);
            this.citiesDict.put(convert, new ArrayList<>());
        }
        for (String city : cities) {
            String firstLetter = String.valueOf(city.toLowerCase().charAt(0));
            citiesDict.get(firstLetter).add(city);
        }
    }

    @Override
    public String getBotUsername() {
        return this.name;
    }

    @Override
    public String getBotToken() {
        return "";
    }

    public String getNextCity(String city) {
        String lastLetter = String.valueOf(city.toLowerCase().charAt(city.length() - 1));
        if (lastLetter.equals("ь") || lastLetter.equals("ъ")) {
            lastLetter = String.valueOf(city.toLowerCase().charAt(city.length() - 2));
        }
        Integer index = this.currUser.counterForCities.get(lastLetter);
        if (index > citiesDict.get(lastLetter).size() - 1) {
            String str = "У меня закончились города на букву " + lastLetter;
            return str;
        } else {
            String nextCity = citiesDict.get(lastLetter).get(index);
            index = index + 1;
            this.currUser.counterForCities.put(lastLetter, index);
            return nextCity;
        }
    }

    public void checkUser(long chat_id, String username) {
        if (chat_id == this.currentChatId) {

        } else {
            this.currentChatId = chat_id;
            String chat_id_str = Long.toString(chat_id);
            User user = this.dictUsers.get(chat_id_str);
            if (user != null) {
                this.currUser = user;
            } else {
                User usr = new User(chat_id_str, username, -1, false);
                this.dictUsers.put(chat_id_str, usr);
                this.currUser = usr;
            }
        }
    }

    private void resetUser() {
        this.currUser.countOfCities = 0;
        this.currUser.flag = false;
        this.currUser.answers = new HashSet<>();
        this.currUser.initializeCounters();
        this.currUser.lastLetter = null;
    }

    private void endGame(Message msg) {
        String str = "Поздравляю, ты победил меня. Ты набрал " + this.currUser.countOfCities + "очков";
        sendMsg(msg, str);
        resetUser();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message msg = update.getMessage();
            String txt = msg.getText();
            long chat_id_num = msg.getChatId();
            String username = msg.getChat().getUserName();
            checkUser(chat_id_num, username);
            if (txt.equals("/start")) {
                sendMsg(msg, "Привет, меня зовут бот " + name + "!");
                showHelp(msg);
                this.currUser.setTrue();
                this.currUser.countOfCities = 0;
                this.currUser.answers = new HashSet<>();
                String hello = "Поиграем? Напиши любой город:";
                sendMsg(msg, hello);
            } else if (txt.equals("/stop")) {
                String score = Integer.toString(this.currUser.countOfCities);
                String finalMess = "Уже уходишь? Ты назвал " + score + " городов!";
                sendMsg(msg, finalMess);
                resetUser();
            } else if (txt.equals("/help")) {
                sendMsg(msg, this.about);
            } else if (txt.equals("/stats")) {
                if (!this.currUser.flag) {
                    String messageStat = "Начните игру, чтобы посмотреть количество верных ответов";
                    sendMsg(msg, messageStat);
                } else {
                    String score = Integer.toString(this.currUser.countOfCities);
                    String finalMess = "Пока ты ответил " + score + " городов";
                    sendMsg(msg, finalMess);
                }
            } else if (this.currUser.flag) {
                String city = update.getMessage().getText();
                if (!checkCityName(city)) {
                    String str = "Некорректный ввод.";
                    sendMsg(msg, str);
                } else if (checkTownOnMap(city) && !checkUserAnswers(city)) {
                    if (checkLetter(city)) {
                        String answerCity = getNextCity(city);
                        if (answerCity.matches("У меня закончились города на букву.+")) {
                            sendMsg(msg, answerCity);
                            endGame(msg);
                        } else {
                            sendMsg(msg, answerCity);
                            this.currUser.incrementCities();
                            this.currUser.answers.add(city.toLowerCase());
                            String lastLetter = String.valueOf(answerCity.charAt(answerCity.length() - 1)).toLowerCase();
                            if (lastLetter.equals("ь") || lastLetter.equals("ъ")) {
                                lastLetter = String.valueOf(answerCity.toLowerCase().charAt(answerCity.length() - 2)).toLowerCase();
                            }
                            this.currUser.lastLetter = lastLetter;
                        }
                    } else {
                        String str = "Нужно назвать город на последнюю букву!";
                        sendMsg(msg, str);
                    }
                } else if (!checkTownOnMap(city)) {
                    boolean flag = false;
                    for (String cityIn : cities) {
                        if (cityIn == city) {
                            flag = true;
                        }
                    }
                    if (!flag) {
                        String str = "Такого города не существует, по крайней мере, я его не знаю!";
                        sendMsg(msg, str);
                    }
                } else if (checkUserAnswers(city)) {
                    String str = "Ты так уже отвечал!";
                    sendMsg(msg, str);
                }
            }
        }
    }

    public boolean checkLetter(String city) {
        String lastLetter = String.valueOf(city.charAt(0)).toLowerCase();
        if (this.currUser.lastLetter == null) {
            return true;
        } else if (this.currUser.lastLetter.equals(lastLetter)) {
            return true;
        } else return false;
    }

    private void showHelp(Message msg) {
        sendMsg(msg, this.about);
    }

    public static boolean checkCityName(String city) {
        if (city.matches("(.*[a-z]+.*)") || city.matches("(.*[A-Z]+.*)") || city.matches("(.*[0-9]+.*)")) {
            return false;
        } else return true;
    }

    public boolean checkUserAnswers(String inAns) {
        return this.currUser.answers.contains(inAns.toLowerCase());
    }
}
