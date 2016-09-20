package nl.ramondevaan.visualization.mesh;

public class Face {
    private int[] indices;
    
    public Face(int ... indices) {
        this.indices = new int[indices.length];
        System.arraycopy(indices, 0, this.indices, 0, indices.length);
    }
    
    private Face() {
        
    }
    
    static Face getFace(int ... indices) {
        Face face = new Face();
        face.indices = indices;
        return face;
    }
}
