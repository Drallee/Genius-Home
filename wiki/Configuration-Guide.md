# Genius-Homes Configuration Guide

This page explains how to install, configure, translate, and customize Genius-Homes.

## Generated File Structure

After the plugin starts for the first time, it creates:

```text
plugins/
+-- Genius-Homes/
    +-- config.yml
    +-- home-icons.yml
    +-- home-sounds.yml
    +-- language/
    |   +-- en_US.yml
    +-- menus/
        +-- home-menu.yml
        +-- home-settings-menu.yml
        +-- delete-confirm-menu.yml
        +-- new-location-confirm-menu.yml
        +-- icon-menu.yml
        +-- player-heads-menu.yml
        +-- sound-menu.yml
        +-- rename-menu.yml
```

## Main Configuration

Edit `plugins/Genius-Homes/config.yml`.

Important options:

```yaml
language: en_US

settings:
  debug: false
  update-checker: true
  bstats: true
  mysql:
    use: false
  homes:
    per-world: false
    need-permission: true
    max-amount: 28
    names:
      min-length: 1
      max-length: 32
      allowed-pattern: '^[A-Za-z0-9_-]+$'
    rename-permission: false
    sound-permission: false
    per-sound-permission: false
```

Restart the server or run:

```text
/reload-home-config
```

## Database Setup

SQLite is used when:

```yaml
settings:
  mysql:
    use: false
```

To use MySQL:

```yaml
settings:
  mysql:
    use: true
    table-prefix: homes
    host: localhost
    port: 3306
    database: database
    username: root
    password: password
```

Restart the server after changing database settings.

## Home Name Rules

Home names are validated before creation and rename operations, including text entered through the AnvilGUI input.

```yaml
settings:
  homes:
    names:
      min-length: 1
      max-length: 32
      allowed-pattern: '^[A-Za-z0-9_-]+$'
```

`allowed-pattern` is a Java regular expression. The default allows letters, numbers, underscores, and hyphens.

## Teleport Cooldowns

```yaml
settings:
  homes:
    teleport:
      cooldown:
        enabled: true
        cancel-on-move: true
        time: 5
```

Players with `genius.homes.bypass.cooldown` skip the cooldown. You can also use `genius.homes.bypass.cooldown.<seconds>` for specific overrides.

## Teleport Costs

```yaml
settings:
  homes:
    teleport:
      cost:
        enabled: false
        type: XP
        item: DIAMOND
        amount: 10
```

Supported cost types:

| Type | Notes |
| --- | --- |
| `VAULT` | Requires Vault and an economy plugin. |
| `XP` | Takes experience points. |
| `LEVEL` | Takes experience levels. |
| `ITEM` | Takes the configured item material. |

## Language Files

Language files are stored in:

```text
plugins/Genius-Homes/language/
```

The default file is:

```text
en_US.yml
```

To create a translation:

1. Copy `en_US.yml`.
2. Rename it, for example `da_DK.yml`.
3. Translate the message values.
4. Set the active language in `config.yml`:

```yaml
language: da_DK
```

If the selected language file does not exist, Genius-Homes falls back to `en_US.yml`. If a translation is missing a message, only that message falls back to `en_US.yml`.

## Menu Configuration

Menus are stored in:

```text
plugins/Genius-Homes/menus/
```

Each menu has its own file. Server owners can change the visual layout without editing Java code.

Common options:

```yaml
rows: 6
title: '&8Menu title'
content-slots: [10-16, 19-25, 28-34, 37-43]
filler:
  enabled: true
  material: GRAY_STAINED_GLASS_PANE
  name: ''
  slots: [0-9, 17, 18, 26, 27, 35, 36, 44-53]
buttons:
  close:
    enabled: true
    slot: 49
    material: BARRIER
    name: '&cClose'
    lore: []
    custom-model-data: 1001
```

### Rows

`rows` controls inventory size. Valid values are `1` through `6`.

### Slots

Slots start at `0`. A 6-row inventory has slots `0` through `53`.

Slot lists support individual slots and ranges:

```yaml
content-slots: [10-16, 19-25, 28-34, 37-43]
```

### Filler Items

Use filler items to fill empty slots:

```yaml
filler:
  enabled: true
  material: GRAY_STAINED_GLASS_PANE
  name: ''
  slots: []
```

If `slots` is empty, the plugin fills every empty slot. If `slots` contains values, only those slots are filled.

### Buttons

Buttons control how visual items appear. Their underlying behavior is still handled by the plugin.

Example:

```yaml
buttons:
  teleport:
    slot: 20
    material: ENDER_PEARL
    name: '&aTeleport'
    lore:
      - '&7Click to teleport'
```

You can change the material, slot, name, lore, and custom model data. Do not rename button keys such as `teleport`, `close`, `back`, or `confirm`, because the plugin uses those keys to apply behavior.

## Placeholders

Common placeholders:

| Placeholder | Meaning |
| --- | --- |
| `%home%` | Home name. |
| `%current%` | Current amount or page. |
| `%max%` | Maximum homes. |
| `%total%` | Total pages. |
| `%x%`, `%y%`, `%z%` | Home coordinates. |
| `%world%` | Home world. |
| `%new_x%`, `%new_y%`, `%new_z%` | New location coordinates. |
| `%new_world%` | New location world. |
| `%sound%` | Current sound display name. |
| `%target%` | Target player name. |
| `%chat_prefix%` | Configured chat prefix. |

## Colors And Gradients

All plugin text that passes through language files, menu files, icon display names, sound display names, chat messages, item names, and item lore supports the same color formats.

Legacy Minecraft color codes:

```yaml
name: '&aGreen text'
name: '&lBold &cRed'
```

Hex colors:

```yaml
name: '#55AAFFBlue text'
name: '&#55AAFFBlue text'
name: '<#55AAFF>Blue text'
```

Gradients:

```yaml
name: '<gradient:#55AAFF:#FF55FF>Gradient text</gradient>'
```

Multi-stop gradients:

```yaml
name: '<gradient:#55AAFF:#FFFFFF:#FF55FF>Three color gradient</gradient>'
name: '<gradient:#00FFAA:#55AAFF:#AA55FF:#FF55AA>Four color gradient</gradient>'
```

You can still use normal legacy formatting codes such as `&l`, `&n`, and `&o` outside gradient tags.

## Icons And Sounds

Home icon options are configured in:

```text
plugins/Genius-Homes/home-icons.yml
```

Home sound options are configured in:

```text
plugins/Genius-Homes/home-sounds.yml
```

If per-sound permissions are enabled, grant:

```text
genius.homes.sounds.<sound>
```

Example:

```text
genius.homes.sounds.entity_enderman_teleport
```

## Troubleshooting

Genius-Homes writes warnings and errors to:

```text
plugins/Genius-Homes/debug.log
```

The debug log includes server version, Bukkit version, and plugin version when the plugin starts. Include this file when reporting bugs, but remove private database credentials if you add config snippets.

If a menu does not look right:

1. Check that `rows` is between `1` and `6`.
2. Check that every slot is inside the inventory size.
3. Check that all materials are valid Bukkit material names.
4. Check the console for Genius-Homes warnings.
5. Run `/reload-home-config`.

If language text is missing:

1. Check `config.yml` has the correct `language:` value.
2. Check that `language/<language>.yml` exists.
3. Compare your language file against `en_US.yml`.
4. Missing keys automatically fall back to `en_US.yml`.

## Getting Help

Join the Discord for setup help and support:

```text
TODO: add Discord invite link
```

When asking for help, include:

- Server software and version
- Java version
- Genius-Homes version
- The relevant config section
- Any console errors
- What you already tried

## Reporting Bugs

Report bugs through GitHub Issues.

Include:

- Steps to reproduce
- Expected behavior
- Actual behavior
- Server version
- Plugin version
- Full console error, if any
- Whether the issue still happens with default config files

Do not include private credentials from `config.yml`.
