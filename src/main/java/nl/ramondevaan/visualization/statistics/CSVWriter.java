package nl.ramondevaan.visualization.statistics;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.List;

public class CSVWriter extends ValueMatrixWriter {
    public static final String DEFAULT_COLSEP = ",";
    public static final String DEFAULT_ROWSEP = System.lineSeparator();

    private String  colSep;
    private String  rowSep;
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
        FileOutputStream    stream  = new FileOutputStream(path, false);
        FileChannel         channel = stream.getChannel();
        FileLock            lock    = channel.tryLock(0, Long.MAX_VALUE, false);

        if(lock == null) {
            throw new IOException("Could not lock \"" + path + "\".");
        }

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
                stream.write(headers[i].getBytes());
                stream.write(colSep.getBytes());
            }
            stream.write(headers[i].getBytes());

            String s;
            if(values.length > 0) {
                stream.write(rowSep.getBytes());
                int k = 0;
                for (int j = 0; j < headerLength - 1; j++) {
                    s = values[k++];
                    stream.write((s == null ? "" : s).getBytes());
                    stream.write(colSep.getBytes());
                }
                s = values[k++];
                stream.write((s == null ? "" : s).getBytes());

                for (i = 1; i < numberOfRows; i++) {
                    stream.write(rowSep.getBytes());
                    for (int j = 0; j < headerLength - 1; j++) {
                        s = values[k++];
                        stream.write((s == null ? "" : s).getBytes());
                        stream.write(colSep.getBytes());
                    }
                    s = values[k++];
                    stream.write((s == null ? "" : s).getBytes());
                }
            }
        }

        stream.close();
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
