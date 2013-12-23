package com.refueling.crawler.impl

import com.refueling.crawler.CrawlerService
import com.refueling.crawler.dto.Refueling
import com.refueling.crawler.parser.RefuelingParser
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.CloseableHttpClient
import org.joda.time.DateTime

class CrawlerServiceImpl implements CrawlerService {
    StatoilCrawler crawler

    CrawlerServiceImpl(final String username, final String password) {
        crawler = new StatoilCrawler(username, password)
    }

    @Override
    List<Refueling> getRefuelings(final DateTime from, final DateTime to) throws Exception {
        BasicCookieStore cookieStore = crawler.authenticate()
        final CloseableHttpClient httpClient = crawler.buildClient(cookieStore)
        String csvContent = crawler.findReport(httpClient, from, to)
        RefuelingParser.getStatoilRefuelings(csvContent)
    }

    @Override
    boolean checkConnection() throws Exception {
        BasicCookieStore cookieStore = crawler.authenticate()
        crawler.hasAuthCookie(cookieStore.getCookies())
    }
}
