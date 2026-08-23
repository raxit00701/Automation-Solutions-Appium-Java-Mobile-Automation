package utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvUtils {

    /** Reads a CSV (with a header row) into Object[][] for TestNG DataProviders. */
    public static Object[][] readCsv(String path) {
        List<String[]> rows = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            List<String[]> all = reader.readAll();
            if (all.isEmpty()) {
                throw new RuntimeException("CSV is empty: " + path);
            }
            // skip header
            for (int i = 1; i < all.size(); i++) {
                rows.add(all.get(i));
            }
        } catch (IOException | CsvException e) {
            throw new RuntimeException("Failed to read CSV: " + path, e);
        }
        Object[][] data = new Object[rows.size()][];
        for (int i = 0; i < rows.size(); i++) data[i] = rows.get(i);
        return data;
    }
}
