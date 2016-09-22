package nl.ramondevaan.visualization.statistics;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.Arrays;

public class Median extends Metric {
    public Median() {
        super("Median");
    }

    @Override
    protected double computeValue() throws IOException {
        DoubleBuffer values = getInput().getValues();

        if(values.limit() <= 0) {
            return Double.NaN;
        }

        values.rewind();

        double[] d = new double[values.limit()];

        int i = 0;
        while(values.hasRemaining()) {
            d[i++] = values.get();
        }

        Arrays.sort(d);

        if (d.length % 2 == 0) {
            return (d[(d.length / 2) - 1] +
                    d[d.length / 2]) / 2d;
        } else {
            return d[d.length / 2];
        }
    }
}
