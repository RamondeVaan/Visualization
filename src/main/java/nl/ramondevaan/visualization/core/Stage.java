package nl.ramondevaan.visualization.core;

public abstract class Stage {
    private boolean running;
    boolean changed;
    
    public Stage() {
        this.changed = true;
    }
    
    protected final void changed() {
        this.changed = true;
    }
    
    public final boolean update() throws Exception {
        if(running) {
            throw new IllegalArgumentException("Component is already running");
        }
        running = true;
        boolean ret = updateBool();
        changed = false;
        running = false;
        return ret;
    }

    abstract boolean updateBool() throws Exception;
}
