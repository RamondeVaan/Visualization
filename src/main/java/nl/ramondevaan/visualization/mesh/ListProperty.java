package nl.ramondevaan.visualization.mesh;

import nl.ramondevaan.visualization.utilities.DataUtils;
import org.apache.commons.lang3.Validate;

import java.nio.ByteBuffer;

public class ListProperty extends Property {
    public  final   PropertyType  listType;
    private final   ByteBuffer    listBuffer;

    public ListProperty(String name, PropertyType type, ByteBuffer values, PropertyType listType, ByteBuffer listBuffer) {
        super(name, type, values);
        Validate.notNull(listType);
        Validate.notNull(listBuffer);

        this.listType   = listType;
        this.listBuffer = DataUtils.clone(listBuffer).asReadOnlyBuffer();
        this.listBuffer.rewind();
    }

    ListProperty(String name, ByteBuffer values, PropertyType type, PropertyType listType, ByteBuffer listBuffer) {
        super(name, values, type);
        this.listType   = listType;
        this.listBuffer = listBuffer;
    }

    public final ByteBuffer getListBuffer() {
        return listBuffer.duplicate();
    }
}
