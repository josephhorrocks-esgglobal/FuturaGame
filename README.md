# FuturaGame

Basic Java framework for a top-down 1v1 tank game inspired by Wii Tanks.

## Current framework

- Fixed-step game loop at 60 FPS
- Swing window and rendering panel
- Player-controlled tank (W, A, S, D + Space)
- AI-controlled tank with simple chase/aim behavior
- Projectile system with cooldowns and lifetime
- Top-down arena map with static obstacles
- Basic collision checks for walls, bounds, and hit detection

## Project structure

- `src/main/java/com/futura/game/core` - game loop, panel, and game state
- `src/main/java/com/futura/game/entities` - tanks and projectiles
- `src/main/java/com/futura/game/world` - top-down arena map and obstacles
- `src/main/java/com/futura/game/input` - keyboard input handling
- `src/main/java/com/futura/game/math` - small math helpers
- `src/main/java/com/futura/game/config` - shared game constants

## Run

Prerequisites:

- Java 17+
- Maven 3.9+

Commands:

```bash
mvn compile
mvn exec:java
```

## Next implementation steps

- Replace placeholder circles with tank sprites and turret/body separation
- Add map loading and more obstacle shapes
- Add health, rounds, and restart flow
- Improve AI with pathing and obstacle avoidance
