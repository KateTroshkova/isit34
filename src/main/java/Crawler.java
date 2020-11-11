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

    private static final String[] ignoreWords = {
            "в", "без", "до", "из", "к", "на", "по", "о", "от", "перед", "при", "через", "с", "у", "за", "над", "об",
            "под", "про", "для", "вблизи", "вглубь", "вдоль", "возле", "около", "вокруг", "впереди", "после",
            "посредством", "путём", "насчёт", "а", "абы", "аж", "ан", "благо", "буде", "будто", "вроде", "да", "дабы",
            "даже", "едва", "ежели", "если", "же", "затем", "зато", "и", "ибо", "или", "итак", "кабы", "как", "когда",
            "коли", "коль", "ли", "либо", "лишь", "нежели", "но", "пока", "покамест", "покуда", "поскольку", "притом",
            "причем", "пускай", "пусть", "раз", "разве", "ровно", "сиречь", "словно", "так", "также", "тоже", "только",
            "точно", "хоть", "хотя", "чем", "чисто", "что", "чтоб", "чтобы", "чуть", "якобы"
    };
    private static final int MAX_LINK_COUNT = 2;
    private static final int MIN_WORD_LENGTH = 10;
    private int urlCount = 0;
    private int wordCount = 0;
    private int locationCount = 0;
    private int linkCount = 0;
    private int wordsInLinks = 0;
    private DBManager db;

    public Crawler() {
        createIndexTables();
    }

    public void crawl(ArrayList<String> pages, int depth) throws IOException, SQLException {
        for (int i = 0; i < depth; i++) {
            ArrayList<String> newPagesSet = new ArrayList<String>();
            for (String page : pages) {
                Document doc = Jsoup.connect(page).get();
                addToIndex(page, doc);
                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    if (newPagesSet.size() < MAX_LINK_COUNT) {
                        String href = link.attributes().get("href");
                        String text = link.text();
                        if (href.contains("https://") &&
                                !href.contains("comment") &&
                                !href.contains("facebook") &&
                                !href.contains("vk") &&
                                !href.contains("twitter") &&
                                isValid(text)) {
                            if (!isIndexed(href)) {
                                newPagesSet.add(href);
                            }
                            addLinkRef(page, href, text);
                        }
                    }
                }
            }
            pages = newPagesSet;
        }
    }

    public void addToIndex(String url, Document html) throws SQLException {
        if (isIndexed(url)) return;
        String[] words = separateWords(getTextOnly(html));
        int urlId = getEntityId(DBManager.DB_URL_LIST, "url", url, true);
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!isIgnored(word)) {
                int wordId = getEntityId(DBManager.DB_WORD_LIST, "word", word, true);
                db.addWordLocation(wordId, urlId, i);
                locationCount++;
            }
        }
    }

    public String getTextOnly(Document doc) {
        return doc.body().text();
    }

    public String[] separateWords(String text) {
        return text.split(" ");
    }

    public boolean isIndexed(String url) throws SQLException {
        int urlId = getEntityId(DBManager.DB_URL_LIST, "url", url, false);
        if (urlId > 0) {
            int wordId = getEntityId(DBManager.DB_LOCATION, "urlid", urlId, false);
            return wordId > 0;
        }
        return false;
    }

    public void addLinkRef(String from, String to, String link) throws SQLException {
        int fromId = getEntityId(DBManager.DB_URL_LIST, "url", from, true);
        int toId = getEntityId(DBManager.DB_URL_LIST, "url", to, true);
        int linkId = getEntityId(DBManager.DB_LINK, "fromurl", fromId, "tourl", toId, true);
        String[] words = separateWords(link);
        for (String word : words) {
            int wordId = getEntityId(DBManager.DB_WORD_LIST, "word", word, true);
            db.addWordLink(wordId, linkId);
            wordsInLinks++;
        }
    }

    private void createIndexTables() {
        db = new DBManager();
    }

    public int getEntityId(
            String table,
            String field,
            Object value,
            boolean createNew
    ) throws SQLException {
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put(field, value);
        ResultSet result = db.selectFiltered(table, filter);
        if (result.next()) {
            return result.getInt("id");
        }
        if (createNew) {
            if (table.equals(DBManager.DB_URL_LIST)) {
                db.addUrl(value.toString());
                urlCount++;
                if (urlCount % 10 == 0) print();
            }
            if (table.equals(DBManager.DB_WORD_LIST)) {
                db.addWord(value.toString(), 0);
                wordCount++;
                if (wordCount % 100 == 0) print();
            }
            return getEntityId(table, field, value, false);
        }
        return -1;
    }

    public int getEntityId(
            String table,
            String field1,
            int value1,
            String field2,
            int value2,
            boolean createNew
    ) throws SQLException {
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put(field1, value1);
        filter.put(field2, value2);
        ResultSet result = db.selectFiltered(table, filter);
        if (result.next()) {
            return result.getInt("id");
        }
        if (createNew) {
            db.addLinkBetweenUrl(value1, value2);
            linkCount++;
            return getEntityId(table, field1, value1, field2, value2, false);
        }
        return -1;
    }

    private boolean isValid(String linkText) {
        return !linkText.equals("");
    }

    private static boolean isIgnored(String word) {
        for (int i = 0; i < word.length(); i++) {
            if (Character.isDigit(word.charAt(i))) return true;
        }
        if (word.contains("https")) return true;
        for (String ignoreWord : ignoreWords) {
            if (word.equals(ignoreWord)) return true;
        }
        return word.length() < MIN_WORD_LENGTH;
    }

    private void print() {
        System.out.println("url size = " + urlCount +
                " words size = " + wordCount +
                " locations size = " + locationCount +
                " links size = " + linkCount +
                "words in links = " + wordsInLinks
        );
    }
}
