import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.bot.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BotTest {

    private BotTask bot;

    @BeforeEach
    public void initTest() {
        bot = new BotTask("javaTaskBot", "src/main/resources/cities.txt", "src/main/resources/aboutme.txt");
    }

    @Test
    public void testCheckName() {
        assertEquals("javaTaskBot", bot.getBotUsername());
    }

    @Test
    public void testCheckTownOnMap() {
        assertTrue(BotTask.checkTownOnMap("Екатеринбург"));
        assertFalse(BotTask.checkTownOnMap("Коноха"));
    }

    @Test
    public void testCheckLetter() {
        bot.currUser = new User("123", "Vasya",0, true);
        assertTrue(bot.checkLetter("Москва"));
        bot.currUser.lastLetter = "а";
        assertTrue(bot.checkLetter("Архангельск"));
        assertFalse(bot.checkLetter("Екатеринбург"));
    }

    @Test
    public void testCheckCityName() {
        assertTrue(BotTask.checkCityName("Ашхабад"));
        assertFalse(BotTask.checkCityName("Aшхабад"));
        assertFalse(BotTask.checkCityName("Ашхaбад"));
        assertFalse(BotTask.checkCityName("Ашхабад 9"));
        assertFalse("A".equals("А"));
    }

    @Test
    public void testAnswersSet() {
        bot.currUser = new User("123", "Vasya",0, true);
        bot.currUser.answers.add("москва");
        assertTrue(bot.checkUserAnswers("Москва"));
        assertFalse(bot.checkUserAnswers("Екатеринбург"));
    }

    @Test
    public void testNextCity() {
        bot.currUser = new User("123", "Vasya",0, true);
        assertEquals(bot.getNextCity("Москва"), "Агуадульче");
        bot.currUser.counterForCities.replace("ю", 24);
        bot.getNextCity("Ю");
        assertEquals(bot.getNextCity("Ю"), "У меня закончились города на букву ю");
        String city = bot.getNextCity("Пермь");
        assertTrue(city.matches("^М.*"));
    }

    @Test
    public void testFileReader() throws FileNotFoundException {
        Scanner scan = new Scanner(new File("src/main/resources/cities.txt"), "utf-8");
        List<String> cities = bot.getCities();
        int i = 0;
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            assertEquals(line, cities.get(i));
            i++;
        }
        scan.close();
    }

    @Test
    public void testFileReaderInString() throws FileNotFoundException {
        Scanner scan = new Scanner(new File("src/main/resources/aboutme.txt"), "utf-8");
        String answers = ReadFile.readFileInString(new File("src/main/resources/aboutme.txt"));
        String res = "";
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            res = res + line;
            if (scan.hasNextLine()) {
                res += "\n";
            }
        }
        scan.close();
        assertEquals(answers, res);
    }

}