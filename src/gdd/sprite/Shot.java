package gdd.sprite;

import gdd.util.Util;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Base class for all projectile types.
 * Handles common projectile behavior and animation.
 */
public class Shot extends Sprite {
    // ===== CONSTANTS =====
    protected static final int ANIMATION_DELAY = 8;
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
        int[] reduceSize = {10, 10};
        this.frames = Util.loadAnimationFrames("src/images/shot/shot5_", 2, 2, reduceSize, false);
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

    // In Shot.java
    @Override
    public Rectangle getCollisionBounds() {
        // Very small hitbox for shots (30% of image size)
        int width = (int)(image.getWidth() * 0.3);
        int height = (int)(image.getHeight() * 0.2);
        int xOffset = (image.getWidth() - width) / 2;
        int yOffset = (image.getHeight() - height) / 2;

        return new Rectangle(x + xOffset, y + yOffset, width, height);
    }
}