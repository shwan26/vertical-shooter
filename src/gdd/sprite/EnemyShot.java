package gdd.sprite;

import gdd.util.Util;
import static gdd.Global.*;

/**
 * Projectile fired by enemies with customizable direction.
 */
public class EnemyShot extends Shot {
    // ===== FIELDS =====
    private double dirX = -4;  // X direction component
    private double dirY = 0;  // Y direction component

    // ===== CONSTRUCTOR =====
    public EnemyShot(int x, int y) {
        super();
        initShot(x, y);
    }

    // ===== INITIALIZATION =====
    @Override
    public void initShot(int x, int y) {
        // Load different frames than player shots
        frames = Util.loadAnimationFrames("src/images/shot/enemy_bullet1_", 3, 1);
        setImage(frames[currentFrame]);
        setX(x);
        setY(y);

        // Reset animation state
        currentFrame = 0;
        animationCounter = 0;
    }

    // ===== DIRECTION SETTING =====
    public void setDirection(double dirX, double dirY) {
        this.dirX = dirX;
        this.dirY = dirY;
    }

    // ===== MOVEMENT METHODS =====
    @Override
    public void act() {
        // Move in specified direction
        this.x += dirX;
        this.y += dirY;
        updateAnimation();

        // Boundary checking
        if (this.x < -50 || this.x > BOARD_WIDTH + 50 ||
                this.y < -50 || this.y > BOARD_HEIGHT + 50) {
            die();
        }
    }
}