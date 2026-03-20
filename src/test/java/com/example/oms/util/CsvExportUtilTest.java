package com.example.oms.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class CsvExportUtilTest {

    @Test
    void toCsvNullRowsReturnsEmpty() {
        assertEquals("", CsvExportUtil.toCsv(null));
    }

    @Test
    void toCsvEscapesQuotesAndSeparators() {
        String csv = CsvExportUtil.toCsv(Arrays.asList(
                new String[]{"a,b", "c\"d"},
                new String[]{"e", null}
        ));
        assertTrue(csv.contains("\"a,b\""));
        assertTrue(csv.contains("\"c\"\"d\""));
        assertTrue(csv.contains("\"e\",\"\""));
    }

    @Test
    void toCsvEmptyRowsListReturnsJustEmptyString() {
        assertEquals("", CsvExportUtil.toCsv(Collections.<String[]>emptyList()));
    }

    @Test
    void toCsvNullRowAddsBlankLine() {
        String csv = CsvExportUtil.toCsv(Collections.singletonList(null));
        assertEquals("\n", csv);
    }
}

