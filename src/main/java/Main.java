import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args){
        ArrayList<String> start = new ArrayList<String>();
        start.add("https://habr.com/ru/post/527334/");
        try {
            Crawler crawler = new Crawler();
            crawler.crawl(start, 2);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
