package com.refueling.crawler.impl;

import com.refueling.crawler.CrawlerService;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

class CrawlerServiceImplTest {
    private String username;
    private String password;

    @Before
    void setUp() throws Exception {
        username = System.getProperty("statoilUser");
        password = System.getProperty("statoilPass");
    }

    @Test
    void testGetRefuelings() throws Exception {

    }

    @Test
    void testCheckConenctionWithInvalidCredentials() throws Exception {
        CrawlerService crawler = new CrawlerServiceImpl("foo", "bar");
        assertThat(crawler.checkConnection(), is(false));
    }

    @Test
    void testCheckConnectionWithValidCredentials() throws Exception {
        CrawlerService crawler = new CrawlerServiceImpl(username, password);
        assertThat(crawler.checkConnection(), is(true));
    }
}
