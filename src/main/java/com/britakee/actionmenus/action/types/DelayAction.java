package com.britakee.actionmenus.action.types;

import com.britakee.actionmenus.action.Action;
import com.britakee.actionmenus.action.ActionContext;

/**
 * Action that represents a delay.
 * This action doesn't execute anything itself,
 * but affects the timing of subsequent actions in a sequence.
 */
public class DelayAction extends Action {
    
    private final int ticks;
    
    public DelayAction(int ticks) {
        this.ticks = ticks;
    }
    
    public int getTicks() {
        return ticks;
    }
    
    @Override
    public void execute(ActionContext context) {
        // Delay action doesn't execute anything
        // It's handled by ActionExecutor for sequence timing
    }
    
    @Override
    public String getType() {
        return "delay";
    }
    
    @Override
    public String toString() {
        return "delay{" + ticks + " ticks}";
    }
}
