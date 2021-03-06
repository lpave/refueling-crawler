package com.refueling.crawler.parser

import com.refueling.crawler.dto.Refueling
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class RefuelingParserTest {

    @Test
    public void testThatRefuelingsParsed() {
        String csv = "Account;Card;Card Text;Date;NotaNo;InvoiceNo;Location;Fueling Type;" +
                "Country;Product;Volume;Currency;Pump price in Currency;NET Price;" +
                "Amount in Currency;VAT in Currency;Price;Amount;VAT;NET Amount;" +
                "Discount;Amount incl Discount;Odometer;Vehicle;Vehicle Name;Driver;Driver Name\n" +
                "12345;54321;Chuck Norris;01-12-2013 12:00;" +
                "01010101;1234567;Statoil;External;Estonia;D miles;" +
                "39,39;EUR;1,32;1,10;52,19;8,70;1,33;52,19;8,70;43,49;0,00;52,19;0;;;;\n";
        RefuelingParser parser = new RefuelingParser(csv);
        List<Refueling> refuelings = parser.getStatoilRefuelings();
        Refueling parsedRefueling = refuelings.get(0);
        Assert.assertThat(parsedRefueling.cardOwner, Matchers.equalTo("Chuck Norris"));
        Assert.assertThat(parsedRefueling.accountNr, Matchers.equalTo("12345"));
        Assert.assertThat(parsedRefueling.stationName, Matchers.equalTo("Statoil"));
    }
}
