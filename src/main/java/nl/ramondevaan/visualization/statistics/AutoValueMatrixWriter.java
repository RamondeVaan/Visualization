package nl.ramondevaan.visualization.statistics;

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;

public class AutoValueMatrixWriter extends ValueMatrixWriter {
    private final static ValueMatrixWriterFactory
            DEFAULT_FACTORY = new ValueMatrixWriterFactory();

    private ValueMatrixWriterFactory factory;
    private ValueMatrixWriter writer;
    private String lastExt;

    public AutoValueMatrixWriter() {
        this.factory = DEFAULT_FACTORY;
    }

    public AutoValueMatrixWriter(ValueMatrixWriterFactory factory) {
        this.factory = factory == null ?
                DEFAULT_FACTORY : factory;
    }

    public final void setFactory(ValueMatrixWriterFactory factory) {
        this.factory = factory == null ?
                DEFAULT_FACTORY : factory;
    }

    @Override
    protected void write(ValueMatrix matrix) throws IOException {
        if(pathChanged) {
            String ext = FilenameUtils.getExtension(path);
            if(!ext.equals(lastExt)) {
                writer = factory.getValueMatrixWriterByExtensionImpl(ext);
            }
            lastExt = ext;
        }

        if(writer == null) {
            throw new UnsupportedOperationException("No appropriate writer was found.");
        }

        writer.path = path;
        writer.file = file;
        writer.write(matrix);
    }
}
