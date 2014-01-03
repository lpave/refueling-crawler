package com.refueling.crawler.impl;

import com.google.common.base.Charsets;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

public class CrawlerUtil {
    private Config config;

    public CrawlerUtil() {
        config = ConfigFactory.load();
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
        return config.getString(key);
    }

    public static UrlEncodedFormEntity buildEncodedEntity(final List<NameValuePair> values) {
        return new UrlEncodedFormEntity(values, Consts.UTF_8);
    }
}
