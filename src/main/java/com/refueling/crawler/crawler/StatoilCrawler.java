package com.refueling.crawler.crawler;

import com.refueling.crawler.model.Refueling;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class StatoilCrawler implements Crawler {
    private static final Logger logger = LoggerFactory.getLogger(StatoilCrawler.class);
    private static final String formLoginUrl = "https://www.statoilwebfuel.com";
    private static final String postLoginForm = "https://www.statoilwebfuel.com/login.aspx?ReturnUrl=%2fHome%2fhome.aspx";
    private static final String reportPageUrl = "https://www.statoilwebfuel.com/Home/Report/reports.aspx";
    private final String username;
    private final String password;

    public StatoilCrawler(final String username, final String password) {
        checkNotNull(username, "Should be initialized");
        checkNotNull(password, "Should be initialized");
        this.username = username;
        this.password = password;
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
    public void authenticate() throws IOException {
        BasicCookieStore cookieStore = new BasicCookieStore();
        final CloseableHttpClient httpClient = buildClient(cookieStore);
        // init cookies
        HttpGet httpGet = new HttpGet(formLoginUrl);
        try {
            try (CloseableHttpResponse getResponse = httpClient.execute(httpGet)) {
                HttpEntity entity = getResponse.getEntity();
                logger.debug("getResponse status: {}", getResponse.getStatusLine());
                EntityUtils.consume(entity);
                if (!cookieStore.getCookies().isEmpty()) {
                    for (Cookie cookie : cookieStore.getCookies()) {
                        logger.debug("Initial cookies are : {} ", cookie.toString());
                    }
                }
            }
            // auth
            HttpPost httpPost = new HttpPost(postLoginForm);
            httpPost.setEntity(new UrlEncodedFormEntity(getPostRequest(), Consts.UTF_8));
            try (CloseableHttpResponse postResponse = httpClient.execute(httpPost)) {
                HttpEntity postEntity = postResponse.getEntity();
                logger.debug("postResponse status: {} and cookies : {} ", postResponse.getStatusLine(), cookieStore.getCookies());
                EntityUtils.consume(postEntity);
            }
            shiftToReportsPage(httpClient); // assume we are authenticated
        } finally {
            httpClient.close();
        }
    }

    private void shiftToReportsPage(final CloseableHttpClient client) {
        HttpGet httpGet = new HttpGet(reportPageUrl);
        try {
            try (CloseableHttpResponse getResponse = client.execute(httpGet)) {
                HttpEntity resp = getResponse.getEntity();
                logger.debug("Resp from reports page: {}", getResponse.getStatusLine());
                EntityUtils.consume(resp);
            }
        } catch (IOException e) {
            logger.error("Could not proceed to reports page:", e);
        }
    }

    private CloseableHttpClient buildClient(final BasicCookieStore cookieStore) {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        httpClientBuilder.setDefaultCookieStore(cookieStore);
        httpClientBuilder.setRedirectStrategy(new LaxRedirectStrategy());
        return httpClientBuilder.build();
    }

    //TODO refactor to properties file
    private List<NameValuePair> getPostRequest() {
        List<NameValuePair> values = new ArrayList<NameValuePair>();
        values.add(new BasicNameValuePair("ctl00$ContentPlaceHolderContent$textboxUserName", username));
        values.add(new BasicNameValuePair("ctl00$ContentPlaceHolderContent$textboxPassword", password));
        values.add(new BasicNameValuePair("ASYNCPOST", "true")); // check whether it is important  ?
        return values;
    }
}
