package com.example.oms.util;

import java.util.List;

public class CsvExportUtil {

    private CsvExportUtil() {
    }

    public static String toCsv(List<String[]> rows) {
        StringBuilder sb = new StringBuilder();
        if (rows == null) {
            return "";
        }

        for (String[] row : rows) {
            if (row == null) {
                sb.append("\n");
                continue;
            }
            for (int i = 0; i < row.length; i++) {
                String value = row[i] == null ? "" : row[i];
                sb.append("\"").append(escape(value)).append("\"");
                if (i < row.length - 1) {
                    sb.append(",");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static String escape(String value) {
        // Legacy CSV escaping: double quotes are escaped as "".
        return value.replace("\"", "\"\"");
    }
}

