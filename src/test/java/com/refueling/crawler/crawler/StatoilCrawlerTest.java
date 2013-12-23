package com.refueling.crawler.crawler;

import com.refueling.crawler.impl.StatoilCrawler;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class StatoilCrawlerTest {
    private String username;
    private String password;

    @Before
    public void setUp() {
        username = System.getProperty("statoilUser");
        password = System.getProperty("statoilPass");
    }
    //TODO
    @Test
    public void testGetRefuelings() {

    }

    @Test
    public void testCheckConenctionWithInvalidCredentials() throws Exception {
        StatoilCrawler crawler = new StatoilCrawler("foo", "bar");
        assertThat(crawler.checkConnection(), is(false));
    }

    @Test
    public void testCheckConnectionWithValidCredentials() throws Exception {
        StatoilCrawler crawler = new StatoilCrawler(username, password);
        assertThat(crawler.checkConnection(), is(true));
    }
}
