package gdd.sprite;

import gdd.scene.Scene1;
import gdd.util.Util;
import static gdd.Global.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class LaserEnemy extends Enemy {
    // Constants
    private static final int ANIMATION_DELAY = 8;
    private static final int CHARGING_TIME = 180;    // 1.5 seconds at 60 FPS
    private static final int FIRING_TIME = 1000;     // 2 seconds of laser fire
    private static final int COOLDOWN_TIME = 180;   // 3 seconds cooldown
    private static final int POSITION_THRESHOLD = 50;
    private static final float ANGLE_INCREMENT = 0.4f;
    private static final float MAX_ANGLE = 90f;
    private static final int LASER_LENGTH = 750;
    private static final int BULLET_SPREAD_COUNT = 8;
    private static final int BULLET_SPREAD_DELAY = 30;
    private static final int BULLET_SPREAD_SPEED = 5;
    private static final int INVULNERABILITY_DURATION = 30;
    private static final int MAX_HEALTH = 10;

    // Enums
    private enum LaserState {
        MOVING, CHARGING, FIRING, COOLDOWN
    }

    // Animation fields
    private BufferedImage[] idleFrames;
    private BufferedImage[] chargingFrames;
    private BufferedImage[] firingFrames;
    private int currentFrame = 0;
    private int animationCounter = 0;

    // State management
    private LaserState currentState = LaserState.MOVING;
    private int stateTimer = 0;
    private Scene1 scene;

    // Laser properties
    private boolean laserActive = false;
    private float currentAngle = 0f;
    private boolean increasingAngle = true;
    private Color laserColor = new Color(255, 0, 0, 200);

    // Movement properties
    private int laserSweepDirection;
    private float circleRadius = 150f;
    private float circleSpeed = 0.8f;
    private float circleAngle = 0f;
    private int circleCenterX, circleCenterY;

    // Visual effects
    private boolean chargingFlash = false;
    private int flashTimer = 0;
    private int bulletSpreadTimer = 0;

    // Health system
    private int currentHealth = MAX_HEALTH;
    private boolean isInvulnerable = false;
    private int invulnerabilityTimer = 0;

    private int laserStartX;  // X position where laser originates
    private int laserStartY;  // Y position where laser originates

    private final Random randomizer = new Random();

    // Constructor
    public LaserEnemy(int x, int y, Scene1 scene) {
        super(x, y);
        this.scene = scene;
        this.laserSweepDirection = randomizer.nextBoolean() ? 1 : -1;
        initLaserEnemy(x, y);
    }

    // Initialization methods
    @Override
    protected void initEnemy(int x, int y) {
        this.x = x;
        this.y = y;
        this.circleCenterX = x;
        this.circleCenterY = y;

        idleFrames = Util.loadAnimationFrames("src/images/enemy/enemy1-", 3, 1);
        chargingFrames = Util.loadAnimationFrames("src/images/enemy/enemy_missile", 2, 1);
        firingFrames = Util.loadAnimationFrames("src/images/enemy/enemy_missile_attack", 2, 1);

        setImage(idleFrames[currentFrame]);
    }

    private void initLaserEnemy(int x, int y) {
        initEnemy(x, y);
        currentState = LaserState.MOVING;
        stateTimer = 0;
    }

    // Update methods
    @Override
    public void act(int direction) {
        debugStateInfo();
        updateInvulnerability();
        updateCircularMovement();
        updateState();
        updateAnimation();
    }

    @Override
    public void act() {
        act(-1);
    }

    private void debugStateInfo() {
        System.out.printf("LaserEnemy State: %s @(%d,%d) Timer: %d%n",
                currentState, x, y, stateTimer);
    }

    private void updateInvulnerability() {
        if (isInvulnerable) {
            invulnerabilityTimer--;
            if (invulnerabilityTimer <= 0) {
                isInvulnerable = false;
            }
        }
    }

    private void updateCircularMovement() {
        circleAngle += circleSpeed;
        this.x = circleCenterX + (int)(circleRadius * Math.cos(Math.toRadians(circleAngle)));
        this.y = circleCenterY + (int)(circleRadius * Math.sin(Math.toRadians(circleAngle)));
        stateTimer++;
    }

    private void updateState() {
        switch (currentState) {
            case MOVING:    handleMovingState(); break;
            case CHARGING:  handleChargingState(); break;
            case FIRING:    handleFiringState(); break;
            case COOLDOWN:  handleCooldownState(); break;
        }
    }

    // State handlers
    private void handleMovingState() {
        if (x > BOARD_WIDTH - POSITION_THRESHOLD) {
            this.x += -1 * 2; // Always move left
        } else {
            currentState = LaserState.CHARGING;
            stateTimer = 0;
        }
    }

    private void handleChargingState() {
        handleChargingPhase();
        if (stateTimer >= CHARGING_TIME) {
            currentState = LaserState.FIRING;
            stateTimer = 0;
            laserActive = true;
            calculateLaserPosition();
            
        }
    }

    private void handleFiringState() {
        updateFanAngle();
        if (stateTimer >= FIRING_TIME) {
            currentState = LaserState.COOLDOWN;
            stateTimer = 0;
            laserActive = false;
            currentAngle = 0f;
            System.out.println("FIRING LASER!");
        }
    }

    private void handleCooldownState() {
        updateBulletSpread();
        if (stateTimer >= COOLDOWN_TIME) {
            currentState = LaserState.MOVING;
            stateTimer = 0;
            this.x = BOARD_WIDTH;
            System.out.println("RESTARTING CYCLE");
        }
    }

    // Combat methods
    public void takeDamage(ArrayList<Explosion> explosions) {
        if (!isInvulnerable) {
            currentHealth--;
            isInvulnerable = true;
            invulnerabilityTimer = INVULNERABILITY_DURATION;
            setImage(chargingFrames[1]);

            if (currentHealth <= 0) {
                setDying(true);
                explosions.add(new Explosion(x, y));
                // Notify scene that we've been killed
                if (scene != null) {
                    scene.enemyKilled(this);
                }
            }
        }
    }

    private void fireBulletSpread() {
        if (scene == null) return;

        ArrayList<EnemyShot> spreadShots = new ArrayList<>();
        float angleStep = 360f / BULLET_SPREAD_COUNT;

        for (int i = 0; i < BULLET_SPREAD_COUNT; i++) {
            float angle = i * angleStep;
            double dirX = Math.cos(Math.toRadians(angle));
            double dirY = Math.sin(Math.toRadians(angle));

            EnemyShot shot = new EnemyShot(
                    x + getImage().getWidth()/2,
                    y + getImage().getHeight()/2
            );
            shot.setDirection(dirX * BULLET_SPREAD_SPEED, dirY * BULLET_SPREAD_SPEED);
            spreadShots.add(shot);
        }

        scene.addEnemyShots(spreadShots);
    }

    // Animation methods
    public void updateAnimation() {
        animationCounter++;
        if (animationCounter >= ANIMATION_DELAY) {
            BufferedImage[] currentFrames = getCurrentAnimationFrames();
            currentFrame = (currentFrame + 1) % currentFrames.length;
            setImage(currentFrames[currentFrame]);
            animationCounter = 0;
        }
    }

    private BufferedImage[] getCurrentAnimationFrames() {
        switch (currentState) {
            case CHARGING: return chargingFrames;
            case FIRING: return firingFrames;
            default: return idleFrames;
        }
    }

    // Laser methods
    private void updateFanAngle() {
        if (increasingAngle) {
            currentAngle += ANGLE_INCREMENT;
            if (currentAngle >= MAX_ANGLE) increasingAngle = false;
        } else {
            currentAngle -= ANGLE_INCREMENT;
            if (currentAngle <= -MAX_ANGLE) increasingAngle = true;
        }
    }

    private void calculateLaserPosition() {
        laserStartX = x - 10;
        laserStartY = y + (getImage().getHeight(null) / 2) - (8 / 2);
    }

    private void handleChargingPhase() {
        flashTimer++;
        if (flashTimer >= 10) {
            chargingFlash = !chargingFlash;
            flashTimer = 0;
        }
    }

    private void updateBulletSpread() {
        bulletSpreadTimer++;
        if (bulletSpreadTimer >= BULLET_SPREAD_DELAY) {
            fireBulletSpread();
            bulletSpreadTimer = 0;
        }
    }

    // Drawing methods
    public void drawLaser(Graphics2D g2d) {
        if (!laserActive) return;

        int endX = (int)(x - LASER_LENGTH * Math.cos(Math.toRadians(currentAngle)));
        int endY = (int)(y + LASER_LENGTH * Math.sin(Math.toRadians(currentAngle))) - 4;

        g2d.setColor(laserColor);
        g2d.setStroke(new BasicStroke(5f));
        g2d.drawLine(x, y + getImage().getHeight()/2, endX, endY);

        g2d.setColor(new Color(255, 50, 50, 100));
        g2d.fillOval(x - 15, y + getImage().getHeight()/2 - 15, 30, 30);
    }

    public void drawChargingEffect(Graphics2D g2d) {
        if (currentState != LaserState.CHARGING || !chargingFlash) return;

        g2d.setColor(new Color(255, 255, 0, 100));
        int glowSize = 40 + (stateTimer / 3);
        g2d.fillOval(x - glowSize/2, y - glowSize/2,
                getImage().getWidth(null) + glowSize,
                getImage().getHeight(null) + glowSize);

        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("CHARGING!", x - 20, y - 10);
    }

    public void drawHealthBar(Graphics2D g2d) {
        if (currentHealth >= MAX_HEALTH) return;

        int barWidth = 60;
        int barHeight = 8;
        int xPos = x - barWidth/2 + getImage().getWidth()/2;
        int yPos = y - 15;

        g2d.setColor(Color.BLACK);
        g2d.fillRect(xPos, yPos, barWidth, barHeight);

        float healthPercent = (float)currentHealth / MAX_HEALTH;
        g2d.setColor(healthPercent > 0.6f ? Color.GREEN :
                healthPercent > 0.3f ? Color.YELLOW : Color.RED);
        g2d.fillRect(xPos, yPos, (int)(barWidth * healthPercent), barHeight);

        g2d.setColor(Color.WHITE);
        g2d.drawRect(xPos, yPos, barWidth, barHeight);
    }

    // Collision detection
    public boolean laserCollidesWith(Player player) {
        if (!laserActive || player == null || !player.isVisible()) {
            return false;
        }

        Line2D laserLine = new Line2D.Float(
                x, y + getImage().getHeight()/2,
                x - LASER_LENGTH * (float)Math.cos(Math.toRadians(currentAngle)),
                y + LASER_LENGTH * (float)Math.sin(Math.toRadians(currentAngle)) - 4
        );

        return laserLine.intersects(
                player.getX(),
                player.getY(),
                player.getImage().getWidth(null),
                player.getImage().getHeight(null)
        );
    }

    // Getters
    public boolean isLaserActive() { return laserActive; }
    public LaserState getCurrentState() { return currentState; }
    public boolean isCharging() { return currentState == LaserState.CHARGING; }
    public int getStateTimer() { return stateTimer; }
    public int getChargingTime() { return CHARGING_TIME; }
    public Rectangle getLaserBounds() {
        return laserActive ? new Rectangle(laserStartX - 800, laserStartY, 800, 8) : null;
    }
}