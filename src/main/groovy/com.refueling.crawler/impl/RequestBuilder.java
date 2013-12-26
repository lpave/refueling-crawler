package com.refueling.crawler.impl;

import com.google.common.collect.Lists;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.jsoup.nodes.Document;

import java.util.List;

public class RequestBuilder {
    private static final Integer zeroValue = 0;
    private static final Integer negativeValue = -1;
    private static final String datePattern = "dd.MM.yyyy";
    private CrawlerUtil util;

    public RequestBuilder() {
        util = new CrawlerUtil();
    }

    public List<NameValuePair> buildAuthRequest(final Document document,
                                                final String username,
                                                final String password) {
        List<NameValuePair> values = Lists.newArrayList();
        values.add(new BasicNameValuePair(util.param("userNameInput"), username));
        values.add(new BasicNameValuePair(util.param("passwordInput"), password));
        values.add(new BasicNameValuePair(util.param("loginButton"), "»")); // submit button
        // hidden form params
        if (document != null) {
            addHiddenFields(document, values, false);
        }
        addPasswordFields(values);
        return values;
    }

    private void addHiddenFields(final Document document,
                                 final List<NameValuePair> values,
                                 final boolean skipValidation) {
        values.add(new BasicNameValuePair(util.param("asyncPost"), "true")); // check whether it is important  ?
        if (!skipValidation) values.add(CrawlerUtil.nameValuePair(document, util.param("eventTarget")));
        values.add(CrawlerUtil.nameValuePair(document, util.param("eventArg")));
        values.add(CrawlerUtil.nameValuePair(document, util.param("focus")));
        if (!skipValidation) values.add(CrawlerUtil.nameValuePair(document, util.param("validation")));
        values.add(CrawlerUtil.nameValuePair(document, util.param("view")));
        values.add(CrawlerUtil.nameValuePair(document, util.param("hiddenField")));
        values.add(CrawlerUtil.nameValuePair(document, util.param("scriptManager")));
    }

    private void addPasswordFields(final List<NameValuePair> values) {
        values.add(emptyValue(util.param("inputPassword1")));
        values.add(emptyValue(util.param("inputPassword2")));
        values.add(emptyValue(util.param("inputPasswordOld")));
    }

    private BasicNameValuePair emptyValue(final String name) {
        return new BasicNameValuePair(name, "");
    }

    public List<NameValuePair> buildDownloadRequest(final Document document,
                                                    final DateTime from,
                                                    final DateTime to) {
        List<NameValuePair> values = Lists.newArrayList();
        values.add(CrawlerUtil.buildPair(util.param("fromDate"), from.toString(datePattern)));
        values.add(CrawlerUtil.buildPair(util.param("toDate"), to.toString(datePattern)));
        values.add(CrawlerUtil.buildPair(util.param("reportsField"), util.param("reportType")));
        values.add(CrawlerUtil.buildPair(util.param("accountFields"), util.param("accountType")));
        values.add(CrawlerUtil.buildPair(util.param("reportTypeFields"), util.param("radioButton")));
        values.add(CrawlerUtil.buildPair(util.param("downloadReport"), util.param("downloadButtonName")));
        values.add(CrawlerUtil.buildPair(util.param("reportsAccount"), zeroValue.toString()));
        values.add(CrawlerUtil.buildPair(util.param("reportsCountry"), zeroValue.toString()));
        values.add(CrawlerUtil.buildPair(util.param("reportsFuelings"), zeroValue.toString()));
        values.add(CrawlerUtil.buildPair(util.param("reportsInvoice"), zeroValue.toString()));
        values.add(CrawlerUtil.buildPair(util.param("reportsProduct"), zeroValue.toString()));
        values.add(CrawlerUtil.buildPair(util.param("eventTarget"), ""));
        values.add(CrawlerUtil.buildPair(util.param("hidExport"), negativeValue.toString()));
        values.add(CrawlerUtil.buildPair(util.param("hidParent"), negativeValue.toString()));
        values.add(CrawlerUtil.buildPair(util.param("hidType"), negativeValue.toString()));
        addHiddenFields(document, values, true);
        addPasswordFields(values);
        return values;
    }

}
