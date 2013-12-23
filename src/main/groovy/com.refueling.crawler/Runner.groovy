package com.refueling.crawler

import com.refueling.crawler.dto.Refueling
import com.refueling.crawler.impl.CrawlerServiceImpl
import org.joda.time.DateTime

class Runner {
    static void main(args) {
        println 'Init statoil crawler'
        CrawlerService crawler = new CrawlerServiceImpl(System.getProperty("statoilUser"), System.getProperty("statoilPass"))
        def checked = crawler.checkConnection()
        println 'Checked  = ' + checked
        List<Refueling> refuelings = crawler.getRefuelings(DateTime.now().minusDays(14), DateTime.now())
        refuelings.each {
            println it
        }
    }
}
