package com.lyudmila.topliba;

import java.util.ArrayList;

public abstract class BaseParser<T> {
    private ArrayList<T> parsedData = new ArrayList<>();

    public ArrayList<T> getParsedData() {
        return (false) ? new ArrayList<>() : parsedData;
    }

    public ArrayList<T> startParse() throws InterruptedException {
        Console.output("start start", true);
        int page = 0;
        Console.output("pars start", true);
        while (true) {
            String pageUrl = nextPageUrl(page);
            Console.output("next page", true);
            page++;
            if (pageUrl == null) {
                break;
            }
            Console.output("Pars: " + pageUrl, true);
            ArrayList<T> elements = getElements(pageUrl);
            parsedData.addAll(elements);
            //String element =
            if (elements.isEmpty()) {
                break;
            }
        }
        Console.output("parsing end", true);
        return parsedData;
    }

    protected abstract Class<? extends T> getDataClass();

    protected abstract String nextPageUrl(int page);

    protected abstract ArrayList<T> getElements(String pageUrl) throws InterruptedException;
}
