package com.refueling.crawler.impl;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.refueling.crawler.Crawler;
import com.refueling.crawler.model.Refueling;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class StatoilCrawler implements Crawler {
    private static final Logger logger = LoggerFactory.getLogger(StatoilCrawler.class);
    private static final String authCookieName = ".ASPXAUTH";
    private String username;
    private String password;
    private CrawlerUtil util;
    private RequestBuilder builder;

    public StatoilCrawler(final String username, final String password) {
        checkNotNull(username, "Should be initialized");
        checkNotNull(password, "Should be initialized");
        this.username = username;
        this.password = password;
        util = new CrawlerUtil();
        builder = new RequestBuilder();
    }

    //TODO proceed with report download
    @Override
    public List<Refueling> getRefuelings(final DateTime from, final DateTime to) throws Exception {
        BasicCookieStore cookieStore = proceedWithReport();
        final CloseableHttpClient httpClient = buildClient(cookieStore);
        proceedToReportPage(httpClient, from, to);
        return null;
    }

    @Override
    public boolean checkConnection() throws Exception {
        BasicCookieStore cookieStore = proceedWithReport();
        return hasAuthCookie(cookieStore.getCookies());
    }

    private BasicCookieStore proceedWithReport() throws Exception {
        BasicCookieStore cookieStore = new BasicCookieStore();
        Document loginPage;
        // init cookies
        try (CloseableHttpClient httpClient = buildClient(cookieStore)) {
            loginPage = parseLoginPage(httpClient);
            authenticate(cookieStore, loginPage, httpClient);
            return cookieStore;
        }
    }

    private Document parseLoginPage(final CloseableHttpClient httpClient) throws IOException {
        HttpGet httpGet = new HttpGet(util.param("mainPage"));
        Document loginPage;
        try (CloseableHttpResponse getResponse = httpClient.execute(httpGet)) {
            HttpEntity entity = getResponse.getEntity();
            loginPage = Jsoup.parse(CrawlerUtil.read(entity));
            logger.debug("getResponse status: {}", getResponse.getStatusLine());
            EntityUtils.consumeQuietly(entity);
        }
        return loginPage;
    }

    private void authenticate(final BasicCookieStore cookieStore,
                              final Document loginPage,
                              final CloseableHttpClient httpClient) throws IOException {
        HttpPost httpPost = new HttpPost(util.param("loginForm"));
        httpPost.setEntity(new UrlEncodedFormEntity(builder.buildAuthRequest(loginPage, username, password), Consts.UTF_8));
        try (CloseableHttpResponse postResponse = httpClient.execute(httpPost)) {
            HttpEntity postEntity = postResponse.getEntity();
            logger.debug("postResponse status: {} and cookies : {} ", postResponse.getStatusLine(), cookieStore.getCookies());
            EntityUtils.consumeQuietly(postEntity);
        }
    }

    private void proceedToReportPage(final CloseableHttpClient client,
                                     final DateTime from, final DateTime to) {
        Document reportsDocument;
        try {
            reportsDocument = getReportsPage(client);
            downloadReport(client, from, to, reportsDocument);
        } catch (IOException e) {
            logger.error("Could not proceed to reports page:", e);
            throw new RuntimeException();
        }
    }

    private void downloadReport(final CloseableHttpClient client,
                                final DateTime from, final DateTime to,
                                final Document reportsDocument) throws IOException {
        HttpPost httpPost = new HttpPost(util.param("reportPage"));
        httpPost.setEntity(new UrlEncodedFormEntity(builder.buildDownloadRequest(reportsDocument, from, to), Consts.UTF_8));
        try (CloseableHttpResponse postResponse = client.execute(httpPost)) {
            HttpEntity reportsResp = postResponse.getEntity();
            logger.debug("Resp after post to reports page: {} ", postResponse.getStatusLine());
            logger.debug("Content type of response: {} ", reportsResp.getContentType());
            InputStream in = reportsResp.getContent();
            new FileOutputStream("test1.csv").write(IOUtils.readFully(in, -1, false));
            EntityUtils.consumeQuietly(reportsResp);
        }
    }

    private Document getReportsPage(CloseableHttpClient client) throws IOException {
        Document reportsDocument;
        HttpGet httpGet = new HttpGet(util.param("reportPage"));
        try (CloseableHttpResponse getResponse = client.execute(httpGet)) {
            HttpEntity resp = getResponse.getEntity();
            logger.debug("Resp from reports page: {}", getResponse.getStatusLine());
            reportsDocument = Jsoup.parse(CrawlerUtil.read(resp));
            Element downloadReport = reportsDocument.getElementById("ctl00_ContentPlaceHolderContent_ButtonHandleReportsDownloadReport");
            logger.debug("DownloadReport element {} ", downloadReport); // means we have reached download button
            EntityUtils.consumeQuietly(resp);
        }
        return reportsDocument;
    }

    private boolean hasAuthCookie(final List<Cookie> cookies) {
        Predicate<Cookie> filter = new Predicate<Cookie>() {
            @Override
            public boolean apply(Cookie input) {
                return authCookieName.equals(input.getName());
            }
        };
        List<Cookie> resp = FluentIterable.from(cookies).filter(filter).toList();
        return !resp.isEmpty();
    }

    private CloseableHttpClient buildClient(final BasicCookieStore cookieStore) {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        httpClientBuilder.setDefaultCookieStore(cookieStore);
        httpClientBuilder.setRedirectStrategy(new LaxRedirectStrategy());
        httpClientBuilder.setUserAgent(util.param("userAgent"));
        return httpClientBuilder.build();
    }

}
