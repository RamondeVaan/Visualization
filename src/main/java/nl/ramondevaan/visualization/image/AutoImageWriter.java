package nl.ramondevaan.visualization.image;

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;

public class AutoImageWriter extends ImageWriter {
    private final static ImageWriterFactory
            DEFAULT_FACTORY = new ImageWriterFactory();
    
    private ImageWriterFactory factory;
    private ImageWriter writer;
    private String lastExt;
    
    public AutoImageWriter() {
        this.factory = DEFAULT_FACTORY;
    }
    
    public AutoImageWriter(ImageWriterFactory factory) {
        this.factory = factory == null ?
                DEFAULT_FACTORY : factory;
    }
    
    public final void setFactory(ImageWriterFactory factory) {
        this.factory = factory == null ?
                DEFAULT_FACTORY : factory;
    }
    
    @Override
    protected void write() throws IOException {
        if(pathChanged) {
            String ext = FilenameUtils.getExtension(path);
            if(!ext.equals(lastExt)) {
                writer = factory.getImageWriterByExtensionImpl(ext);
            }
            lastExt = ext;
        }
        
        if(writer == null) {
            throw new UnsupportedOperationException("No appropriate writer was found.");
        }
        
        writer.path = path;
        writer.file = file;
        writer.write();
    }
}
