package nl.ramondevaan.visualization.image;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

public abstract class ImageReader {
    private String lastPath;
    String path;
    File file;
    boolean pathChanged;
    private Image image;
    
    public final void setPath(String path) {
        this.path = FilenameUtils.normalize(path);
        pathChanged = !FilenameUtils
                .equalsOnSystem(this.lastPath, this.path);
    }
    
    protected final boolean pathChanged() {
        return pathChanged;
    }
    
    public final Image getOutput() {
        return image;
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
        if(path == null) {
            throw new UnsupportedOperationException("No path was provided.");
        }
        file = new File(path);
        if(!file.exists()) {
            throw new UnsupportedOperationException("Given path does not point to an existing file.");
        }
        image = read();
        lastPath = path;
        pathChanged = false;
    }
    
    protected abstract Image read() throws IOException;
}
