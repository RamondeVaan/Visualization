package nl.ramondevaan.visualization.statistics;

import javafx.util.Pair;

import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class CSVWriter {
    private String colSep;
    private String rowSep;
    private boolean quoted;
    private File file;
    private String[] headers;
    private String[] values;
    private boolean append;
    
    public CSVWriter() {
        colSep = ",";
        rowSep = System.lineSeparator();
        quoted = true;
        headers = new String[0];
        resetValues();
    }
    
    public final void setAppend(boolean append) {
        this.append = append;
    }
    
    public final void tryReadHeaders(File file, String[] backup) {
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            String[] headers = line.split(colSep);
            
            if(quoted) {
                for(int i = 0; i < headers.length; i++) {
                    if(headers[i].startsWith("\"")) {
                        headers[i] = headers[i].substring(1);
                    }
                    if(headers[i].endsWith("\"")) {
                        headers[i] = headers[i].substring(0, headers[i].length() - 1);
                    }
                }
            }
            
            setHeaders(headers);
        } catch (IOException e) {
            setHeaders(backup);
        }
    }
    
    public final void setHeaders(String[] headers) {
        if(headers == null) {
            this.headers = new String[]{};
            this.values = new String[]{};
            return;
        }
        this.headers = new String[headers.length];
        System.arraycopy(headers, 0, this.headers, 0, headers.length);
        resetValues();
    }
    
    public final void setValues(String[] values) {
        if(values == null) {
            resetValues();
            return;
        }
        this.values = new String[headers.length];
        System.arraycopy(values, 0, this.values, 0,
                Math.min(this.values.length, values.length));
    }
    
    public final void resetValues() {
        this.values = new String[headers.length];
        for(int i = 0; i < this.values.length; i++) {
            this.values[i] = null;
        }
    }
    
    public final void setColSep(String colSep) {
        this.colSep = colSep;
    }
    
    public final void setRowSep(String rowSep) {
        this.rowSep = rowSep;
    }
    
    public final void setQuoted(boolean quoted) {
        this.quoted = quoted;
    }
    
    public final void setPath(String path) {
        if(path == null) {
            this.file = null;
            return;
        }
        this.file = new File(path);
    }
    
    public final void setPath(File file) {
        if(file == null) {
            this.file = null;
            return;
        }
        this.file = new File(file.getPath());
    }
    
    public final void setValues(List<Pair<String, String>> metrics) {
        resetValues();
        
        for(Pair<String, String> d : metrics) {
            for(int i = 0; i < headers.length; i++) {
                if(headers[i].equals(d.getKey())) {
                    values[i] = d.getValue();
                }
            }
        }
    }
    
    public final void writeFile() throws IOException {
        if(file == null) {
            throw new UnsupportedOperationException("No path was given");
        }
        if(headers == null || headers.length == 0) {
            throw new UnsupportedOperationException("No metrics were given");
        }
        
        final boolean exists = file.exists();
        
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(file, append))){
            final int len = headers.length;
            final int redLen = len - 1;
            
            if(!append || !exists) {
                for(int i = 0; i < len; i++) {
                    writer.write(quoteFormat(headers[i]));
                    if(i < redLen) {
                        writer.write(colSep);
                    }
                }
                writer.write(rowSep);
            }
            
            for(int i = 0; i < len; i++) {
                writer.write(quoteFormat(values[i] == null ? "" : values[i]));
                if(i < redLen) {
                    writer.write(colSep);
                }
            }
            writer.write(rowSep);
        }
    }
    
    private String quoteFormat(String s) {
        if(quoted) {
            return "\"" + s + "\"";
        }
        
        return s;
    }
}
