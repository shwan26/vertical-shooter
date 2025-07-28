package gdd.sprite;

import gdd.util.Util;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static gdd.Global.*;

public class Player extends Sprite {
   
    private static final int ANIMATION_DELAY = 10;
    private static final int START_X = 50;
    private static final int START_Y = 270;
    private static final int MULTI_SHOT_TIME = 600;
    private static final int THREE_WAY_SHOT_TIME = 800;

    private BufferedImage[] frames;
    private Exhaust exhaust;
    private int currentSpeed = 2;         
    private int dx = 0, dy = 0;            
    private boolean multiShotEnabled = false;
    private int multiShotDuration = 0;
    private boolean threeWayShotEnabled = false;
    private int threeWayShotDuration = 0;

    private boolean invulnerable = false;
    private int invulnerabilityTimer = 0;
    private static final int INVULNERABILITY_DURATION = 80;

    public Player() {
        initPlayer();
    }

    private void initPlayer() {
        int[] reduceSize = {30, 30};
        this.frames = Util.loadAnimationFrames("src/images/playert/player", 1, 2, reduceSize, false);
        setImage(frames[currentFrame]);
        setX(START_X);
        setY(START_Y);
        this.exhaust = new Exhaust("src/images/exhaust/exhaust5_", 4, 5, this);
    }

    public void updateAnimation() {
        if (++animationCounter >= ANIMATION_DELAY) {
            currentFrame = (currentFrame + 1) % frames.length;
            setImage(frames[currentFrame]);
            animationCounter = 0;
        }

        exhaust.update();
    }

    public Exhaust getExhaust() {
        return exhaust;
    }

    public List<Shot> createShots() {
        List<Shot> shots = new ArrayList<>();

        if (threeWayShotEnabled) {
         
            int baseX = getX(), baseY = getY();
            shots.add(new DirectionalShot(baseX, baseY, 1.0, 0.0));
            shots.add(new DirectionalShot(baseX, baseY, 0.95, -0.2));
            shots.add(new DirectionalShot(baseX, baseY, 0.95, 0.2));
        }
        else if (multiShotEnabled) {
            
            int baseX = getX(), baseY = getY();
            shots.add(new Shot(baseX, baseY));
            shots.add(new Shot(baseX, baseY - 15));
            shots.add(new Shot(baseX, baseY + 15));
            shots.add(new Shot(baseX, baseY - 30));
        }
        else {
            
            shots.add(new Shot(getX(), getY()));
        }

        return shots;
    }

    @Override
    public void act() {
        x += dx;
        y += dy;

        x = Math.max(0, Math.min(x, BOARD_WIDTH / 2 - getImage().getWidth()));
        y = Math.max(2, Math.min(y, BOARD_HEIGHT - getImage().getHeight() - 2));

        if (multiShotEnabled && --multiShotDuration <= 0) multiShotEnabled = false;
        if (threeWayShotEnabled && --threeWayShotDuration <= 0) threeWayShotEnabled = false;

        if (invulnerable) {
            invulnerabilityTimer--;
            if (invulnerabilityTimer <= 0) {
                invulnerable = false;
            }
        }
        updateAnimation();
    }

    @Override
    public Rectangle getCollisionBounds() {
        int width = (int)(image.getWidth() * 0.7);
        int height = (int)(image.getHeight() * 0.5);
        int xOffset = (image.getWidth() - width) / 2;
        int yOffset = (image.getHeight() - height) / 2;

        return new Rectangle(x + xOffset, y + yOffset, width, height);
    }

    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP: dy = -currentSpeed; break;
            case KeyEvent.VK_DOWN: dy = currentSpeed; break;
            case KeyEvent.VK_LEFT: dx = -currentSpeed; break;
            case KeyEvent.VK_RIGHT: dx = currentSpeed; break;
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) dy = 0;
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) dx = 0;
    }

    public void reset() {
        this.x = 100;
        this.y = 200;
        this.visible = true;
       
    }


    public int getSpeed() { return currentSpeed; }
    public int setSpeed(int speed) { return currentSpeed = Math.max(1, speed); }
    public void setMultiShotEnabled(boolean enabled) {
        multiShotEnabled = enabled;
        if (enabled) multiShotDuration = MULTI_SHOT_TIME;
    }
    public boolean isMultiShotEnabled() { return multiShotEnabled; }
    public void setThreeWayShotEnabled(boolean enabled) {
        threeWayShotEnabled = enabled;
        if (enabled) threeWayShotDuration = THREE_WAY_SHOT_TIME;
    }
    public boolean isThreeWayShotEnabled() { return threeWayShotEnabled; }
    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
        if (invulnerable) {
            this.invulnerabilityTimer = INVULNERABILITY_DURATION;
        }
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public void addLife() {
        
    }
}