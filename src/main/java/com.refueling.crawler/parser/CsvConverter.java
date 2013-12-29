package com.refueling.crawler.parser;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class CsvConverter {
    private static final int startPos = 10;
    private static final int zeroPos = 0;
    private Map<Row, List<String>> csvMap = Maps.newLinkedHashMap();
    private DataFormatter formatter = new DataFormatter(true);
    private FormulaEvaluator evaluator = null;

    public String convertToCsv(final InputStream in) throws IOException {
        Workbook wb = null;
        try {
            wb = WorkbookFactory.create(in);
            if (wb != null) findData(wb);
            return join(csvMap);
        } catch (InvalidFormatException e) {
            throw new RuntimeException("CSV convertation failed");
        }
    }

    private void findData(final Workbook wb) {
        this.evaluator = wb.getCreationHelper().createFormulaEvaluator();
        Sheet sheet = wb.getSheetAt(zeroPos);
        int lastRowNum = sheet.getLastRowNum();
        for (int i = startPos; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            List<String> csvLines = Lists.newArrayList();
            if (row != null) {
                parseRow(row, csvLines);
            }
        }
    }

    private void parseRow(final Row row, final List<String> csvLines) {
        int lastCellNum = row.getLastCellNum();
        for (int i = zeroPos; i <= lastCellNum; i++) {
            Cell cell = row.getCell(i);
            if (cell != null) csvLines.add(parse(cell));
        }
        csvMap.put(row, csvLines);
    }

    private String parse(final Cell cell) {
        if (cell.getCellType() != Cell.CELL_TYPE_FORMULA) {
            return formatter.formatCellValue(cell);
        }
        return formatter.formatCellValue(cell, evaluator);
    }

    private String join(final Map<Row, List<String>> csvMap) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Row, List<String>> rowEntry : csvMap.entrySet()) {
            sb.append(Joiner.on(";").join(rowEntry.getValue()));
            sb.append("\n");
        }
        return sb.toString();
    }
}
