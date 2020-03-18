package com.bot;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CityParser {
    public List<String> cities = new ArrayList<String>();
    public CityParser()
    {
        this.parseCities();
    }
    public static Document parseUrl(String url) {
        Document doc;
        try {
            doc = Jsoup.connect(url).get();
            return doc;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void parseCities() {
        Document doc = parseUrl("https://www.tema.ru/travel/cities/");
        List<String> textCities = doc.getElementsByClass("cols-content").eachText();
        for (int i = 0; i < textCities.size(); i++) {
            String str = textCities.get(i);
            str = str.split(" ")[0];
            textCities.set(i, str);
        }
        List<String> citiesFinal = new ArrayList<String>();
        for (int i = 0; i < textCities.size(); i++) {
            String str = textCities.get(i);
            Pattern p = Pattern.compile("(^[A-Z])|(.+[a-z]+)");
            Matcher m = p.matcher(str);
            if (!m.find()) {
                if (str.length() != 1) {
                    citiesFinal.add(str);
                }
            }
        }
        this.cities = citiesFinal;

        String url2 = "https://gist.githubusercontent.com/gorborukov/0722a93c35dfba96337b/raw/435b297ac6d90d13a68935e1ec7a69a225969e58/russia";
        Document doc2 = parseUrl(url2);
        List<String> jsonCities = doc2.getElementsByTag("body").eachText();
        String jsonRaw = jsonCities.get(0);
        JsonElement obj = new JsonParser().parse(jsonRaw);
        for (JsonElement one : obj.getAsJsonArray()) {
            String str = one.toString();
            int city = str.indexOf("\"city\":");
            city = city + 8;
            String cityStr = str.substring(city);
            cityStr = cityStr.substring(0, cityStr.length() - 2);
            if (cityStr.matches("^[А-Я].+")) {
                cities.add(cityStr);
            }
        }
        HashSet<String> set = new HashSet<String>(this.cities);
        this.cities = new ArrayList<String>(set);
    }

    public static void main(String[] args){
        try {
            CityParser parser = new CityParser();
            FileWriter writer = new FileWriter("data/cities.txt");
            for (String str : parser.cities) {
                writer.write(str);
                writer.write("\n");
            }
            writer.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}