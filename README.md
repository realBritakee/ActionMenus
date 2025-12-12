# ActionMenus

[![NeoForge](https://img.shields.io/badge/NeoForge-1.21.1-orange)](https://neoforged.net/)
[![Java](https://img.shields.io/badge/Java-21-blue)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

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

## Quick Start

### Installation

1. Download the latest release from [Releases](releases)
2. Place `actionmenus-x.x.x.jar` in your server's `mods/` folder
3. Start the server
4. Menu configs are created in `config/actionmenus/menus/`

### Your First Menu

Create `config/actionmenus/menus/hello.json`:

```json
{
  "id": "hello",
  "title": "&a&lHello World!",
  "rows": 1,
  "commands": ["hello"],
  
  "items": {
    "greeting": {
      "slot": 4,
      "material": "minecraft:diamond",
      "name": "&bHello, %player_name%!",
      "lore": [
        "&7Click me!"
      ],
      "clickActions": [
        "[message] &aYou clicked the diamond!",
        "[sound] minecraft:entity.experience_orb.pickup 1.0 1.0"
      ]
    }
  }
}
```

Then use `/am reload` and `/hello` to open your menu!

## Commands

| Command | Description |
|---------|-------------|
| `/am open <menu>` | Open a menu |
| `/am open <menu> <player>` | Open menu for another player |
| `/am list` | List all menus |
| `/am reload` | Reload configurations |
| `/am help` | Show help |

## Documentation

See the full [Developer Guide](DEVELOPER_GUIDE.md) for:
- Complete configuration reference
- All placeholders
- Condition system
- Action system
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

MIT License - see [LICENSE](LICENSE) for details.

## Credits

- Inspired by [DeluxeMenus](https://www.spigotmc.org/resources/deluxemenus.11734/) for Bukkit/Spigot
- Created for the NeoForge 1.21.1 modding community
