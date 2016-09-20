package nl.ramondevaan.visualization.mesh;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PLYReader extends MeshReader {
    private enum DataFormat {
        ASCII, LITTLE_ENDIAN, BIG_ENDIAN;
    }
    
    private final static char LF = 10;
    private final static char CR = 13;
    
    private boolean coordinatesSet;
    private boolean facesSet;
    private float[][] coordinates;
    private int[][] faces;
    private InputStream stream;
    private String line;
    private DataFormat format;
    private int dimensionality;
    
    @Override
    protected Mesh read() throws IOException {
        coordinatesSet = false;
        facesSet = false;
        stream = new BufferedInputStream(new FileInputStream(file));
        line = null;
        
        readLine();
        if(!"ply".equalsIgnoreCase(line)) {
            throw new IllegalArgumentException("PLY file was of incorrect format");
        }
        readLine();
        while(line != null) {
            line = line.trim();
            if(line.equalsIgnoreCase("end_header")) {
                break;
            } else if(line.startsWith("element")) {
                readElement();
            } else if(line.startsWith("format")) {
                readFormat();
                readLine();
            } else if(line.startsWith("comment")) {
                readLine();
            } else {
                throw new IllegalArgumentException("Unexpected line:" + System.lineSeparator() + line);
            }
        }
        if(line == null) {
            throw new IllegalArgumentException("Could not read PLY headers");
        }
        
        if(!coordinatesSet) {
            throw new IllegalArgumentException("No vertex information given in header");
        }
        if(!facesSet) {
            throw new IllegalArgumentException("No face information given in header");
        }
    
        readCoordinates();
        readFaces();
    
        stream.close();
        
        return new Mesh(dimensionality, coordinates, faces);
    }
    
    private void readLine() throws IOException {
        line = "";
        int curChar = stream.read();
        while(curChar != -1 && curChar != LF && curChar != CR) {
            line += ((char) curChar);
            curChar = stream.read();
        }
        if(curChar == CR) {
            stream.mark(1);
            curChar = stream.read();
            if(curChar != LF) {
                stream.reset();
            }
        }
    }
    
    private void readFormat() {
        String f = line.replaceAll("\\s+", " ");
        switch(f) {
            case "format ascii 1.0":
                format = DataFormat.ASCII;
                break;
            case "format binary_little_endian 1.0":
                format = DataFormat.LITTLE_ENDIAN;
                break;
            case "format binary_big_endian 1.0":
                format = DataFormat.BIG_ENDIAN;
                break;
            default:
                throw new IllegalArgumentException("Format \"" + f + "\" is not (yet) supported");
        }
    }
    
    private void readElement() throws IOException {
        String[] split = line.split("\\s+");
        if(split.length < 2) {
            throw new IllegalArgumentException("Element was of incorrect format");
        }
        
        String id = split[1];
        
        if(id.equalsIgnoreCase("vertex")) {
            readVertex(split);
        } else if(id.equalsIgnoreCase("face")) {
            readFace(split);
            readLine();
        } else {
            throw new IllegalArgumentException("Elements other than " +
                    "\"face\" or \"vertex\" are not currently supported");
        }
    }
    
    private void readVertex(String[] split) throws IOException {
        if(coordinatesSet) {
            throw new IllegalArgumentException("Vertex elements were already set");
        }
        if(split.length < 3) {
            throw new IllegalArgumentException("Could not find number of vertices in:" +
                    System.lineSeparator() + line);
        }
        dimensionality = 0;
        readLine();
        while(line != null) {
            line = line.trim();
            if(!line.startsWith("property")) {
                break;
            }
            dimensionality++;
            String[] spl = line.split("\\s+");
            if(spl.length < 2) {
                throw new IllegalArgumentException("Property is of incorrect format");
            }
            if(!spl[1].equalsIgnoreCase("float")) {
                throw new IllegalArgumentException("Currently, only float precision is supported");
            }
            readLine();
        }
        coordinates = new float[Integer.parseInt(split[2])][dimensionality];
        coordinatesSet = true;
    }
    
    private void readFace(String[] split) throws IOException {
        if(facesSet) {
            throw new IllegalArgumentException("Face elements were already set");
        }
        if(split.length < 3) {
            throw new IllegalArgumentException("Could not find number of faces in:" +
                    System.lineSeparator() + line);
        }
        faces = new int[Integer.parseInt(split[2])][];
        readLine();
        if(line == null) {
            throw new IllegalArgumentException("Unexpected eof");
        }
        line = line.trim();
        if(!line.equalsIgnoreCase("property list uchar int vertex_indices")) {
            throw new IllegalArgumentException("Currently only " +
                    "\"property list uchar int vertex_indices\" is accepted for faces");
        }
        facesSet = true;
    }
    
    private void readCoordinates() throws IOException {
        if(format == DataFormat.ASCII) {
            readCoordinatesASCII();
        } else {
            readCoordinatesBinary();
        }
    }
    
    private void readCoordinatesBinary() throws IOException {
        byte[] bytes = new byte[4];
    
        for(int i = 0; i < coordinates.length; i++) {
            for(int j = 0; j < dimensionality; j++) {
                int num = stream.read(bytes, 0, 4);
                if (num < 4) {
                    throw new IOException("Error reading file binary data");
                }
                coordinates[i][j] = ByteBuffer.wrap(bytes).order(format == DataFormat.LITTLE_ENDIAN ?
                        ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).getFloat();
            }
        }
    }
    
    private void readCoordinatesASCII() throws IOException {
        String[] split;
        for(int i = 0; i < coordinates.length; i++) {
            readLine();
            split = line.split("\\s+");
            if(split.length != dimensionality) {
                throw new IOException("File had incorrect format:"
                        + System.lineSeparator() + line);
            }
            for(int j = 0; j < dimensionality; j++) {
                coordinates[i][j] = Float.parseFloat(split[j]);
            }
        }
    }
    
    private void readFaces() throws IOException {
        if(format == DataFormat.ASCII) {
            readFacesASCII();
        } else {
            readFacesBinary();
        }
    }
    
    private void readFacesASCII() throws IOException {
        String[] split;
        for(int i = 0; i < faces.length; i++) {
            readLine();
            split = line.split("\\s+");
            int num = Integer.parseInt(split[0]);
            if(num <= 0) {
                throw new IOException("Faces cannot have 0 or less coordinates");
            }
            if(split.length != num + 1) {
                throw new IOException("Specified number of coordinates " +
                        "was not equal to number of coordinates read");
            }
            faces[i] = new int[num];
            for(int j = 0; j < num; j++) {
                faces[i][j] = Integer.parseInt(split[j + 1]);
            }
        }
    }
    
    private void readFacesBinary() throws IOException {
        int num;
        byte[] vals = new byte[4];
        int read;
        int numFac;
        
        for(int i = 0; i < faces.length; i++) {
            num = stream.read();
            
            if(num <= 0) {
                throw new IOException("Faces cannot have 0 or less coordinates");
            }
            
            faces[i] = new int[num];
            for(int j = 0; j < num; j++) {
                read = stream.read(vals, 0, 4);
                if(read != 4) {
                    throw new IOException("Could not read specified number of bytes");
                }
                
                faces[i][j] = ByteBuffer.wrap(vals).order(format == DataFormat.LITTLE_ENDIAN ?
                        ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).getInt();
            }
        }
    }
}
