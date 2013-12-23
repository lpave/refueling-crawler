package com.refueling.crawler

import com.refueling.crawler.dto.Refueling
import org.joda.time.DateTime

interface CrawlerService {
    List<Refueling> getRefuelings(DateTime from, DateTime to) throws Exception;

    boolean checkConnection() throws Exception;

}
