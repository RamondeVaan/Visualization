package nl.ramondevaan.visualization.image;

import nl.ramondevaan.visualization.mesh.Mesh;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

public abstract class ImageWriter {
    private String lastPath;
    String path;
    File file;
    Image image;
    boolean pathChanged;
    
    public final void setPath(String path) {
        this.path = FilenameUtils.normalize(path);
        pathChanged = !FilenameUtils
                .equalsOnSystem(this.lastPath, this.path);
    }
    
    public final void setImage(Image image) {
        this.image = image;
    }
    
    protected final boolean pathChanged() {
        return pathChanged;
    }
    
    protected final String getPath() {
        return path;
    }
    
    protected final File getFile() {
        return file;
    }
    
    public final boolean modified() {
        return pathChanged || modifiedExt();
    }
    
    protected boolean modifiedExt() {
        return false;
    }
    
    public final void update() throws IOException {
        if(!modified()) {
            return;
        }
        if(image == null) {
            throw new UnsupportedOperationException("No image was provided");
        }
        if(path == null) {
            throw new UnsupportedOperationException("No path was provided.");
        }
        file = new File(path);
        file.getParentFile().mkdirs();
        write();
        lastPath = path;
        pathChanged = false;
    }
    
    protected abstract void write() throws IOException;
}
