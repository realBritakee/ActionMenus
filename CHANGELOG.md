# ActionMenus Changelog

All notable changes to this project will be documented in this file.

## [1.0.0] - 2024-12-15

### Added
- Initial release of ActionMenus for NeoForge 1.21.1
- **Menu System**
  - JSON-based menu configuration (DeluxeMenus-style format)
  - Support for 1-6 row chest menus
  - Custom command aliases per menu (e.g., `/warp`, `/rtp`)
  - Dynamic placeholder support with auto-refresh
  - Conditional item display (view requirements)
  - Click requirements for items

- **Item Features**
  - All Minecraft materials supported (including modded items via `modid:item`)
  - Player head support with `skull_owner` (player name, UUID, or `%player_name%`)
  - Custom skull textures via base64 `skull_texture`
  - Enchantment glow effect
  - Custom model data support
  - Lore with color codes and placeholders
  - Per-item permissions with custom deny messages

- **Actions (17 types)**
  - `[player]` - Run command as player
  - `[console]` - Run command as console
  - `[op]` - Run command with OP permissions
  - `[message]` - Send message to player
  - `[broadcast]` - Broadcast to all players
  - `[close]` - Close menu
  - `[open]` - Open another menu
  - `[back]` - Go back to previous menu
  - `[refresh]` - Refresh current menu
  - `[sound]` - Play sound
  - `[title]` - Show title/subtitle
  - `[actionbar]` - Show actionbar message
  - `[teleport]` - Teleport player
  - `[give]` - Give item to player
  - `[take]` - Take item from player
  - `[connect]` - Connect to server (proxy)
  - `[delay]` - Delay next action

- **Placeholders (5 providers)**
  - Player: `%player_name%`, `%player_uuid%`, `%player_health%`, `%player_food%`, `%player_level%`, `%player_world%`, `%player_x/y/z%`
  - Server: `%server_online%`, `%server_max%`, `%server_tps%`, `%server_motd%`
  - DateTime: `%date%`, `%time%`, `%datetime%`, `%year%`, `%month%`, `%day%`, `%hour%`, `%minute%`
  - Session: `%arg_N%`, `%args%`, `%menu_id%`, `%slot%`
  - Math: `%math_EXPRESSION%`

- **Permission System**
  - Per-menu permissions
  - Per-item permissions with custom deny messages
  - LuckPerms integration (via reflection API)
  - FTB Ranks support
  - OP fallback for vanilla servers

- **Sound System**
  - `open_sound` option for menus
  - Support for all Minecraft sounds
  - Support for modded sounds (`modid:sound`)
  - Configurable volume and pitch

- **Menu Updates**
  - Efficient item comparison prevents unnecessary network packets
  - Smart caching system for smooth updates

- **Example Menus**
  - `warps.json` - Warp menu example
  - `rtp.json` - Random teleport menu with permission examples

---

## Version History

| Version | Date | Minecraft | NeoForge |
|---------|------|-----------|----------|
| 1.0.0 | 2024-12-15 | 1.21.1 | 21.1.x |
