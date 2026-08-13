# Genius-Homes

Genius-Homes is a configurable Minecraft homes plugin for Paper servers. It provides GUI-based home management, SQLite or MySQL storage, teleport cooldowns, optional teleport costs, per-player home limits, localization, and configurable menu layouts.

## Features

- `/home`, `/sethome`, `/delhome`, `/homes`, and `/home-rename` commands
- GUI home list, home settings, icon picker, sound picker, and confirmation menus
- SQLite by default, with optional MySQL support
- Optional teleport cooldowns and movement cancellation
- Optional teleport costs using Vault, XP, levels, or items
- Configurable max home limits with permission overrides
- Configurable menu layouts in `plugins/Genius-Homes/menus/`
- Language files in `plugins/Genius-Homes/language/`
- Fallback language support through `en_US.yml`
- Legacy, hex, gradient, and multi-gradient color support in configurable text
- Optional bStats and update checker

## Requirements

- Paper 1.21.1 or compatible server software
- Java 17+
- Vault and an economy plugin if you enable Vault teleport costs

## Installation

1. Download the latest Genius-Homes jar from the release page.
2. Place the jar in your server's `plugins/` folder.
3. Restart the server.
4. Edit the generated files in `plugins/Genius-Homes/`.
5. Run `/reload-home-config` or restart the server after configuration changes.

## Commands

| Command | Description |
| --- | --- |
| `/home [home]` | Open the homes menu or teleport to a named home. |
| `/homes [player]` | Open your homes menu, or view another player's homes with permission. |
| `/sethome <name>` | Create a new home. |
| `/delhome <name>` | Delete a home. |
| `/home-rename <old> <new>` | Rename a home. |
| `/reload-home-config` | Reload configuration and clear the home cache. |

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `genius.homes.use` | true | Allows basic home commands. |
| `genius.homes.admin` | op | Grants administrative homes permissions. |
| `genius.homes.rename` | true | Allows renaming own homes when rename permission checks are enabled. |
| `genius.homes.sound` | true | Allows changing home teleport sounds when sound permission checks are enabled. |
| `genius.homes.sounds.*` | true | Allows every configured teleport sound. |
| `genius.homes.sounds.<sound>` | inherited | Allows one specific sound when per-sound permission checks are enabled. |
| `genius.homes.bypass.cooldown` | false | Bypasses teleport cooldowns. |
| `genius.homes.bypass.cooldown.<seconds>` | false | Uses a specific cooldown override. |
| `genius.homes.max.*` | false | Uses the configured maximum home amount. |
| `genius.homes.max.<amount>` | false | Allows a specific maximum home amount. |
| `genius.homes.others` | op | Allows viewing other players' homes. |
| `genius.homes.others.teleport` | op | Allows teleporting to other players' homes. |
| `genius.others.settings` | op | Legacy permission for all other-player home settings. |
| `genius.homes.others.settings.delete` | op | Allows deleting other players' homes. |
| `genius.homes.others.settings.rename` | op | Allows renaming other players' homes. |
| `genius.homes.others.settings.change.sounds` | op | Allows changing other players' home sounds. |
| `genius.homes.others.settings.new.location` | op | Allows updating other players' home locations. |
| `genius.homes.others.settings.change.icons` | op | Allows changing other players' home icons. |

## Configuration

Plugin metadata, command registration, and permission defaults are defined in:

```text
src/main/resources/plugin.yml
```

Main settings are generated in:

```text
plugins/Genius-Homes/config.yml
```

Menu layouts are generated in:

```text
plugins/Genius-Homes/menus/
```

Language files are generated in:

```text
plugins/Genius-Homes/language/
```

Set the active language in `config.yml`:

```yaml
language: en_US
```

Only `en_US.yml` is included by default. To add a translation, copy `language/en_US.yml`, rename it, translate the values, and set `language:` to the new file name without `.yml`.

For full setup and customization instructions, see [the wiki guide](wiki/Configuration-Guide.md).

## Support

For help, setup questions, and community support, join the Discord:

```text
TODO: add Discord invite link
```

Before asking for help, please include:

- Server version
- Genius-Homes version
- Any console errors
- Relevant config snippets
- Steps to reproduce the problem

## Reporting Issues

Use GitHub Issues for bugs and feature requests. A good bug report includes:

- What you expected to happen
- What actually happened
- Steps to reproduce
- Server version and plugin version
- Full console error or stack trace
- `plugins/Genius-Homes/debug.log`
- Any changed config files related to the issue

Do not paste private database credentials, IP addresses, tokens, or other secrets into public issues.

## Building From Source

```bash
mvn -DskipTests package
```

The built jar is created in `target/`.

## License

Genius-Homes is licensed under the [MIT License](LICENSE).
