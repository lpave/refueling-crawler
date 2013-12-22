package com.refueling.crawler.crawler;

import com.google.common.base.Charsets;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
        Document loginPage = null;
        final CloseableHttpClient httpClient = buildClient(cookieStore);
        // init cookies
        HttpGet httpGet = new HttpGet(formLoginUrl);
        try {
            try (CloseableHttpResponse getResponse = httpClient.execute(httpGet)) {
                HttpEntity entity = getResponse.getEntity();
                loginPage = Jsoup.parse(EntityUtils.toString(entity, Charsets.UTF_8));
                logger.debug("getResponse status: {}", getResponse.getStatusLine());
                EntityUtils.consumeQuietly(entity);
                if (!cookieStore.getCookies().isEmpty()) {
                    for (Cookie cookie : cookieStore.getCookies()) {
                        logger.debug("Initial cookies are : {} ", cookie.toString());
                    }
                }
            }
            // auth
            HttpPost httpPost = new HttpPost(postLoginForm);
            httpPost.setEntity(new UrlEncodedFormEntity(getPostRequest(loginPage), Consts.UTF_8));
            try (CloseableHttpResponse postResponse = httpClient.execute(httpPost)) {
                HttpEntity postEntity = postResponse.getEntity();
                logger.debug("postResponse status: {} and cookies : {} ", postResponse.getStatusLine(), cookieStore.getCookies());
                EntityUtils.consumeQuietly(postEntity);
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
                Document reportsPage = Jsoup.parse(EntityUtils.toString(resp, Charsets.UTF_8));
                Element downloadReport = reportsPage.getElementById("ctl00_ContentPlaceHolderContent_ButtonHandleReportsDownloadReport");
                logger.debug("DownloadReport element {} ", downloadReport); // means we have reached download button
                EntityUtils.consumeQuietly(resp);

            }
        } catch (IOException e) {
            logger.error("Could not proceed to reports page:", e);
            throw new RuntimeException();
        }
    }

    private CloseableHttpClient buildClient(final BasicCookieStore cookieStore) {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        httpClientBuilder.setDefaultCookieStore(cookieStore);
        httpClientBuilder.setRedirectStrategy(new LaxRedirectStrategy());
        httpClientBuilder.setUserAgent("Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:25.0) Gecko/20100101 Firefox/25.0");
        return httpClientBuilder.build();
    }

    //TODO refactor to properties file
    private List<NameValuePair> getPostRequest(final Document document) {
        List<NameValuePair> values = new ArrayList<NameValuePair>();
        values.add(new BasicNameValuePair("ctl00$ContentPlaceHolderContent$textboxUserName", username));
        values.add(new BasicNameValuePair("ctl00$ContentPlaceHolderContent$textboxPassword", password));
        values.add(new BasicNameValuePair("ctl00$ContentPlaceHolderContent$buttonLogin", "»")); // submit button
        //weird stuff for aspx ?
        values.add(new BasicNameValuePair("__ASYNCPOST", "true")); // check whether it is important  ?
        // hidden form params
        if (document != null) {
            values.add(nameValuePair(document, "__EVENTTARGET"));
            values.add(nameValuePair(document, "__EVENTARGUMENT"));
            values.add(nameValuePair(document, "__LASTFOCUS"));
            values.add(nameValuePair(document, "__EVENTVALIDATION"));
            values.add(nameValuePair(document, "__VIEWSTATE"));
            values.add(nameValuePair(document, "ctl00_ScriptManagerWebfuelSite_HiddenField"));
            values.add(nameValuePair(document, "ctl00_ScriptManagerWebfuelSite"));
        }
        values.add(new BasicNameValuePair("ctl00$InputNewPassword1", ""));
        values.add(new BasicNameValuePair("ctl00$InputNewPassword2", ""));
        values.add(new BasicNameValuePair("ctl00$InputPasswordOld", ""));
        return values;
    }

    private BasicNameValuePair nameValuePair(final Document document, final String name) {
        return new BasicNameValuePair(name, value(document, name));
    }

    private String value(final Document document, final String id) {
        return document.getElementById(id).val();
    }
}
