# Futura Game - development guidelines

This document describes the current gameplay direction, presentation style, and development expectations for the Futura project.

---

### What type of project this is

1. This is a project written in the Java programming language.

2. The project is a 2D top-down tank battle game.

3. The game is centered on a 1v1 tank fight, with a player tank versus an AI tank, and multiplayer support also exists in the project.

4. The blocks change colour briefly when hit by projectiles.

---

### Current implemented features

1. Fixed-step game loop with Swing rendering.

2. Player tank, AI tank, projectiles, hit detection, and health.

3. Arena maps with obstacles, lakes, and slow zones.

4. Dragon event system with warning HUD, attack timing, and bomb-style attacks.

5. A siren warning before the dragon appears.

6. Decorative rabbits that bounce around and nibble grass.

7. Slowly moving clouds across the battlefield.

8. A startup splash screen shown before game setup.

9. Bottom-positioned HP graphics, with Player 1 on the left and AI / opponent on the right.

10. Military-style HUD and UI font treatment.

11. Original cartoon-style visuals for tanks, projectiles, effects, and battlefield elements.

---

### Development guidance

1. Keep the game focused on readable, fun 2D tank combat.

2. Preserve the current Java + Swing architecture unless a task clearly requires a structural change.

3. Prefer extending the current entity, world, and rendering systems instead of introducing unnecessary complexity.

4. Keep new visuals original and consistent with the current colorful cartoon battlefield style.

5. Keep UI and gameplay information readable during active combat.

6. Maintain the existing behavior where battlefield blocks react visually when hit.

---
