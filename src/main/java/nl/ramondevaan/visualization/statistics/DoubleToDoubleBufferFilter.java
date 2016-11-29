package nl.ramondevaan.visualization.statistics;

import nl.ramondevaan.visualization.core.Filter;

import java.nio.DoubleBuffer;

public class DoubleToDoubleBufferFilter extends Filter<Double, DoubleBuffer> {
    @Override
    protected DoubleBuffer updateImpl() throws Exception {
        int num = getNumberOfInputs();

        DoubleBuffer ret = DoubleBuffer.allocate(num);

        for(int i = 0; i < num; i++) {
            ret.put(getInput(num));
        }

        ret.rewind();
        return ret.asReadOnlyBuffer();
    }
}
