package nl.ramondevaan.visualization.image;

import java.util.Arrays;

public class ImageShifter {
    private Image input;
    private Image output;
    private int[] shift;
    
    public final void setInput(Image input) {
        this.input = input;
    }
    
    public final void setShift(int[] shift) {
        this.shift = shift;
    }
    
    public final Image getOutput() {
        return output;
    }
    
    public final void update() {
        if(shift == null) {
            throw new IllegalArgumentException("No shift was provided");
        }
        if(input == null) {
            throw new IllegalArgumentException("No input image was provided");
        }
        if(shift.length == 0 || Arrays.equals(new int[shift.length], shift)) {
            output = input.copy();
            return;
        }
        
        final int dimensionality = input.dimensionality;
        if(shift.length < dimensionality) {
            int[] t = new int[dimensionality];
            System.arraycopy(shift, 0, t, 0, shift.length);
            this.shift = t;
        }
        int[] dimSizes = new int[input.dimensionality];
        for(int i = 0; i < input.dimensionality; i++) {
            dimSizes[i] = input.dimensionality + Math.abs(shift[i]);
        }
        int num = 1;
        for(int i = 0; i < dimensionality; i++) {
            num *= dimSizes[i];
        }
        num *= input.dataType.numBytes;
        byte[] values = new byte[num];
    }
}
