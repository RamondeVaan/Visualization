package nl.ramondevaan.visualization.core;

public abstract class Source<T> extends Stage {
    T output;
    
    @Override
    boolean updateBool() throws Exception {
        if(!this.changed) {
            return false;
        }
        try {
            output = updateImpl();
            return true;
        } catch (Exception e) {
            output = null;
            throw e;
        }
    }
    
    protected abstract T updateImpl() throws Exception;
    
    public final T getOutput() {
        return output;
    }
}
