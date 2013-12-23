package com.refueling.crawler;

import com.refueling.crawler.impl.StatoilCrawler;
import org.joda.time.DateTime;

public class Runner {
    public static void main(String[] args) throws Exception {
        System.out.println("Init statoil crawler");
        Crawler crawler = new StatoilCrawler(System.getProperty("statoilUser"), System.getProperty("statoilPass"));
        boolean checked = crawler.checkConnection();
        System.out.println("Checked  = " + checked);
        crawler.getRefuelings(DateTime.now().minusDays(60), DateTime.now());
    }
}
