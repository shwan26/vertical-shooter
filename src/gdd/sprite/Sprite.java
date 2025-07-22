package gdd.sprite;

import java.awt.image.BufferedImage;

public abstract class Sprite {
    // Fields
    protected boolean visible;      
    protected boolean dying;         
    protected int x, y;              
    protected BufferedImage image;   
    protected BufferedImage[] frames;
    protected int currentFrame = 0;  
    protected int animationCounter = 0; 
    protected int animationDelay = 0; 

    public Sprite() {
        this.visible = true;
    }

    /**
     * Main update method to be implemented by subclasses
     */
    public abstract void act();

    /**
     * Updates animation frame if multiple frames exist
     */
    public void updateAnimation() {
        if (frames != null && frames.length > 1) {
            animationCounter++;
            if (animationCounter > animationDelay) {
                animationCounter = 0;
                currentFrame = (currentFrame + 1) % frames.length;
                setImage(frames[currentFrame]);
            }
        }
    }

    /**
     * Checks collision with another sprite using bounding boxes
     * @param other The other sprite to check collision with
     * @return true if sprites collide, false otherwise
     */
    public boolean collidesWith(Sprite other) {
        if (other == null || !this.isVisible() || !other.isVisible()) return false;
        if (this.getImage() == null || other.getImage() == null) return false;

        return this.getX() < other.getX() + other.getImage().getWidth() &&
                this.getX() + this.getImage().getWidth() > other.getX() &&
                this.getY() < other.getY() + other.getImage().getHeight() &&
                this.getY() + this.getImage().getHeight() > other.getY();
    }

    // Lifecycle Methods
    public void die() {
        this.visible = false;
    }

    // Getters and Setters
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public BufferedImage getImage() { return image; }
    public void setImage(BufferedImage image) { this.image = image; }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public boolean isDying() { return dying; }
    public void setDying(boolean dying) { this.dying = dying; }
    public void setFrames(BufferedImage[] frames) {
        this.frames = frames;
        if (frames != null && frames.length > 0) setImage(frames[0]);
    }
}