package nl.ramondevaan.visualization.mesh;

import nl.ramondevaan.visualization.utilities.DataUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class OFFWriter extends MeshWriter {
    @Override
    protected void write() throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new FileOutputStream(file, false));
    
        pw.println("OFF");
        pw.print(String.valueOf(mesh.coordinates.length));
        pw.print(" ");
        pw.print(String.valueOf(mesh.faces.length));
        pw.println(" 0");
    
        for(int i = 0; i < mesh.coordinates.length; i++) {
            for(int j = 0; j < mesh.dimensionality; j++) {
                pw.print(DataUtils.NUMBER_FORMAT.format(mesh.coordinates[i][j]));
                pw.print(' ');
            }
            pw.println();
        }
        
        for(int i = 0; i < mesh.faces.length; i++) {
            pw.print(mesh.faces[i].length);
            pw.print(' ');
            for(int j = 0; j < mesh.faces[i].length; j++) {
                pw.print(String.valueOf(mesh.faces[i][j]));
                pw.print(' ');
            }
            pw.println();
        }
    
        pw.close();
    }
}
