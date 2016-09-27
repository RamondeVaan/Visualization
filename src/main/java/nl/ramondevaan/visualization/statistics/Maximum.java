package nl.ramondevaan.visualization.statistics;

import nl.ramondevaan.visualization.core.Filter;

import java.io.IOException;
import java.nio.DoubleBuffer;

public class Maximum extends Metric {
    @Override
    protected Double updateImpl() throws Exception {
        DoubleBuffer b = getInput();

        if(b.limit() <= 0) {
            return Double.NaN;
        }

        b.rewind();

        double max = b.get();
        while(b.hasRemaining()) {
            max = Math.max(max, b.get());
        }

        return max;
    }
}
