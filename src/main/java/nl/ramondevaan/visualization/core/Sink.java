package nl.ramondevaan.visualization.core;

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
        if(index < inputs.size()) {
            inputs.set(index, input);
        } else if(index == inputs.size()) {
            inputs.add(input);
        } else {
            throw new IndexOutOfBoundsException();
        }
        changed();
    }
    
    protected final T getInput(int index) {
        return inputs.get(index).getOutput();
    }
    
    @Override
    void update(final long c) throws Exception {
        for(Source<T> s : inputs) {
            if(s != null) {
                s.update(c);
            }
        }
        updateImpl();
    }
    
    protected abstract void updateImpl() throws Exception;
    
    @Override
    long maxChanged() {
        return Math.max(changed,
                inputs.stream()
                        .filter(tSource -> tSource != null)
                        .mapToLong(Source::maxChanged)
                        .max()
                .orElse(0)
        );
    }
}
