package nl.ramondevaan.visualization.mesh;

import nl.ramondevaan.visualization.utilities.DataUtils;
import org.apache.commons.lang3.Validate;

import java.nio.ByteBuffer;

public class Property {
    public  final   String          name;
    public  final   PropertyType    type;
    private final   ByteBuffer      values;

    public Property(String name, PropertyType type, ByteBuffer values) {
        Validate.notNull(name);
        Validate.notNull(type);
        Validate.notNull(values);

        this.name = name;
        this.type   = type;
        this.values = DataUtils.clone(values).asReadOnlyBuffer();
        this.values.rewind();
    }

    Property(String name, ByteBuffer values, PropertyType type) {
        this.name   = name;
        this.type   = type;
        this.values = values;
    }

    public final ByteBuffer getValues() {
        return values.duplicate();
    }
}
