package nl.ramondevaan.visualization.core;

public abstract class Source<T> extends Stage {
    long changed;
    long updated;
    private T output;
    
    public Source() {
        changed = 0;
        updated = -1;
    }
    
    @Override
    void update(long c) throws Exception {
        try {
            output = updateImpl();
        } catch (Exception e) {
            output = null;
            throw e;
        }
    }
    
    protected abstract T updateImpl() throws Exception;
    
    @Override
    long maxChanged() {
        return changed;
    }
    
    public final T getOutput() {
        return output;
    }
}
