package nl.ramondevaan.visualization.image;

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;

public class AutoImageReader extends ImageReader {
    private final static ImageReaderFactory
            DEFAULT_FACTORY = new ImageReaderFactory();
    
    private ImageReaderFactory factory;
    private ImageReader reader;
    private String lastExt;
    
    public AutoImageReader() {
        this.factory = DEFAULT_FACTORY;
    }
    
    public AutoImageReader(ImageReaderFactory factory) {
        this.factory = factory == null ?
                DEFAULT_FACTORY : factory;
    }
    
    public final void setFactory(ImageReaderFactory factory) {
        this.factory = factory == null ?
                DEFAULT_FACTORY : factory;
    }
    
    @Override
    protected Image read() throws IOException {
        String ext = FilenameUtils.getExtension(path);
        if(!ext.equals(lastExt)) {
            reader = factory.getImageReaderByExtensionImpl(ext);
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
