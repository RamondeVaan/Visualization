package nl.ramondevaan.visualization.core;

import nl.ramondevaan.visualization.data.DataInt;
import nl.ramondevaan.visualization.data.DataType;
import nl.ramondevaan.visualization.data.DataUChar;
import nl.ramondevaan.visualization.image.*;
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




//        ImageReader reader = new AutoImageReader();
//        ImageWriter writer = new AutoImageWriter();
//        ImageShifter shifter = new ImageShifter();
//
//        reader.setPath("D:\\Projects\\RegisterImages\\Data\\TRUS3DPhantomSequence0.mhd");
//        writer.setPath("C:\\Users\\ramon\\Desktop\\Test\\Test.mhd");
//        shifter.setShift(new int[]{30, 0, 0});
//
//        try {
//            reader.update();
//            shifter.setInput(reader.getOutput());
//            shifter.update();
//            writer.setImage(shifter.getOutput());
//            writer.update();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        DataType dataType = new DataUChar();
        int dimensionality = 3;
        int[] dimensions = new int[]{2, 2, 2};
        double[] offset = new double[]{0, 0, 0};
        double[] spacing = new double[]{1, 1, 1};
        double[] size = new double[]{1, 1, 1};
        double[] transform = new double[]{1, 0, 0, 0, 1, 0, 0, 0, 1};
        byte[] values = new byte[8];
        Arrays.fill(values, (byte) 255);
        ByteBuffer buffer = ByteBuffer.wrap(values);

        Image image = new Image(dataType, dimensionality,
            dimensions, spacing, size, offset, transform, buffer.array());
        ImageShifter shifter = new ImageShifter();
        shifter.setShift(new int[]{1, 0, 0});
        shifter.setInput(image);
        shifter.update();


//        DataType dataType = new DataInt(ByteOrder.BIG_ENDIAN);
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
//            System.out.println(iterator.get().getInt());
//            iterator.next();
//        }
    }
}
