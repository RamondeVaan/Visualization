package nl.ramondevaan.visualization.core;

public abstract class Stage {
    long changed;
    long updated;
    
    public Stage() {
        changed = 0;
        updated = 0;
    }
    
    protected final void changed() {
        this.changed = System.currentTimeMillis();
    }
    
    public final void update() throws Exception {
        long c = maxChanged();
        if(c > updated) {
            update(c);
            updated = System.currentTimeMillis();
        }
    }
    
    abstract void update(final long c) throws Exception;
    
    abstract long maxChanged();
}
