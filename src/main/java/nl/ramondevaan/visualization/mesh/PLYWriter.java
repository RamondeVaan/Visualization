package nl.ramondevaan.visualization.mesh;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;

public class PLYWriter extends MeshWriter {
    private final static byte[] LINE_SEP    = System.lineSeparator().getBytes();
    private final static String HEADER      = "ply";
    private final static String END_HEADER  = "end_header";

    private final static String ELEMENT_FORMAT      = "element %1$s %2$d";
    private final static String PROPERTY_FORMAT     = "property %1$s %2$s";
    private final static String PROP_LIST_FORMAT    = "property list %1$s %2$s %3$s";

    private ByteOrder           order;
    private FileOutputStream    stream;
    private FileChannel         channel;

    public PLYWriter() {
        order = ByteOrder.BIG_ENDIAN;
    }

    public final void setOrder(ByteOrder order) {
        if(this.order != order) {
            this.order = order;
            changed();
        }
    }

    @Override
    protected void write(Mesh mesh) throws IOException {
        stream  = new FileOutputStream(path, false);
        channel = stream.getChannel();

        FileLock lock = channel.tryLock(0, Long.MAX_VALUE, false);

        if(lock == null) {
            throw new IOException("Could not lock \"" + path + "\".");
        }

        List<Element> elements = getElements(mesh);

        writeLine(HEADER);
        writeHeader(elements);
        writeLine(END_HEADER);
        writeData(elements);

        stream.close();
    }

    private void writeLine(String s) throws IOException {
        stream.write(s.getBytes());
        stream.write(LINE_SEP);
    }

    private List<Element> getElements(Mesh mesh) throws IOException {
        List<Element> elements = new ArrayList<>();
        elements.addAll(mesh.elements);
        elements.set(mesh.vertexElementIndex, getVertex(mesh));
        elements.set(mesh.faceElementIndex, getFace(mesh));

        return elements;
    }

    private Element getVertex(Mesh mesh) throws IOException {
        List<Property> props    = new ArrayList<>();

        IntBuffer coordInd  = mesh.getCoordinatePropertyIndices();
        IntBuffer normInd   = mesh.getNormalPropertyIndices();

        if(coordInd.remaining() > PLYUtilities.COORD_DIM_NAMES.size()) {
            throw new UnsupportedOperationException("PLY vertices only supports a dimensionality of " +
                    PLYUtilities.COORD_DIM_NAMES.size());
        }
        if(normInd.remaining() > PLYUtilities.NORMAL_DIM_NAMES.size()) {
            throw new UnsupportedOperationException("PLY normals only support a dimensionality of " +
                    PLYUtilities.NORMAL_DIM_NAMES.size());
        }

        props.addAll(mesh.vertexElement.properties);

        for(int i = 0; coordInd.hasRemaining(); i++) {
            int num = coordInd.get();
            props.set(num, new Property(
                    PLYUtilities.COORD_DIM_NAMES.get(i),
                    props.get(num).getValues(),
                    props.get(num).type
            ));
        }
        for(int i = 0; normInd.hasRemaining(); i++) {
            int num = normInd.get();
            props.set(num, new Property(
                    PLYUtilities.NORMAL_DIM_NAMES.get(i),
                    props.get(num).getValues(),
                    props.get(num).type
            ));
        }

        return new Element(PLYUtilities.VERTEX_NAME, props, mesh.vertexElement.numberOfEntries);
    }

    private Element getFace(Mesh mesh) {
        List<Property> props    = new ArrayList<>();

        props.addAll(mesh.faceElement.properties);

        props.set(mesh.vertexIndicesIndex, new ListProperty(PLYUtilities.INDICES_NAME,
                mesh.vertexIndices.getValues(),
                mesh.vertexIndices.type,
                mesh.vertexIndices.listType,
                mesh.vertexIndices.getListBuffer()));

        return new Element(PLYUtilities.FACE_NAME, props, mesh.numberOfFaces);
    }

    private void writeHeader(List<Element> elements) throws IOException {
        if(order == null) {
            writeLine(PLYUtilities.FORMAT_ASCII);
        } else if(order == ByteOrder.BIG_ENDIAN) {
            writeLine(PLYUtilities.FORMAT_BIG_ENDIAN);
        } else {
            writeLine(PLYUtilities.FORMAT_LITTLE_ENDIAN);
        }

        for(Element e : elements) {
            writeLine(String.format(ELEMENT_FORMAT, e.name, e.numberOfEntries));
            for(Property p : e.properties) {
                if(p instanceof ListProperty) {
                    ListProperty lp = (ListProperty) p;
                    writeLine(String.format(PROP_LIST_FORMAT,
                            p.type.name, lp.listType.name, p.name));
                } else {
                    writeLine(String.format(PROPERTY_FORMAT,
                            p.type.name, p.name));
                }
            }
        }
    }

    private void writeData(List<Element> elements) throws IOException {
        for(Element e : elements) {
            List<PropertyTemp> properties = new ArrayList<>();
            ByteOrder usedOrder = order == null ? ByteOrder.BIG_ENDIAN : order;

            for (Property p : e.properties) {
                if (p instanceof ListProperty) {
                    ListProperty lp = (ListProperty) p;
                    properties.add(new PropertyTemp(
                            lp.listType.toByteOrder(lp.getListBuffer(), usedOrder),
                            lp.listType,
                            lp.type.toByteOrder(lp.getValues(), usedOrder),
                            lp.type)
                    );
                } else {
                    properties.add(new PropertyTemp(
                            p.type.toByteOrder(p.getValues(), usedOrder),
                            p.type,
                            null,
                            null)
                    );
                }
            }

            if(order != null) {
                for (int i = 0; i < e.numberOfEntries; i++) {
                    for (PropertyTemp p : properties) {
                        if (p.count != null) {
                            p.countBuffer.limit(p.countBuffer.position() +
                                    p.countType.numberOfBytes);
                            p.values.limit(p.values.position() +
                                    p.count.get() * p.valueType.numberOfBytes);
                            channel.write(p.countBuffer);
                            channel.write(p.values);
                        } else {
                            p.values.limit(p.values.position() +
                                    p.valueType.numberOfBytes);
                            channel.write(p.values);
                        }
                    }
                }
            } else {
                for (int i = 0; i < e.numberOfEntries; i++) {
                    List<String> line = new ArrayList<>();
                    for (PropertyTemp p : properties) {
                        if (p.count != null) {
                            int num = p.count.get();
                            line.add(Integer.toString(num));

                            for(int j = 0; j < num; j++) {
                                int next = p.values.position() + p.valueType.numberOfBytes;
                                p.values.limit(next);
                                line.add(p.valueType.toString(p.values));
                                p.values.position(next);
                            }
                        } else {
                            int next = p.values.position() + p.valueType.numberOfBytes;
                            p.values.limit(next);
                            line.add(p.valueType.toString(p.values));
                            p.values.position(next);
                        }
                    }
                    writeLine(String.join(" ", line));
                }
            }
        }
    }

    private static class PropertyTemp {
        ByteBuffer      values;
        PropertyType    valueType;
        ByteBuffer      countBuffer;
        PropertyType    countType;
        IntBuffer       count;

        PropertyTemp(ByteBuffer values, PropertyType valueType,
                     ByteBuffer countBuffer, PropertyType countType) {
            this.values = values;
            this.valueType = valueType;
            this.countBuffer = countBuffer;
            this.countType = countType;
            if(countBuffer != null) {
                this.count = countType.parseIntBuffer(countBuffer);
            }
        }
    }
}
