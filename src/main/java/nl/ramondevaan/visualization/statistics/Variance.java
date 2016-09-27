package nl.ramondevaan.visualization.statistics;

import java.io.IOException;
import java.nio.DoubleBuffer;

public class Variance extends Metric {
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

        double mean = sum / values.limit();

        values.rewind();
        sum = 0;
        while(values.hasRemaining()) {
            sum += Math.pow(mean - values.get(), 2);
        }

        return sum / values.limit();
    }
}
