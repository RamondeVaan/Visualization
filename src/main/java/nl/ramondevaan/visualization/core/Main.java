package nl.ramondevaan.visualization.core;

import nl.ramondevaan.visualization.mesh.*;
import nl.ramondevaan.visualization.point.DensePointSetGenerator;
import nl.ramondevaan.visualization.point.RandomInBoundsPointGenerator;
import nl.ramondevaan.visualization.point.RandomInMeshPointGenerator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.EnumSet;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
//        PLYReader reader = new PLYReader();
//        AddNormalsMeshFilter filter = new AddNormalsMeshFilter();
//        PLYWriter writer = new PLYWriter();
//
//        reader.setPath("C:\\Users\\ramon\\Desktop\\MeshesTestData\\MeshesTestData2015-3\\SmoothingTest\\cylinderLowNF.ply");
//        writer.setPath("C:\\Users\\ramon\\Downloads\\Tiger\\Cylinder1.ply");
//
//        try{
//            reader.update();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        writer.setOrder(null);
//        filter.setInput(reader);
//        writer.setInput(filter);
//
////        filter.setNormalizeMethods(EnumSet.of(AddNormalsMeshFilter.NormalizeMethod.AREA_WEIGHTED,
////                AddNormalsMeshFilter.NormalizeMethod.ANGLE_WEIGHTED));
//
//        try {
//            writer.update();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        PLYReader reader1 = new PLYReader();
        PLYReader reader2 = new PLYReader();
        AddNormalsMeshFilter filter1 = new AddNormalsMeshFilter();
        AddNormalsMeshFilter filter2 = new AddNormalsMeshFilter();
        PLYWriter writer1 = new PLYWriter();
        PLYWriter writer2 = new PLYWriter();

        writer1.setOrder(null);
        writer2.setOrder(null);

        filter1.setInput(reader1);
        filter2.setInput(reader2);
        writer1.setInput(filter1);
        writer2.setInput(filter2);

        reader1.setPath("C:\\Users\\ramon\\Downloads\\Tiger\\DeerNone3.ply");
        reader2.setPath("C:\\Users\\ramon\\Downloads\\Tiger\\DeerNone4.ply");
        writer1.setPath("C:\\Users\\ramon\\Downloads\\Tiger\\DeerNone5.ply");
        writer2.setPath("C:\\Users\\ramon\\Downloads\\Tiger\\DeerNone6.ply");

        try {
            writer1.update();
            writer2.update();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
