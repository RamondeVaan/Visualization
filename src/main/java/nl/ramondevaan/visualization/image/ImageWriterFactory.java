package nl.ramondevaan.visualization.image;

import org.apache.commons.io.FilenameUtils;

public class ImageWriterFactory {
    public final ImageWriter getImageWriter(String filename) {
        if(filename == null) {
            throw new IllegalArgumentException("Filename can not be null");
        }
        
        return getImageWriterByExtensionImpl(FilenameUtils.getExtension(filename));
    }
    
    public final ImageWriter getImageWriterByExtension(String extension) {
        if(extension == null) {
            throw new IllegalArgumentException("Extension can not be null");
        }
        
        String e = FilenameUtils.getExtension(extension);
        if(e.isEmpty()) {
            return getImageWriterByExtensionImpl(extension);
        }
        
        return getImageWriterByExtensionImpl(e);
    }
    
    final ImageWriter getImageWriterByExtensionImpl(String extension) {
        ImageWriter ret;
        
        switch(extension) {
            case "mhd":
                ret = new MetaImageWriter();
                break;
            default:
                ret = getImageWriterByExtensionExt(extension);
        }
        
        if(ret == null) {
            throw new UnsupportedOperationException("No appropriate writer could be found");
        }
        
        return ret;
    }
    
    protected ImageWriter getImageWriterByExtensionExt(String extension) {
        return null;
    }
}
