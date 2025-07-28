package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import gdd.powerup.PowerUp;
import gdd.powerup.SpeedUp;
import gdd.sprite.Alien1;
import gdd.sprite.Enemy;
import gdd.sprite.Explosion;
import gdd.sprite.Player;
import gdd.sprite.Shot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class TutorialScene extends JPanel {

    private final Game game;
    private final Player player = new Player();
    private final List<Shot> shots = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<PowerUp> powerups = new ArrayList<>();
    private final List<Explosion> explosions = new ArrayList<>();

    private int stage = 0;
    private boolean powerupCollected = false;
    private boolean enemyKilled = false;
    private boolean playerHit = false;

    private Timer timer;

    private int frame = 0; // For background scrolling

    private static final int BLOCKWIDTH = 50;
    private static final int BLOCKHEIGHT = 50;

    private final int[][] MAP = {
        {1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0},
        {0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0},
        {0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0},
        {0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0},
        {0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0},
        {0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0},
        {0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0},
        {0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0},
        {0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0},
        {0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0},
        {0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0},
        {0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1}
    };

    public TutorialScene(Game game) {
        this.game = game;
        setFocusable(true);
        setBackground(Color.BLACK);
        addKeyListener(new TAdapter());
    }

    public void start() {
        requestFocusInWindow();
        timer = new Timer(1000 / 60, e -> gameCycle());
        timer.start();
    }

    private void gameCycle() {
        frame++;
        update();
        repaint();
    }

    private void update() {
        player.act();

        if (stage == 1) {
            List<PowerUp> toRemove = new ArrayList<>();
            List<PowerUp> toAdd = new ArrayList<>();
            
            for (PowerUp p : new ArrayList<>(powerups)) {  // Use a copy of the list to avoid concurrent modification
                p.act();

                if (p.collidesWith(player)) {
                    p.upgrade(player);
                    p.setVisible(false);
                    powerupCollected = true;
                    toRemove.add(p);
                } else if (p.getX() < -50) {
                    toRemove.add(p);
                    toAdd.add(new SpeedUp(800, 200));  // re-spawn new powerup off-screen
                }
            }

            powerups.removeAll(toRemove);
            powerups.addAll(toAdd);
        }


        if (stage == 2) {
            List<Enemy> toRemove = new ArrayList<>();
            List<Enemy> toAdd = new ArrayList<>();

            for (Enemy enemy : new ArrayList<>(enemies)) {
                enemy.act(-1);

                for (Shot shot : shots) {
                    if (shot.collidesWith(enemy)) {
                        enemy.setDying(true);
                        enemyKilled = true;
                        explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                        toRemove.add(enemy);
                        break;
                    }
                }

                if (enemy.collidesWith(player)) {
                    playerHit = true;
                    explosions.add(new Explosion(player.getX(), player.getY()));
                    stop();
                    restartTutorial();
                    return;
                }

                if (enemy.getX() < -50) {
                    toRemove.add(enemy);
                    toAdd.add(new Alien1(800, 200));
                }
            }

            enemies.removeAll(toRemove);
            enemies.addAll(toAdd);

        }

        for (Shot shot : shots) shot.act();
        for (Explosion ex : explosions) ex.act();

        if (stage == 0 && !shots.isEmpty()) {
            stage = 1;
            powerups.clear();
            powerups.add(new SpeedUp(400, 200));
        }

        if (stage == 1 && powerupCollected) {
            stage = 2;
            enemies.clear();
            enemies.add(new Alien1(400, 200));
        }

        if (stage == 2 && enemyKilled) {
            stage = 3;
            stop();
            game.loadScene3(); 
        }
    }

    private void stop() {
        if (timer != null) timer.stop();
    }

    private void restartTutorial() {
   
        stage = 0;
        powerupCollected = false;
        enemyKilled = false;
        playerHit = false;

        shots.clear();
        powerups.clear();
        enemies.clear();
        explosions.clear();

        player.reset(); 

        start(); 
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawMap(g); // Add background map
        drawGameObjects(g);
    }

    private void drawGameObjects(Graphics g) {
        if (player.isVisible()) {
            g.drawImage(player.getImage(), player.getX(), player.getY(), this);
        }

        for (Shot s : shots) {
            if (s.isVisible()) g.drawImage(s.getImage(), s.getX(), s.getY(), this);
        }

        for (PowerUp p : powerups) {
            if (p.isVisible()) g.drawImage(p.getImage(), p.getX(), p.getY(), this);
        }

        for (Enemy e : enemies) {
            if (e.isVisible()) g.drawImage(e.getImage(), e.getX(), e.getY(), this);
        }

        for (Explosion ex : explosions) {
            if (ex.isVisible()) g.drawImage(ex.getImage(), ex.getX(), ex.getY(), this);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));

        if (stage == 0) g.drawString("Press SPACE to shoot", 250, 50);
        else if (stage == 1 && !powerupCollected) g.drawString("Collect the power-up!", 250, 50);
        else if (stage == 2 && !enemyKilled) g.drawString("Destroy the enemy!", 250, 50);
        else if (stage == 3) g.drawString("Tutorial Complete!", 250, 50);
    }

    private void drawMap(Graphics g) {
        int scrollOffset = (frame) % BLOCKWIDTH;
        int baseColumn = (frame) / BLOCKWIDTH;
        int columnsNeeded = (800 / BLOCKWIDTH) + 2;

        for (int screenCol = 0; screenCol < columnsNeeded; screenCol++) {
            int mapCol = (baseColumn + screenCol) % MAP.length;
            int x = 800 - ((screenCol * BLOCKWIDTH) - scrollOffset);

            if (x < -BLOCKWIDTH || x > 800) continue;

            for (int row = 0; row < MAP[mapCol].length; row++) {
                if (MAP[mapCol][row] == 1) {
                    int y = row * BLOCKHEIGHT;
                    drawStarCluster(g, x, y, BLOCKWIDTH, BLOCKHEIGHT);
                }
            }
        }
    }

    private void drawStarCluster(Graphics g, int x, int y, int width, int height) {
        g.setColor(Color.WHITE);
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        g.fillOval(centerX - 2, centerY - 2, 4, 4);
        g.fillOval(centerX - 15, centerY - 10, 2, 2);
        g.fillOval(centerX + 12, centerY - 8, 2, 2);
        g.fillOval(centerX - 8, centerY + 12, 2, 2);
        g.fillOval(centerX + 10, centerY + 15, 2, 2);
        g.fillOval(centerX - 20, centerY + 5, 1, 1);
        g.fillOval(centerX + 18, centerY - 15, 1, 1);
        g.fillOval(centerX - 5, centerY - 18, 1, 1);
        g.fillOval(centerX + 8, centerY + 20, 1, 1);
    }

    private class TAdapter extends KeyAdapter {
        @Override
        public void keyReleased(KeyEvent e) {
            player.keyReleased(e);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            player.keyPressed(e);

            if (e.getKeyCode() == KeyEvent.VK_SPACE && stage < 3) {
                if (shots.size() < 5) {
                    shots.addAll(player.createShots());
                    try {
                        new AudioPlayer("src/audio/shot.wav").play();
                    } catch (Exception ex) {
                        System.err.println("Shot sound error: " + ex.getMessage());
                    }
                }
            }
        }
    }
}
