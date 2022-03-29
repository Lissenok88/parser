package com.lyudmila.topliba;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class ToplibaParser extends BaseParser<BookInformation> {
    private static final String urlBase = "https://topliba.com/?q=";
    private boolean needStop;
    private static String key;
    private int positionCounter = 1;

    public static List<BookInformation> parser(String message) {
        ArrayList<BookInformation> list = new ArrayList<>();
        try {
            ToplibaParser parser = new ToplibaParser();
            key = message.replace(" ", "%20");
            Console.output(key, true);
            list = new ArrayList<>(parser.startParse());
            Console.output("pars end", true);
            Console.output("Parsing good", true);
            Console.output("list size - " + list.size(), true);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            Console.output(ex.getMessage(), true);
        }
        return list;
    }

    public String nextPageUrl(int page) {
        page++;
        String result = urlBase + key + "&p=" + page;
        System.out.println("Parsing page: \r\n" + result + "\r\n");
        return result;
    }

    protected ArrayList<BookInformation> getElements(String pageUrl) {
        if (positionCounter > 50) {
            needStop = true;
        }
        if (needStop) {
            return new ArrayList<>();
        }
        int position = positionCounter;
        Document doc = HttpConnector.getHtml(pageUrl);
        Elements elements = doc.getElementsByClass("media-body");
        List<BookInformation> element = elements.stream().map(q -> {
            BookInformation bookInformation = new BookInformation();
            Element titleElement1 = q.getElementsByClass("book-title").first();
            if (titleElement1 != null) {
                String element1 = "";
                if (titleElement1 != null)
                    element1 = titleElement1.getElementsByTag("a").text();
                Element titleElement2 = q.getElementsByClass("book-author").first();
                String element2 = "";
                if (titleElement2 != null)
                    element2 = titleElement2.getElementsByTag("a").text();
                bookInformation.setPosition(positionCounter++);
                bookInformation.setTitle(element1 + " - " + element2);
                String url = titleElement1.attr("href");
                bookInformation.setUrl(url);
                Console.output(bookInformation.getPosition() + ". " + bookInformation.getTitle() + ": " +
                        bookInformation.getUrl(), true);
            }
            return bookInformation;
        }).toList();
        element = element.stream().filter(q -> q.getPosition() > 0).toList();
        return (position != positionCounter) ? new ArrayList<>(element) : new ArrayList<>();
    }

    protected static String convertData(ArrayList<BookInformation> elements, String message) {
        String result = "Поиск по запросу: " + message + "\n\r";
        for (BookInformation element : elements) {
            result = result + element.getPosition() + ". " + element.getTitle() + "\n\r";
            Console.output(element.getPosition() + " " + element.getTitle() + "\n\r", true);
        }
        Console.output(result, true);
        return result;
    }

    protected static BookInformation fillElements(String url) {
        BookInformation bookInformation = new BookInformation();
        Document doc = HttpConnector.getHtml(url, (response) -> {
            if (response.statusCode == 200) {
                return true;
            }
            Console.input("need bun");
            return false;
        });
        Console.output(url);
        bookInformation.setTitle(doc.getElementsByClass("book-title").first().text());
        bookInformation.setDescription(doc.getElementsByClass("description").first().text());
        String dange = doc.getElementsByClass("alert-danger").text();
        if (dange != "") {
            bookInformation.setFragment(dange + " \n\r Данный файл скачать невозможно.");
            bookInformation.setUrlFb2("");
        } else {
            String el = doc.getElementsByClass("alert-info").text();
            int index = el.indexOf("Доступен");
            if (index != -1) {
                bookInformation.setFragment("Скачать ознакомительный фрагмент");
            } else bookInformation.setFragment("Скачать файл полностью");
            String urlFb2 = doc.getElementsByAttributeValue("rel", "nofollow").first().attr("href");
            if (urlFb2 != null) {
                if (!urlFb2.contains("http"))
                    urlFb2 = "https://topliba.com" + urlFb2;
            } else
                urlFb2 = "";
            bookInformation.setUrlFb2(urlFb2);
        }
        Console.output(bookInformation.getTitle() + "\n\r" + bookInformation.getDescription() + "\n\r" +
                        bookInformation.getFragment() + "\n\r" + bookInformation.getUrlFb2() + "\n\r",
                true);
        Console.output("parsing number end", true);
        return bookInformation;
    }

    @Override
    protected Class<? extends BookInformation> getDataClass() {
        return BookInformation.class;
    }
}
