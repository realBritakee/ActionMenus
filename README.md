# ActionMenus

[![NeoForge](https://img.shields.io/badge/NeoForge-1.21.1-orange)](https://neoforged.net/)
[![Java](https://img.shields.io/badge/Java-21-blue)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-red)](LICENSE)

**A server-side inventory GUI menu system for NeoForge 1.21.1** - like DeluxeMenus for Forge!

ActionMenus allows server administrators to create custom inventory-based menus through JSON configuration files, with no client-side mod required.

## Features

- 📦 **Config-Driven Menus** - Define menus entirely in JSON files
- 🔄 **Dynamic Placeholders** - Live-updating player stats, server info, math expressions
- ✅ **Conditional Logic** - Show/hide items based on permissions and conditions
- ⚡ **Powerful Actions** - Commands, messages, sounds, menu navigation, item giving
- 🔁 **Auto-Refresh** - Menus automatically update dynamic content every second
- 🎮 **Custom Commands** - Each menu can have its own command aliases
- 🖥️ **Server-Side Only** - Works with vanilla Minecraft clients
- 🔐 **Permission Support** - LuckPerms, FTB Ranks, or vanilla OP levels
- 🔊 **Sound Effects** - Play sounds when menus open or on item clicks
- 👤 **Player Heads** - Display player skulls with real skins or custom textures

## Quick Start

### Installation

1. Download the latest release from [Releases](releases)
2. Place `actionmenus-x.x.x.jar` in your server's `mods/` folder
3. Start the server
4. Menu configs are created in `config/actionmenus/menus/`

### Example Menus

ActionMenus comes with **2 example menus** to help you get started:

| Menu | Command | Description |
|------|---------|-------------|
| `warps.json` | `/warps` | A warp menu with spawn, nether, and end teleports |
| `rtp.json` | `/rtp` | Random teleport menu with permission examples |

You can **customize** these menus to fit your server, or use them as templates for your own menus.

**To disable an example menu:**
1. Open `config/actionmenus/config.json`
2. Remove the menu entry from the `gui_menus` section
3. Delete the menu file from `config/actionmenus/menus/`
4. Run `/am reload`

For example, to disable the warps menu, change your config from:
```json
{
  "gui_menus": {
    "warps": { "file": "warps.json" },
    "rtp": { "file": "rtp.json" }
  }
}
```
To:
```json
{
  "gui_menus": {
    "rtp": { "file": "rtp.json" }
  }
}
```

The removed menu file won't be regenerated on reload or restart.

### Your First Menu

Create `config/actionmenus/menus/hello.json`:

```json
{
  "menu_title": "&a&lHello World!",
  "size": 9,
  "open_command": ["hello"],
  "open_sound": "minecraft:block.chest.open",
  
  "items": {
    "greeting": {
      "slot": 4,
      "material": "diamond",
      "display_name": "&bHello, %player_name%!",
      "lore": [
        "&7Click me!"
      ],
      "left_click_commands": [
        "[message] &aYou clicked the diamond!",
        "[sound] minecraft:entity.experience_orb.pickup"
      ]
    }
  }
}
```

Then use `/am reload` and `/hello` to open your menu!

### Player Head Example

Display the player's own head in a menu:

```json
{
  "player_info": {
    "slot": 4,
    "material": "player_head",
    "skull_owner": "%player_name%",
    "display_name": "&a%player_name%",
    "lore": [
      "&7Health: &c%player_health%",
      "&7Level: &a%player_level%"
    ]
  }
}
```

Or use a custom texture (base64):

```json
{
  "custom_head": {
    "slot": 0,
    "material": "player_head",
    "skull_texture": "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzdhMmMxOGZmYTRkM2Y0MjE2YjI0MTQxNzllY2Q4OGFlNzllNmIxZTBlODljN2JmMjVkZDM1OTk0ZjdiOTYifX19",
    "display_name": "&6Custom Head"
  }
}
```

## Commands

| Command | Description |
|---------|-------------|
| `/am open <menu>` | Open a menu |
| `/am open <menu> <player>` | Open menu for another player |
| `/am list` | List all menus |
| `/am reload` | Reload configurations |
| `/am help` | Show help |

## Actions

ActionMenus supports 17 different action types:

| Action | Example | Description |
|--------|---------|-------------|
| `[message]` | `[message] &aHello!` | Send message to player |
| `[player]` | `[player] spawn` | Run command as player |
| `[console]` | `[console] give %player_name% diamond` | Run as console |
| `[close]` | `[close]` | Close the menu |
| `[open]` | `[open] warps` | Open another menu |
| `[sound]` | `[sound] minecraft:entity.experience_orb.pickup` | Play a sound |
| `[title]` | `[title] &aWelcome;&7Subtitle;20;60;20` | Show title |
| `[teleport]` | `[teleport] 0,64,0,world` | Teleport player |

See the [Developer Guide](DEVELOPER_GUIDE.md) for all actions.

## Placeholders

| Category | Examples |
|----------|----------|
| **Player** | `%player_name%`, `%player_uuid%`, `%player_health%`, `%player_level%`, `%player_x%` |
| **Server** | `%server_online%`, `%server_max%`, `%server_tps%` |
| **DateTime** | `%date%`, `%time%`, `%hour%`, `%minute%` |
| **Session** | `%menu_id%`, `%slot%`, `%args%` |
| **Math** | `%math_1+1%`, `%math_%player_level%*10%` |

## Color Formats

ActionMenus supports multiple color formats:

| Format | Example | Description |
|--------|---------|-------------|
| Legacy codes | `&a`, `&c`, `&l` | Standard Minecraft color codes |
| Hex (ampersand) | `&#FF5500` | Hex color with ampersand |
| Hex (bracket) | `{#FF5500}` | Hex color with brackets |
| Hex (angle) | `<#FF5500>` | Hex color with angle brackets |
| Hex (plain) | `#FF5500` | Plain hex color |
| Spigot hex | `&x&F&F&5&5&0&0` | Spigot-style hex |
| MiniMessage colors | `<red>`, `<gold>`, `<aqua>` | Named color tags |
| MiniMessage formats | `<bold>`, `<italic>`, `<underlined>` | Format tags |
| Gradient | `<gradient:#FF0000:#0000FF>text</gradient>` | Gradient between two colors |
| Rainbow | `<rainbow>text</rainbow>` | Rainbow effect |

**Examples:**
```json
{
  "display_name": "&a&lGreen Bold Text",
  "display_name": "&#FF5500Orange Hex Text",
  "display_name": "<gradient:#FF0000:#00FF00>Gradient Text</gradient>",
  "display_name": "<rainbow>Rainbow Text</rainbow>"
}
```

## Documentation

See the full [Developer Guide](DEVELOPER_GUIDE.md) for:
- Complete configuration reference
- All placeholders
- Condition system
- Action system
- Permission system
- API & extensibility
- Troubleshooting

## Building from Source

```bash
# Clone the repository
git clone https://github.com/yourusername/actionmenus.git
cd actionmenus

# Build
./gradlew build

# The JAR will be in build/libs/
```

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.x+
- Java 21

## License

**All Rights Reserved** - This mod is proprietary software. You may not copy, modify, distribute, or use this code without explicit permission from the author. See [LICENSE](LICENSE) for details.

## Credits

- Inspired by [DeluxeMenus](https://www.spigotmc.org/resources/deluxemenus.11734/) for Bukkit/Spigot
- Created for the NeoForge 1.21.1 modding community
