[![Build Status](https://travis-ci.org/plavreshin/refueling-crawler.png?branch=master)](https://travis-ci.org/plavreshin/refueling-crawler)
---------

**Refueling crawler** was crafted to simplify acces to statoilwebfuel.com intranet, where one can download reports and track refuelings. 

It is a proof-of-concept in Groovy/Java, where different open-source libraries are put together to automate query and download steps:
> - Apache HttpClient for queries 
> - Jsoup for html parsing
> - Apache POI for xls - > csv transformation 
> and others...

Gradle should be used to build and there is a Runner class to test api and see desired result. One should use -DstatoilUser= and -DstatoilPass= parameters.

> **NOTE:** Logging can be turned on with :
-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog -Dorg.apache.commons.logging.simplelog.showdatetime=true -Dorg.apache.commons.logging.simplelog.log.org.apache.http=DEBUG -Dorg.apache.commons.logging.simplelog.log.org.apache.http.wire=ERROR
