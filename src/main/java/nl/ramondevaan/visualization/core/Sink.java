package nl.ramondevaan.visualization.core;

import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class Sink<T> extends Stage {
    private final List<Source<T>> inputs;
    
    public Sink() {
        inputs = new ArrayList<>();
    }
    
    public Sink(int numberOfInputs) {
        inputs = new ArrayList<>(numberOfInputs);
    }

    public final void setInput(int index, Source<T> input) {
        Validate.notNull(input);
        if(index < inputs.size()) {
            inputs.set(index, input);
        } else if(index == inputs.size()) {
            inputs.add(input);
        } else {
            throw new IndexOutOfBoundsException();
        }
        changed();
    }
    
    public final void addInput(Source<T> input) {
        Validate.notNull(input);
        inputs.add(input);
    }
    
    public final int getNumberOfInputs() {
        return inputs.size();
    }
    
    protected final T getInput(int index) {
        return inputs.get(index).getOutput();
    }
    
    @Override
    final void updateLongImpl() throws Exception {
        long maxUpdated = changed;
        long t;
        for(Source<T> s : inputs) {
            if(s != null) {
                t = s.updateLong();
                if(maxUpdated - t < 0) {
                    maxUpdated = t;
                }
            }
        }
        if(updated - maxUpdated < 0) {
            updateImpl();
            updated = System.nanoTime();
        }
    }
    
    protected abstract void updateImpl() throws Exception;
}
