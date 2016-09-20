package nl.ramondevaan.visualization.mesh;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PLYWriter extends MeshWriter {
    private final static String[] AXES = new String[] {
            "x", "y", "z"
    };
    private OutputStream stream;
    
    @Override
    protected void write() throws IOException {
        stream = new FileOutputStream(file);
        
        if(mesh.dimensionality > AXES.length) {
            throw new UnsupportedOperationException("Cannot handle dimensionality > "
                    + AXES.length + " (yet).");
        }
        
        println("ply");
        println("format binary_big_endian 1.0");
        println("element vertex " + mesh.coordinates.length);
        for(int i = 0; i < mesh.dimensionality; i++) {
            println("property float " + AXES[i]);
        }
        println("element face " + mesh.faces.length);
        println("property list uchar int vertex_indices");
        println("end_header");
        
        for(int i = 0; i < mesh.coordinates.length; i++) {
            for(int j = 0; j < mesh.dimensionality; j++) {
                stream.write(ByteBuffer.allocate(4)
                        .order(ByteOrder.BIG_ENDIAN)
                        .putFloat(mesh.coordinates[i][j])
                        .array());
            }
        }
        for(int i = 0; i < mesh.faces.length; i++) {
            stream.write(mesh.faces[i].length);
            for(int j = 0; j < mesh.faces[i].length; j++) {
                stream.write(ByteBuffer.allocate(4)
                        .order(ByteOrder.BIG_ENDIAN)
                        .putInt(mesh.faces[i][j])
                        .array());
            }
        }
    }
    
    private void println(String s) throws IOException {
        stream.write(s.getBytes());
        stream.write('\n');
    }
}
