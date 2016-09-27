package nl.ramondevaan.visualization.mesh;

import nl.ramondevaan.visualization.utilities.DataUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class OFFWriter extends MeshWriter {
    @Override
    protected void write(Mesh mesh) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new FileOutputStream(file, false));
    
        pw.println("OFF");
        pw.print(String.valueOf(mesh.numberOfCoordinates));
        pw.print(" ");
        pw.print(String.valueOf(mesh.numberOfFaces));
        pw.println(" 0");
    
        FloatBuffer cBuf = mesh.coordinatesRead;
        cBuf.rewind();
        
        while(cBuf.hasRemaining()) {
            pw.print(DataUtils.NUMBER_FORMAT.format(cBuf.get()));
            pw.print(' ');
            pw.print(DataUtils.NUMBER_FORMAT.format(cBuf.get()));
            pw.print(' ');
            pw.println(DataUtils.NUMBER_FORMAT.format(cBuf.get()));
        }
    
        IntBuffer fBuf = mesh.facesRead;
        fBuf.rewind();
        
        int n, i;
        while(fBuf.hasRemaining()) {
            n = fBuf.get();
            pw.print(String.valueOf(n));
            pw.print(' ');
            for(i = 0; i < n - 1; i++) {
                pw.print(String.valueOf(fBuf.get()));
                pw.print(' ');
            }
            pw.println(String.valueOf(fBuf.get()));
        }
    
        pw.close();
    }
}
