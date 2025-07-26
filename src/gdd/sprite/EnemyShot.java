package gdd.sprite;

import gdd.util.Util;
import static gdd.Global.*;

/**
 * Projectile fired by enemies with customizable direction and enemy-specific visuals.
 */
public class EnemyShot extends Shot {
    // ===== CONSTANTS =====
    private static final String DEFAULT_SHOT_PATH = "src/images/shot/shot1_";
    private static final String[] ENEMY_SHOT_PATHS = {
            "src/images/shot/shot1_",  // For Alien1
            "src/images/shot/shot3_",  // For QuadShotEnemy
            "src/images/shot/shot6_"  // For LaserEnemy
    };

    // ===== FIELDS =====
    private double dirX = -4;  // X direction component
    private double dirY = 0;   // Y direction component
    private int enemyType;     // To determine which shot image to use

    // ===== CONSTRUCTORS =====
    public EnemyShot(int x, int y) {
        this(x, y, 0); // Default to first enemy type if not specified
    }

    public EnemyShot(int x, int y, int enemyType) {
        super();
        this.enemyType = enemyType;
        initShot(x, y);
    }

    // ===== INITIALIZATION =====
    @Override
    public void initShot(int x, int y) {
        // Determine which frames to load based on enemy type
        String imagePath;
        int frameCount;
        int scale;

        switch(enemyType) {
            case 1: // Alien
                imagePath = ENEMY_SHOT_PATHS[0];
                frameCount = 4;
                scale = 2;
                break;
            case 2: // Quad Shot enemy
                imagePath = ENEMY_SHOT_PATHS[1];
                frameCount = 3;
                scale = 2;
                break;
            case 3: // Laser enemy
                imagePath = ENEMY_SHOT_PATHS[2];
                frameCount = 3;
                scale = 2;
                break;
            default: // Default shot
                imagePath = DEFAULT_SHOT_PATH;
                frameCount = 4;
                scale = 2;
        }

        // Load appropriate frames
        frames = Util.loadAnimationFrames(imagePath, frameCount, scale, true);
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

    // ===== GETTERS =====
    public int getEnemyType() {
        return enemyType;
    }
}