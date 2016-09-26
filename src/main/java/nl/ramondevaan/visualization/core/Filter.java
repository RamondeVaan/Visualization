package nl.ramondevaan.visualization.core;

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
    
    protected final void setInput(int index, Source<S> input) {
        if(index < inputs.size()) {
            inputs.set(index, input);
        } else if(index == inputs.size()) {
            inputs.add(input);
        } else {
            throw new IndexOutOfBoundsException();
        }
        changed();
    }
    
    @Override
    void update(final long c) throws Exception {
        for(Source<S> s : inputs) {
            if(s != null) {
                s.update(c);
            }
        }
        updateImpl();
    }
    
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
