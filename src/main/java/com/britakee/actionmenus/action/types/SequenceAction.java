package com.britakee.actionmenus.action.types;

import com.britakee.actionmenus.ActionMenus;
import com.britakee.actionmenus.action.Action;
import com.britakee.actionmenus.action.ActionContext;

import java.util.List;

/**
 * Action that executes a sequence of other actions.
 */
public class SequenceAction extends Action {
    
    private final List<Action> actions;
    
    public SequenceAction(List<Action> actions) {
        this.actions = actions;
    }
    
    @Override
    public void execute(ActionContext context) {
        ActionMenus.getInstance().getActionExecutor().executeAll(actions, context);
    }
    
    @Override
    public String getType() {
        return "sequence";
    }
    
    public List<Action> getActions() {
        return actions;
    }
    
    @Override
    public String toString() {
        return "sequence{" + actions.size() + " actions}";
    }
}
