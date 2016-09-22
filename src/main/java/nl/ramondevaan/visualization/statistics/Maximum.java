package nl.ramondevaan.visualization.statistics;

import java.io.IOException;
import java.nio.DoubleBuffer;

public class Maximum extends Metric {
    public Maximum() {
        super("Maximum");
    }

    @Override
    protected double computeValue() throws IOException {
        DoubleBuffer values = getInput().getValues();

        if(values.limit() <= 0) {
            return Double.NaN;
        }

        values.rewind();

        double d = values.get();

        while(values.hasRemaining()) {
            d = Math.max(values.get(), d);
        }

        return d;
    }
}
