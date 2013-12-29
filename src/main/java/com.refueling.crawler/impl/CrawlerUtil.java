package com.refueling.crawler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

class CrawlerUtil {
    private static final Logger logger = LoggerFactory.getLogger(CrawlerUtil.class);
    private Map<String, Object> config;

    CrawlerUtil() {
        config = initConfig();
        checkNotNull(config, "Config initialization failed");
    }

    private Map<String, Object> initConfig() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream in = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("com.refueling.crawler/impl/config.json")) {
            if (in != null) {
                return mapper.readValue(in, Map.class);
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException("failed to load essential configuration");
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

    public static String read(HttpEntity reportsResp) throws IOException {
        return EntityUtils.toString(reportsResp, Charsets.UTF_8);
    }

    public String param(final String key) {
        return (String) config.get(key);
    }

    public static UrlEncodedFormEntity buildEncodedEntity(final List<NameValuePair> values) {
        return new UrlEncodedFormEntity(values, Consts.UTF_8);
    }
}
