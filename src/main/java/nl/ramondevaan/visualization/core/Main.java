package nl.ramondevaan.visualization.core;

import nl.ramondevaan.visualization.data.DataInt;
import nl.ramondevaan.visualization.data.DataType;
import nl.ramondevaan.visualization.image.Image;
import nl.ramondevaan.visualization.image.ImageRegionIterator;
import nl.ramondevaan.visualization.image.MetaImageReader;
import nl.ramondevaan.visualization.mesh.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
//        MeshReader reader = new AutoMeshReader();
//        MeshWriter writer = new AutoMeshWriter();
//
//        reader.setPath("C:\\Users\\310240452\\SharePoint\\Kustra, Jacek\\itgk\\sharedRamon\\data\\prostate_hd.ply");
//        writer.setPath("C:\\Users\\310240452\\Desktop\\Test.ply");
//
//        MeshShifter shifter = new MeshShifter();
//        shifter.setShift(new float[]{50, 0, 0});
//
//        try {
//            reader.update();
//            shifter.setMesh(reader.getOutput());
//            shifter.update();
//            writer.setMesh(shifter.getOutput());
//            writer.update();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        ByteBuffer b = ByteBuffer.allocate(4).putFloat(4f);
        
        b.rewind();
        System.out.println("MSB: " + b.getFloat());

//        DataType dataType = new DataInt();
//        int dimensionality = 3;
//        int[] dimensions = new int[]{5, 5, 5};
//        double[] offset = new double[]{0, 0, 0};
//        double[] spacing = new double[]{1, 1, 1};
//        double[] size = new double[]{1, 1, 1};
//        double[] transform = new double[]{1, 0, 0, 0, 1, 0, 0, 0, 1};
//        ByteBuffer buffer = ByteBuffer.allocate(500);
//
//
//        int k = 0;
//        for(int x = 0; x < 5; x++) {
//            for(int y = 0; y < 5; y++) {
//                for(int z = 0; z < 5; z++) {
//                    if(x > 0 && x < 4 && y > 0 && y < 4 && z > 0 && z < 4) {
//                        buffer.putInt(k++);
//                    } else {
//                        buffer.putInt(0);
//                    }
//                }
//            }
//        }
//
//        Image image = new Image(dataType, dimensionality,
//                dimensions, spacing, size, offset, transform, buffer.array());
//
//        ImageRegionIterator iterator = new ImageRegionIterator(image, new int[]{1, 3, 1, 3, 1, 3});
//
//        while(iterator.hasNext()) {
//            System.out.print(Arrays.toString(iterator.getIndex()));
//            System.out.print(" -> ");
//            System.out.println(ByteBuffer.wrap(iterator.get()).getInt());
//            iterator.next();
//        }
    }
}
