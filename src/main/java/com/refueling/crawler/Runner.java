package com.refueling.crawler;

import com.refueling.crawler.crawler.Crawler;
import com.refueling.crawler.crawler.StatoilCrawler;

public class Runner {
    public static void main(String[] args) throws Exception {
        System.out.println("Init statoil crawler");
        Crawler crawler = new StatoilCrawler(System.getProperty("statoilUser"), System.getProperty("statoilPass"));
        crawler.authenticate();
    }
}
