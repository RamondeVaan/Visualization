package nl.ramondevaan.visualization.mesh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;

public class OFFReader extends MeshReader {
    
    @Override
    protected final Mesh read() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        
        String line;
        
        while((line = reader.readLine()) != null) {
            line = line.trim();
            if(!line.equalsIgnoreCase("OFF") &&
                    !line.startsWith("#")) {
                break;
            }
        }
        if(line == null) {
            throw new IllegalArgumentException("OFF file did not contain headers.");
        }
        
        String[] sizes = line.split("\\s+");
        final int numberOfCoordinates = Integer.parseInt(sizes[0]);
        FloatBuffer coordinates = FloatBuffer.allocate(numberOfCoordinates * 3);
        int[][] faces = new int[Integer.parseInt(sizes[1])][];
        
        String[] coords;
        for(int i = 0; i < numberOfCoordinates; i++) {
            line = reader.readLine();
            if(line == null) {
                reader.close();
                throw new UnsupportedOperationException("OFF file was missing coordinates.");
            }
            coords = line.trim().split("\\s+");
            if(coords.length != 3) {
                throw new IllegalArgumentException("Incorrect coordinate dimensionality");
            }
            coordinates.put(Float.parseFloat(coords[0]));
            coordinates.put(Float.parseFloat(coords[1]));
            coordinates.put(Float.parseFloat(coords[2]));
        }
        
        String[] inds;
        int num;
        for(int i = 0; i < faces.length; i++) {
            line = reader.readLine();
            if(line == null) {
                reader.close();
                throw new UnsupportedOperationException("OFF file was missing faces.");
            }
            inds = line.split("\\s+");
            num = Integer.parseInt(inds[0]);
            if(num != inds.length - 1) {
                throw new IllegalArgumentException("Specified number of faces was not equal" +
                        "to the number of faces read");
            }
            faces[i] = new int[num];
            for(int j = 0; j < num; j++) {
                faces[i][j] = Integer.parseInt(inds[j + 1]);
            }
        }
        
        reader.close();
        
        return new Mesh(coordinates, numberOfCoordinates, faces);
    }
}
