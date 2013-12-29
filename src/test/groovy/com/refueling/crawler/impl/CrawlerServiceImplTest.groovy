package com.refueling.crawler.impl
import com.refueling.crawler.CrawlerService
import com.refueling.crawler.dto.Refueling
import org.hamcrest.Matchers
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Before

import static org.hamcrest.Matchers.everyItem

class CrawlerServiceImplTest {
    private String username;
    private String password;

    @Before
    public void setUp() throws Exception {
        username = System.getProperty("statoilUser");
        password = System.getProperty("statoilPass");
    }

//    @Test will fail
    public void testGetRefuelings() throws Exception {
        CrawlerService crawler = new CrawlerServiceImpl(username, password);
        DateTime from = new DateTime(2013, 12, 01, 0, 0);
        DateTime to = new DateTime(2013, 12, 31, 0, 0);
        List<Refueling> refuelings = crawler.getRefuelings(from, to);
        Assert.assertThat(refuelings.isEmpty(), Matchers.is(false));
        Assert.assertThat(refuelings, everyItem(Matchers.<Refueling> hasProperty("accountNr")));
        Assert.assertThat(refuelings, everyItem(Matchers.<Refueling> hasProperty("refuelingDate")));
        Assert.assertThat(refuelings, everyItem(Matchers.<Refueling> hasProperty("numberOfLitres")));
    }

//    @Test
    public void testCheckConnectionWithInvalidCredentials() throws Exception {
        CrawlerService crawler = new CrawlerServiceImpl('foo', 'bar');
        Assert.assertThat(crawler.checkConnection(), Matchers.is(false));
    }

//    @Test will fail
    public void testCheckConnectionWithValidCredentials() throws Exception {
        CrawlerService crawler = new CrawlerServiceImpl(username, password);
        Assert.assertThat(crawler.checkConnection(), Matchers.is(true));
    }
}
