package nl.ramondevaan.visualization.image;

import org.apache.commons.io.FilenameUtils;

public class ImageReaderFactory {
    public final ImageReader getImageReader(String filename) {
        if(filename == null) {
            throw new IllegalArgumentException("Filename can not be null");
        }
        
        return getImageReaderByExtensionImpl(FilenameUtils.getExtension(filename));
    }
    
    public final ImageReader getImageReaderByExtension(String extension) {
        if(extension == null) {
            throw new IllegalArgumentException("Extension can not be null");
        }
        
        String e = FilenameUtils.getExtension(extension);
        if(e.isEmpty()) {
            return getImageReaderByExtensionImpl(extension);
        }
        
        return getImageReaderByExtensionImpl(e);
    }
    
    final ImageReader getImageReaderByExtensionImpl(String extension) {
        ImageReader ret;
        
        switch(extension) {
            case "mhd":
                ret = new MetaImageReader();
                break;
            default:
                ret = getImageReaderByExtensionExt(extension);
        }
        
        if(ret == null) {
            throw new UnsupportedOperationException("No appropriate reader could be found");
        }
        
        return ret;
    }
    
    protected ImageReader getImageReaderByExtensionExt(String extension) {
        return null;
    }
}
