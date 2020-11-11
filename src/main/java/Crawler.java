import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Crawler {

    private static DBManager db;
    private static final String[] ignoreWords = {"по", "на", "в", "над", "под", "за", "перед"};

    public static void main(String[] args) throws IOException, SQLException {
        createIndexTables();
        ArrayList<String> start = new ArrayList<String>();
        //start.add("https://habr.com/ru/post/527334/");
        start.add("https://account.habr.com/login/?state=c25f4225cce5d75a4b44fe8af7296700&consumer=habr&hl=ru_RU");
        crawl(start, 2);
    }

    public static boolean addToIndex(String url, Document html) {
        return true;
    }

    public static String getTextOnly(String text) {
        return "";
    }

    public static void separateWords(String text) {

    }

    public static boolean isIndexed(String url) throws SQLException {
        HashMap<String, Object> linkFilter = new HashMap<String, Object>();
        linkFilter.put("url", url);
        ResultSet savedLink = db.selectFiltered("urllist", linkFilter);
        if (savedLink.next()) {
            HashMap<String, Object> wordFilter = new HashMap<String, Object>();
            linkFilter.put("urlid", savedLink.getInt("id"));
            ResultSet words = db.selectFiltered("wordlocation", wordFilter);
            return words.next();
        }
        return false;
    }

    public static void addLinkRef(String from, String to, String link) throws SQLException {
        int fromId = getEntryId("urllist", "url", from, false);
        int toId = getEntryId("urllist", "url", to, false);
        db.addLinkBetweenUrl(fromId, toId, link);
    }

    public static void crawl(ArrayList<String> pages, int depth) throws IOException, SQLException {
        for (int i = 0; i < depth; i++) {
            ArrayList<String> newPagesSet = new ArrayList<String>();
            for (int j = 0; j < pages.size(); j++) {
                String page = pages.get(j);
                Document doc = Jsoup.connect(page).get();
                addToIndex(page, doc);
                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String href = link.attributes().get("href");
                    String text = link.text();
                    if (href.contains("https://") && !href.contains("social") && isValid(text)) {
                        if (!isIndexed(href)) {
                            newPagesSet.add(href);
                        }
                        addLinkRef(page, href, text);
                    }
                }
            }
            pages = newPagesSet;
        }
    }

    private static void createIndexTables() {
        db = new DBManager();
        /*try {
            ResultSet resultRows = db.selectFrom("urllist");
            while (resultRows.next()) {
                System.out.println("url->"+resultRows.getString("url"));
            }
            resultRows = db.selectFrom("linkurl");
            while (resultRows.next()) {
                System.out.println(resultRows.getString("content"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }*/
    }

    public static int getEntryId(String table, String field, String value, boolean createNew) throws SQLException {
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put(field, value);
        ResultSet savedLink = db.selectFiltered(table, filter);
        if (savedLink.next()) {
            return savedLink.getInt("id");
        }
        return -1;
    }

    private static boolean isValid(String linkText) {
        for (int i = 0; i < linkText.length(); i++) {
            if (Character.isLetter(linkText.charAt(i))) return true;
        }
        return !linkText.contains("https") && !linkText.equals("");
    }
}
