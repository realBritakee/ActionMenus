package com.britakee.actionmenus.command;

import com.britakee.actionmenus.ActionMenus;
import com.britakee.actionmenus.action.ActionContext;
import com.britakee.actionmenus.menu.MenuDefinition;
import com.britakee.actionmenus.menu.MenuSession;
import com.britakee.actionmenus.util.TextUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Main command handler for ActionMenus.
 * Registers /actionmenus (alias: /am) and all subcommands.
 */
public class ActionMenusCommand {
    
    private static final String PERMISSION_OPEN = "actionmenus.command.open";
    private static final String PERMISSION_OPEN_OTHERS = "actionmenus.command.open.others";
    private static final String PERMISSION_LIST = "actionmenus.command.list";
    private static final String PERMISSION_RELOAD = "actionmenus.command.reload";
    private static final String PERMISSION_EXECUTE = "actionmenus.command.execute";
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, ActionMenus mod) {
        // Register main command with aliases
        dispatcher.register(Commands.literal("actionmenus")
                .then(Commands.literal("open")
                        .then(Commands.argument("menu", StringArgumentType.word())
                                .suggests(ActionMenusCommand::suggestMenus)
                                .executes(ctx -> openMenu(ctx, mod))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .requires(src -> hasPermission(src, PERMISSION_OPEN_OTHERS))
                                        .executes(ctx -> openMenuForPlayer(ctx, mod))
                                        .then(Commands.argument("args", StringArgumentType.greedyString())
                                                .executes(ctx -> openMenuForPlayerWithArgs(ctx, mod))))))
                .then(Commands.literal("list")
                        .requires(src -> hasPermission(src, PERMISSION_LIST))
                        .executes(ctx -> listMenus(ctx, mod)))
                .then(Commands.literal("reload")
                        .requires(src -> hasPermission(src, PERMISSION_RELOAD))
                        .executes(ctx -> reload(ctx, mod)))
                .then(Commands.literal("execute")
                        .requires(src -> hasPermission(src, PERMISSION_EXECUTE))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("action", StringArgumentType.greedyString())
                                        .executes(ctx -> executeAction(ctx, mod)))))
                .then(Commands.literal("help")
                        .executes(ctx -> showHelp(ctx)))
                .executes(ctx -> showHelp(ctx)));
        
        // Register alias /am
        dispatcher.register(Commands.literal("am")
                .redirect(dispatcher.getRoot().getChild("actionmenus")));
        
        // Register menu command aliases
        registerMenuCommands(dispatcher, mod);
    }
    
    /**
     * Register custom command aliases for menus.
     */
    private static void registerMenuCommands(CommandDispatcher<CommandSourceStack> dispatcher, ActionMenus mod) {
        // This runs after menus are loaded, so we need to register dynamically
        // For now, we'll handle this through a different mechanism
        // Menu commands are registered when configs are loaded
    }
    
    /**
     * Re-register menu commands after reload.
     */
    public static void registerMenuAliases(CommandDispatcher<CommandSourceStack> dispatcher, ActionMenus mod) {
        for (MenuDefinition menu : mod.getMenuRegistry().getAllMenus()) {
            for (String command : menu.getCommands()) {
                String cmd = command.replace("/", "");
                
                // Register the command
                try {
                    dispatcher.register(Commands.literal(cmd)
                            .executes(ctx -> {
                                try {
                                    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                        return mod.getMenuManager().openMenu(player, menu, null) ? 1 : 0;
                                    }
                                    ctx.getSource().sendFailure(Component.literal("This command can only be run by a player."));
                                    return 0;
                                } catch (Exception e) {
                                    ActionMenus.LOGGER.error("Error opening menu '{}': ", menu.getId(), e);
                                    ctx.getSource().sendFailure(Component.literal("Error opening menu: " + e.getMessage()));
                                    return 0;
                                }
                            })
                            .then(Commands.argument("args", StringArgumentType.greedyString())
                                    .executes(ctx -> {
                                        try {
                                            if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                                String args = StringArgumentType.getString(ctx, "args");
                                                return mod.getMenuManager().openMenu(player, menu, args.split("\\s+")) ? 1 : 0;
                                            }
                                            return 0;
                                        } catch (Exception e) {
                                            ActionMenus.LOGGER.error("Error opening menu '{}' with args: ", menu.getId(), e);
                                            ctx.getSource().sendFailure(Component.literal("Error opening menu: " + e.getMessage()));
                                            return 0;
                                        }
                                    })));
                } catch (Exception e) {
                    ActionMenus.LOGGER.warn("Failed to register command alias '{}' for menu '{}': {}", 
                            cmd, menu.getId(), e.getMessage());
                }
            }
        }
    }
    
    private static boolean hasPermission(CommandSourceStack source, String permission) {
        if (source.getEntity() instanceof ServerPlayer player) {
            return ActionMenus.getInstance().getPermissionManager().hasPermission(player, permission);
        }
        return source.hasPermission(2); // Console always has permission
    }
    
    private static CompletableFuture<Suggestions> suggestMenus(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(
                ActionMenus.getInstance().getMenuRegistry().getMenuIds(),
                builder
        );
    }
    
    /**
     * /am open <menu>
     */
    private static int openMenu(CommandContext<CommandSourceStack> ctx, ActionMenus mod) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            ctx.getSource().sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        
        String menuId = StringArgumentType.getString(ctx, "menu");
        
        if (mod.getMenuManager().openMenu(player, menuId)) {
            return 1;
        } else {
            ctx.getSource().sendFailure(TextUtil.colorize("&cFailed to open menu: " + menuId));
            return 0;
        }
    }
    
    /**
     * /am open <menu> <player>
     */
    private static int openMenuForPlayer(CommandContext<CommandSourceStack> ctx, ActionMenus mod) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        String menuId = StringArgumentType.getString(ctx, "menu");
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        
        if (mod.getMenuManager().openMenu(target, menuId)) {
            ctx.getSource().sendSuccess(() -> TextUtil.colorize("&aOpened menu " + menuId + " for " + target.getName().getString()), true);
            return 1;
        } else {
            ctx.getSource().sendFailure(TextUtil.colorize("&cFailed to open menu: " + menuId));
            return 0;
        }
    }
    
    /**
     * /am open <menu> <player> <args...>
     */
    private static int openMenuForPlayerWithArgs(CommandContext<CommandSourceStack> ctx, ActionMenus mod) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        String menuId = StringArgumentType.getString(ctx, "menu");
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        String argsStr = StringArgumentType.getString(ctx, "args");
        String[] args = argsStr.isEmpty() ? new String[0] : argsStr.split("\\s+");
        
        if (mod.getMenuManager().openMenu(target, menuId, args)) {
            ctx.getSource().sendSuccess(() -> TextUtil.colorize("&aOpened menu " + menuId + " for " + target.getName().getString()), true);
            return 1;
        } else {
            ctx.getSource().sendFailure(TextUtil.colorize("&cFailed to open menu: " + menuId));
            return 0;
        }
    }
    
    /**
     * /am list
     */
    private static int listMenus(CommandContext<CommandSourceStack> ctx, ActionMenus mod) {
        Collection<MenuDefinition> menus = mod.getMenuRegistry().getAllMenus();
        
        ctx.getSource().sendSuccess(() -> TextUtil.colorize("&6=== ActionMenus (" + menus.size() + " menus) ==="), false);
        
        for (MenuDefinition menu : menus) {
            String commands = menu.getCommands().isEmpty() ? "" : " &7(" + String.join(", ", menu.getCommands()) + ")";
            ctx.getSource().sendSuccess(() -> TextUtil.colorize("&e- " + menu.getId() + "&r: &f" + menu.getTitle() + commands), false);
        }
        
        return menus.size();
    }
    
    /**
     * /am reload
     */
    private static int reload(CommandContext<CommandSourceStack> ctx, ActionMenus mod) {
        long start = System.currentTimeMillis();
        
        mod.getConfigManager().reloadAll();
        
        long duration = System.currentTimeMillis() - start;
        int menuCount = mod.getMenuRegistry().getMenuCount();
        
        ctx.getSource().sendSuccess(() -> TextUtil.colorize(
                "&aReloaded ActionMenus! &7(" + menuCount + " menus in " + duration + "ms)"), true);
        
        return menuCount;
    }
    
    /**
     * /am execute <player> <action>
     */
    private static int executeAction(CommandContext<CommandSourceStack> ctx, ActionMenus mod) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        String actionStr = StringArgumentType.getString(ctx, "action");
        
        MenuSession session = mod.getMenuManager().getSession(target);
        ActionContext actionCtx = new ActionContext(target, session, session != null ? session.getMenu() : null, -1);
        
        mod.getActionExecutor().executeString(actionStr, actionCtx);
        
        ctx.getSource().sendSuccess(() -> TextUtil.colorize("&aExecuted action on " + target.getName().getString()), true);
        return 1;
    }
    
    /**
     * /am help
     */
    private static int showHelp(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> TextUtil.colorize("&6=== ActionMenus Help ==="), false);
        ctx.getSource().sendSuccess(() -> TextUtil.colorize("&e/am open <menu> &7- Open a menu"), false);
        ctx.getSource().sendSuccess(() -> TextUtil.colorize("&e/am open <menu> <player> &7- Open a menu for another player"), false);
        ctx.getSource().sendSuccess(() -> TextUtil.colorize("&e/am list &7- List all menus"), false);
        ctx.getSource().sendSuccess(() -> TextUtil.colorize("&e/am reload &7- Reload configurations"), false);
        ctx.getSource().sendSuccess(() -> TextUtil.colorize("&e/am execute <player> <action> &7- Execute an action"), false);
        return 1;
    }
}
