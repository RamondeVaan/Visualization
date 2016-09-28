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

    protected final void setInput(int index, Source<T> input) {
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

    protected final void addInput(Source<T> input) {
        Validate.notNull(input);
        inputs.add(input);
    }

    protected final int getNumberOfInputs() {
        return inputs.size();
    }
    
    protected final T getInput(int index) {
        return inputs.get(index).getOutput();
    }
    
    @Override
    final boolean updateBool() throws Exception {
        boolean b = false;
        for(Source<T> s : inputs) {
            if(s != null) {
                b = b || s.update();
            }
        }
        if(b || changed) {
            updateImpl();
            return true;
        }

        return false;
    }
    
    protected abstract void updateImpl() throws Exception;
}
