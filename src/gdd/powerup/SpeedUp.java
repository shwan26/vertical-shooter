package gdd.powerup;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import gdd.sprite.Player;

public class SpeedUp extends PowerUp {
    private static final int WIDTH = 20;
    private static final int HEIGHT = 20;

    public SpeedUp(int x, int y) {
        super(x, y);
        initSpeedUp();
    }

    private void initSpeedUp() {
        try {
            BufferedImage original = ImageIO.read(new File("src/images/powerup-s.png"));
            // Scale image to 48x48
            BufferedImage scaled = new BufferedImage(WIDTH, HEIGHT, original.getType());
            Graphics2D g2d = scaled.createGraphics();
            g2d.drawImage(original, 0, 0, WIDTH, HEIGHT, null);
            g2d.dispose();
            setImage(scaled);
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load SpeedUp image: " + e.getMessage());
            // Create blank 48x48 image as fallback
            setImage(new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB));
        }
    }

    @Override
    public void act() {
        x -= 2;
    }

    @Override
    public void upgrade(Player player) {
        player.setSpeed(player.getSpeed() + 4);
        this.die();
    }
}