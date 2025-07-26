package gdd.sprite;

import gdd.util.Util;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Enemy that tracks and attacks the player when in range.
 * Features warning indicators before attacking.
 */
public class MissileEnemy extends Enemy {
    // ===== CONSTANTS =====
    private static final int ANIMATION_DELAY = 10;
    private static final double DETECTION_RANGE = 500.0;  // Pixels
    private static final double ATTACK_SPEED = 8.0;       // Movement speed when attacking
    private static final double IDLE_DRIFT_SPEED = 0.5;   // Movement speed when idle
    private static final int WARNING_DURATION = 60;       // Frames before attack

    // ===== FIELDS =====
    private BufferedImage[] idleFrames;    // Normal state frames
    private BufferedImage[] attackFrames;  // Attack state frames
    private boolean isAttacking = false;   // Attack state flag
    private boolean isActivated = false;   // Detection state flag
    private double targetX, targetY;       // Last recorded player position
    private double velocityX, velocityY;   // Attack movement vector
    private int warningTimer = 0;          // Warning countdown
    private boolean showWarning = false;   // Warning visibility flag
    private Exhaust exhaust;

    // ===== CONSTRUCTOR =====
    public MissileEnemy(int x, int y) {
        super(x, y);
        initMissileEnemy(x, y);
    }

    // ===== INITIALIZATION =====
    @Override
    protected void initEnemy(int x, int y) {
        super.initEnemy(x, y);
        int[] reduceSize = {10, 10};
        this.idleFrames = Util.loadAnimationFrames("src/images/enemyt/enemy2_", 1, 2, reduceSize, true);
        this.attackFrames = Util.loadAnimationFrames("src/images/enemyt/enemy2_", 1, 2, reduceSize, true);
        setImage(idleFrames[currentFrame]);
        this.exhaust = new Exhaust("src/images/exhaust/exhaust2_", 4, 5, this);
    }

    private void initMissileEnemy(int x, int y) {
        initEnemy(x, y);
    }

    // ===== CORE METHODS =====
    /**
     * Updates animation based on current state
     */
    public void updateAnimation() {
        if (++animationCounter >= ANIMATION_DELAY) {
            BufferedImage[] currentFrames = isAttacking ? attackFrames : idleFrames;
            currentFrame = (currentFrame + 1) % currentFrames.length;
            setImage(currentFrames[currentFrame]);
            animationCounter = 0;
        }

        exhaust.update();
    }

    @Override
    public Rectangle getCollisionBounds() {
        // Reduce hitbox to 70% of width and 80% of height, centered
        int width = (int)(image.getWidth() * 0.7);
        int height = (int)(image.getHeight() * 0.2);
        int xOffset = (image.getWidth() - width) / 2;
        int yOffset = (image.getHeight() - height) / 2;

        return new Rectangle(x + xOffset, y + yOffset, width, height);
    }

    public Exhaust getExhaust() {
        return exhaust;
    }

    /**
     * Checks player proximity and activates attack if in range
     * @param player The player object to track
     */
    public void checkPlayerProximity(Player player) {
        if (isAttacking || player == null || !player.isVisible()) return;

        double deltaX = player.getX() - x;
        double deltaY = player.getY() - y;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distance <= DETECTION_RANGE && !isActivated) {
            isActivated = true;
            showWarning = true;
            warningTimer = WARNING_DURATION;
            targetX = player.getX();
            targetY = player.getY();
        }
    }

    /**
     * Initiates attack sequence
     */
    private void startAttack() {
        isAttacking = true;
        showWarning = false;
        setImage(attackFrames[currentFrame]);

        double deltaX = targetX - x;
        double deltaY = targetY - y;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distance > 0) {
            velocityX = (deltaX / distance) * ATTACK_SPEED;
            velocityY = (deltaY / distance) * ATTACK_SPEED;
        }
    }

    // ===== MOVEMENT METHODS =====
    @Override
    public void act(int direction) {
        if (showWarning && warningTimer > 0) {
            warningTimer--;
            if (warningTimer <= 0) startAttack();
            x += direction * IDLE_DRIFT_SPEED;
            updateAnimation();
            return;
        }

        if (isAttacking) {
            x += velocityX;
            y += velocityY;
            if (x < -100 || x > 1000 || y < -100 || y > 600) setDying(true);
        } else {
            x += direction * IDLE_DRIFT_SPEED;
        }
        updateAnimation();
    }

    // ===== VISUAL EFFECTS =====
    /**
     * Draws warning indicator before attack
     * @param g Graphics context
     */
    public void drawWarningEffect(Graphics g) {
        if (showWarning) {
            int alpha = (warningTimer % 20 < 10) ? 255 : 100;
            g.setColor(new Color(255, 0, 0, alpha));

            int warningRadius = 30 + (WARNING_DURATION - warningTimer) / 2;
            g.drawOval(x - warningRadius/2, y - warningRadius/2, warningRadius, warningRadius);

            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("!", x + 10, y - 10);
        }
    }

    // ===== GETTERS =====
    public boolean isShowingWarning() { return showWarning; }
    public boolean isAttacking() { return isAttacking; }
    public int getWarningTimer() { return warningTimer; }

    /**
     * Updates enemy state based on player position
     * @param player The player object to track
     */
    public void update(Player player) {
        checkPlayerProximity(player);
    }
}