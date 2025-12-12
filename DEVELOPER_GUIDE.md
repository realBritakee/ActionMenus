# ActionMenus Developer Guide

## Complete Server-Side GUI Menu System for NeoForge 1.21.1

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Project Structure](#project-structure)
4. [Installation & Building](#installation--building)
5. [Configuration Reference](#configuration-reference)
6. [Placeholder System](#placeholder-system)
7. [Condition System](#condition-system)
8. [Action System](#action-system)
9. [API & Extensibility](#api--extensibility)
10. [Example Menus](#example-menus)
11. [Troubleshooting](#troubleshooting)

---

## Overview

ActionMenus is a server-side NeoForge mod that replicates the functionality of the popular Bukkit plugin DeluxeMenus. It allows server administrators to create custom inventory-based GUI menus through JSON configuration files, without requiring any client-side modifications.

### Key Features

- **Config-Driven**: All menus defined in JSON files
- **Dynamic Placeholders**: Live-updating player stats, server info, math expressions
- **Conditional Logic**: Show/hide items based on permissions, conditions, expressions
- **Powerful Actions**: Commands, messages, sounds, menu navigation, item giving
- **Auto-Refresh**: Menus automatically update dynamic content (playtime timers, etc.)
- **Custom Commands**: Each menu can define its own command aliases
- **Server-Side Only**: Works with vanilla clients

### Requirements

- Minecraft 1.21.1
- NeoForge 21.1.x
- Java 21

---

## Architecture

### Architecture Diagram (Text)

```
┌─────────────────────────────────────────────────────────────────────┐
│                         ActionMenus Mod                              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐          │
│  │   Config     │    │    Menu      │    │  Placeholder │          │
│  │   Manager    │───▶│   Registry   │◀───│   Manager    │          │
│  └──────────────┘    └──────────────┘    └──────────────┘          │
│         │                   │                   │                   │
│         ▼                   ▼                   ▼                   │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐          │
│  │    JSON      │    │    Menu      │    │  Placeholder │          │
│  │   Parser     │    │   Manager    │    │  Providers   │          │
│  └──────────────┘    └──────────────┘    └──────────────┘          │
│                            │                                        │
│         ┌──────────────────┼──────────────────┐                    │
│         ▼                  ▼                  ▼                    │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐          │
│  │  Condition   │    │   Action     │    │   Command    │          │
│  │  Evaluator   │    │  Executor    │    │   Handler    │          │
│  └──────────────┘    └──────────────┘    └──────────────┘          │
│         │                  │                                        │
│         ▼                  ▼                                        │
│  ┌──────────────┐    ┌──────────────┐                              │
│  │  Condition   │    │   Action     │                              │
│  │   Types      │    │   Types      │                              │
│  └──────────────┘    └──────────────┘                              │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### Core Components

| Component | Purpose |
|-----------|---------|
| **ConfigManager** | Loads and parses JSON menu configurations |
| **MenuRegistry** | Stores all loaded menu definitions |
| **MenuManager** | Opens, closes, updates menus for players |
| **MenuSession** | Tracks state for each open menu per player |
| **PlaceholderManager** | Resolves dynamic placeholders |
| **ConditionEvaluator** | Evaluates conditions for visibility/actions |
| **ActionExecutor** | Executes actions with delays, chances, conditions |
| **PermissionManager** | Simple permission checking with OP fallback |

### Data Flow

1. **Config Loading**: JSON files → ConfigManager → MenuConfigParser → MenuDefinition
2. **Menu Opening**: Player command → MenuManager → Create session → Build inventory → Send to client
3. **Click Handling**: Client click → ActionMenuChestMenu → MenuManager → ActionExecutor
4. **Auto-Update**: ServerTick → MenuUpdateScheduler → MenuManager.updateMenu()

---

## Project Structure

```
actionmenus/
├── build.gradle                    # NeoForge build configuration
├── settings.gradle                 # Gradle settings
├── gradle.properties               # Gradle properties
│
└── src/main/
    ├── java/com/britakee/actionmenus/
    │   │
    │   ├── ActionMenus.java        # Main mod class
    │   │
    │   ├── config/                 # Configuration loading
    │   │   ├── ConfigManager.java
    │   │   ├── MenuConfigParser.java
    │   │   └── model/
    │   │       ├── MenuConfig.java
    │   │       ├── ItemConfig.java
    │   │       ├── ActionConfig.java
    │   │       └── ConditionConfig.java
    │   │
    │   ├── menu/                   # Menu system
    │   │   ├── MenuManager.java
    │   │   ├── MenuRegistry.java
    │   │   ├── MenuDefinition.java
    │   │   ├── MenuItem.java
    │   │   ├── MenuSession.java
    │   │   ├── ClickType.java
    │   │   ├── ActionMenuContainer.java
    │   │   ├── ActionMenuChestMenu.java
    │   │   └── MenuUpdateScheduler.java
    │   │
    │   ├── placeholder/            # Placeholder system
    │   │   ├── PlaceholderManager.java
    │   │   ├── PlaceholderProvider.java
    │   │   ├── PlaceholderContext.java
    │   │   └── providers/
    │   │       ├── PlayerPlaceholderProvider.java
    │   │       ├── ServerPlaceholderProvider.java
    │   │       ├── DateTimePlaceholderProvider.java
    │   │       ├── MathPlaceholderProvider.java
    │   │       └── SessionPlaceholderProvider.java
    │   │
    │   ├── condition/              # Condition evaluation
    │   │   ├── Condition.java
    │   │   ├── ConditionContext.java
    │   │   └── ConditionEvaluator.java
    │   │
    │   ├── action/                 # Action execution
    │   │   ├── Action.java
    │   │   ├── ActionContext.java
    │   │   ├── ActionRegistry.java
    │   │   ├── ActionExecutor.java
    │   │   └── types/
    │   │       ├── CommandAction.java
    │   │       ├── MessageAction.java
    │   │       ├── OpenMenuAction.java
    │   │       ├── CloseAction.java
    │   │       ├── SoundAction.java
    │   │       ├── GiveItemAction.java
    │   │       ├── TitleAction.java
    │   │       └── ... (more action types)
    │   │
    │   ├── command/                # Command handling
    │   │   └── ActionMenusCommand.java
    │   │
    │   ├── permission/             # Permission system
    │   │   └── PermissionManager.java
    │   │
    │   └── util/                   # Utilities
    │       ├── TextUtil.java
    │       └── ItemBuilder.java
    │
    └── resources/
        ├── META-INF/neoforge.mods.toml
        ├── pack.mcmeta
        ├── actionmenus.mixins.json
        └── data/actionmenus/default_configs/menus/
            ├── example_main_menu.json
            ├── example_shop.json
            └── example_player_info.json
```

---

## Installation & Building

### Prerequisites

1. Java Development Kit 21
2. An IDE (IntelliJ IDEA recommended)

### Building from Source

```bash
# Clone/download the project
cd actionmenus

# Build the mod
./gradlew build

# The JAR will be in build/libs/actionmenus-1.0.0.jar
```

### Development Setup

```bash
# Generate IDE project files
./gradlew genIntellijRuns  # For IntelliJ IDEA
./gradlew genEclipseRuns   # For Eclipse

# Run the client
./gradlew runClient

# Run the server
./gradlew runServer
```

### Deploying

1. Build the JAR: `./gradlew build`
2. Copy `build/libs/actionmenus-1.0.0.jar` to your server's `mods/` folder
3. Start the server
4. Menu configs will be created in `config/actionmenus/menus/`

---

## Configuration Reference

### Menu Configuration

Menus are defined in JSON files in `config/actionmenus/menus/`.

```json
{
  "id": "my_menu",
  "title": "&6&lMy Menu Title",
  "rows": 3,
  "updateInterval": 20,
  "commands": ["mymenu", "mm"],
  "permission": "myplugin.menu.access",
  
  "settings": {
    "preventClose": false,
    "updateOnClick": true
  },
  
  "openRequirements": {
    "permission": "some.permission"
  },
  
  "openActions": [
    "[sound] minecraft:ui.button.click 1.0 1.0"
  ],
  
  "closeActions": [
    "[message] &7You closed the menu"
  ],
  
  "filler": {
    "material": "minecraft:gray_stained_glass_pane",
    "name": " "
  },
  
  "items": {
    "item_id": { ... }
  }
}
```

### Menu Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | string | Yes | Unique menu identifier |
| `title` | string | Yes | Menu title (supports colors & placeholders) |
| `rows` | int | No | Number of rows (1-6), default 3 |
| `updateInterval` | int | No | Ticks between updates (20 = 1 second) |
| `commands` | array | No | Command aliases to open this menu |
| `permission` | string | No | Permission required to open |
| `settings` | object | No | Menu-specific settings |
| `openRequirements` | object | No | Conditions to open the menu |
| `openActions` | array | No | Actions when menu opens |
| `closeActions` | array | No | Actions when menu closes |
| `filler` | object | No | Filler item configuration |
| `items` | object | Yes | Map of item ID to item config |

### Item Configuration

```json
{
  "slot": 13,
  "slots": [10, 11, 12],
  "material": "minecraft:diamond",
  "amount": 1,
  "name": "&b&lDiamond",
  "lore": [
    "&7A precious gem",
    "",
    "&eClick to interact!"
  ],
  "customModelData": 0,
  "enchanted": true,
  "skullOwner": "%player_name%",
  "skullTexture": "base64...",
  "itemFlags": ["HIDE_ATTRIBUTES"],
  "dynamic": true,
  "priority": 0,
  
  "viewRequirement": { ... },
  "clickRequirement": { ... },
  
  "clickActions": [ ... ],
  "leftClickActions": [ ... ],
  "rightClickActions": [ ... ],
  "shiftLeftClickActions": [ ... ],
  "shiftRightClickActions": [ ... ],
  "middleClickActions": [ ... ]
}
```

### Item Fields

| Field | Type | Description |
|-------|------|-------------|
| `slot` | int | Single slot position (0-53) |
| `slots` | array | Multiple slot positions |
| `material` | string | Item material ID (e.g., "minecraft:diamond") |
| `amount` | int | Stack size (1-64) |
| `name` | string | Display name with color codes |
| `lore` | array | Lore lines |
| `customModelData` | int | Custom model data for resource packs |
| `enchanted` | bool | Add enchantment glint |
| `skullOwner` | string | Player name for player heads |
| `skullTexture` | string | Base64 texture for custom heads |
| `itemFlags` | array | Item flags to apply |
| `dynamic` | bool | Force dynamic updates |
| `priority` | int | Priority for slot conflicts |
| `viewRequirement` | object | Condition to show this item |
| `clickRequirement` | object | Condition to execute click actions |
| `*ClickActions` | array | Actions for different click types |

---

## Placeholder System

Placeholders use the format `%category_placeholder%` or `%placeholder%`.

### Player Placeholders (`%player_*%`)

| Placeholder | Description |
|-------------|-------------|
| `%player_name%` | Player's display name |
| `%player_uuid%` | Player's UUID |
| `%player_health%` | Current health |
| `%player_max_health%` | Maximum health |
| `%player_health_percent%` | Health as percentage |
| `%player_food%` | Food level (0-20) |
| `%player_saturation%` | Saturation level |
| `%player_level%` | Experience level |
| `%player_exp%` | Total experience |
| `%player_x%` | X coordinate |
| `%player_y%` | Y coordinate |
| `%player_z%` | Z coordinate |
| `%player_world%` | Current dimension |
| `%player_gamemode%` | Current game mode |
| `%player_ping%` | Network ping (ms) |
| `%player_playtime_formatted%` | Formatted playtime (e.g., "2h 30m") |
| `%player_playtime_hours%` | Playtime in hours |
| `%player_armor%` | Armor points |
| `%player_is_flying%` | Whether flying |
| `%player_main_hand%` | Item in main hand |

### Server Placeholders (`%server_*%`)

| Placeholder | Description |
|-------------|-------------|
| `%server_online%` | Online player count |
| `%server_max%` | Max player slots |
| `%server_tps%` | Server TPS |
| `%server_tps_color%` | TPS color code |
| `%server_mspt%` | Milliseconds per tick |
| `%server_uptime%` | Server uptime |
| `%server_ram_used%` | RAM usage |
| `%server_ram_max%` | Max RAM |
| `%server_ram_percent%` | RAM usage percentage |

### Time Placeholders (`%time_*%`)

| Placeholder | Description |
|-------------|-------------|
| `%time_hour%` | Current hour (00-23) |
| `%time_hour12%` | Hour (01-12) |
| `%time_minute%` | Current minute |
| `%time_second%` | Current second |
| `%time_ampm%` | AM/PM |
| `%time_day%` | Day of month |
| `%time_month%` | Month number |
| `%time_month_name%` | Month name |
| `%time_year%` | Year |
| `%time_day_of_week%` | Day name |
| `%time_formatted%` | HH:mm:ss |
| `%time_minecraft_time%` | In-game time |
| `%time_is_day%` | Is daytime |

### Math Placeholders (`%math_*%`)

| Placeholder | Description |
|-------------|-------------|
| `%math_5+3%` | Basic math (= 8) |
| `%math_10/2%` | Division |
| `%math_(2+3)*4%` | Parentheses |
| `%math_round(3.7)%` | Round (= 4) |
| `%math_floor(3.7)%` | Floor (= 3) |
| `%math_ceil(3.2)%` | Ceiling (= 4) |
| `%math_sqrt(16)%` | Square root |
| `%math_pow(2,8)%` | Power |
| `%math_random(1,100)%` | Random number |

### Session Placeholders (`%session_*%`)

| Placeholder | Description |
|-------------|-------------|
| `%arg_0%`, `%arg_1%` | Command arguments |
| `%args%` | All arguments |
| `%session_page%` | Current page |
| `%session_menu%` | Current menu ID |
| `%session_data_key%` | Custom session data |

---

## Condition System

### Simple Conditions

```json
{
  "permission": "my.permission"
}
```

```json
{
  "condition": "permission my.permission"
}
```

### Comparison Conditions

```json
{
  "condition": "%player_level% >= 10"
}
```

```json
{
  "type": "comparison",
  "input": "%player_health%",
  "operator": ">",
  "output": "10"
}
```

### String Conditions

```json
{
  "condition": "%player_name% equals Steve"
}
```

```json
{
  "type": "string_contains",
  "input": "%player_world%",
  "output": "nether"
}
```

### Combined Conditions (AND)

```json
{
  "conditions": [
    {"permission": "shop.access"},
    {"condition": "%player_level% >= 5"}
  ]
}
```

### Combined Conditions (OR)

```json
{
  "any": [
    {"permission": "shop.vip"},
    {"permission": "shop.admin"}
  ]
}
```

### Negated Conditions

```json
{
  "permission": "banned.from.shop",
  "negate": true
}
```

### With Deny Actions

```json
{
  "permission": "shop.access",
  "denyMessage": "&cYou don't have access!",
  "denyActions": [
    "[sound] minecraft:entity.villager.no 1.0 1.0"
  ]
}
```

---

## Action System

### Action Format

Actions use the format `[type] value`.

### Command Actions

```json
"[command] spawn"              // Run as player
"[console] give %player_name% diamond 1"  // Run as console
"[op] gamemode creative"       // Run with OP permission
```

### Message Actions

```json
"[message] &aHello, %player_name%!"
"[broadcast] &e%player_name% opened the shop!"
"[actionbar] &bWelcome to the server!"
"[title] &6Welcome|&7to our server|10|70|20"
```

### Menu Actions

```json
"[open] shop"                  // Open another menu
"[open] shop arg1 arg2"        // With arguments
"[close]"                      // Close current menu
"[refresh]"                    // Refresh current menu
"[back]"                       // Go to previous menu
```

### Sound Actions

```json
"[sound] minecraft:entity.experience_orb.pickup"
"[sound] minecraft:ui.button.click 1.0 1.5"  // volume, pitch
```

### Item Actions

```json
"[give] minecraft:diamond 5"   // Give items
"[take] minecraft:emerald 10"  // Take items
```

### Other Actions

```json
"[teleport] 100 64 200"        // Teleport to coordinates
"[teleport] ~ ~10 ~"           // Relative coordinates
"[delay] 20"                   // Delay 1 second (20 ticks)
"[setdata] key=value"          // Store session data
```

### Action Sequences

```json
"clickActions": [
  "[sound] minecraft:ui.button.click 1.0 1.0",
  "[message] &aProcessing...",
  "[delay] 20",
  "[message] &aDone!",
  "[close]"
]
```

### Conditional Actions

```json
{
  "action": "[give] minecraft:diamond 1",
  "requirement": {
    "permission": "shop.vip"
  },
  "denyActions": [
    "[message] &cVIP only!"
  ]
}
```

### Chance-Based Actions

```json
{
  "action": "[give] minecraft:diamond 1",
  "chance": 0.1
}
```

---

## API & Extensibility

### Registering Custom Placeholders

```java
public class MyPlaceholderProvider implements PlaceholderProvider {
    @Override
    public String getIdentifier() {
        return "myplugin";  // %myplugin_placeholder%
    }
    
    @Override
    public String resolve(String placeholder, PlaceholderContext context) {
        if (placeholder.equals("custom")) {
            return "Custom Value";
        }
        return null;  // Not handled
    }
}

// Register in your mod
ActionMenus.getInstance().getPlaceholderManager().register(new MyPlaceholderProvider());
```

### Registering Custom Actions

```java
// In ActionRegistry
actionRegistry.register("myaction", value -> new MyCustomAction(value));
```

### Opening Menus Programmatically

```java
// Get the menu manager
MenuManager menuManager = ActionMenus.getInstance().getMenuManager();

// Open a menu
menuManager.openMenu(serverPlayer, "shop");

// With arguments
menuManager.openMenu(serverPlayer, "shop", new String[]{"category", "tools"});
```

---

## Example Menus

### Main Menu (`example_main_menu.json`)

A hub menu with navigation to other menus, player head display, and admin panel.

### Shop Menu (`example_shop.json`)

A 6-row shop with categories, buy options (left/right click for 1/64), and permission-gated items.

### Player Info Menu (`example_player_info.json`)

Dynamic player statistics display with auto-refreshing health, XP, playtime, and server stats.

---

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/am open <menu>` | `actionmenus.command.open` | Open a menu |
| `/am open <menu> <player>` | `actionmenus.command.open.others` | Open menu for player |
| `/am list` | `actionmenus.command.list` | List all menus |
| `/am reload` | `actionmenus.command.reload` | Reload configurations |
| `/am execute <player> <action>` | `actionmenus.command.execute` | Execute action on player |
| `/am help` | - | Show help |

Custom menu commands are registered via the `commands` field in menu config.

---

## Troubleshooting

### Common Issues

**Menu doesn't open:**
- Check if menu ID matches filename
- Verify JSON syntax is valid
- Check server logs for parsing errors
- Verify player has permission

**Placeholders not working:**
- Ensure format is `%placeholder%` (with percent signs)
- Check for typos in placeholder name
- Verify placeholder provider is registered

**Items not showing:**
- Check slot number is within menu size
- Verify view requirements pass
- Check material ID is valid

**Actions not executing:**
- Check click requirement conditions
- Verify action syntax `[type] value`
- Check server logs for errors

**Menu not updating:**
- Set `dynamic: true` on items with placeholders
- Verify `updateInterval` is set
- Check if placeholders are in name/lore

### Debug Logging

Enable debug logging in your server:

```properties
# In server.properties or log4j config
logger.actionmenus.level=DEBUG
```

### JSON Validation

Use a JSON validator to check your configs:
- https://jsonlint.com/
- VS Code with JSON extension

### Common JSON Errors

```json
// WRONG - trailing comma
{
  "id": "test",
  "rows": 3,  // <- Remove this comma
}

// WRONG - unquoted string
{
  "id": test  // <- Add quotes: "test"
}

// WRONG - single quotes
{
  'id': 'test'  // <- Use double quotes
}
```

---

## Support

For issues, feature requests, or contributions:
- GitHub Issues: [Your Repository URL]
- Discord: [Your Discord Server]

---

## License

ActionMenus is licensed under the MIT License.

---

**ActionMenus v1.0.0** | NeoForge 1.21.1 | Created for Britakee
