package nl.ramondevaan.visualization.mesh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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
        
        int dimensionality = -1;
        String[] sizes = line.split("\\s+");
        float[][] coordinates = new float[Integer.parseInt(sizes[0])][];
        int[][] faces = new int[Integer.parseInt(sizes[1])][];
        
        String[] coords;
        for(int i = 0; i < coordinates.length; i++) {
            line = reader.readLine();
            if(line == null) {
                reader.close();
                throw new UnsupportedOperationException("OFF file was missing coordinates.");
            }
            coords = line.trim().split("\\s+");
            if(coords.length > dimensionality) {
                dimensionality = coords.length;
                fixFormer(coordinates, i, dimensionality);
            }
            coordinates[i] = new float[dimensionality];
            for(int j = 0; j < dimensionality; j++) {
                coordinates[i][j] = Float.parseFloat(coords[j]);
            }
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
        
        return new Mesh(dimensionality, coordinates, faces);
    }
    
    private static void fixFormer(float[][] values, int before, int dimensionality) {
        for(int i = 0; i < before; i++) {
            if(values[i].length < dimensionality) {
                float[] t = new float[dimensionality];
                System.arraycopy(values[i], 0, t, 0, values[i].length);
                values[i] = t;
            }
        }
    }
}
