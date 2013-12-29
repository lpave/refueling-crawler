package com.refueling.crawler.parser

import com.google.common.collect.Lists
import com.refueling.crawler.dto.Refueling
import com.xlson.groovycsv.CsvParser
import org.apache.commons.lang3.StringUtils
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

class RefuelingParser {
    final String content

    RefuelingParser(final String content) {
        this.content = content
    }

    List<Refueling> getStatoilRefuelings() {
        def parsedCsvIterator = buildCsvIterator()
        List<Refueling> refuelings = Lists.newArrayList();
        parsedCsvIterator.each {
            refuelings.add(
                    new Refueling(accountNr: it.Account, cardNr: it.Card, cardOwner: it.CardText,
                            refuelingDate: formatter().parseDateTime(it.Date), stationName: it.Location,
                            product: it.Product, numberOfLitres: parsePrice(it.Volume),
                            pricePerLitre: parsePrice(it.PumppriceinCurrency), discount: it.Discount))
        }
        refuelings
    }

    //TODO refactor me, we need access header fields but those contain unnecessary whitespaces
    private Iterator buildCsvIterator() {
        String[] lines = content.split('\n')
        String headerLine = StringUtils.deleteWhitespace(lines.head())
        StringBuilder sb = new StringBuilder(headerLine)
        for (i in 1..lines.size() - 1) {
            sb.append('\n' + lines[i])
        }
        def parsedCsv = new CsvParser().parse(sb.toString(), separator: ';')
        parsedCsv
    }

    private float parsePrice(final String price) {
        Float.parseFloat(price.replace(',', '.'))
    }

    private DateTimeFormatter formatter() {
        DateTimeFormat.forPattern('dd-MM-yyyy HH:mm')
    }
}