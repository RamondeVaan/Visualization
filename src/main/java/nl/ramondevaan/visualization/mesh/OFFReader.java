package nl.ramondevaan.visualization.mesh;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OFFReader extends MeshReader {
    private final static String VERTICES_NAME   = "vertex";
    private final static String FACES_NAME      = "face";
    private final static String X_NAME          = "x";
    private final static String Y_NAME          = "y";
    private final static String Z_NAME          = "z";
    private final static String INDICES_NAME    = "vertex_indices";

    private final static char LF = 10;
    private final static char CR = 13;

    private String          lineSeparator;

    private FileInputStream stream;
    private String          tempLine;
    private String          line;


    private int numberOfVertices;
    private int numberOfFaces;

    @Override
    protected final Mesh read() throws IOException {
        resetVariables();

        stream  = new FileInputStream(path);

        FileLock lock = stream.getChannel().tryLock(0, Long.MAX_VALUE, true);

        if(lock == null) {
            throw new IOException("Could not lock file \"" + path + "\"");
        }

        initRead();
        readFormat();

        List<Element> elements = new ArrayList<>();
        elements.add(readVertices());
        elements.add(readFaces());

        stream.close();

        return (new Mesh.Builder())
                .setElements(elements)
                .setVertexId(0)
                .setFaceId(1)
                .setCoordIds(new int[] {0, 1, 2})
                .setIndicesId(0)
                .build();
    }

    private void resetVariables() {
        numberOfVertices    = 0;
        numberOfFaces       = 0;
        tempLine            = "";
    }

    private void readLine() throws IOException {
        do {
            readLineImpl();
        } while(line != null && line.isEmpty() &&
                line.toLowerCase().startsWith("#"));
    }

    private void readLineImpl() throws IOException {
        line = "";
        int curChar;

        do {
            curChar = stream.read();
            if(curChar == -1) {
                break;
            }
            tempLine += (char) curChar;
        } while(!tempLine.endsWith(lineSeparator));

        line = tempLine.trim();
        tempLine = "";
    }

    private void initRead() throws IOException {
        lineSeparator = "";

        String s = "";
        for(int i = 0; i < 3; i++) {
            s += (char) stream.read();
        }

        if(!s.equals("OFF")) {
            throw new IOException("OFF file was of incorrect format");
        }

        char c = (char) stream.read();
        if(c == LF) {
            lineSeparator += LF;
        } else if (c == CR) {
            char n = (char) stream.read();
            lineSeparator += CR;

            if(n == LF) {
                lineSeparator += LF;
            } else {
                tempLine = String.valueOf(n);
            }
        }
        readLine();
    }

    private void readFormat() {
        String[] f = line.split("\\s+");
        if(f.length != 3) {
            throw new IllegalArgumentException("OFF file was of incorrect format");
        }

        numberOfVertices    = Integer.parseInt(f[0]);
        numberOfFaces       = Integer.parseInt(f[1]);
    }

    private Element readVertices() throws IOException {
        ByteBuffer x = ByteBuffer.allocate(PropertyType.DOUBLE.numberOfBytes * numberOfVertices);
        ByteBuffer y = ByteBuffer.allocate(PropertyType.DOUBLE.numberOfBytes * numberOfVertices);
        ByteBuffer z = ByteBuffer.allocate(PropertyType.DOUBLE.numberOfBytes * numberOfVertices);

        for(int i = 0; i < numberOfVertices; i++) {
            readLineImpl();
            String[] s = line.split("\\s+");
            if(s.length != 3) {
                throw new IOException("Expected three values in line \"" + line + "\"");
            }

            x.putDouble(Double.parseDouble(s[0]));
            y.putDouble(Double.parseDouble(s[1]));
            z.putDouble(Double.parseDouble(s[2]));
        }

        x.rewind();
        y.rewind();
        z.rewind();

        return new Element(
                VERTICES_NAME,
                Collections.unmodifiableList(Arrays.asList(
                        new Property(X_NAME, x.asReadOnlyBuffer(), PropertyType.DOUBLE),
                        new Property(Y_NAME, y.asReadOnlyBuffer(), PropertyType.DOUBLE),
                        new Property(Z_NAME, z.asReadOnlyBuffer(), PropertyType.DOUBLE)
                )),
                numberOfVertices
        );
    }

    private Element readFaces() throws IOException {
        int             total   = 0;
        ByteBuffer      inds    = ByteBuffer.allocate(PropertyType.INT.numberOfBytes * numberOfFaces);
        ByteBuffer[]    bufs    = new ByteBuffer[numberOfFaces];

        for(int i = 0; i < numberOfFaces; i++) {
            readLineImpl();
            String[] s = line.split("\\s+");

            if (s.length <= 0) {
                throw new IOException("Could not read face from line \"" + line + "\"");
            }

            int num = Integer.parseInt(s[0]);
            if(num < 1) {
                throw new IOException("Number of values may not be smaller than 1 in line \"" + line + "\"");
            }
            if(num != s.length - 1) {
                throw new IOException("Number of values did not match number of strings in line \"" + line + "\"");
            }

            inds.putInt(num);
            total += num;
            bufs[i] = ByteBuffer.allocate(num * PropertyType.INT.numberOfBytes);

            for(int j = 1; j <= num; j++) {
                bufs[i].putInt(Integer.parseInt(s[j]));
            }
        }
        ByteBuffer all = ByteBuffer.allocate(total * PropertyType.INT.numberOfBytes);
        for(ByteBuffer b : bufs) {
            b.rewind();
            all.put(b);
        }

        inds.rewind();
        all.rewind();

        return new Element(
                FACES_NAME,
                Collections.singletonList(
                        new ListProperty(
                                INDICES_NAME,
                                inds.asReadOnlyBuffer(),
                                PropertyType.INT,
                                PropertyType.INT,
                                all.asReadOnlyBuffer()
                        )
                ),
                numberOfFaces
        );
    }
}
