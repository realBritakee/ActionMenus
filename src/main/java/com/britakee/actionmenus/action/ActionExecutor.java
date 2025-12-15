package com.britakee.actionmenus.action;

import com.britakee.actionmenus.ActionMenus;
import com.britakee.actionmenus.action.types.DelayAction;
import com.britakee.actionmenus.condition.Condition;
import com.britakee.actionmenus.condition.ConditionContext;
import com.britakee.actionmenus.placeholder.PlaceholderContext;
import com.britakee.actionmenus.placeholder.PlaceholderManager;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Executes actions with proper delay, chance, and condition handling.
 */
public class ActionExecutor {
    
    private final ActionRegistry actionRegistry;
    private final PlaceholderManager placeholderManager;
    private final ScheduledExecutorService scheduler;
    private final Random random = new Random();
    
    public ActionExecutor(ActionRegistry actionRegistry, PlaceholderManager placeholderManager) {
        this.actionRegistry = actionRegistry;
        this.placeholderManager = placeholderManager;
        this.scheduler = Executors.newScheduledThreadPool(2);
    }
    
    /**
     * Execute a single action.
     */
    public void execute(Action action, ActionContext context) {
        if (action == null || context.getPlayer() == null) {
            return;
        }
        
        ServerPlayer player = context.getPlayer();
        
        // Check chance
        if (action.hasChance() && random.nextDouble() > action.getChance()) {
            ActionMenus.LOGGER.debug("Action {} skipped due to chance", action.getType());
            return;
        }
        
        // Check condition
        if (action.hasCondition()) {
            PlaceholderContext placeholderCtx = new PlaceholderContext(player, context.getSession(), context.getArguments());
            ConditionContext condCtx = new ConditionContext(player, placeholderCtx, placeholderManager, 
                    ActionMenus.getInstance().getConditionEvaluator());
            
            if (!action.getCondition().evaluate(condCtx)) {
                // Execute deny actions if any
                if (!action.getDenyActions().isEmpty()) {
                    for (Action denyAction : action.getDenyActions()) {
                        execute(denyAction, context);
                    }
                }
                return;
            }
        }
        
        // Handle delay
        if (action.hasDelay()) {
            scheduleAction(action, context, action.getDelay());
        } else {
            executeNow(action, context);
        }
    }
    
    /**
     * Execute a list of actions.
     */
    public void executeAll(List<Action> actions, ActionContext context) {
        if (actions == null || actions.isEmpty()) {
            return;
        }
        
        int cumulativeDelay = 0;
        
        for (Action action : actions) {
            if (action == null) continue;
            
            // Check if action chain was cancelled
            if (context.isCancelled()) {
                ActionMenus.LOGGER.debug("Action chain cancelled, skipping remaining actions");
                break;
            }
            
            // Delay actions affect subsequent actions
            if (action instanceof DelayAction delayAction) {
                cumulativeDelay += delayAction.getTicks();
                continue;
            }
            
            if (cumulativeDelay > 0) {
                // Add cumulative delay to this action
                int totalDelay = cumulativeDelay + action.getDelay();
                scheduleAction(action, context, totalDelay);
            } else {
                execute(action, context);
            }
        }
    }
    
    /**
     * Execute an action immediately.
     */
    private void executeNow(Action action, ActionContext context) {
        try {
            // Execute on the main server thread
            ServerPlayer player = context.getPlayer();
            if (player.getServer() != null) {
                player.getServer().execute(() -> {
                    try {
                        action.execute(context);
                    } catch (Exception e) {
                        ActionMenus.LOGGER.error("Error executing action {}: {}", action.getType(), e.getMessage(), e);
                    }
                });
            }
        } catch (Exception e) {
            ActionMenus.LOGGER.error("Error scheduling action execution: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Schedule an action for delayed execution.
     */
    private void scheduleAction(Action action, ActionContext context, int delayTicks) {
        // Convert ticks to milliseconds (1 tick = 50ms)
        long delayMs = delayTicks * 50L;
        
        scheduler.schedule(() -> {
            // Check if player is still online
            ServerPlayer player = context.getPlayer();
            if (player == null || player.hasDisconnected()) {
                return;
            }
            
            executeNow(action, context);
        }, delayMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Parse and execute an action string.
     */
    public void executeString(String actionString, ActionContext context) {
        Action action = actionRegistry.parseAction(actionString);
        if (action != null) {
            execute(action, context);
        }
    }
    
    /**
     * Get the placeholder manager.
     */
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }
    
    /**
     * Shutdown the executor.
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}
