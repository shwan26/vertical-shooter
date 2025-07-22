package gdd.powerup;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import gdd.sprite.Player;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


public class SpeedUp extends PowerUp {

    public SpeedUp(int x, int y) {
    super(x, y);

    try {
        BufferedImage img = ImageIO.read(new File("src/images/powerup-s.png"));
        setImage(img);
    } catch (IOException e) {
        System.err.println("[ERROR] Failed to load SpeedUp image: " + e.getMessage());
    }
}


    public void act() {
        
        this.x -= 2; 
    }

    public void upgrade(Player player) {
        player.setSpeed(player.getSpeed() + 4); 
        this.die(); 
    }

}
