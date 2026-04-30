package com.futura.game.entities;

import com.futura.game.config.GameConfig;
import com.futura.game.math.Vector2;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

public class DragonFireball extends Entity {
    private enum Phase { INCOMING, EXPLODING, DONE }

    private Phase phase;
    private final Vector2 targetPosition;
    private double warningTimer;
    private double explosionTimer;
    private boolean damageApplied;

    public DragonFireball(Vector2 targetPosition) {
        super(targetPosition, 0.0);
        this.targetPosition = targetPosition;
        this.phase = Phase.INCOMING;
        this.warningTimer = GameConfig.DRAGON_BOMB_WARNING_SECONDS;
        this.explosionTimer = GameConfig.DRAGON_BOMB_EXPLOSION_SECONDS;
        this.damageApplied = false;
    }

    @Override
    public void update(double deltaTime) {
        if (phase == Phase.INCOMING) {
            warningTimer -= deltaTime;
            if (warningTimer <= 0.0) {
                phase = Phase.EXPLODING;
            }
        } else if (phase == Phase.EXPLODING) {
            explosionTimer -= deltaTime;
            if (explosionTimer <= 0.0) {
                phase = Phase.DONE;
            }
        }
    }

    @Override
    public void render(Graphics2D g2d) {
        if (phase == Phase.INCOMING) {
            renderWarning(g2d);
        } else if (phase == Phase.EXPLODING) {
            renderExplosion(g2d);
        }
    }

    private void renderWarning(Graphics2D g2d) {
        Graphics2D wg = (Graphics2D) g2d.create();
        wg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = (int) Math.round(targetPosition.x());
        int cy = (int) Math.round(targetPosition.y());
        int r  = (int) Math.round(GameConfig.DRAGON_BOMB_EXPLOSION_RADIUS);

        double progress = 1.0 - (warningTimer / GameConfig.DRAGON_BOMB_WARNING_SECONDS);
        float alpha = (float) (0.35 + 0.30 * Math.sin(progress * Math.PI * 8));
        Composite old = wg.getComposite();
        wg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, alpha)));
        wg.setColor(new Color(220, 40, 0));
        wg.fillOval(cx - r, cy - r, r * 2, r * 2);
        wg.setComposite(old);

        Stroke oldStroke = wg.getStroke();
        wg.setStroke(new BasicStroke(2.5f));
        wg.setColor(new Color(255, 80, 0));
        wg.drawOval(cx - r, cy - r, r * 2, r * 2);
        wg.setStroke(oldStroke);

        wg.setColor(new Color(255, 60, 0, 180));
        wg.drawLine(cx - r, cy, cx + r, cy);
        wg.drawLine(cx, cy - r, cx, cy + r);

        // Falling bomb icon descends toward impact point
        double bombY = cy - r - 30 + (r + 30) * progress;
        int bx = cx - 7;
        int by = (int) Math.round(bombY);
        wg.setColor(new Color(40, 40, 40));
        wg.fillRoundRect(bx, by - 12, 14, 22, 6, 6);
        wg.setColor(new Color(160, 160, 160));
        wg.fillRect(bx + 3, by - 18, 8, 8);
        wg.setColor(new Color(255, 160, 0));
        wg.fillOval(bx + 3, by + 8, 8, 8);

        wg.dispose();
    }

    private void renderExplosion(Graphics2D g2d) {
        Graphics2D eg = (Graphics2D) g2d.create();
        eg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = (int) Math.round(targetPosition.x());
        int cy = (int) Math.round(targetPosition.y());
        double maxR = GameConfig.DRAGON_BOMB_EXPLOSION_RADIUS;

        double elapsed = GameConfig.DRAGON_BOMB_EXPLOSION_SECONDS - explosionTimer;
        double total   = GameConfig.DRAGON_BOMB_EXPLOSION_SECONDS;
        double t = elapsed / total;

        double radiusFraction = t < 0.45 ? (t / 0.45) : (1.0 - (t - 0.45) / 0.55);
        int r = (int) Math.round(maxR * radiusFraction);
        if (r < 1) {
            eg.dispose();
            return;
        }

        float fadeAlpha = (float) Math.max(0.0, 1.0 - t * 0.8);
        Composite old = eg.getComposite();
        eg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha));

        eg.setColor(new Color(220, 60, 0));
        eg.fillOval(cx - r, cy - r, r * 2, r * 2);

        int mid = (int) Math.round(r * 0.65);
        eg.setColor(new Color(255, 150, 0));
        eg.fillOval(cx - mid, cy - mid, mid * 2, mid * 2);

        int core = (int) Math.round(r * 0.30);
        eg.setColor(new Color(255, 245, 180));
        eg.fillOval(cx - core, cy - core, core * 2, core * 2);

        eg.setComposite(old);

        float ringAlpha = (float) Math.max(0.0, 0.9 - t);
        eg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, ringAlpha)));
        eg.setColor(new Color(255, 200, 80));
        Stroke oldStroke = eg.getStroke();
        eg.setStroke(new BasicStroke(3.0f));
        eg.drawOval(cx - r - 4, cy - r - 4, (r + 4) * 2, (r + 4) * 2);
        eg.setStroke(oldStroke);
        eg.setComposite(old);

        eg.dispose();
    }

    public boolean shouldApplyDamage() {
        return phase == Phase.EXPLODING && !damageApplied;
    }

    public void markDamageApplied() {
        damageApplied = true;
    }

    public boolean isDone() {
        return phase == Phase.DONE;
    }

    public Vector2 getTargetPosition() {
        return targetPosition;
    }

    public double getExplosionRadius() {
        return GameConfig.DRAGON_BOMB_EXPLOSION_RADIUS;
    }

    public boolean isExpired() {
        return isDone();
    }

    public double getRadius() {
        return GameConfig.DRAGON_BOMB_EXPLOSION_RADIUS;
    }
}
