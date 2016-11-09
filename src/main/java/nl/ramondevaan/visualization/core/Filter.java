package nl.ramondevaan.visualization.core;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;

public abstract class Filter<S, T> extends Source<T> {
    private final List<Source<S>> inputs;
    
    public Filter() {
        inputs = new ArrayList<>();
    }
    
    public Filter(int numberOfInputs) {
        inputs = new ArrayList<>(numberOfInputs);
    }
    
    protected final S getInput(int index) {
        return inputs.get(index).getOutput();
    }
    
    public final void setInput(int index, Source<S> input) {
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
    
    public final void addInput(Source<S> input) {
        Validate.notNull(input);
        inputs.add(input);
    }
    
    public final int getNumberOfInputs() {
        return inputs.size();
    }
    
    @Override
    final void updateLongImpl() throws Exception {
        long maxUpdated = changed;
        long t;
        for(Source<S> s : inputs) {
            if(s != null) {
                t = s.updateLong();
                if(maxUpdated - t < 0) {
                    maxUpdated = t;
                }
            }
        }
        if(updated - maxUpdated <= 0) {
            try {
                output = updateImpl();
                updated = System.nanoTime();
            } catch (Exception e) {
                output = null;
                throw e;
            }
        }
    }
}
