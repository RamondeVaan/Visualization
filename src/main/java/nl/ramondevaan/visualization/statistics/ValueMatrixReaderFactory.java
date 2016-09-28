package nl.ramondevaan.visualization.statistics;

import org.apache.commons.io.FilenameUtils;

public class ValueMatrixReaderFactory {
    public final ValueMatrixReader getValueMatrixReader(String filename) {
        if(filename == null) {
            throw new IllegalArgumentException("Filename can not be null");
        }

        return getValueMatrixReaderByExtensionImpl(FilenameUtils.getExtension(filename));
    }

    public final ValueMatrixReader getValueMatrixReaderByExtension(String extension) {
        if(extension == null) {
            throw new IllegalArgumentException("Extension can not be null");
        }

        String e = FilenameUtils.getExtension(extension);
        if(e.isEmpty()) {
            return getValueMatrixReaderByExtensionImpl(extension);
        }

        return getValueMatrixReaderByExtensionImpl(e);
    }

    final ValueMatrixReader getValueMatrixReaderByExtensionImpl(String extension) {
        ValueMatrixReader ret;

        switch(extension) {
            case "csv":
                ret = new CSVReader();
                break;
            default:
                ret = getValueMatrixReaderByExtensionExt(extension);
        }

        if(ret == null) {
            throw new UnsupportedOperationException("No appropriate reader could be found");
        }

        return ret;
    }

    protected ValueMatrixReader getValueMatrixReaderByExtensionExt(String extension) {
        return null;
    }
}
