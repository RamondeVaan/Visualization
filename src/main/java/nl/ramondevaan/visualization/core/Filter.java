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
    final boolean updateBool() throws Exception {
        boolean b = false;
        for(Source<S> s : inputs) {
            if(s != null) {
                b = s.update() || b;
            }
        }
        if(b || changed) {
            try {
                output = updateImpl();
                return true;
            } catch (Exception e) {
                output = null;
                throw e;
            }
        }

        return false;
    }
}
