package nl.ramondevaan.visualization.mesh;

import com.sun.javafx.geom.Vec3d;
import nl.ramondevaan.visualization.core.Filter;
import nl.ramondevaan.visualization.core.Source;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AddNormalsMeshFilter extends Filter<Mesh, Mesh> {
    public enum NormalizeMethod {
        AREA_WEIGHTED, ANGLE_WEIGHTED
    }

    private final static PropertyType   VALUE_TYPE      = PropertyType.DOUBLE;

    private EnumSet<NormalizeMethod> methods;

    public AddNormalsMeshFilter() {
        methods = EnumSet.noneOf(NormalizeMethod.class);
    }

    public final void setNormalizeMethods(Set<NormalizeMethod> methods) {
        if(methods == null) {
            if(!this.methods.isEmpty()) {
                this.methods.clear();
                changed();
            }
        } else if(!this.methods.equals(methods)) {
            this.methods = EnumSet.copyOf(methods);
            changed();
        }
    }

    public final void setInput(Source<Mesh> mesh) {
        setInput(0, mesh);
    }

    @Override
    protected Mesh updateImpl() throws Exception {
        Mesh input = getInput(0);

        if(input.dimensionality != 3) {
            throw new IllegalArgumentException("Can currently only add normals for 3d meshes");
        }
        if(input.getNormalPropertyIndices().capacity() ==
                input.getCoordinatePropertyIndices().capacity()) {
            return input;
        }

        Vec3d[] normals = new Vec3d[input.numberOfVertices];
        for(int i = 0; i <  normals.length; i++) {
            normals[i] = new Vec3d();
        }

        final Vec3dOperator operator1 = methods.contains(NormalizeMethod.AREA_WEIGHTED) ?
                vec -> {
                    double mult = vec.length() / 2;
                    vec.normalize();
                    vec.mul(mult);
                } :
                Vec3d::normalize;
        final Vec3dFaceOperator operator2 = methods.contains(NormalizeMethod.ANGLE_WEIGHTED) ?
                (vec, face, ind) -> {
                    int cnext = (ind + 1) % 3;
                    int cprev = (ind + 2) % 3;

                    Vec3d sub1 = new Vec3d(face[cnext]);
                    Vec3d sub2 = new Vec3d(face[cprev]);

                    sub1.sub(face[ind]);
                    sub2.sub(face[ind]);

                    sub1.normalize();
                    sub2.normalize();

                    vec.mul(Math.acos(sub1.dot(sub2)));
                } :
                (vec, face, ind) -> {
                    //Do nothing
                };

        List<DoubleBuffer> coords = input.coordinates.stream()
                .map(p -> p.type.parseDoubleBuffer(p.getValues()))
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        Collections::unmodifiableList));

        IntBuffer numVerts = input.getNumVerticesInFaces();
        IntBuffer vertInds = input.getVerticesInFaces();

        IntStream.range(0, input.numberOfFaces).parallel().forEach(i -> {
            if(numVerts.get(i) != 3) {
                throw new UnsupportedOperationException("Can only calculcate normals with triangular faces");
            }

            int x = vertInds.get(3 * i);
            int y = vertInds.get(3 * i + 1);
            int z = vertInds.get(3 * i + 2);

            Vec3d[] face = new Vec3d[] {
                    new Vec3d(coords.get(0).get(x), coords.get(1).get(x), coords.get(2).get(x)),
                    new Vec3d(coords.get(0).get(y), coords.get(1).get(y), coords.get(2).get(y)),
                    new Vec3d(coords.get(0).get(z), coords.get(1).get(z), coords.get(2).get(z))
            };

            Vec3d sub10 = new Vec3d();
            Vec3d sub20 = new Vec3d();
            Vec3d cross = new Vec3d();

            sub10.sub(face[1], face[0]);
            sub20.sub(face[2], face[0]);
            cross.cross(sub10, sub20);

            operator1.execute(cross);

            Vec3d vec_x = new Vec3d(cross);
            Vec3d vec_y = new Vec3d(cross);
            Vec3d vec_z = new Vec3d(cross);

            operator2.execute(vec_x, face, 0);
            operator2.execute(vec_y, face, 1);
            operator2.execute(vec_z, face, 2);

            synchronized (normals[x]) {
                normals[x].add(vec_x);
            }
            synchronized (normals[y]) {
                normals[y].add(vec_y);
            }
            synchronized (normals[z]) {
                normals[z].add(vec_z);
            }
        });

        Arrays.stream(normals).parallel().forEach(Vec3d::normalize);

        List<Property> properties = new ArrayList<>();
        properties.addAll(input.vertexElement.properties);

        int[] indices = new int[input.dimensionality];

        List<ByteBuffer> normalBufs = new ArrayList<>();
        for(int i = 0; i < input.dimensionality; i++) {
            normalBufs.add(ByteBuffer.allocate(VALUE_TYPE.numberOfBytes * input.numberOfVertices));
        }
        for(Vec3d vec : normals) {
            normalBufs.get(0).putDouble(vec.x);
            normalBufs.get(1).putDouble(vec.y);
            normalBufs.get(2).putDouble(vec.z);
        }

        List<String> normalNames = getNormalNames(properties);
        for(int i = 0; i < indices.length; i++) {
            indices[i] = properties.size();
            properties.add(new Property(normalNames.get(i), VALUE_TYPE, normalBufs.get(i)));
        }

        List<Element> elements = new ArrayList<>();
        elements.addAll(input.elements);
        elements.set(input.vertexIndicesIndex, new Element(input.vertexElement.name,
                input.numberOfVertices, properties));

        return (new Mesh.Builder())
                .setElements(elements)
                .setVertexId(input.vertexElementIndex)
                .setFaceId(input.faceElementIndex)
                .setIndicesId(input.vertexIndicesIndex)
                .setCoordIds(input.getCoordinatePropertyIndices())
                .setNormalIds(indices)
                .build();
    }

    private List<String> getNormalNames(List<Property> properties) {
        List<String> orig = Arrays.asList("nx", "ny", "nz");
        List<String> ret = new ArrayList<>(orig);

        for(int i = 1; properties.parallelStream().anyMatch(ret::contains); i++) {
            int finalI = i;
            ret = orig.stream().map(s -> s + finalI).collect(Collectors.toList());
        }

        return ret;
    }

    private interface Vec3dOperator {
        void execute(Vec3d vec);
    }

    private interface Vec3dFaceOperator {
        void execute(Vec3d vec, Vec3d[] face, int ind);
    }
}
