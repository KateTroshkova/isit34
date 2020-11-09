import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

public class PageParser {

    public static void main(String[] args) {


        String URL = "http://lenta.ru/"; // URL адрес страницы
        try {

            Document html_doc = Jsoup.connect(URL).get(); // # получить HTML код страницы и разобрать его на элементы

            // Найти и удалить на странице блоки со скриптами и стилями оформления ('script', 'style')
            html_doc.select("script, style").remove();

            System.out.println("\n# Код страницы ------------------------------------------------------------------------------");
            System.out.println(html_doc.outerHtml());

            System.out.println("\n# Тест страницы -------------------------------------------------------------------");
            System.out.println(html_doc.text());

            System.out.println("\n# Заголовок -----------------------------------------------------------------------");
            System.out.println(html_doc.title());


            // Вывести отдельные элементы из объекта
            System.out.println("\n# Код страницы --------------------------------------------------------------------");
            System.out.println(html_doc.outerHtml());

            System.out.println("\n# Тест страницы -------------------------------------------------------------------");
            System.out.println(html_doc.text());

            System.out.println("\n# Заголовок -----------------------------------------------------------------------");
            System.out.println(html_doc.title());


            System.out.println("\n# Поиск элементов по тегу 'a' -----------------------------------------------------");
            Elements links = html_doc.getElementsByTag("a");
            for (Element link : links) {
                String linkHref = link.attr("href");
                String linkText = link.text();
                System.out.println("    " + linkText + " " + linkHref);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}