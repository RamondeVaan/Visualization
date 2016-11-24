package nl.ramondevaan.visualization.mesh;

import org.apache.commons.lang3.ArrayUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PLYReader extends MeshReader {
    private final static char LF = 10;
    private final static char CR = 13;

    private String              lineSeparator;

    private FileInputStream     stream;
    private FileChannel         channel;
    private String              tempLine;
    private String              line;

    private boolean             binary;
    private ByteOrder           byteOrder;
    private List<ElementTemp>   elements;

    @Override
    protected Mesh read() throws IOException {
        resetVariables();
        stream  = new FileInputStream(file);
        channel = stream.getChannel();

        FileLock lock = channel.tryLock(0, Long.MAX_VALUE, true);
        if(lock == null) {
            throw new IOException("Could not lock \"" + path + "\".");
        }

        initRead();
        readFormat();

        readLine();
        while(line != null) {
            if(line.equals("end_header")) {
                break;
            } else if(line.startsWith("element")) {
                readElement();
            } else {
                throw new IllegalArgumentException("Unexpected line:" + System.lineSeparator() + line);
            }
        }

        readData();

        stream.close();
        stream  = null;
        channel = null;

        List<Element> actElements = elements.stream().map(e -> new Element(
                e.name,
                Collections.unmodifiableList(e.properties.stream().map(p -> {
                    if(p.total < 0) {
                        return new Property(
                                p.name,
                                p.valueBuffer,
                                p.valueType
                        );
                    } else {
                        return new ListProperty(
                                p.name,
                                p.valueBuffer,
                                p.valueType,
                                p.listType,
                                p.listBuffer
                        );
                    }
                }).collect(Collectors.toList())),
                e.numberOfEntries))
                .collect(Collectors.toList());

        return (new Mesh.Builder())
                .setElements(actElements)
                .setVertexId(PLYUtilities.VERTEX_NAME)
                .setFaceId(PLYUtilities.FACE_NAME)
                .setIndicesId(PLYUtilities.INDICES_NAME)
                .setCoordIds(PLYUtilities.COORD_DIM_NAMES)
                .setNormalIds(PLYUtilities.NORMAL_DIM_NAMES, false)
                .build();
    }

    private void resetVariables() {
        lineSeparator   = "";
        tempLine        = "";
        line            = null;
        binary          = false;
        byteOrder       = ByteOrder.BIG_ENDIAN;
        elements        = new ArrayList<>();
    }

    private void initRead() throws IOException {
        lineSeparator = "";

        String s = "";
        for(int i = 0; i < 3; i++) {
            s += (char) stream.read();
        }

        if(!s.equals("ply")) {
            throw new IOException("PLY file was of incorrect format");
        }

        char c = (char) stream.read();
        if(c == LF) {
            lineSeparator += LF;
        } else if (c == CR) {
            char n = (char) stream.read();
            lineSeparator += CR;

            if(n == LF) {
                lineSeparator += LF;
            } else {
                tempLine = String.valueOf(n);
            }
        }
        readLine();
    }

    private void readFormat() {
        if(!line.toLowerCase().startsWith("format")) {
            throw new IllegalArgumentException("Expected format specification, but found:\n" + line);
        }
        String f = line.replaceAll("\\s+", " ");
        switch(f) {
            case PLYUtilities.FORMAT_ASCII:
                binary = false;
                break;
            case PLYUtilities.FORMAT_LITTLE_ENDIAN:
                binary = true;
                byteOrder = ByteOrder.LITTLE_ENDIAN;
                break;
            case PLYUtilities.FORMAT_BIG_ENDIAN:
                binary = true;
                byteOrder = ByteOrder.BIG_ENDIAN;
                break;
            default:
                throw new IllegalArgumentException("Format \"" + f + "\" is not (yet) supported");
        }
    }

    private void readLine() throws IOException {
        do {
            readLineImpl();
        } while(line != null && !line.isEmpty() &&
                line.toLowerCase().trim().startsWith("comment"));
    }

    private void readLineImpl() throws IOException {
        line = "";
        int curChar;

        do {
            curChar = stream.read();
            if(curChar == -1) {
                break;
            }
            tempLine += (char) curChar;
        } while(!tempLine.endsWith(lineSeparator));

        line = tempLine.trim();
        tempLine = "";
    }

    private void readElement() throws IOException {
        String[] split = line.split("\\s+");
        if(split.length != 3) {
            throw new IllegalArgumentException("Element was of incorrect format");
        }

        String          name            = split[1];
        int             numberOfEntries = Integer.parseInt(split[2]);
        List<PropertyTemp>  properties      = new ArrayList<>();

        for(ElementTemp e : elements) {
            if(e.name.equals(name)) {
                throw new IllegalArgumentException("Element name \"" +
                        name + "\" occurs more than once");
            }
        }
        if(numberOfEntries < 0) {
            throw new IllegalArgumentException("Number of entries for property \"" +
                    name + "\" must be 0 or greater");
        }

        readLine();
        PropertyTemp property;
        while(line.toLowerCase().startsWith("property")) {
            property = readProperty(numberOfEntries);
            for(PropertyTemp p : properties) {
                if(p.name.equals(property.name)) {
                    throw new IllegalArgumentException("Property \"" +
                            p.name + "\" occurs more than once in element \"" +
                            name + "\"");
                }
            }
            properties.add(property);
            readLine();
        }

        elements.add(new ElementTemp(
                name,
                numberOfEntries,
                Collections.unmodifiableList(properties)
        ));
    }

    private PropertyTemp readProperty(int entries) throws IOException {
        String[] split = line.split("\\s+");

        if(split.length == 5) {
            //List property
            if(!split[1].toLowerCase().equals("list")) {
                throw new IllegalArgumentException("Error in parsing property:\n" + line);
            }

            String          name        = split[4];
            PropertyType    dataType    = PropertyType.parse(split[2]);
            PropertyType    listType    = PropertyType.parse(split[3]);
            ByteBuffer      dataBuffer  = ByteBuffer.allocate(entries * dataType.numberOfBytes).order(byteOrder);

            return new PropertyTemp(name, dataType, dataBuffer, listType, new ByteBuffer[entries], 0);
        } else if (split.length == 3) {
            //Single value property
            String          name = split[2];
            PropertyType    type = PropertyType.parse(split[1]);
            ByteBuffer      buff = ByteBuffer.allocate(entries * type.numberOfBytes).order(byteOrder);

            return new PropertyTemp(name, type, buff, null, null, -1);
        } else {
            throw new IllegalArgumentException("Error in parsing property:\n" + line);
        }
    }

    private void readData() throws IOException {
        PropertiesParser parser;

        if(binary) {
            parser = (properties, entry) -> {
                for (PropertyTemp property : properties) {
                    readPropertyBinary(property, entry);
                }
            };
        } else {
            parser = (properties, entry) -> {
                readLine();
                String[] split = line.split("\\s+");

                int numRead = 0;
                for (PropertyTemp property : properties) {
                    split = ArrayUtils.subarray(split, numRead, split.length);
                    numRead = readPropertyASCII(split, property, entry);
                }
            };
        }

        for(ElementTemp e : elements) {
            readElementData(e, parser);
        }
    }

    private void readElementData(ElementTemp e, PropertiesParser parser) throws IOException {
        for(int i = 0; i < e.numberOfEntries; i++) {
            parser.readProperties(e.properties, i);
        }

        e.properties.parallelStream().filter(p -> p.total >= 0).forEach(p -> {
            p.listBuffer = ByteBuffer
                    .allocate(p.total * p.listType.numberOfBytes)
                    .order(byteOrder);

            for(ByteBuffer b : p.listBuffers) {
                b.rewind();
                p.listBuffer.put(b);
            }

            p.listBuffer.rewind();
            p.listType.setByteOrder(p.listBuffer, ByteOrder.BIG_ENDIAN);
        });

        e.properties.parallelStream().forEach(p -> {
            p.valueBuffer.rewind();
            p.valueType.setByteOrder(p.valueBuffer, ByteOrder.BIG_ENDIAN);
        });
    }

    private void readPropertyBinary(PropertyTemp property, int entry) throws IOException {
        int numRead = 0;
        property.valueBuffer.mark();
        property.valueBuffer.limit(property.valueBuffer.position() +
                property.valueType.numberOfBytes);

        while(property.valueBuffer.hasRemaining() && numRead >= 0) {
            numRead = channel.read(property.valueBuffer);
        }
        if(property.valueBuffer.hasRemaining()) {
            throw new IllegalArgumentException("Could not read required number of bytes");
        }

        if(property.total < 0) {
            return;
        }

        property.valueBuffer.reset();
        int numberOfValues = property.valueType.parseInt(property.valueBuffer);
        property.valueBuffer.position(property.valueBuffer.limit());
        if(numberOfValues < 0) {
            throw new IllegalArgumentException("Value count cannot be smaller than 0");
        }

        int bytesToRead             = property.listType.numberOfBytes * numberOfValues;
        property.listBuffers[entry] = ByteBuffer.allocate(bytesToRead).order(byteOrder);

        numRead = 0;

        while(property.listBuffers[entry].hasRemaining() && numRead >= 0) {
            numRead = channel.read(property.listBuffers[entry]);
        }
        if(property.listBuffers[entry].hasRemaining()) {
            throw new IllegalArgumentException("Could not read required number of bytes");
        }

        property.total += numberOfValues;
    }

    private int readPropertyASCII(String[] split, PropertyTemp property,
                                  int entry) throws IOException {
        if(split.length <= 0) {
            throw new IllegalArgumentException("Not enough values in line");
        }
        ByteBuffer val = property.valueType.parseValue(byteOrder, split[0]);
        property.valueBuffer.put(val);

        if(property.total < 0) {
            return 1;
        }

        val.rewind();
        int numberOfValues = property.valueType.parseInt(val);
        if(numberOfValues < 0) {
            throw new IllegalArgumentException("Value count cannot be smaller than 0");
        }

        if(numberOfValues > split.length - 1) {
            throw new IllegalArgumentException("Not enough values in line");
        }

        int bytesToRead             = property.listType.numberOfBytes * numberOfValues;
        property.listBuffers[entry] = ByteBuffer.allocate(bytesToRead);

        for(int i = 1; i <= numberOfValues; i++) {
            property.listType.parseValue(property.listBuffers[entry], split[i]);
        }

        property.total += numberOfValues;
        return numberOfValues + 1;
    }

    private interface PropertiesParser {
        void readProperties(List<PropertyTemp> properties, int entry) throws IOException;
    }

    private static class ElementTemp {
        String              name;
        int                 numberOfEntries;
        List<PropertyTemp>  properties;

        ElementTemp(String name, int numberOfEntries, List<PropertyTemp> properties) {
            this.name = name;
            this.numberOfEntries = numberOfEntries;
            this.properties = properties;
        }
    }

    private static class PropertyTemp {
        String              name;
        PropertyType        valueType;
        ByteBuffer          valueBuffer;
        PropertyType        listType;
        ByteBuffer[]        listBuffers;
        int                 total;
        ByteBuffer          listBuffer;

        PropertyTemp(String name, PropertyType valueType,
                            ByteBuffer valueBuffer, PropertyType listType,
                            ByteBuffer[] listBuffers, int total) {
            this.name = name;
            this.valueType = valueType;
            this.valueBuffer = valueBuffer;
            this.listType = listType;
            this.listBuffers = listBuffers;
            this.total = total;
        }
    }
}
