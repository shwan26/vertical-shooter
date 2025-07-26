package gdd.sprite;

import gdd.util.Util;
import java.awt.image.BufferedImage;

public class Exhaust {
    private BufferedImage[] frames;
    private int currentFrame = 0;
    private int animationCounter = 0;
    private int animationDelay;
    private int xOffset;
    private int yOffset;
    private boolean active = true;
    private int[] reduceSize = {20, 20};

    public Exhaust(String basePath, int frameCount, int animationDelay, Sprite parent) {
        if (parent instanceof Player) {
            this.frames = Util.loadAnimationFrames(basePath, frameCount, 2, reduceSize, false);
            calculateOffsets(parent, false, 10);
        }

        if (parent instanceof Alien1)
        {
            System.out.println("Enemy: Exhaust");
            this.frames = Util.loadAnimationFrames(basePath, frameCount, 2, reduceSize, true);
            calculateOffsets(parent, true, 10);
        }

        if (!(parent instanceof Alien1) && !(parent instanceof Player)) {
            this.frames = Util.loadAnimationFrames(basePath, frameCount, 2, reduceSize, true);
            calculateOffsets(parent, true, 20);
        }
        this.animationDelay = animationDelay;

    }

    private void calculateOffsets(Sprite parent, boolean isBack, int offset) {
        BufferedImage parentImage = parent.getImage();
        int parentWidth = parentImage.getWidth();
        int parentHeight = parentImage.getHeight();

        int exhaustWidth = getCurrentFrame().getWidth();
        int exhaustHeight = getCurrentFrame().getHeight();

        if (isBack) {
            this.xOffset = parentWidth - offset;
            this.yOffset = (parentHeight / 2) - (exhaustHeight / 2);
        } else {
            this.xOffset = -exhaustWidth + offset;
            this.yOffset = (parentHeight / 2) - (exhaustHeight / 2) + 8;
        }

    }

    public void update() {
        if (active && ++animationCounter >= animationDelay) {
            currentFrame = (currentFrame + 1) % frames.length;
            animationCounter = 0;
        }
    }

    public BufferedImage getCurrentFrame() {
        return frames[currentFrame];
    }

    public int getX(Sprite parent) {
        return parent.getX() + xOffset;
    }

    public int getY(Sprite parent) {
        return parent.getY() + yOffset;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void setOffsets(int xOffset, int yOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }
}