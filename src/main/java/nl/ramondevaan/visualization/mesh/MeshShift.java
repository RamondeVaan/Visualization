package nl.ramondevaan.visualization.mesh;

import nl.ramondevaan.visualization.core.Filter;
import nl.ramondevaan.visualization.core.Source;
import nl.ramondevaan.visualization.utilities.DataUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class MeshShifter extends Filter<Mesh, Mesh> {
    private float[] shift;
    
    public MeshShifter() {
        super(1);
    }
    
    public final void setInput(Source<Mesh> mesh) {
        setInput(0, mesh);
    }
    
    public final void setShift(float[] shift) {
        if(!Arrays.equals(this.shift, shift)) {
            this.shift = ArrayUtils.clone(shift);
            changed();
        }
    }
    
    @Override
    protected Mesh updateImpl() throws Exception {
        Mesh input = getInput(0);
        
        if(ArrayUtils.isEmpty(shift) ||
                Arrays.equals(new float[shift.length], shift)) {
            return input.copy();
        }
        
        FloatBuffer buf = input.coordinatesRead;
        FloatBuffer coordinates = buf.isDirect() ?
                ByteBuffer.allocateDirect(buf.capacity() * 4)
                        .order(buf.order()).asFloatBuffer() :
                ByteBuffer.allocate(buf.capacity() * 4)
                        .order(buf.order()).asFloatBuffer();
        buf.rewind();
                
        float[] actShift = Arrays.copyOf(shift, 3);
        
        while(buf.hasRemaining()) {
            coordinates.put(buf.get() + actShift[0]);
            coordinates.put(buf.get() + actShift[1]);
            coordinates.put(buf.get() + actShift[2]);
        }
        
        return new Mesh(coordinates, input.numberOfCoordinates,
                DataUtils.clone(input.faces), input.numberOfFaces);
    }
}
