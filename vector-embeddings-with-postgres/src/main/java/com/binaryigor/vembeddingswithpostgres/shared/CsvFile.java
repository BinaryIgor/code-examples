package com.binaryigor.vembeddingswithpostgres.shared;

import java.util.ArrayList;
import java.util.List;

public class CsvFile {

    public static List<String> columns(String row, String separator) {
        var possibleColumns = row.split(separator);
        var columns = new ArrayList<String>();

        var columnBuffer = new StringBuilder();
        var columnsBuffered = false;
        for (var c : possibleColumns) {
            if (c.startsWith("\"") && c.endsWith("\"") && !columnsBuffered) {
                if (c.length() > 1) {
                    columns.add(c.substring(1, c.length() - 1));
                } else {
                    columns.add("");
                }
            } else if (c.startsWith("\"") && !columnsBuffered) {
                columnBuffer.append(c.substring(1));
                columnsBuffered = true;
            } else if (c.endsWith("\"") && columnsBuffered) {
                columnBuffer.append(",").append(c, 0, c.length() - 1);
                columnsBuffered = false;
                columns.add(columnBuffer.toString());
                columnBuffer = new StringBuilder();
            } else if (columnsBuffered) {
                columnBuffer.append(",").append(c);
            } else {
                columns.add(c);
            }
        }

        return columns;
    }
}
