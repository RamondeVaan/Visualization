package nl.ramondevaan.visualization.statistics;

import java.io.IOException;

public abstract class Metric {
    private final String name;
    private ValueSource input;
    long changed;
    private long updated;
    private double value;
    
    public Metric(String name) {
        this.name = name;
        changed = System.currentTimeMillis();
        updated = 0L;
    }
    
    public final String getName() {
        return name;
    }
    
    public final void setInput(ValueSource input) {
        this.input = input;
        changed();
    }
    
    public final ValueSource getInput() {
        return input;
    }
    
    public final boolean update() throws IOException {
        if(input.changed > this.changed) {
            this.changed = input.changed;
        }
        input.update();
        if(updated < changed) {
            this.value = computeValue();
            updated = System.currentTimeMillis();
            return true;
        }
        return false;
    }
    
    protected final void changed() {
        changed = System.currentTimeMillis();
    }
    
    protected abstract double computeValue() throws IOException;
    
    public final double getValue() {
        return value;
    }
}
