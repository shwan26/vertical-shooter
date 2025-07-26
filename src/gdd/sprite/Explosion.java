package gdd.sprite;

import gdd.util.Util;
import java.awt.image.BufferedImage;

/**
 * Visual effect for explosions with different variants.
 * Handles its own animation lifecycle.
 */
public class Explosion extends Sprite {
    // ===== CONSTANTS =====
    private static final int ANIMATION_DELAY = 10;

    // ===== FIELDS =====
    private BufferedImage[] frames;          // Explosion animation frames
    private boolean isPlayerExplosion;       // Flag for player-specific visuals

    // ===== CONSTRUCTORS =====
    public Explosion(int x, int y) {
        this(x, y, false);
    }

    public Explosion(int x, int y, boolean isPlayerExplosion) {
        this.isPlayerExplosion = isPlayerExplosion;
        initExplosion(x, y);
    }

    // ===== INITIALIZATION =====
    private void initExplosion(int x, int y) {
        this.x = x;
        this.y = y;

        // Load appropriate frames based on explosion type
        String path = isPlayerExplosion ?
                "src/images/explosion/player_explosion" :
                "src/images/explosion/enemy_explosion";
        this.frames = Util.loadAnimationFrames(path, 3, 1, false);
        setImage(frames[currentFrame]);
    }

    // ===== ANIMATION METHODS =====
    public void updateAnimation() {
        if (++animationCounter >= ANIMATION_DELAY) {
            animationCounter = 0;
            currentFrame++;
            if (currentFrame < frames.length) {
                setImage(frames[currentFrame]);
            } else {
                visible = false;  // End of animation
            }
        }
    }

    // ===== CORE METHODS =====
    @Override
    public void act() {
        if (isVisible()) updateAnimation();
    }
}