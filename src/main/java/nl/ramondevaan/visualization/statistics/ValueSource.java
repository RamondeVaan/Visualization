package nl.ramondevaan.visualization.statistics;

import java.io.IOException;
import java.nio.DoubleBuffer;

public abstract class ValueSource {
    private final String name;
    private ValueSource[] inputs;
    long changed;
    private long updated;
    private DoubleBuffer values;
    private DoubleBuffer valuesReadOnly;
    
    public ValueSource(String name, int numInputs) {
        this.name = name;
        inputs = new ValueSource[numInputs];
        changed = System.currentTimeMillis();
        updated = 0L;
    }
    
    public final String getName() {
        return name;
    }
    
    protected final void setInput(int i, ValueSource input) {
        inputs[i] = input;
        changed();
    }
    
    protected final ValueSource getInput(int i) {
        return inputs[i];
    }
    
    public final boolean update() throws IOException {
        for(ValueSource i : inputs) {
            if(i.changed > this.changed) {
                this.changed = i.changed;
            }
            i.update();
        }
        if(updated < changed) {
            double[] val = computeValues();
            
            this.values = DoubleBuffer.allocate(val == null ? 0 : val.length);
            if(val != null && val.length > 0) {
                this.values.put(val);
            }
            
            this.valuesReadOnly = this.values.asReadOnlyBuffer();
            updated = System.currentTimeMillis();
            return true;
        }
        return false;
    }
    
    protected final void changed() {
        changed = System.currentTimeMillis();
    }
    
    protected abstract double[] computeValues() throws IOException;
    
    public final DoubleBuffer getValues() {
        return valuesReadOnly;
    }
}
