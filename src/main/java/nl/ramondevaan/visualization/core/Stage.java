package nl.ramondevaan.visualization.core;

public abstract class Stage {
    private boolean running;
    long changed;
    long updated;
    
    public Stage() {
        this.updated = System.nanoTime();
        this.changed = System.nanoTime();
    }
    
    public final void changed() {
        this.changed = System.nanoTime();
    }
    
    public final boolean update() throws Exception {
        long before = updated;
        long after = updateLong();
        return before - after < 0;
    }
    
    final long updateLong() throws Exception {
        if(running) {
            throw new IllegalArgumentException("Component is already running");
        }
        running = true;
        updateLongImpl();
        running = false;
        return updated;
    }
    
    abstract void updateLongImpl() throws Exception;
}
