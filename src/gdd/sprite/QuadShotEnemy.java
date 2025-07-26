package gdd.sprite;

import gdd.util.Util;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static gdd.Global.BOARD_HEIGHT;
import static gdd.Global.BOARD_WIDTH;

/**
 * Enemy that fires 4 shots simultaneously with a cooldown period
 * and moves in a small range while occasionally changing position.
 */
public class QuadShotEnemy extends Enemy {
    // Constants
    private static final int ANIMATION_DELAY = 10;
    private static final int SHOT_COOLDOWN = 180; // 2 seconds at 60 FPS
    private static final int POSITION_CHANGE_COOLDOWN = 360; // 6 seconds at 60 FPS
    private static final int WARNING_DURATION = 60; // 1 second warning before position change
    private static final int SHOT_SPREAD = 15; // Spread between shots in pixels
    private static final int MOVEMENT_RANGE = 20; // Smaller movement range (was 50)
    private static final double MOVEMENT_SPEED = 0.8; // Slower movement speed

    // Fields
    private BufferedImage[] frames;
    private BufferedImage[] warningFrames; // Frames for warning state
    private Exhaust exhaust;
    private int shotTimer = 0;
    private int positionChangeTimer = 0;
    private int minX; // Minimum X position (right half of screen)
    private List<EnemyShot> pendingShots = new ArrayList<>();
    private int originalX, originalY; // Store original position
    private double targetX, targetY; // Current movement target
    private double moveX, moveY; // Movement direction vector
    private boolean isWarning = false; // Whether warning is active
    private int warningTimer = 0; // Timer for warning effect

    public QuadShotEnemy(int x, int y) {
        super(x, y);
        this.minX = BOARD_WIDTH / 2;
        this.originalX = x;
        this.originalY = y;
        this.targetX = x;
        this.targetY = y;
        initQuadShotEnemy(x, y);
    }

    @Override
    protected void initEnemy(int x, int y) {
        super.initEnemy(x, y);
        int[] reduceSize = {20, 20};
        this.frames = Util.loadAnimationFrames("src/images/enemyt/enemy3_", 2, 2, reduceSize, true);

        // Create warning frames (red tinted versions)
        this.warningFrames = new BufferedImage[frames.length];
        for (int i = 0; i < frames.length; i++) {
            warningFrames[i] = tintImageRed(frames[i]);
        }

        setImage(frames[currentFrame]);
        this.exhaust = new Exhaust("src/images/exhaust/exhaust3_", 4, 5, this);
    }

    /**
     * Creates a red-tinted version of the image for warning effect
     */
    private BufferedImage tintImageRed(BufferedImage image) {
        BufferedImage tinted = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                // Keep alpha, set red to max, reduce green and blue
                int alpha = (pixel >> 24) & 0xff;
                int red = 255;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;
                green = green / 2;
                blue = blue / 2;
                tinted.setRGB(x, y, (alpha << 24) | (red << 16) | (green << 8) | blue);
            }
        }
        return tinted;
    }

    private void initQuadShotEnemy(int x, int y) {
        initEnemy(x, y);
    }

    public List<EnemyShot> getPendingShots() {
        List<EnemyShot> shots = new ArrayList<>(pendingShots);
        pendingShots.clear();
        return shots;
    }

    public void updateAnimation() {
        if (isWarning) {
            // Faster animation during warning
            if (++animationCounter >= ANIMATION_DELAY / 2) {
                currentFrame = (currentFrame + 1) % frames.length;
                // Alternate between normal and warning frames
                setImage(warningTimer % 10 < 5 ? warningFrames[currentFrame] : frames[currentFrame]);
                animationCounter = 0;
            }
        } else {
            if (++animationCounter >= ANIMATION_DELAY) {
                currentFrame = (currentFrame + 1) % frames.length;
                setImage(frames[currentFrame]);
                animationCounter = 0;
            }
        }
        exhaust.update();
    }

    public Exhaust getExhaust() {
        return exhaust;
    }

    /**
     * Sets a new random target position within small movement range
     */
    private void setRandomTarget() {
        targetX = originalX + (Math.random() * MOVEMENT_RANGE * 2) - MOVEMENT_RANGE;
        targetY = originalY + (Math.random() * MOVEMENT_RANGE * 2) - MOVEMENT_RANGE;

        // Clamp to right half of screen
        targetX = Math.max(minX, Math.min(targetX, BOARD_WIDTH - getImage().getWidth()));
        targetY = Math.max(0, Math.min(targetY, BOARD_HEIGHT - getImage().getHeight()));

        // Calculate direction vector
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            moveX = (dx / distance) * MOVEMENT_SPEED;
            moveY = (dy / distance) * MOVEMENT_SPEED;
        }
    }

    /**
     * Occasionally changes to a completely new random position in right half
     */
    private void changePosition() {
        // Random position in right half of screen
        int newX = minX + (int)(Math.random() * (BOARD_WIDTH - minX - getImage().getWidth()));
        int newY = (int)(Math.random() * (BOARD_HEIGHT - getImage().getHeight()));

        setX(newX);
        setY(newY);
        originalX = newX; // Update original position for small movements
        originalY = newY;
        positionChangeTimer = 0;
        isWarning = false; // End warning state
        setRandomTarget(); // Set new small movement target
    }

    /**
     * Moves toward current target position
     */
    private void moveToTarget() {
        // Check if reached target or needs new target
        if (Math.abs(x - targetX) < MOVEMENT_SPEED && Math.abs(y - targetY) < MOVEMENT_SPEED) {
            setRandomTarget();
        }

        // Apply movement
        x += moveX;
        y += moveY;
    }

    /**
     * Creates 4 spread shots
     */
    private void createShots() {
        int centerY = y + getImage().getHeight() / 2;

        EnemyShot shot1 = new EnemyShot(x, (int) (centerY - SHOT_SPREAD * 1.5), 2);
        EnemyShot shot2 = new EnemyShot(x, centerY - SHOT_SPREAD / 2, 2);
        EnemyShot shot3 = new EnemyShot(x, centerY + SHOT_SPREAD / 2, 2);
//        EnemyShot shot4 = new EnemyShot(x, (int) (centerY + SHOT_SPREAD * 1.5), 2);

        shot1.setDirection(-4, 0);
        shot2.setDirection(-4, 0);
        shot3.setDirection(-4, 0);
//        shot4.setDirection(-4, 0);

        pendingShots.add(shot1);
        pendingShots.add(shot2);
        pendingShots.add(shot3);
//        pendingShots.add(shot4);
    }

    @Override
    public void act(int direction) {
        // Update timers
        shotTimer++;
        positionChangeTimer++;

        // Handle warning state
        if (isWarning) {
            warningTimer++;
            if (warningTimer >= WARNING_DURATION) {
                changePosition();
            }
        }
        // Check if it's time to start warning
        else if (positionChangeTimer >= POSITION_CHANGE_COOLDOWN - WARNING_DURATION) {
            isWarning = true;
            warningTimer = 0;
        }
        // Handle small movements when not in warning state
        else if (!isWarning) {
            moveToTarget();
        }

        // Handle shooting
        if (shotTimer >= SHOT_COOLDOWN) {
            shotTimer = 0;
            createShots();
        }

        updateAnimation();
    }

    public boolean isWarning() {
        return isWarning;
    }
}