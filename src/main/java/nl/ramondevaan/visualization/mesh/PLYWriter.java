package nl.ramondevaan.visualization.mesh;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class PLYWriter extends MeshWriter {
    private final static String[] AXES = new String[] {
            "x", "y", "z"
    };
    private OutputStream stream;
    
    @Override
    protected void write() throws IOException {
        Mesh mesh = getInput(0);
        stream = new BufferedOutputStream(new FileOutputStream(file));
        
        if(mesh.dimensionality > AXES.length) {
            throw new UnsupportedOperationException("Cannot handle dimensionality > "
                    + AXES.length + " (yet).");
        }
        
        println("ply");
        println("format binary_big_endian 1.0");
        println("element vertex " + mesh.numberOfCoordinates);
        for(int i = 0; i < mesh.dimensionality; i++) {
            println("property float " + AXES[i]);
        }
        println("element face " + mesh.numberOfFaces);
        println("property list uchar int vertex_indices");
        println("end_header");
    
        FloatBuffer cBuf = mesh.coordinatesRead;
        cBuf.rewind();
        
        while(cBuf.hasRemaining()) {
            stream.write(ByteBuffer.allocate(4)
                    .order(ByteOrder.BIG_ENDIAN)
                    .putFloat(cBuf.get())
                    .array());
        }
    
        IntBuffer fBuf = mesh.facesRead;
        fBuf.rewind();
        
        int n, i;
        while(fBuf.hasRemaining()) {
            n = fBuf.get();
            stream.write(n);
            for(i = 0; i < n; i++) {
                stream.write(ByteBuffer.allocate(4)
                        .order(ByteOrder.BIG_ENDIAN)
                        .putInt(fBuf.get())
                        .array());
            }
        }
        
        stream.close();
    }
    
    private void println(String s) throws IOException {
        stream.write(s.getBytes());
        stream.write(System.lineSeparator().getBytes());
    }
}
