package nl.ramondevaan.visualization.statistics;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.Arrays;

public class Median extends Metric {
    @Override
    protected Double updateImpl() {
        DoubleBuffer values = getInput();

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
            int m = d.length / 2;
            return (d[m - 1] + d[m]) / 2d;
        } else {
            return d[d.length / 2];
        }
    }
}
