package nl.ramondevaan.visualization.mesh;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;

public class OFFWriter extends MeshWriter {
    private final static byte[] LINE_SEP    = System.lineSeparator().getBytes();

    private FileOutputStream stream;

    @Override
    protected void write(Mesh mesh) throws IOException {
        if(mesh.dimensionality != 3) {
            throw new UnsupportedOperationException("OFF writer (currently) only supports meshes of dimensionality 3");
        }

        stream  = new FileOutputStream(path, false);

        FileLock lock = stream.getChannel().tryLock(0, Long.MAX_VALUE, false);

        if(lock == null) {
            throw new IllegalArgumentException("Could not lock file");
        }

        writeLine("OFF");
        writeLine(String.join(" ",
                String.valueOf(mesh.numberOfVertices),
                String.valueOf(mesh.numberOfFaces),
                String.valueOf(0)
        ));
        printVertices(mesh);
        printFaces(mesh);


        stream.close();
    }

    private void writeLine(String s) throws IOException {
        stream.write(s.getBytes());
        stream.write(LINE_SEP);
    }

    private void printVertices(Mesh mesh) throws IOException {
        IntBuffer coordInds = mesh.getCoordinatePropertyIndices();

        List<PropertyType> coordTypes   = new ArrayList<>();
        List<ByteBuffer> coordBufs      = new ArrayList<>();
        while(coordInds.hasRemaining()) {
            Property c = mesh.vertexElement.properties.get(coordInds.get());
            coordTypes.add(c.type);
            coordBufs.add(c.getValues());
        }

        for(int entry = 0; entry < mesh.numberOfVertices; entry++) {
            List<String> line = new ArrayList<>();
            for (int i = 0; i < coordBufs.size(); i++) {
                int next = coordBufs.get(i).position() + coordTypes.get(i).numberOfBytes;
                coordBufs.get(i).limit(next);
                line.add(coordTypes.get(i).toString(coordBufs.get(i)));
                coordBufs.get(i).position(next);
            }
            writeLine(String.join(" ", line));
        }
    }

    private void printFaces(Mesh mesh) throws IOException {
        IntBuffer countBuf      = mesh.getNumVerticesInFaces();
        IntBuffer verticesBuf   = mesh.getVerticesInFaces();

        while(countBuf.hasRemaining()) {
            List<String> line = new ArrayList<>();

            int num = countBuf.get();

            line.add(String.valueOf(num));

            for(int i = 0; i < num; i++) {
                line.add(String.valueOf(verticesBuf.get()));
            }

            writeLine(String.join(" ", line));
        }
    }
}
