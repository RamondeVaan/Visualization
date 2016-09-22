package nl.ramondevaan.visualization.statistics;

import java.io.IOException;
import java.nio.DoubleBuffer;

public class StdDev extends Metric {
    public StdDev() {
        super("Standard Deviation");
    }

    @Override
    protected double computeValue() throws IOException {
        DoubleBuffer values = getInput().getValues();

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

        return Math.sqrt(sum / values.limit());
    }
}
