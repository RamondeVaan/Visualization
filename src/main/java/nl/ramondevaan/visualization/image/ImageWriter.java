package nl.ramondevaan.visualization.image;

import nl.ramondevaan.visualization.core.Sink;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

public abstract class ImageWriter extends Sink<Image> {
    private String lastPath;
    String path;
    File file;
    boolean pathChanged;
    
    public ImageWriter() {
        super(1);
    }
    
    public final void setPath(String path) {
        String p = FilenameUtils.normalize(path);
        if(!FilenameUtils.equalsOnSystem(p, this.path)) {
            this.path = p;
            changed();
            pathChanged = !FilenameUtils
                    .equalsOnSystem(this.lastPath, this.path);
        }
    }
    
    public final String getPath() {
        return path;
    }
    
    public final File getFile() {
        return file;
    }
    
    protected final void updateImpl() throws IOException {
        if(getInput(0) == null) {
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
