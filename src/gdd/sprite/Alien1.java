package gdd.sprite;

import gdd.util.Util;
import java.awt.image.BufferedImage;

/**
 * Basic enemy type that moves horizontally and drops bombs.
 */
public class Alien1 extends Enemy {
    // Constants
    private static final int ANIMATION_DELAY = 10;
    private static final int BOMB_DROP_DELAY = 180;

    // Fields
    private Bomb bomb;               // Bomb object
    private BufferedImage[] frames;  // Animation frames
    private int animationCounter = 0; // Animation counter
    private int bombTimer = 0;       // Bomb drop timer

    public Alien1(int x, int y) {
        super(x, y);
        initAlien(x, y);
    }

    /**
     * Initializes enemy with animation and bomb
     */
    @Override
    protected void initEnemy(int x, int y) {
        super.initEnemy(x, y);
        this.frames = Util.loadAnimationFrames("src/images/enemy/enemy1-", 3, 1);
        setImage(frames[currentFrame]);
        this.bomb = new Bomb(x, y);
    }

    private void initAlien(int x, int y) {
        initEnemy(x, y);
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
     * Updates enemy state including movement and bomb dropping
     */
    @Override
    public void act(int direction) {
        super.act(direction);
        updateAnimation();
        handleBombDropping();
    }

    /**
     * Handles bomb dropping logic
     */
    private void handleBombDropping() {
        if (++bombTimer >= BOMB_DROP_DELAY && bomb.isDestroyed()) {
            bomb = new Bomb(x, y);
            bomb.setDestroyed(false);
            bombTimer = 0;
        }

        if (!bomb.isDestroyed()) {
            bomb.act();
            if (bomb.getX() < 0) bomb.setDestroyed(true);
        }
    }

    /**
     * Inner class representing bombs dropped by the alien
     */
    public class Bomb extends Sprite {
        private static final int ANIMATION_DELAY = 5;

        private boolean destroyed;    // Destruction state
        private BufferedImage[] frames; // Animation frames
        private int animationCounter = 0;

        public Bomb(int x, int y) {
            initBomb(x, y);
        }

        private void initBomb(int x, int y) {
            setDestroyed(true);
            this.x = x;
            this.y = y;
            this.frames = Util.loadAnimationFrames("src/images/bomb/bomb", 8, 1);
            setImage(frames[currentFrame]);
        }

        @Override
        public void act() {
            x -= 1; // Move left
            updateAnimation();
        }

        public void updateAnimation() {
            if (++animationCounter >= ANIMATION_DELAY) {
                currentFrame = (currentFrame + 1) % frames.length;
                setImage(frames[currentFrame]);
                animationCounter = 0;
            }
        }

        // Getters and Setters
        public boolean isDestroyed() { return destroyed; }
        public void setDestroyed(boolean destroyed) { this.destroyed = destroyed; }
    }

    // Getters and Setters
    public Bomb getBomb() { return bomb; }
    public void setBomb(Bomb bomb) { this.bomb = bomb; }
}