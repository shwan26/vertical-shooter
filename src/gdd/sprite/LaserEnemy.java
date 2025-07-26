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
    private static final int ANIMATION_DELAY = 8;
    private static final int CHARGING_TIME = 180;    // 3 seconds at 60 FPS
    private static final int FIRING_TIME = 120;      // 2 seconds of laser fire
    private static final int COOLDOWN_TIME = 180;    // 3 seconds cooldown
    private static final float ANGLE_INCREMENT = 0.4f;
    private static final float MAX_ANGLE = 90f;
    private static final int LASER_LENGTH = 550;
    private static final int BULLET_SPREAD_COUNT = 8;
    private static final int BULLET_SPREAD_DELAY = 30;
    private static final int BULLET_SPREAD_SPEED = 5;
    private static final int INVULNERABILITY_DURATION = 30;
    private static final int MAX_HEALTH = 10;
    private static final int APPROACH_SPEED = 1; // Slower movement speed when approaching center

    // Enums
    private enum LaserState {
        APPROACHING {
            @Override
            public void handle(LaserEnemy enemy) {
                enemy.updateApproach();

                // Transition to MOVING when reaching middle of screen
                if (enemy.x <= BOARD_WIDTH / 2) {
                    enemy.transitionTo(LaserState.MOVING);
                    enemy.circleCenterX = enemy.x;
                    enemy.circleCenterY = enemy.y;
                }
            }
        },
        MOVING {
            @Override
            public void handle(LaserEnemy enemy) {
                enemy.updateCircularMovement();

                if (enemy.stateTimer >= 180) { // Adjust time as needed
                    enemy.transitionTo(LaserState.CHARGING);
                }
            }
        },
        CHARGING {
            @Override
            public void handle(LaserEnemy enemy) {
                enemy.updateCircularMovement();
                enemy.handleChargingPhase();

                if (enemy.stateTimer >= CHARGING_TIME) {
                    enemy.transitionTo(LaserState.FIRING);
                    enemy.laserActive = true;
                    enemy.calculateLaserPosition();
                }
            }
        },
        FIRING {
            @Override
            public void handle(LaserEnemy enemy) {
                enemy.updateCircularMovement();
                enemy.updateFanAngle();

                if (enemy.stateTimer >= FIRING_TIME) {
                    enemy.transitionTo(LaserState.COOLDOWN);
                    enemy.laserActive = false;
                    enemy.currentAngle = 0f;
                }
            }
        },
        COOLDOWN {
            @Override
            public void handle(LaserEnemy enemy) {
                enemy.updateCircularMovement();
                enemy.updateBulletSpread();

                if (enemy.stateTimer >= COOLDOWN_TIME) {
                    enemy.transitionTo(LaserState.MOVING);
                }
            }
        };

        public abstract void handle(LaserEnemy enemy);
    }

    // Animation fields
    private BufferedImage[] idleFrames;
    private BufferedImage[] chargingFrames;
    private BufferedImage[] firingFrames;
    private int currentFrame = 0;
    private int animationCounter = 0;
    private Exhaust exhaust;

    // State management
    private LaserState currentState = LaserState.APPROACHING;
    private int stateTimer = 0;
    private Scene1 scene;

    // Laser properties
    private boolean laserActive = false;
    private float currentAngle = 0f;
    private boolean increasingAngle = true;
    private Color laserColor = new Color(255, 0, 0, 200);

    // Movement properties
    private int laserSweepDirection;
    private float circleRadius = 50f;
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

    private int laserStartX;
    private int laserStartY;

    private final Random randomizer = new Random();

    public LaserEnemy(int x, int y, Scene1 scene) {
        super(x, y);
        this.scene = scene;
        this.laserSweepDirection = randomizer.nextBoolean() ? 1 : -1;
        initLaserEnemy(x, y);
    }

    @Override
    protected void initEnemy(int x, int y) {
        this.x = x;
        this.y = y;
        this.circleCenterX = x;
        this.circleCenterY = y;

        idleFrames = Util.loadAnimationFrames("src/images/enemyt/enemy6_", 2, 2, true);
        chargingFrames = Util.loadAnimationFrames("src/images/enemyt/enemy6_", 2, 2, true);
        firingFrames = Util.loadAnimationFrames("src/images/enemyt/enemy6_", 2, 2, true);

        setImage(idleFrames[currentFrame]);
        this.exhaust = new Exhaust("src/images/exhaust/exhaust6_", 4, 5, this);
    }

    private void initLaserEnemy(int x, int y) {
        initEnemy(x, y);
        transitionTo(LaserState.APPROACHING);
    }

    private void transitionTo(LaserState newState) {
        this.currentState = newState;
        this.stateTimer = 0;
    }

    private void updateApproach() {
        // Move left until reaching middle of screen
        x -= APPROACH_SPEED;
    }

    private void resetPosition() {
        this.x = BOARD_WIDTH;
        this.circleAngle = 0f;
        this.circleCenterX = BOARD_WIDTH;
        this.circleCenterY = (int)(BOARD_HEIGHT * 0.3f);
    }

    @Override
    public void act(int direction) {
        updateInvulnerability();
        currentState.handle(this);
        stateTimer++;
        updateAnimation();
    }

    @Override
    public void act() {
        act(-1);
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
    }

    private void handleChargingPhase() {
        flashTimer++;
        if (flashTimer >= 10) {
            chargingFlash = !chargingFlash;
            flashTimer = 0;
        }
    }

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

    private void updateBulletSpread() {
        bulletSpreadTimer++;
        if (bulletSpreadTimer >= BULLET_SPREAD_DELAY) {
            fireBulletSpread();
            bulletSpreadTimer = 0;
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

            // Calculate starting position relative to enemy's current position
            int shotX = x + getImage().getWidth()/2;
            int shotY = y + getImage().getHeight()/2;

            EnemyShot shot = new EnemyShot(shotX, shotY, 3);

            // Set pure radial spread direction (no circular motion influence)
            shot.setDirection(
                    dirX * BULLET_SPREAD_SPEED,
                    dirY * BULLET_SPREAD_SPEED
            );

            spreadShots.add(shot);
        }

        scene.addEnemyShots(spreadShots);
    }

    public void takeDamage(ArrayList<Explosion> explosions) {
        if (!isInvulnerable) {
            currentHealth--;
            isInvulnerable = true;
            invulnerabilityTimer = INVULNERABILITY_DURATION;
            setImage(chargingFrames[1]);

            if (currentHealth <= 0) {
                setDying(true);
                explosions.add(new Explosion(x, y));
                if (scene != null) {
                    scene.enemyKilled(this);
                }
            }
        }
    }

    public void updateAnimation() {
        animationCounter++;
        if (animationCounter >= ANIMATION_DELAY) {
            BufferedImage[] currentFrames = getCurrentAnimationFrames();
            currentFrame = (currentFrame + 1) % currentFrames.length;
            setImage(currentFrames[currentFrame]);
            animationCounter = 0;
        }

        exhaust.update();
    }

    private BufferedImage[] getCurrentAnimationFrames() {
        switch (currentState) {
            case CHARGING: return chargingFrames;
            case FIRING: return firingFrames;
            default: return idleFrames;
        }
    }

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
    public Exhaust getExhaust() {
        return exhaust;
    }
}