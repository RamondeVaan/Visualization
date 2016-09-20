package nl.ramondevaan.visualization.mesh;

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;

public class AutoMeshReader extends MeshReader {
    private final static MeshReaderFactory
            DEFAULT_FACTORY = new MeshReaderFactory();
    
    private MeshReaderFactory factory;
    private MeshReader reader;
    private boolean readerChanged;
    private String lastExt;
    
    public AutoMeshReader() {
        this.factory = DEFAULT_FACTORY;
    }
   
    public AutoMeshReader(MeshReaderFactory factory) {
        this.factory = factory == null ?
                DEFAULT_FACTORY : factory;
    }
    
    public final void setFactory(MeshReaderFactory factory) {
        this.factory = factory == null ?
                DEFAULT_FACTORY : factory;
    }
    
    @Override
    protected Mesh read() throws IOException {
        if(pathChanged()) {
            String ext = FilenameUtils.getExtension(path);
            if(!ext.equals(lastExt)) {
                reader = factory.getMeshReaderByExtensionImpl(ext);
                readerChanged = true;
            }
            lastExt = ext;
        }
    
        if(reader == null) {
            throw new UnsupportedOperationException("No appropriate reader was found.");
        }
        
        if(readerChanged || pathChanged) {
            reader.path = path;
            reader.file = file;
        }
        reader.pathChanged = pathChanged;
        readerChanged = false;
        return reader.read();
    }
}
