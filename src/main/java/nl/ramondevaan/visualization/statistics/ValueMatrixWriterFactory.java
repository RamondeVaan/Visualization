package nl.ramondevaan.visualization.statistics;

import org.apache.commons.io.FilenameUtils;

public class ValueMatrixWriterFactory {
    public final ValueMatrixWriter getValueMatrixWriter(String filename) {
        if(filename == null) {
            throw new IllegalArgumentException("Filename can not be null");
        }

        return getValueMatrixWriterByExtensionImpl(FilenameUtils.getExtension(filename));
    }

    public final ValueMatrixWriter getValueMatrixWriterByExtension(String extension) {
        if(extension == null) {
            throw new IllegalArgumentException("Extension can not be null");
        }

        String e = FilenameUtils.getExtension(extension);
        if(e.isEmpty()) {
            return getValueMatrixWriterByExtensionImpl(extension);
        }

        return getValueMatrixWriterByExtensionImpl(e);
    }

    final ValueMatrixWriter getValueMatrixWriterByExtensionImpl(String extension) {
        ValueMatrixWriter ret;

        switch(extension) {
            case "csv":
                ret = new CSVWriter();
                break;
            default:
                ret = getValueMatrixWriterByExtensionExt(extension);
        }

        if(ret == null) {
            throw new UnsupportedOperationException("No appropriate writer could be found");
        }

        return ret;
    }

    protected ValueMatrixWriter getValueMatrixWriterByExtensionExt(String extension) {
        return null;
    }
}
