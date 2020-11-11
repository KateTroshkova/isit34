import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

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

    public static void addToIndex(String url, Document html) throws SQLException {
        if (!isIndexed(url)) return;
        String[] words = separateWords(getTextOnly(html));
        int urlId = getEntityId("urllist", "url", url, true);
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!isIgnored(word)) {
                int wordId = getEntityId("wordlist", "word", word, true);
                db.addWordLocation(wordId, urlId, i);
            }
        }
    }

    public static String getTextOnly(Document doc) {
        return doc.body().text();
    }

    public static String[] separateWords(String text) {
        return text.split(" ");
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
        int fromId = getEntityId("urllist", "url", from, false);
        int toId = getEntityId("urllist", "url", to, false);
        int linkId = getEntityId("linkurl", "fromurl", fromId, "tourl", toId, true);
        String[] words = separateWords(link);
        for(String word:words){
            int wordId = getEntityId("wordlist", "word", word, true);
            db.addWordLink(wordId, linkId);
        }
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

    public static int getEntityId(String table, String field, String value, boolean createNew) throws SQLException {
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put(field, value);
        ResultSet savedLink = db.selectFiltered(table, filter);
        if (savedLink.next()) {
            return savedLink.getInt("id");
        }
        if (createNew) {
            if (table.equals("urllist")) {
                db.addUrl(value);
            } else {
                db.addWord(value, 0);
            }
            return getEntityId(table, field, value, false);
        }
        return -1;
    }

    public static int getEntityId(String table, String field1, int value1, String field2, int value2, boolean createNew) throws SQLException {
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put(field1, value1);
        filter.put(field2, value2);
        ResultSet savedLink = db.selectFiltered(table, filter);
        if (savedLink.next()) {
            return savedLink.getInt("id");
        }
        if (createNew) {
            db.addLinkBetweenUrl(value1, value2);
            return getEntityId(table, field1, value1, field2, value2, false);
        }
        return -1;
    }

    private static boolean isValid(String linkText) {
        for (int i = 0; i < linkText.length(); i++) {
            if (Character.isLetter(linkText.charAt(i))) return true;
        }
        return !linkText.contains("https") && !linkText.equals("");
    }

    private static boolean isIgnored(String word) {
        for (String ignoreWord : ignoreWords) {
            if (word.equals(ignoreWord)) return true;
        }
        return false;
    }
}
