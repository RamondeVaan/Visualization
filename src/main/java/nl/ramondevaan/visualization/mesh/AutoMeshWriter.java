package nl.ramondevaan.visualization.mesh;

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;

public class AutoMeshWriter extends MeshWriter {
    private final static MeshWriterFactory
            DEFAULT_FACTORY = new MeshWriterFactory();
    
    private MeshWriterFactory factory;
    private MeshWriter writer;
    private String lastExt;
    
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
        if(pathChanged) {
            String ext = FilenameUtils.getExtension(path);
            if(!ext.equals(lastExt)) {
                writer = factory.getMeshWriterByExtensionImpl(ext);
            }
            lastExt = ext;
        }
    
        if(writer == null) {
            throw new UnsupportedOperationException("No appropriate writer was found.");
        }
    
        writer.path = path;
        writer.file = file;
        writer.write();
    }
}
