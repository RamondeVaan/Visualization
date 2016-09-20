package nl.ramondevaan.visualization.mesh;

import org.apache.commons.io.FilenameUtils;

public class MeshReaderFactory {
    public final MeshReader getMeshReader(String filename) {
        if(filename == null) {
            throw new IllegalArgumentException("Filename can not be null");
        }
        
        return getMeshReaderByExtensionImpl(FilenameUtils.getExtension(filename));
    }
    
    public final MeshReader getMeshReaderByExtension(String extension) {
        if(extension == null) {
            throw new IllegalArgumentException("Extension can not be null");
        }
        
        String e = FilenameUtils.getExtension(extension);
        if(e.isEmpty()) {
            return getMeshReaderByExtensionImpl(extension);
        }
        
        return getMeshReaderByExtensionImpl(e);
    }
    
    final MeshReader getMeshReaderByExtensionImpl(String extension) {
        MeshReader ret;
        
        switch(extension) {
            case "off":
                ret = new OFFReader();
                break;
            case "ply":
                ret = new PLYReader();
                break;
            default:
                ret = getMeshReaderByExtensionExt(extension);
        }
        
        if(ret == null) {
            throw new UnsupportedOperationException("No appropriate reader could be found");
        }
        
        return ret;
    }
    
    protected MeshReader getMeshReaderByExtensionExt(String extension) {
        return null;
    }
}
