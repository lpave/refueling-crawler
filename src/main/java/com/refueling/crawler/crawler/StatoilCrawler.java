package com.refueling.crawler.crawler;

import com.refueling.crawler.model.Refueling;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class StatoilCrawler implements Crawler {
    private static final Logger logger = LoggerFactory.getLogger(StatoilCrawler.class);
    private static final String formLoginUrl = "https://www.statoilwebfuel.com";

    public StatoilCrawler() {

    }

    @Override
    public List<Refueling> getRefuelings() {
        return null;
    }

    @Override
    public boolean checkConnection(final String username, final String password) {
        return false;
    }

    @Override
    public void authenticate(final String username, final String password) throws IOException {
        BasicCookieStore cookieStore = new BasicCookieStore();
        final CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        HttpGet httpGet = new HttpGet(formLoginUrl);
        try {
            CloseableHttpResponse getResponse = httpClient.execute(httpGet);
            try {
                HttpEntity entity = getResponse.getEntity();
                logger.debug("getResponse status: {}", getResponse.getStatusLine());
                EntityUtils.consume(entity);
                if (!cookieStore.getCookies().isEmpty()) {
                    for (Cookie cookie : cookieStore.getCookies()) {
                        logger.debug("Initial cookies are : {} ", cookie.toString());
                    }
                }
            } finally {
                getResponse.close();
            }
        } finally {
            httpClient.close();
        }
    }


}
