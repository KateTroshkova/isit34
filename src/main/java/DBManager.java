import java.sql.*;

public class DBManager {

    public static void main(String[] args) {

        String fileName = "java_SQLite.db"; // имя файла для хранения БД
        String db_url = "jdbc:sqlite:" + fileName; // формируемая строка для подключения к локальному файлу

        try {
            Connection conn = DriverManager.getConnection(db_url); // открыть соединение с БД в локальном файле

            /** conn.setAutoCommit(true | false);
             *  Управление режимом AutoCommit БД, при включенном AutoCommit
             *  после выполнения каждого запроса БД сама фиксирует (commit) изменения.
             */
            conn.setAutoCommit(true); // включить режим автоматической фиксации (commit) изменений БД


            if (conn != null) {
                // Если соединение открыто успешно
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("Используемый JDBC-драйвер: " + meta.getDriverName());
                System.out.println("Файл " + fileName + " создан");

                // Получить Statement для того, чтобы выполнить sql-запрос
                Statement statement = conn.createStatement();


                // Удаление прошлой версии таблицы из БД --------------------------------------------------------------

                // сформировать SQL запрос
                String sqlDropTable = "DROP TABLE IF EXISTS wordlist;";

                // выполнить SQL запрос
                statement.execute(sqlDropTable);
                System.out.println("Таблица удалена.");


                // Создания таблицы wordlist в БД ---------------------------------------------------------------------
                // Сформировать SQL запрос
                String sqlCreateTable = "CREATE TABLE IF NOT EXISTS wordlist ( \n "
                        + "    rowid INTEGER PRIMARY KEY AUTOINCREMENT,  -- первичный ключ\n"
                        + "    word TEXT NOT NULL, -- слово\n"
                        + "    isFiltred INTEGER NOT NULL -- флаг фильтрации\n"
                        + ");";

                // Выполнить SQL запрос
                statement.execute(sqlCreateTable);
                System.out.println("Таблица создана.");


                // Добавить строки. Способ 1. По одной записи  --------------------------------------------------------
                statement.execute("INSERT INTO wordlist (word, isFiltred) VALUES ('Четверг', 0)");
                statement.execute("INSERT INTO wordlist (word, isFiltred) VALUES ('Пятница', 0)");
                System.out.println("Строки добавлены в таблицу.");

                // Сохранить (commit) изменения в БД ------------------------------------------------------------------
                //conn.commit(); // необходим только в режиме conn.setAutoCommit(false); иначе препятствует работе. (см. выше)


                // Получение данных из БД и вывод в консоль ----------------------------------------------------------
                System.out.println("\n===SQL=== 1. Получение данных из БД. Вывод всех элементов таблицы wordlist =========================================");
                // Сформировать SQL запрос
                String sqlSelect = "SELECT rowid, word, isFiltred FROM wordlist";
                // Выполнить SQL запрос и созранить ответ
                ResultSet resultRows = statement.executeQuery(sqlSelect);

                // Обход каждого элемента в полученном объекте
                while (resultRows.next()) {
                    System.out.println(
                            resultRows.getInt("rowid") + "\t" +
                                    resultRows.getString("word") + "\t" +
                                    resultRows.getInt("isFiltred"));
                }


                System.out.println("\n===SQL=== 2. Получение данных из БД. Запрос строк таблицы wordlist подходящих под условие ==========================");
                String searchedWord = "Пятница";
                // Сформировать SQL запрос
                String sqlSelectCondition = String.format( "SELECT rowid, word, isFiltred FROM wordlist where word='%s'" , searchedWord);

                System.out.println(sqlSelectCondition);
                // Выполнить SQL запрос и созранить ответ
                ResultSet resultRowsConditions = statement.executeQuery(sqlSelectCondition);


                // Обход каждого элемента в полученном объекте
                while (resultRowsConditions.next()) {
                    System.out.println(
                            resultRowsConditions.getInt("rowid") + "\t" +
                                    resultRowsConditions.getString("word") + "\t" +
                                    resultRowsConditions.getInt("isFiltred"));
                }



            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
