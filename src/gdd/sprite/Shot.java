package gdd.sprite;

import gdd.util.Util;
import java.awt.image.BufferedImage;

/**
 * Base class for all projectile types.
 * Handles common projectile behavior and animation.
 */
public class Shot extends Sprite {
    // ===== CONSTANTS =====
    protected static final int ANIMATION_DELAY = 5;
    protected static final int H_SPACE = 35;  // Horizontal spawn offset
    protected static final int V_SPACE = 5;   // Vertical spawn offset

    // ===== FIELDS =====
    protected BufferedImage[] frames;  // Projectile animation frames

    // ===== CONSTRUCTORS =====
    public Shot() {}

    public Shot(int x, int y) {
        initShot(x, y);
    }

    // ===== INITIALIZATION =====
    protected void initShot(int x, int y) {
        this.frames = Util.loadAnimationFrames("src/images/shot/enemy_bullet1_", 3, 1);
        setImage(frames[currentFrame]);
        setX(x + H_SPACE);
        setY(y + V_SPACE);
    }

    // ===== MOVEMENT METHODS =====
    /**
     * Default projectile movement (rightward)
     */
    @Override
    public void act() {
        x += 12; // Move right
        updateAnimation();
    }

    // ===== ANIMATION METHODS =====
    public void updateAnimation() {
        if (++animationCounter >= ANIMATION_DELAY) {
            currentFrame = (currentFrame + 1) % frames.length;
            setImage(frames[currentFrame]);
            animationCounter = 0;
        }
    }
}