import java.sql.*;
import java.util.Map;

public class DBManager {

    private static final String FILE_NAME = "isit34_db";
    private static final String LOCAL_PATH = "D:\\sqlite\\";
    private static final String DB_URL = "jdbc:sqlite:" + LOCAL_PATH + FILE_NAME;
    private static final String CREATE_URL_LIST = "create table if not exists urllist ( " +
            "id integer primary key autoincrement," +
            "url text not null" +
            ");";
    private static final String CREATE_WORD_LIST = "create table if not exists wordlist ( " +
            "id integer primary key autoincrement," +
            "word text not null," +
            "isFiltered integer not null" +
            ");";
    private static final String CREATE_WORD_LOCATION = "create table if not exists wordlocation ( " +
            "id integer primary key autoincrement," +
            "wordid integer not null," +
            "urlid integer not null," +
            "location integer not null" +
            ");";
    private static final String CREATE_LINK_URL = "create table if not exists linkurl ( " +
            "id integer primary key autoincrement," +
            "fromurl integer not null," +
            "tourl integer not null" +
            ");";
    private static final String CREATE_LINK_WORD = "create table if not exists linkword ( " +
            "id integer primary key autoincrement," +
            "wordid integer not null," +
            "linkid integer not null" +
            ");";
    private static final String DROP_URL_LIST = "drop table if exists urllist;";
    private static final String DROP_WORD_LIST = "drop table if exists wordlist;";
    private static final String DROP_WORD_LOCATION = "drop table if exists wordlocation;";
    private static final String DROP_LINK_URL = "drop table if exists linkurl;";
    private static final String DROP_LINK_WORD = "drop table if exists linkword;";

    private static Connection conn;

    public DBManager() {
        try {
            conn = DriverManager.getConnection(DB_URL);
            conn.setAutoCommit(true);
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("Используемый JDBC-драйвер: " + meta.getDriverName());
            System.out.println("Файл " + FILE_NAME + " создан");
            Statement statement = conn.createStatement();
            recreateTables(statement);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public ResultSet selectFiltered(String tableName, Map<String, Object> filter) {
        try {
            Statement statement = conn.createStatement();
            StringBuilder request = new StringBuilder("select * from " + tableName + " where ");
            for (String key : filter.keySet()) {
                Object value = filter.get(key);
                if (value instanceof String) {
                    request.append(key).append("=").append("'").append(filter.get(key)).append("' and ");
                } else {
                    request.append(key).append("=").append(filter.get(key)).append(" and ");
                }
            }
            request = new StringBuilder(request.substring(0, request.length() - 5));
            request.append(";");
            return statement.executeQuery(request.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResultSet selectFrom(String tableName) {
        try {
            Statement statement = conn.createStatement();
            String request = "select * from " + tableName + ";";
            return statement.executeQuery(request);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addUrl(String url) {
        Statement statement = null;
        try {
            statement = conn.createStatement();
            statement.execute("INSERT INTO urllist (url) VALUES ('" + url + "');");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addWord(String word, int isFiltered) {
        Statement statement = null;
        try {
            statement = conn.createStatement();
            statement.execute(
                    "INSERT INTO wordlist (word, isFiltered) VALUES ('" + word + "'," + isFiltered + ");"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addWordLocation(int wordId, int urlId, int location) {
        Statement statement = null;
        try {
            statement = conn.createStatement();
            statement.execute(
                    "INSERT INTO wordlocation (wordid, urlid, location) VALUES " +
                            "(" + wordId + "," + urlId + "," + location + ");"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addLinkBetweenUrl(int fromUrl, int toUrl) {
        try {
            Statement statement = conn.createStatement();
            statement.execute(
                    "INSERT INTO linkurl (fromurl, tourl) VALUES " +
                            "(" + fromUrl + "," + toUrl + ");"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addWordLink(int wordId, int linkId) {
        Statement statement = null;
        try {
            statement = conn.createStatement();
            statement.execute(
                    "INSERT INTO linkword (wordid, linkid) VALUES " +
                            "(" + wordId + "," + linkId + ");"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void dropTables(Statement statement) {
        try {
            statement.execute(DROP_URL_LIST);
            statement.execute(DROP_WORD_LIST);
            statement.execute(DROP_WORD_LOCATION);
            statement.execute(DROP_LINK_URL);
            statement.execute(DROP_LINK_WORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Таблица удалена.");
    }

    private static void recreateTables(Statement statement) {
        dropTables(statement);
        try {
            statement.execute(CREATE_URL_LIST);
            statement.execute(CREATE_WORD_LIST);
            statement.execute(CREATE_WORD_LOCATION);
            statement.execute(CREATE_LINK_URL);
            statement.execute(CREATE_LINK_WORD);
            System.out.println("Таблица создана.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
