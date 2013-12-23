package com.refueling.crawler.parser

import com.google.common.collect.Lists
import com.refueling.crawler.dto.Refueling
import com.xlson.groovycsv.CsvParser
import org.apache.commons.lang3.StringUtils
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

class RefuelingParser {

    static List<Refueling> getStatoilRefuelings(final String csvContent) {
        List<Refueling> refuelings = Lists.newArrayList()
        def it = parseCsv(csvContent)
        it.each {
            refuelings.add(new Refueling(accountNr: it.Account, cardNr: it.Card, cardOwner: it.CardText,
                    refuelingDate: formatter().parseDateTime(it.Date), stationName: it.Location, product: it.Product,
                    numberOfLitres: parsePrice(it.Volume), pricePerLitre: parsePrice(it.PumppriceinCurrency),
                    discount: it.Discount))
        }
        refuelings
    }

    static Iterator parseCsv(final String content) {
        String[] lines = content.split('\n')
        String headerLine = StringUtils.deleteWhitespace(lines.head())
        StringBuilder sb = new StringBuilder(headerLine)
        for (i in 1..lines.size()-1) {
            sb.append('\n' + lines[i])
        }
        new CsvParser().parse(sb.toString(), separator: ';')
    }

    private float parsePrice(final String price) {
        Float.parseFloat(price.replace(',', '.'))
    }

    private DateTimeFormatter formatter() {
        DateTimeFormat.forPattern('dd-MM-yyyy HH:mm')
    }
}
