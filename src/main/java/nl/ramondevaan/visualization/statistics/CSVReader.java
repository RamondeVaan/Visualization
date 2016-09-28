package nl.ramondevaan.visualization.statistics;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVReader extends ValueMatrixReader {
    public static final String DEFAULT_COLSEP = ",";
    public static final String DEFAULT_ROWSEP = System.lineSeparator();

    private String colSep;
    private String rowSep;
    private boolean quoted;

    private BufferedInputStream stream;
    private String line;
    private int curChar;
    private String[] headers;
    private List<String> valuesList;
    private String[] values;

    public CSVReader() {
        colSep = DEFAULT_COLSEP;
        rowSep = DEFAULT_ROWSEP;
        quoted = false;
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
    protected ValueMatrix read() throws IOException {
        stream = new BufferedInputStream(new FileInputStream(file));

        boolean hasVals = readLine();
        headers = line.split(colSep);
        valuesList = new ArrayList<>();

        if(!hasVals) {
            return new ValueMatrix(headers, new String[0], 0);
        }

        int numRows = 0;

        if(quoted) {
            unquote(headers);

            while(readLine()) {
                numRows++;
                parseLineQuoted();
            }
            numRows++;
            parseLineQuoted();
        } else {
            while(readLine()) {
                numRows++;
                parseLine();
            }
            numRows++;
            parseLine();
        }

        stream.close();
        return new ValueMatrix(headers, valuesList, numRows);
    }

    private void parseLine() throws IOException {
        values = line.split(colSep);
        if(values.length != headers.length) {
            throw new IOException("Incorrect number of columns in row");
        }
        for(String s : values) {
            valuesList.add(s);
        }
    }

    private void parseLineQuoted() throws IOException {
        values = line.split(colSep);
        if(values.length != headers.length) {
            throw new IOException("Incorrect number of columns in row");
        }
        unquote(values);
        for(String s : values) {
            valuesList.add(s);
        }
    }

    private static void unquote(String[] s) {
        for(int i = 0; i < s.length; i++) {
            s[i] = unquote(s[i]);
        }
    }

    private static String unquote(String s) {
        String ret = s;
        if(ret.startsWith("\"")) {
            ret = ret.substring(1);
        }
        if(ret.endsWith("\"")) {
            ret = ret.substring(0, ret.length() - 1);
        }
        return ret;
    }

    private boolean readLine() throws IOException {
        line = "";
        curChar = stream.read();
        while(curChar != -1) {
            line += ((char) curChar);
            if(line.endsWith(rowSep)) {
                break;
            }
            curChar = stream.read();
        }
        if(curChar == -1) {
            return false;
        }
        line = line.substring(0, line.length() - rowSep.length());
        return true;
    }
}
