import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class Crawler {

    private static DBManager db;

    public static void main(String[] args) {
        try {
            db = new DBManager();
            db.addUrl("url1");
            db.addUrl("url2");
            db.addUrl("url3");
            db.addUrl("url4");

            ResultSet resultRows = db.selectFrom("urllist");
            while (resultRows.next()) {
                System.out.println(resultRows.getString("url"));
            }

            HashMap<String, Object> filter = new HashMap<String, Object>();
            filter.put("id", 1);
            ResultSet set1 = db.selectFiltered("urllist", filter);
            if (set1 != null) {
                while (set1.next()) {
                    System.out.println(set1.getString("url"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
