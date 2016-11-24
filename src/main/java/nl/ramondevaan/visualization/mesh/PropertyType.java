package nl.ramondevaan.visualization.mesh;

import nl.ramondevaan.visualization.utilities.DataUtils;

import java.nio.*;

public enum PropertyType {
    CHAR    ("char",    1),
    UCHAR   ("uchar",   1),
    SHORT   ("short",   2),
    USHORT  ("ushort",  2),
    INT     ("int",     4),
    UINT    ("uint",    4),
    FLOAT   ("float",   4),
    DOUBLE  ("double",  8);

    public final String name;
    public final int    numberOfBytes;

    PropertyType(String name, int numberOfBytes) {
        this.name = name;
        this.numberOfBytes = numberOfBytes;
    }

    public final void setByteOrder(ByteBuffer buffer, ByteOrder order) {
        if(buffer.order() == order) {
            return;
        }

        switch(this) {
            case CHAR:
            case UCHAR: {
                CharBuffer orig = buffer.asCharBuffer();
                buffer.order(order);
                orig.put(buffer.asCharBuffer());
                break;
            }
            case SHORT:
            case USHORT: {
                ShortBuffer orig = buffer.asShortBuffer();
                buffer.order(order);
                orig.put(buffer.asShortBuffer());
                break;
            }
            case INT:
            case UINT: {
                IntBuffer orig = buffer.asIntBuffer();
                buffer.order(order);
                orig.put(buffer.asIntBuffer());
                break;
            }
            case FLOAT: {
                FloatBuffer orig = buffer.asFloatBuffer();
                buffer.order(order);
                orig.put(buffer.asFloatBuffer());
                break;
            }
            case DOUBLE: {
                DoubleBuffer orig = buffer.asDoubleBuffer();
                buffer.order(order);
                orig.put(buffer.asDoubleBuffer());
                break;
            }
            default:
                throw new UnsupportedOperationException("Endianness conversion for type \"" +
                        name + "\" is not (yet) supported.");
        }
    }

    public ByteBuffer toByteOrder(ByteBuffer buffer, ByteOrder order) {
        if(buffer.order() == order) {
            return buffer.slice().order(order);
        }

        ByteBuffer ret = ByteBuffer.allocate(buffer.remaining()).order(order);
        ByteBuffer cpy = buffer.duplicate().order(buffer.order());

        switch(this) {
            case CHAR:
            case UCHAR:
                ret.asCharBuffer().put(cpy.asCharBuffer());
                break;
            case SHORT:
            case USHORT:
                ret.asShortBuffer().put(cpy.asShortBuffer());
                break;
            case INT:
            case UINT:
                ret.asIntBuffer().put(cpy.asIntBuffer());
                break;
            case FLOAT:
                ret.asFloatBuffer().put(cpy.asFloatBuffer());
                break;
            case DOUBLE:
                ret.asDoubleBuffer().put(cpy.asDoubleBuffer());
                break;
            default:
                throw new UnsupportedOperationException("Endianness conversion for type \"" +
                        name + "\" is not (yet) supported.");
        }

        ret.rewind();
        return ret;
    }

    public final String toString(ByteBuffer buffer) {
        ByteBuffer cpy = buffer.duplicate();

        switch(this) {
            case CHAR:
                return Byte.toString(cpy.get());
            case UCHAR:
                return Integer.toString(Byte.toUnsignedInt(cpy.get()));
            case SHORT:
                return Short.toString(cpy.getShort());
            case USHORT:
                return Integer.toString(Short.toUnsignedInt(cpy.getShort()));
            case INT:
                return Integer.toString(cpy.getInt());
            case UINT:
                return Integer.toUnsignedString(cpy.getInt());
            case FLOAT:
                return DataUtils.NUMBER_FORMAT.format(cpy.getFloat());
            case DOUBLE:
                return DataUtils.NUMBER_FORMAT.format(cpy.getDouble());
            default:
                throw new UnsupportedOperationException("Property type \"" +
                        name + "\" does not (yet) support toString");
        }
    }

    public static PropertyType parse(String s) {
        for(PropertyType p : PropertyType.values()) {
            if(p.name.equals(s)) {
                return p;
            }
        }

        throw new IllegalArgumentException("Could not parse property type \"" + s + "\"");
    }

    public final int parseInt(ByteBuffer buffer) {
        ByteBuffer temp = buffer.duplicate().order(buffer.order());
        switch(this) {
            case CHAR:
                return temp.get();
            case UCHAR:
                return Byte.toUnsignedInt(temp.get());
            case SHORT:
                return temp.getShort();
            case USHORT:
                return Short.toUnsignedInt(temp.getShort());
            case INT:
                return temp.getInt();
            default:
                throw new IllegalArgumentException("Property type \"" +
                        name() + "\" can not be parsed to integer values");
        }
    }

    public IntBuffer parseIntBuffer(ByteBuffer buffer) {
        ByteBuffer temp = buffer.duplicate().order(buffer.order());
        IntBuffer ret = IntBuffer.allocate(buffer.remaining() / numberOfBytes);

        switch(this) {
            case CHAR:
                while(ret.hasRemaining()) {
                    ret.put(temp.get());
                }
                break;
            case UCHAR:
                while(ret.hasRemaining()) {
                    ret.put(Byte.toUnsignedInt(temp.get()));
                }
                break;
            case SHORT:
                while(ret.hasRemaining()) {
                    ret.put(temp.getShort());
                }
                break;
            case USHORT:
                while(ret.hasRemaining()) {
                    ret.put(Short.toUnsignedInt(temp.getShort()));
                }
                break;
            case INT:
                ret.put(temp.asIntBuffer());
                break;
            default:
                throw new IllegalArgumentException("Property type \"" +
                        name() + "\" can not be parsed to integer values");
        }

        ret.rewind();
        return ret;
    }

    public DoubleBuffer parseDoubleBuffer(ByteBuffer buffer) {
        ByteBuffer temp = buffer.duplicate().order(buffer.order());
        DoubleBuffer ret = DoubleBuffer.allocate(buffer.remaining() / numberOfBytes);

        switch(this) {
            case CHAR:
                while(ret.hasRemaining()) {
                    ret.put(temp.get());
                }
                break;
            case UCHAR:
                while(ret.hasRemaining()) {
                    ret.put(Byte.toUnsignedInt(temp.get()));
                }
                break;
            case SHORT:
                while(ret.hasRemaining()) {
                    ret.put(temp.getShort());
                }
                break;
            case USHORT:
                while(ret.hasRemaining()) {
                    ret.put(Short.toUnsignedInt(temp.getShort()));
                }
                break;
            case INT:
                while(ret.hasRemaining()) {
                    ret.put(temp.getInt());
                }
                break;
            case UINT:
                while(ret.hasRemaining()) {
                    ret.put(Integer.toUnsignedLong(temp.getInt()));
                }
                break;
            case FLOAT:
                while(ret.hasRemaining()) {
                    ret.put(temp.getFloat());
                }
                break;
            case DOUBLE:
                ret.put(temp.asDoubleBuffer());
                break;
            default:
                throw new IllegalArgumentException("Property type \"" +
                        name() + "\" can not be parsed to double values");
        }

        ret.rewind();
        return ret;
    }

    public void parseValue(ByteBuffer resultBuffer, String s) {
        switch(this) {
            case CHAR:
                resultBuffer.put(Byte.parseByte(s));
                break;
            case UCHAR:
                short sh = Short.parseShort(s);
                if(sh < 0 || sh > 255) {
                    throw new IllegalArgumentException("Value out of bounds");
                }
                resultBuffer.put((byte) sh);
                break;
            case SHORT:
                resultBuffer.putShort(Short.parseShort(s));
                break;
            case USHORT:
                int i = Integer.parseInt(s);
                if(i < 0 || i > Short.MAX_VALUE) {
                    throw new IllegalArgumentException("Value out of bounds");
                }
                resultBuffer.putShort((short) i);
                break;
            case INT:
                resultBuffer.putInt(Integer.parseInt(s));
                break;
            case UINT:
                resultBuffer.putInt(Integer.parseUnsignedInt(s));
                break;
            case FLOAT:
                resultBuffer.putFloat(Float.parseFloat(s));
                break;
            case DOUBLE:
                resultBuffer.putDouble(Double.parseDouble(s));
                break;
            default:
                throw new UnsupportedOperationException("Parsing is not (yet) implemented for \"" + name + "\".");
        }
    }

    public ByteBuffer parseValue(ByteOrder byteOrder, String s) {
        ByteBuffer ret;

        switch(this) {
            case CHAR:
                ret = ByteBuffer.allocate(1)
                        .put(Byte.parseByte(s));
                break;
            case UCHAR:
                short sh = Short.parseShort(s);
                if(sh < 0 || sh > 255) {
                    throw new IllegalArgumentException("Value out of bounds");
                }
                ret = ByteBuffer.allocate(1)
                        .put((byte) sh);
                break;
            case SHORT:
                ret = ByteBuffer.allocate(numberOfBytes)
                        .order(byteOrder)
                        .putShort(Short.parseShort(s));
                break;
            case USHORT:
                int i = Integer.parseInt(s);
                if(i < 0 || i > Short.MAX_VALUE) {
                    throw new IllegalArgumentException("Value out of bounds");
                }
                ret = ByteBuffer.allocate(numberOfBytes)
                        .order(byteOrder)
                        .putShort((short) i);
                break;
            case INT:
                return ByteBuffer.allocate(numberOfBytes)
                        .order(byteOrder)
                        .putInt(Integer.parseInt(s));
            case UINT:
                ret = ByteBuffer.allocate(numberOfBytes)
                        .order(byteOrder)
                        .putInt(Integer.parseUnsignedInt(s));
                break;
            case FLOAT:
                ret = ByteBuffer.allocate(numberOfBytes)
                        .order(byteOrder)
                        .putFloat(Float.parseFloat(s));
                break;
            case DOUBLE:
                ret = ByteBuffer.allocate(numberOfBytes)
                        .order(byteOrder)
                        .putDouble(Double.parseDouble(s));
                break;
            default:
                throw new UnsupportedOperationException("Parsing is not (yet) implemented for \"" + name + "\".");
        }

        ret.rewind();
        return ret;
    }
}
