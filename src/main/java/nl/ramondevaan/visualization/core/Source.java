package nl.ramondevaan.visualization.core;

public abstract class Source<T> extends Stage {
    T output;
    
    @Override
    void updateLongImpl() throws Exception {
        if(updated - changed <= 0) {
            try {
                output = updateImpl();
                updated = System.nanoTime();
            } catch (Exception e) {
                output = null;
                throw e;
            }
        }
    }
    
    protected abstract T updateImpl() throws Exception;
    
    public final T getOutput() {
        return output;
    }
}
