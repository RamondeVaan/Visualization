package nl.ramondevaan.visualization.mesh;

import nl.ramondevaan.visualization.core.Source;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

public abstract class MeshReader extends Source<Mesh> {
    String path;
    File file;
    
    public final void setPath(String path) {
        String p = FilenameUtils.normalize(path);
        if(!FilenameUtils.equalsOnSystem(p, this.path)) {
            this.path = p;
            changed();
        }
    }
    
    public final String getPath() {
        return path;
    }
    
    public final File getFile() {
        return file;
    }
    
    protected final Mesh updateImpl() throws IOException {
        if(path == null) {
            throw new UnsupportedOperationException("No path was provided.");
        }
        file = new File(path);
        if(!file.exists()) {
            throw new UnsupportedOperationException("Given path does not point to an existing file.");
        }
        return read();
    }
    
    protected abstract Mesh read() throws IOException;
}
