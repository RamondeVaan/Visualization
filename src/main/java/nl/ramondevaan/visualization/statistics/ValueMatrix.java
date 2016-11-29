package nl.ramondevaan.visualization.statistics;

import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ValueMatrix {
    public final int numberOfRows;
    public final List<String> headers;
    public final List<String> values;

    public ValueMatrix(String[] headers, String[][] values) {
        Validate.notNull(headers);
        Validate.notNull(values);

        this.numberOfRows = values.length;
        String[] h = Arrays.copyOf(headers, headers.length);
        String[] v = new String[values.length * headers.length];

        int k = 0;
        for(int i = 0; i < values.length; i++) {
            if(values[i] == null || values[i].length != h.length) {
                throw new IllegalArgumentException("Row was missing values");
            }
            System.arraycopy(values[i], 0, v, k, values[i].length);
            k += values[i].length;
        }

        this.headers = Collections.unmodifiableList(Arrays.asList(h));
        this.values = Collections.unmodifiableList(Arrays.asList(v));
    }

    ValueMatrix(String[] headers, String[] values, int numberOfRows) {
        this.numberOfRows = numberOfRows;
        this.headers = Collections.unmodifiableList(Arrays.asList(headers));
        this.values = Collections.unmodifiableList(Arrays.asList(values));
    }

    ValueMatrix(String[] headers, List<String> values, int numberOfRows) {
        this.numberOfRows = numberOfRows;
        this.headers = Collections.unmodifiableList(Arrays.asList(headers));
        this.values = Collections.unmodifiableList(values);
    }

    ValueMatrix(List<String> headers, String[] values, int numberOfRows) {
        this.numberOfRows = numberOfRows;
        this.headers = Collections.unmodifiableList(headers);
        this.values = Collections.unmodifiableList(Arrays.asList(values));
    }

    ValueMatrix(List<String> headers, List<String> values, int numberOfRows) {
        this.numberOfRows = numberOfRows;
        this.headers = Collections.unmodifiableList(headers);
        this.values = Collections.unmodifiableList(values);
    }

    public final int getNumberOfRows() {
        return numberOfRows;
    }

    public final List<String> getHeaders() {
        return headers;
    }

    public final List<String> getValues() {
        return values;
    }
}
