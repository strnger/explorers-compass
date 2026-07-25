# Explorer's Compass

A RuneLite plugin that points a compass arrow at the **nearest agility shortcut you can use but haven't discovered yet**. Great for organically exploring content you're already eligible for.

Think of it as *"Log Hunter's auto-eligibility detection"* meets *"a destination arrow"* — it figures out what you qualify for, then nudges you toward it.

## How it works

1. The plugin ships with a curated catalogue of agility shortcuts, each with a location and Agility level requirement.
2. Every game tick it finds the **nearest** shortcut where:
   - your **real Agility level** meets the requirement, and
   - you **haven't discovered it yet**.
3. It points the game's **hint arrow** at that shortcut and shows a small info overlay (name, level, distance).
4. When you **walk near a shortcut**, it's marked *discovered* and saved — so the compass moves on to the next thing to explore.

Discovered your whole eligible list? The arrow disappears until you level up and unlock more.

## Configuration

Open RuneLite **Settings** → **Explorer's Compass** (gear icon).

### Display
| Setting | Default | Description |
|---------|---------|-------------|
| Show hint arrow |  | Points the game hint arrow at the nearest shortcut |
| Show info overlay |  | On-screen panel with target name, level and distance |

### Behaviour
| Setting | Default | Description |
|---------|---------|-------------|
| Discovery radius | 3 tiles | Walk within this many tiles to mark a shortcut discovered |
| Max distance | 0 (unlimited) | Ignore shortcuts farther than this many tiles |
| Level buffer | 0 | Only target shortcuts whose requirement is this far below your level |

### Data
| Setting | Description |
|---------|-------------|
| Reset discovered | Wipe all discovery progress (untoggles itself) |

## Roadmap

This first release focuses on agility shortcuts because they're purely level-gated — a clean fit for "assuming you have the level". Future ideas:

- Fairy rings, spirit trees, gnome gliders and other transport unlocks
- World-map markers in addition to the hint arrow
- Filtering by region or skill

## Building

```bash
./gradlew build
```

## Running (dev mode)

The project ships with a ready-to-use IntelliJ run configuration named
**"Run Explorer's Compass"** (in the `.run/` folder). Just pick it from the
run-config dropdown (top-right of IntelliJ) and hit the green arrow.

**Why not the gutter icon?** RuneLite requires assertions to be enabled
(`-ea`). The bundled config sets this in the JVM's *VM options*. If you instead
use the green arrow next to `main()` (which runs via Gradle) or hand-roll a
config, you may hit:

```
RuntimeException: Assertions are not enabled, add '-ea' to your VM options.
```

If you build your own config, make sure `-ea` goes in **VM options** (JVM args),
*not* Program arguments. This is a RuneLite requirement for every plugin dev
client, not something specific to this plugin.

## Credits

Shortcut coordinates and level requirements were derived from the excellent
[Shortest Path](https://github.com/Skretzo/shortest-path) plugin's open transport data.
