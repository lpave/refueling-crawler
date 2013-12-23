package com.refueling.crawler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.refueling.crawler.Crawler;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class StatoilCrawler implements Crawler {
    private static final Logger logger = LoggerFactory.getLogger(StatoilCrawler.class);
    private static final String datePattern = "dd.MM.yyyy";
    private static final String authCookieName = ".ASPXAUTH";
    private static final Integer zeroValue = 0;
    private static final Integer negativeValue = -1;
    private String username;
    private String password;
    private Map<String, Object> config;

    public StatoilCrawler(final String username, final String password) {
        checkNotNull(username, "Should be initialized");
        checkNotNull(password, "Should be initialized");
        this.username = username;
        this.password = password;
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

    //TODO proceed with report download
    @Override
    public List<Refueling> getRefuelings(final DateTime from, final DateTime to) throws Exception {
        BasicCookieStore cookieStore = authenticate();
        final CloseableHttpClient httpClient = buildClient(cookieStore);
        shiftToReportsPage(httpClient, from, to);
        return null;
    }

    @Override
    public boolean checkConnection() throws Exception {
        BasicCookieStore cookieStore = authenticate();
        return hasAuthCookie(cookieStore.getCookies());
    }

    // TODO minimize
    private BasicCookieStore authenticate() throws Exception {
        BasicCookieStore cookieStore = new BasicCookieStore();
        Document loginPage = null;
        // init cookies
        HttpGet httpGet = new HttpGet(param("mainPage"));
        try (CloseableHttpClient httpClient = buildClient(cookieStore)) {
            try (CloseableHttpResponse getResponse = httpClient.execute(httpGet)) {
                HttpEntity entity = getResponse.getEntity();
                loginPage = Jsoup.parse(read(entity));
                logger.debug("getResponse status: {}", getResponse.getStatusLine());
                EntityUtils.consumeQuietly(entity);
            }
            // auth
            HttpPost httpPost = new HttpPost(param("loginForm"));
            httpPost.setEntity(new UrlEncodedFormEntity(buildAuthRequest(loginPage), Consts.UTF_8));
            try (CloseableHttpResponse postResponse = httpClient.execute(httpPost)) {
                HttpEntity postEntity = postResponse.getEntity();
                logger.debug("postResponse status: {} and cookies : {} ", postResponse.getStatusLine(), cookieStore.getCookies());
                EntityUtils.consumeQuietly(postEntity);
            }
            return cookieStore;
        }
    }

    private void shiftToReportsPage(final CloseableHttpClient client, final DateTime from, final DateTime to) {
        Document reportsDocument = null;
        HttpGet httpGet = new HttpGet(param("reportPage"));
        try {
            try (CloseableHttpResponse getResponse = client.execute(httpGet)) {
                HttpEntity resp = getResponse.getEntity();
                logger.debug("Resp from reports page: {}", getResponse.getStatusLine());
                reportsDocument = Jsoup.parse(read(resp));
                Element downloadReport = reportsDocument.getElementById("ctl00_ContentPlaceHolderContent_ButtonHandleReportsDownloadReport");
                logger.debug("DownloadReport element {} ", downloadReport); // means we have reached download button
                EntityUtils.consumeQuietly(resp);
            }
            HttpPost httpPost = new HttpPost(param("reportPage"));
            httpPost.setEntity(new UrlEncodedFormEntity(buildDownloadRequest(reportsDocument, from, to), Consts.UTF_8));
            try (CloseableHttpResponse postResponse = client.execute(httpPost)) {
                HttpEntity reportsResp = postResponse.getEntity();
                logger.debug("Resp after post to reports page: {} ", postResponse.getStatusLine());
                logger.debug("Content type of response: {} ", reportsResp.getContentType());
                InputStream in = reportsResp.getContent();
                new FileOutputStream("test1.csv").write(IOUtils.readFully(in, -1, false));
                EntityUtils.consumeQuietly(reportsResp);
            }
        } catch (IOException e) {
            logger.error("Could not proceed to reports page:", e);
            throw new RuntimeException();
        }
    }

    private String read(HttpEntity reportsResp) throws IOException {
        return EntityUtils.toString(reportsResp, Charsets.UTF_8);
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
        httpClientBuilder.setUserAgent(param("userAgent"));
        return httpClientBuilder.build();
    }

    //TODO refactor to properties file
    private List<NameValuePair> buildAuthRequest(final Document document) {
        List<NameValuePair> values = new ArrayList<NameValuePair>();
        values.add(new BasicNameValuePair(param("userNameInput"), username));
        values.add(new BasicNameValuePair(param("passwordInput"), password));
        values.add(new BasicNameValuePair(param("loginButton"), "»")); // submit button
        // hidden form params
        if (document != null) {
            addHiddenFields(document, values, false);
        }
        addPasswordFields(values);
        return values;
    }

    private void addHiddenFields(Document document, List<NameValuePair> values, boolean skipValidation) {
        values.add(new BasicNameValuePair(param("asyncPost"), "true")); // check whether it is important  ?
        if (!skipValidation) values.add(nameValuePair(document, param("eventTarget")));
        values.add(nameValuePair(document, param("eventArg")));
        values.add(nameValuePair(document, param("focus")));
        if (!skipValidation) values.add(nameValuePair(document, param("validation")));
        values.add(nameValuePair(document, param("view")));
        values.add(nameValuePair(document, param("hiddenField")));
        values.add(nameValuePair(document, param("scriptManager")));
    }

    private void addPasswordFields(List<NameValuePair> values) {
        values.add(emptyValue(param("inputPassword1")));
        values.add(emptyValue(param("inputPassword2")));
        values.add(emptyValue(param("inputPasswordOld")));
    }

    private BasicNameValuePair emptyValue(final String name) {
        return new BasicNameValuePair(name, "");
    }

    private List<NameValuePair> buildDownloadRequest(final Document document, final DateTime from, final DateTime to) {
        List<NameValuePair> values = Lists.newArrayList();
        values.add(buildPair(param("fromDate"), from.toString(datePattern)));
        values.add(buildPair(param("toDate"), to.toString(datePattern)));
        values.add(buildPair(param("reportsField"), param("reportType")));
        values.add(buildPair(param("accountFields"), param("accountType")));
        values.add(buildPair(param("reportTypeFields"), param("radioButton")));
        values.add(buildPair(param("downloadReport"), param("downloadButtonName")));
        values.add(buildPair(param("reportsAccount"), zeroValue.toString()));
        values.add(buildPair(param("reportsCountry"), zeroValue.toString()));
        values.add(buildPair(param("reportsFuelings"), zeroValue.toString()));
        values.add(buildPair(param("reportsInvoice"), zeroValue.toString()));
        values.add(buildPair(param("reportsProduct"), zeroValue.toString()));
        values.add(buildPair(param("eventTarget"), param("downloadEventTarget")));
        values.add(buildPair(param("hidExport"), negativeValue.toString()));
        values.add(buildPair(param("hidParent"), negativeValue.toString()));
        values.add(buildPair(param("hidType"), negativeValue.toString()));
        addHiddenFields(document, values, true);
        addPasswordFields(values);
        return values;
    }

    private BasicNameValuePair buildPair(final String name, final String value) {
        return new BasicNameValuePair(name, value);
    }

    private BasicNameValuePair nameValuePair(final Document document, final String name) {
        return new BasicNameValuePair(name, value(document, name));
    }

    private String value(final Document document, final String id) {
        return document.getElementById(id).val();
    }

    private String param(final String key) {
        return (String) config.get(key);
    }
}
