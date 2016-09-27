package nl.ramondevaan.visualization.statistics;

import java.nio.DoubleBuffer;

public class Minimum extends Metric {
    @Override
    protected Double updateImpl() throws Exception {
        DoubleBuffer b = getInput();

        if(b.limit() <= 0) {
            return Double.NaN;
        }

        b.rewind();

        double min = b.get();
        while(b.hasRemaining()) {
            min = Math.min(min, b.get());
        }

        return min;
    }
}