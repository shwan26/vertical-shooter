package gdd.powerup;

import gdd.sprite.Player;
import gdd.util.Util;


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


public class SpeedUp extends PowerUp {

    public SpeedUp(int x, int y) {
        super(x, y);

        frames = Util.loadAnimationFrames("src/images/powerup-s", 1, 1);
        setImage(frames[0]);
    }

    public void act() {
        
        this.x -= 2; 
    }

    public void upgrade(Player player) {
        player.setSpeed(player.getSpeed() + 4); 
        this.die(); 
    }

}
