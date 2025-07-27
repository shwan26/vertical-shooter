package gdd.powerup;

import gdd.sprite.Player;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class ExtraLife extends PowerUp {

    public ExtraLife(int x, int y) throws IOException {
        super(x, y);
        BufferedImage original = ImageIO.read(new File("src/images/live.png"));
        setImage(original);
    }

    @Override
    public void upgrade(Player player) {
        player.addLife(); // Implement this method in Player
    }

    @Override
    public void act() {
        x -= 2;
    }
}
