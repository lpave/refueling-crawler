package com.refueling.crawler.crawler;

import com.refueling.crawler.model.Refueling;

import java.util.List;

public interface Crawler {

    List<Refueling> getRefuelings();

    boolean checkConnection(String username, String password);

    void authenticate(String username, String password) throws Exception;
}
