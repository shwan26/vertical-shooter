package gdd.sprite;

import gdd.util.Util;

import static gdd.Global.*;

public class DirectionalShot extends Shot {

    private static final int SHOT_SPEED = 12;  

    private double directionX, directionY;  
    private double posX, posY;             

    public DirectionalShot(int x, int y, double directionX, double directionY) {
        super();
        this.directionX = directionX;
        this.directionY = directionY;
        this.posX = x;
        this.posY = y;
        initDirectionalShot(x, y);
    }

    private void initDirectionalShot(int x, int y) {
        frames = Util.loadAnimationFrames("src/images/shot/bullet_round", 2, 1);
        setImage(frames[currentFrame]);
        setX(x + H_SPACE);
        setY(y + V_SPACE);
        this.posX = getX();
        this.posY = getY();
    }

    @Override
    public void act() {
       
        posX += directionX * SHOT_SPEED;
        posY += directionY * SHOT_SPEED;

        setX((int) posX);
        setY((int) posY);
        updateAnimation();

        if (posX > BOARD_WIDTH + 50 || posX < -50 ||
                posY > BOARD_HEIGHT + 50 || posY < -50) {
            setVisible(false);
        }
    }
}