package nl.ramondevaan.visualization.mesh;

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;

public class AutoMeshWriter extends MeshWriter {
    private final static MeshWriterFactory
            DEFAULT_FACTORY = new MeshWriterFactory();
    
    private MeshWriterFactory factory;
    private MeshWriter writer;
    private String lastExt;
    private boolean writerChanged;
    
    public AutoMeshWriter() {
        this.factory = DEFAULT_FACTORY;
    }
    
    public AutoMeshWriter(MeshWriterFactory factory) {
        this.factory = factory == null ?
                DEFAULT_FACTORY : factory;
    }
    
    public final void setFactory(MeshWriterFactory factory) {
        this.factory = factory == null ?
                DEFAULT_FACTORY : factory;
    }
    
    @Override
    protected void write() throws IOException {
        if(pathChanged()) {
            String ext = FilenameUtils.getExtension(path);
            if(!ext.equals(lastExt)) {
                writer = factory.getMeshWriterByExtensionImpl(ext);
                writerChanged = true;
            }
            lastExt = ext;
        }
    
        if(writer == null) {
            throw new UnsupportedOperationException("No appropriate writer was found.");
        }
    
        if(writerChanged || pathChanged) {
            writer.path = path;
            writer.file = file;
        }
        writer.mesh = mesh;
        writer.pathChanged = pathChanged;
        writer.write();
        writerChanged = false;
    }
}
