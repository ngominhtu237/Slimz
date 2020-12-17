package com.samsung.lookup.utils;

import com.samsung.lookup.model.Word;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Created by tu.nm1 on 16,December,2020
 */
public class HtmlUtils {
    public static Document format(Word word) {
        String result = word.getDetails().replaceAll("_", "'");
        result = result.replaceAll("\\\\r\\\\n", "");
        Document document = Jsoup.parse(result);
        Elements remove_b_tag = document.select("b");
        remove_b_tag.tagName("p");
        Elements add_arrow = document.select("ul > li > ul > li > font > p");
        add_arrow.attr("class", "arrow");

        Elements tmpE1 = document.select("body > i");
        tmpE1.attr("class", "title");

        Elements tmpE2 = document.select("ul > li > ul > li");
        tmpE2.attr("class", "delColon");

        Elements tmpE3 = document.select("ul > li > ul > li > ul > li > p");
        tmpE3.attr("class", "underscore");
        return document;
    }

    public static String createStyleIntro(String intro) {
        return "<html><body><p style=\"color:#ECEFF1 \"><em>" + intro +
                "</em></p>" +
                "</body></html>";
    }
}
