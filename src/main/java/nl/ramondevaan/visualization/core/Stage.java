package nl.ramondevaan.visualization.core;

public abstract class Stage {
    boolean changed;
    
    public Stage() {
        this.changed = true;
    }
    
    protected final void changed() {
        this.changed = true;
    }
    
    public final boolean update() throws Exception {
        boolean ret = updateBool();
        changed = false;
        return ret;
    }

    abstract boolean updateBool() throws Exception;
}
