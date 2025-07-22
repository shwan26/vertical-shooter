package gdd.powerup;

import gdd.sprite.Player;
import gdd.util.Util;


public class MultiShot extends PowerUp {

    public MultiShot(int x, int y) {
        super(x, y);
        initMultiShot();
    }

    private void initMultiShot() {
        frames = Util.loadAnimationFrames("src/images/powerup_multishot", 1, 1);
        setImage(frames[0]);
    }

    @Override
    public void upgrade(Player player) {
        // Enable multi-shot capability for the player
        player.setMultiShotEnabled(true);

        // Optional: You could also increase shot count or add a timer
        // For now, we'll just enable the multi-shot capability

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