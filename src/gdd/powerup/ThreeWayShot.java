package gdd.powerup;

import gdd.sprite.Player;
import gdd.util.Util;

public class ThreeWayShot extends PowerUp {

    public ThreeWayShot(int x, int y) {
        super(x, y);
        initThreeWayShot();
    }

    private void initThreeWayShot() {
        frames = Util.loadAnimationFrames("src/images/powerup_threewayshot", 1, 1);
        setImage(frames[0]);
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