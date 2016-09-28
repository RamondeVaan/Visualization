package nl.ramondevaan.visualization.statistics;

import nl.ramondevaan.visualization.core.Sink;
import nl.ramondevaan.visualization.core.Source;
import nl.ramondevaan.visualization.mesh.Mesh;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

public abstract class ValueMatrixWriter extends Sink<ValueMatrix> {
    private String lastPath;
    String path;
    File file;
    boolean pathChanged;

    public ValueMatrixWriter() {
        super(1);
    }

    public final void setInput(Source<ValueMatrix> source) {
        setInput(0, source);
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

    @Override
    protected final void updateImpl() throws IOException {
        ValueMatrix matrix = getInput(0);
        if(matrix == null) {
            throw new UnsupportedOperationException("No mesh was provided");
        }
        if(path == null) {
            throw new UnsupportedOperationException("No path was provided.");
        }
        file = new File(path);
        file.getParentFile().mkdirs();
        write(matrix);
        lastPath = path;
        pathChanged = false;
    }

    protected abstract void write(ValueMatrix matrix) throws IOException;
}
