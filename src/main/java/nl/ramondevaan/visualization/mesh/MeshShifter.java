package nl.ramondevaan.visualization.mesh;

public class MeshShifter {
    private float[] shift;
    private Mesh input;
    private Mesh output;
    private boolean shiftChanged;
    private boolean meshChanged;
    
    public final void setMesh(Mesh mesh) {
        input = mesh;
        meshChanged = true;
    }
    
    private boolean modified() {
        return meshChanged || shiftChanged;
    }
    
    public final void setShift(float[] shift) {
        this.shift = shift;
        shiftChanged = true;
    }
    
    public final Mesh getOutput() {
        return output;
    }
    
    public final void update() {
        if(input == null) {
            throw new UnsupportedOperationException("No mesh was provided");
        }
        if(shift == null) {
            throw new UnsupportedOperationException("No shift was provided");
        }
        
        if(!modified()) {
            return;
        }
        
        float[][] coordinates = input.getCoordinates();
        float[][] newCoordinates = new float[coordinates.length][input.dimensionality];
    
        int j;
        for(int i = 0; i < coordinates.length; i++) {
            for(j = 0; j < Math.min(input.dimensionality, shift.length); j++) {
                newCoordinates[i][j] = coordinates[i][j] + shift[j];
            }
            for(; j < input.dimensionality; j++) {
                newCoordinates[i][j] = coordinates[i][j];
            }
        }
        
        int[][] faces = input.faces;
        int[][] newFaces = new int[faces.length][];
        
        for(int i = 0; i < faces.length; i++) {
            newFaces[i] = new int[faces[i].length];
            System.arraycopy(faces[i], 0, newFaces[i], 0, faces[i].length);
        }
    
        output = new Mesh(input.dimensionality, newCoordinates, newFaces);
        shiftChanged = false;
        meshChanged = false;
    }
}
