package nl.ramondevaan.visualization.statistics;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class CSVWriter extends ValueMatrixWriter {
    public static final String DEFAULT_COLSEP = ",";
    public static final String DEFAULT_ROWSEP = System.lineSeparator();

    private String colSep;
    private String rowSep;
    private boolean quoted;
    
    public CSVWriter() {
        colSep = DEFAULT_COLSEP;
        rowSep = DEFAULT_ROWSEP;
        quoted = true;
    }

    public final void setColSep(String colSep) {
        this.colSep = colSep == null ?
                DEFAULT_COLSEP : colSep;
        changed();
    }

    public final void setRowSep(String rowSep) {
        this.rowSep = rowSep == null ?
                DEFAULT_ROWSEP : rowSep;
        changed();
    }

    public final void setQuoted(boolean quoted) {
        this.quoted = quoted;
        changed();
    }

    @Override
    protected void write(ValueMatrix matrix) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
            final int headerLength = matrix.headers.size();
            final int numberOfRows = matrix.numberOfRows;

            String[] headers;
            String[] values;

            if(quoted) {
                headers = getQuoted(matrix.headers);
                values = getQuoted(matrix.values);
            } else {
                headers = matrix.headers.toArray(new String[matrix.headers.size()]);
                values = matrix.values.toArray(new String[matrix.values.size()]);
            }

            if(headerLength > 0) {
                int i;
                for (i = 0; i < headerLength - 1; i++) {
                    writer.write(headers[i]);
                    writer.write(colSep);
                }
                writer.write(headers[i]);

                String s;
                if(values.length > 0) {
                    writer.write(rowSep);
                    int k = 0;
                    for (int j = 0; j < headerLength - 1; j++) {
                        s = values[k++];
                        writer.write(s == null ? "" : s);
                        writer.write(colSep);
                    }
                    s = values[k++];
                    writer.write(s == null ? "" : s);

                    for (i = 1; i < numberOfRows; i++) {
                        writer.write(rowSep);
                        for (int j = 0; j < headerLength - 1; j++) {
                            s = values[k++];
                            writer.write(s == null ? "" : s);
                            writer.write(colSep);
                        }
                        s = values[k++];
                        writer.write(s == null ? "" : s);
                    }
                }
            }
        }
    }
    
    private static String[] getQuoted(List<String> values) {
        if(values == null) {
            return new String[0];
        }

        String[] ret = new String[values.size()];
        for(int i = 0; i < values.size(); i++) {
            ret[i] = '"' + values.get(i) + '"';
        }

        return ret;
    }
}
