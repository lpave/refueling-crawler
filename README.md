refueling-crawler

Add -DstatoilUser= and -DstatoilPass= to your run configuration.

Apache http client debug logging, add this to your run configuration :

-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog
-Dorg.apache.commons.logging.simplelog.showdatetime=true
-Dorg.apache.commons.logging.simplelog.log.org.apache.http=DEBUG
-Dorg.apache.commons.logging.simplelog.log.org.apache.http.wire=ERROR