package gdd.sprite;

/**
 * Base class for all enemy types.
 * Provides common enemy behavior like movement and direction handling.
 */
public class Enemy extends Sprite {
    // Constants
    protected static final int DEFAULT_SPEED = 2;

    // Fields
    protected int direction = -1; // Movement direction (-1 = left, 1 = right)

    public Enemy(int x, int y) {
        super();
        initEnemy(x, y);
    }

    /**
     * Initializes enemy at specified position
     * @param x X-coordinate
     * @param y Y-coordinate
     */
    protected void initEnemy(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Moves enemy in specified direction
     * @param direction Movement direction (-1 = left, 1 = right)
     */
    public void act(int direction) {
        this.x += direction * DEFAULT_SPEED;
    }

    /**
     * Default movement using class direction
     */
    @Override
    public void act() {
        act(direction);
    }
}