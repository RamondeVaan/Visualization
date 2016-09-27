package nl.ramondevaan.visualization.statistics;

import java.io.IOException;
import java.nio.DoubleBuffer;

public class Mean extends Metric {
    @Override
    protected Double updateImpl() throws IOException {
        DoubleBuffer values = getInput();

        if(values.limit() <= 0) {
            return Double.NaN;
        }

        values.rewind();

        double sum = 0;
        while(values.hasRemaining()) {
            sum += values.get();
        }

        return sum / values.limit();
    }
}
