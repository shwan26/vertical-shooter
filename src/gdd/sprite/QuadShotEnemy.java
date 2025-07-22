package gdd.sprite;

import gdd.util.Util;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static gdd.Global.BOARD_HEIGHT;
import static gdd.Global.BOARD_WIDTH;

/**
 * Enemy that fires 4 shots simultaneously with a cooldown period
 * and periodically changes position on the right half of the screen.
 */
public class QuadShotEnemy extends Enemy {
    // Constants
    private static final int ANIMATION_DELAY = 10;
    private static final int SHOT_COOLDOWN = 180; // 2 seconds at 60 FPS
    private static final int POSITION_CHANGE_COOLDOWN = 360; // 6 seconds at 60 FPS
    private static final int SHOT_SPREAD = 15; // Spread between shots in pixels

    // Fields
    private BufferedImage[] frames;          // Animation frames
    private int shotTimer = 0;               // Timer for shot cooldown
    private int positionChangeTimer = 0;     // Timer for position changes
    private int minX;                        // Minimum X position (right half of screen)
    private List<EnemyShot> pendingShots = new ArrayList<>(); // Shots waiting to be added to game

    public QuadShotEnemy(int x, int y) {
        super(x, y);
        this.minX = BOARD_WIDTH / 2; // Right half of screen
        initQuadShotEnemy(x, y);
    }

    @Override
    protected void initEnemy(int x, int y) {
        super.initEnemy(x, y);
        this.frames = Util.loadAnimationFrames("src/images/enemy/enemy1-", 2, 1);
        setImage(frames[currentFrame]);
    }

    private void initQuadShotEnemy(int x, int y) {
        initEnemy(x, y);
    }

    /**
     * Gets any shots that were created this frame
     * @return List of new shots (empty if none created)
     */
    public List<EnemyShot> getPendingShots() {
        List<EnemyShot> shots = new ArrayList<>(pendingShots);
        pendingShots.clear();
        return shots;
    }

    /**
     * Updates animation frames
     */
    public void updateAnimation() {
        if (++animationCounter >= ANIMATION_DELAY) {
            currentFrame = (currentFrame + 1) % frames.length;
            setImage(frames[currentFrame]);
            animationCounter = 0;
        }
    }

    /**
     * Changes position randomly within right half of screen
     */
    private void changePosition() {
        // Random position in right half of screen
        int newX = minX + (int)(Math.random() * (BOARD_WIDTH - minX - getImage().getWidth()));
        int newY = (int)(Math.random() * (BOARD_HEIGHT - getImage().getHeight()));

        setX(newX);
        setY(newY);
        positionChangeTimer = 0;
    }

    /**
     * Creates 4 spread shots
     */
    private void createShots() {
        int centerY = y + getImage().getHeight() / 2;

        // Create 4 shots with vertical spread
        EnemyShot shot1 = new EnemyShot(x, (int) (centerY - SHOT_SPREAD * 1.5));
        EnemyShot shot2 = new EnemyShot(x, centerY - SHOT_SPREAD / 2);
        EnemyShot shot3 = new EnemyShot(x, centerY + SHOT_SPREAD / 2);
//        EnemyShot shot4 = new EnemyShot(x, (int) (centerY + SHOT_SPREAD * 1.5));

        // Set direction for all shots (leftward)
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

        // Handle position changes
        if (positionChangeTimer >= POSITION_CHANGE_COOLDOWN) {
            changePosition();
        }

        // Handle shooting
        if (shotTimer >= SHOT_COOLDOWN) {
            shotTimer = 0;
            createShots();
        }

        updateAnimation();
    }
}