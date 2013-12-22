package com.refueling.crawler.crawler;

import com.refueling.crawler.model.Refueling;

import java.util.List;

public interface Crawler {

    List<Refueling> getRefuelings() throws Exception;

    boolean checkConnection() throws Exception;

}
