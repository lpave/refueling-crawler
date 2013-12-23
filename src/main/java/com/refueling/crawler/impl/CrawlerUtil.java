package com.refueling.crawler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class CrawlerUtil {
    private static final Logger logger = LoggerFactory.getLogger(CrawlerUtil.class);
    private Map<String, Object> config;

    public CrawlerUtil() {
        init();
    }

    private void init() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            InputStream in = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("com/refueling/crawler/impl/config.json");
            config = mapper.readValue(in, Map.class);
        } catch (IOException e) {
            logger.error("Json configuration initialization failed:", e);
            throw new RuntimeException();
        }
    }

    public static BasicNameValuePair buildPair(final String name, final String value) {
        return new BasicNameValuePair(name, value);
    }

    public static BasicNameValuePair nameValuePair(final Document document, final String name) {
        return new BasicNameValuePair(name, value(document, name));
    }

    public static String value(final Document document, final String id) {
        return document.getElementById(id).val();
    }

    public String param(final String key) {
        return (String) config.get(key);
    }
}
