package nl.ramondevaan.visualization.statistics;

import nl.ramondevaan.visualization.core.Filter;
import nl.ramondevaan.visualization.core.Source;

import java.io.IOException;
import java.nio.DoubleBuffer;

public abstract class Metric extends Filter<DoubleBuffer, Double> {
    public Metric() {
        super(1);
    }
    
    public final void setInput(Source<DoubleBuffer> input) {
        setInput(0, input);
    }
    
    protected final DoubleBuffer getInput() {
        return getInput(0);
    }
}
