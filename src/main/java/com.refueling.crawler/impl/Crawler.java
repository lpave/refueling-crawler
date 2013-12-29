package com.refueling.crawler.impl;

import org.apache.http.impl.client.*;

public abstract class Crawler {
    protected CrawlerUtil util = new CrawlerUtil();
    protected RequestBuilder builder = new RequestBuilder();

    protected CloseableHttpClient buildClient(final BasicCookieStore cookieStore) {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        httpClientBuilder.setDefaultCookieStore(cookieStore);
        httpClientBuilder.setRedirectStrategy(new LaxRedirectStrategy());
        httpClientBuilder.setUserAgent(util.param("userAgent"));
        return httpClientBuilder.build();
    }
}
