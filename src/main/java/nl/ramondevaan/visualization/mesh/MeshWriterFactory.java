package nl.ramondevaan.visualization.mesh;

import org.apache.commons.io.FilenameUtils;

public class MeshWriterFactory {
    public final MeshWriter getMeshWriter(String filename) {
        if(filename == null) {
            throw new IllegalArgumentException("Filename can not be null");
        }
        
        return getMeshWriterByExtensionImpl(FilenameUtils.getExtension(filename));
    }
    
    public final MeshWriter getMeshWriterByExtension(String extension) {
        if(extension == null) {
            throw new IllegalArgumentException("Extension can not be null");
        }
        
        String e = FilenameUtils.getExtension(extension);
        if(e.isEmpty()) {
            return getMeshWriterByExtensionImpl(extension);
        }
        
        return getMeshWriterByExtensionImpl(e);
    }
    
    final MeshWriter getMeshWriterByExtensionImpl(String extension) {
        MeshWriter ret;
    
        switch(extension) {
            case "off":
                ret = new OFFWriter();
                break;
            case "ply":
                ret = new PLYWriter();
                break;
            default:
                ret = getMeshWriterByExtensionExt(extension);
        }
    
        if(ret == null) {
            throw new UnsupportedOperationException("No appropriate writer could be found");
        }
    
        return ret;
    }
    
    protected MeshWriter getMeshWriterByExtensionExt(String extension) {
        return null;
    }
}
