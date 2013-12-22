package com.refueling.crawler.crawler;

import com.refueling.crawler.model.Refueling;
import org.joda.time.DateTime;

import java.util.List;

public interface Crawler {

    List<Refueling> getRefuelings(DateTime from, DateTime to) throws Exception;

    boolean checkConnection() throws Exception;

}
