package nl.ramondevaan.visualization.statistics;

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;

public class AutoValueMatrixReader extends ValueMatrixReader {
    private final static ValueMatrixReaderFactory
            DEFAULT_FACTORY = new ValueMatrixReaderFactory();

    private ValueMatrixReaderFactory factory;
    private ValueMatrixReader reader;
    private String lastExt;

    public AutoValueMatrixReader() {
        this.factory = DEFAULT_FACTORY;
    }

    public AutoValueMatrixReader(ValueMatrixReaderFactory factory) {
        this.factory = factory == null ?
                DEFAULT_FACTORY : factory;
    }

    public final void setFactory(ValueMatrixReaderFactory factory) {
        this.factory = factory == null ?
                DEFAULT_FACTORY : factory;
    }

    @Override
    protected ValueMatrix read() throws IOException {
        String ext = FilenameUtils.getExtension(path);
        if(!ext.equals(lastExt)) {
            reader = factory.getValueMatrixReaderByExtensionImpl(ext);
        }
        lastExt = ext;

        if(reader == null) {
            throw new UnsupportedOperationException("No appropriate reader was found.");
        }

        reader.path = path;
        reader.file = file;

        return reader.read();
    }
}
