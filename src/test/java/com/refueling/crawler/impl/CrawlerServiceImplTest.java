package com.refueling.crawler.impl;

import com.refueling.crawler.CrawlerService;
import com.refueling.crawler.dto.Refueling;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CrawlerServiceImplTest {
    private String username;
    private String password;

    @Before
    public void setUp() throws Exception {
        username = System.getProperty("statoilUser");
        password = System.getProperty("statoilPass");
    }

    @Test
    public void testGetRefuelings() throws Exception {
        CrawlerService crawler = new CrawlerServiceImpl(username, password);
        DateTime from = new DateTime(2013, 12, 01, 0, 0);
        DateTime to = new DateTime(2013, 12, 31, 0, 0);
        List<Refueling> refuelings = crawler.getRefuelings(from, to);
        assertThat(refuelings.isEmpty(), is(false));
        assertThat(refuelings, everyItem(Matchers.<Refueling>hasProperty("accountNr")));
        assertThat(refuelings, everyItem(Matchers.<Refueling>hasProperty("refuelingDate")));
        assertThat(refuelings, everyItem(Matchers.<Refueling>hasProperty("numberOfLitres")));
    }

    @Test
    public void testCheckConenctionWithInvalidCredentials() throws Exception {
        CrawlerService crawler = new CrawlerServiceImpl("foo", "bar");
        assertThat(crawler.checkConnection(), is(false));
    }

    @Test
    public void testCheckConnectionWithValidCredentials() throws Exception {
        CrawlerService crawler = new CrawlerServiceImpl(username, password);
        assertThat(crawler.checkConnection(), is(true));
    }
}
