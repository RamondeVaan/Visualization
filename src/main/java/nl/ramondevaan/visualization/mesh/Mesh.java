package nl.ramondevaan.visualization.mesh;

import org.apache.commons.lang3.Validate;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Mesh {
    public  final int               dimensionality;
    public  final int               numberOfVertices;
    public  final int               numberOfFaces;
    public  final List<Element>     elements;

    public  final Element           vertexElement;
    public  final Element           faceElement;
    public  final ListProperty      vertexIndices;
    public  final int               vertexElementIndex;
    public  final int               faceElementIndex;
    public  final int               vertexIndicesIndex;

    public  final List<Property>    coordinates;
    public  final List<Property>    normals;
    private final IntBuffer         coordinatePropertyIndices;
    private final IntBuffer         normalPropertyIndices;

    private final IntBuffer         numVerticesInFaces;
    private final IntBuffer         verticesInFaces;

    Mesh(List<Element> elements, int vertexElementIndex,
         int faceElementIndex, int vertexIndicesIndex,
         int[] coordPropertyIndices, int[] normalPropertyIndices) {
        Validate.notNull(elements);
        Validate.notNull(coordPropertyIndices);

        if(coordPropertyIndices.length < 2) {
            throw new IllegalArgumentException("Dimensionality must be 2 or larger");
        }
        this.dimensionality = coordPropertyIndices.length;

        this.elements           = elements;
        this.vertexElementIndex = vertexElementIndex;
        this.vertexElement      = elements.get(vertexElementIndex);
        this.numberOfVertices   = vertexElement.numberOfEntries;
        this.faceElementIndex   = faceElementIndex;
        this.faceElement        = elements.get(faceElementIndex);
        this.vertexIndicesIndex = vertexIndicesIndex;
        this.vertexIndices      = (ListProperty) faceElement.properties.get(vertexIndicesIndex);
        this.numberOfFaces      = faceElement.numberOfEntries;
        this.numVerticesInFaces = vertexIndices.type
                .parseIntBuffer(vertexIndices.getValues())
                .asReadOnlyBuffer();
        this.verticesInFaces    = vertexIndices.listType
                .parseIntBuffer(vertexIndices.getListBuffer())
                .asReadOnlyBuffer();

        List<Property> coordsTemp = new ArrayList<>();
        IntBuffer coordIndTemp = IntBuffer.allocate(coordPropertyIndices.length);
        for(int i : coordPropertyIndices) {
            Property prop = vertexElement.properties.get(i);
            if(prop instanceof ListProperty) {
                throw new IllegalArgumentException("Coordinate properties may not be list properties");
            }
            coordIndTemp.put(i);
            coordsTemp.add(prop);
        }
        coordIndTemp.rewind();
        this.coordinatePropertyIndices  = coordIndTemp.asReadOnlyBuffer();
        this.coordinates                = Collections.unmodifiableList(coordsTemp);

        if(normalPropertyIndices != null && normalPropertyIndices.length >= coordPropertyIndices.length) {
            List<Property>  normalsTemp = new ArrayList<>();
            IntBuffer       propIndTemp = IntBuffer.allocate(coordPropertyIndices.length);

            for(int i = 0; i < coordPropertyIndices.length; i++) {
                Property prop = vertexElement.properties.get(normalPropertyIndices[i]);
                if(prop instanceof ListProperty) {
                    throw new IllegalArgumentException("Normal properties may not be list properties");
                }
                propIndTemp.put(normalPropertyIndices[i]);
                normalsTemp.add(prop);
            }

            propIndTemp.rewind();
            this.normalPropertyIndices  = propIndTemp.asReadOnlyBuffer();
            this.normals                = Collections.unmodifiableList(normalsTemp);
        } else {
            this.normalPropertyIndices  = IntBuffer.allocate(0);
            this.normals                = Collections.emptyList();
        }
    }

    public final IntBuffer getNumVerticesInFaces() {
        return numVerticesInFaces.duplicate();
    }

    public final IntBuffer getVerticesInFaces() {
        return verticesInFaces.duplicate();
    }

    public final IntBuffer getCoordinatePropertyIndices() {
        return coordinatePropertyIndices.duplicate();
    }

    public final IntBuffer getNormalPropertyIndices() {
        return normalPropertyIndices.duplicate();
    }

    public static class Builder {
        private List<Element>   elements;
        private String          vertexName;
        private String          faceName;
        private String          indicesName;
        private String[]        coordNames;
        private String[]        normalNames;

        private Integer         vertexIndex;
        private Integer         faceIndex;
        private Integer         indicesIndex;
        private int[]           coordIndices;
        private int[]           normalIndices;

        private boolean         throwNormalsError;

        public final Builder setElements(List<Element> elements) {
            this.elements = Element.copyElements(elements);
            return this;
        }

        public final Builder setVertexId(int vertexIndex) {
            this.vertexIndex    = vertexIndex;
            this.vertexName     = null;
            return this;
        }

        public final Builder setVertexId(String vertexName) {
            this.vertexIndex    = null;
            this.vertexName     = vertexName;
            return this;
        }

        public final Builder setFaceId(int faceIndex) {
            this.faceIndex  = faceIndex;
            this.faceName   = null;
            return this;
        }

        public final Builder setFaceId(String faceName) {
            this.faceIndex  = null;
            this.faceName   = faceName;
            return this;
        }

        public final Builder setIndicesId(int indicesIndex) {
            this.indicesIndex   = indicesIndex;
            this.indicesName    = null;
            return this;
        }

        public final Builder setIndicesId(String indicesName) {
            this.indicesIndex   = null;
            this.indicesName    = indicesName;
            return this;
        }

        public final Builder setCoordIds(IntBuffer buffer) {
            this.coordIndices = new int[buffer.remaining()];
            IntBuffer t = buffer.duplicate();

            for(int k = 0; k < this.coordIndices.length; k++) {
                this.coordIndices[k] = t.get();
            }

            return this;
        }

        public final Builder setCoordIds(String[] coordNames) {
            this.coordIndices = null;
            if(coordNames == null) {
                this.coordNames = null;
                return this;
            }
            this.coordNames = new String[coordNames.length];
            System.arraycopy(coordNames, 0, this.coordNames, 0, coordNames.length);
            return this;
        }

        public final Builder setCoordIds(List<String> coordNames) {
            this.coordIndices = null;
            if(coordNames == null) {
                this.coordNames = null;
                return this;
            }
            this.coordNames = new String[coordNames.size()];
            coordNames.toArray(this.coordNames);
            return this;
        }

        public final Builder setCoordIds(int[] coordIndices) {
            this.coordNames = null;
            if(coordIndices == null) {
                this.coordIndices = null;
                return this;
            }
            this.coordIndices = new int[coordIndices.length];
            System.arraycopy(coordIndices, 0, this.coordIndices, 0, coordIndices.length);
            return this;
        }

        public final Builder setNormalIds(IntBuffer buffer, boolean throwError) {
            this.throwNormalsError = throwError;
            this.normalIndices = new int[buffer.remaining()];
            IntBuffer t = buffer.duplicate();

            for(int k = 0; k < this.normalIndices.length; k++) {
                this.normalIndices[k] = t.get();
            }

            return this;
        }

        public final Builder setNormalIds(String[] normalNames, boolean throwError) {
            this.throwNormalsError = throwError;
            return setNormalIds(normalNames);
        }

        public final Builder setNormalIds(List<String> normalNames, boolean throwError) {
            this.throwNormalsError = throwError;
            return setNormalIds(normalNames);
        }

        public final Builder setNormalIds(int[] normalIndices, boolean throwError) {
            this.throwNormalsError = throwError;
            return setNormalIds(normalIndices);
        }

        public final Builder setNormalIds(String[] normalNames) {
            this.normalIndices = null;
            if(normalNames == null) {
                this.normalNames = null;
                return this;
            }
            this.normalNames = new String[normalNames.length];
            System.arraycopy(normalNames, 0, this.normalNames, 0, normalNames.length);
            return this;
        }

        public final Builder setNormalIds(List<String> normalNames) {
            this.normalIndices = null;
            if(normalNames == null) {
                this.normalNames = null;
                return this;
            }
            this.normalNames = new String[normalNames.size()];
            normalNames.toArray(this.normalNames);
            return this;
        }

        public final Builder setNormalIds(int[] normalIndices) {
            this.normalNames = null;
            if(normalIndices == null) {
                this.normalIndices = null;
                return this;
            }
            this.normalIndices = new int[normalIndices.length];
            System.arraycopy(normalIndices, 0, this.normalIndices, 0, normalIndices.length);
            return this;
        }

        public final Mesh build() {
            Validate.notNull(elements);

            for(int i = 0; i < elements.size(); i++) {
                for(int j = i + 1; j < elements.size(); j++) {
                    if(elements.get(i).name.equals(elements.get(j).name)) {
                        throw new IllegalArgumentException("Element name \"" +
                                elements.get(i).name + "\" occurs multiple times");
                    }
                }
            }

            //Find vertex element
            if(vertexIndex == null) {
                Validate.notNull(vertexName);
                vertexIndex = Element.findElement(elements, vertexName);
            }

            //Find coordinate properties
            if(coordIndices == null) {
                Validate.notNull(coordNames);
                coordIndices = elements.get(vertexIndex).findProperties(coordNames);
            }

            //Find normal indices, if necessary.
            //Throw no error if they are not found.
            if(normalIndices == null && normalNames != null) {
                normalIndices = elements.get(vertexIndex).findProperties(normalNames, throwNormalsError);
            }

            if(faceIndex == null) {
                Validate.notNull(faceName);
                faceIndex = Element.findElement(elements, faceName);
            }

            //Find indicesIndex if necessary
            if(indicesIndex == null) {
                Validate.notNull(indicesName);
                indicesIndex = elements.get(faceIndex).findProperty(indicesName);
            }

            return new Mesh(elements, vertexIndex, faceIndex, indicesIndex, coordIndices, normalIndices);
        }
    }
}