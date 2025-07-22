package gdd.powerup;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import gdd.sprite.Player;
import gdd.util.Util;

public class ThreeWayShot extends PowerUp {

    public ThreeWayShot(int x, int y) {
        super(x, y);
        initThreeWayShot();
    }

    private void initThreeWayShot() {
        try {
            BufferedImage img = ImageIO.read(new File("src/images/powerup-threeway.png"));
            setImage(img);
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load MultiShot image: " + e.getMessage());
        }
    }

    @Override
    public void upgrade(Player player) {
        // Enable three-way shot capability for the player
        player.setThreeWayShotEnabled(true);

        // Mark this power-up as collected
        setVisible(false);
    }

    @Override
    public void act() {
        // Move the power-up to the left (like other sprites in the side-scrolling game)
        x -= 2;

        // Remove if off-screen
        if (x < -50) {
            setVisible(false);
        }
    }
}