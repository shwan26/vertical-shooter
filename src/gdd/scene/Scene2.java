package gdd.scene;

import gdd.*;
import gdd.powerup.*;
import gdd.sprite.*;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Scene2 extends JPanel {
    private final int LASER_ENEMY_COUNT = 2;
    private final int BOARD_WIDTH = Global.BOARD_WIDTH;
    private final int BOARD_HEIGHT = Global.BOARD_HEIGHT;

    private int lives = 5;
    private int deaths = 100; 
    private final int LEVEL_BAR_X = 100;
    private final int LEVEL_BAR_Y = 10;
    private final int LEVEL_BAR_WIDTH = 200;
    private final int LEVEL_BAR_HEIGHT = 10;

    private Game game;
    private Player player;
    private List<LaserEnemy> enemies;
    private List<Shot> shots;
    private List<Explosion> explosions;
    private List<PowerUp> powerUps;
    private List<EnemyShot> enemyShots;
    private boolean inGame = true;
    private int laserEnemiesKilled = 0;
    private int frame = 0;
    private Timer timer;
    private Random random = new Random();
    private boolean powerUpOnScreen = false;

    private AudioPlayer audioPlayer;

    private final int[][] MAP = {
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}
    };

    private final int BLOCK_WIDTH = 50;
    private final int BLOCK_HEIGHT = 50;
    private int mapOffset = 0;


    public Scene2(Game game) {
        this.game = game;
        setFocusable(true);
        setBackground(Color.BLACK);
        addKeyListener(new TAdapter());
        initGame();
    }

    private void initGame() {
        player = new Player();
        enemies = new ArrayList<>();
        shots = new ArrayList<>();
        explosions = new ArrayList<>();
        powerUps = new ArrayList<>();
        enemyShots = new ArrayList<>();

        for (int i = 0; i < LASER_ENEMY_COUNT; i++) {
            int y = 100 + random.nextInt(300);
            enemies.add(new LaserEnemy(BOARD_WIDTH, y, this));
        }

        // Play warning sound before background music
        new Thread(() -> {
            try {
                AudioPlayer warning = new AudioPlayer("src/audio/scene2_warning.wav");
                warning.play();
                Thread.sleep(2500); // wait for warning sound to finish
                audioPlayer = new AudioPlayer("src/audio/scene2.wav");
                audioPlayer.setLoop(true);
                audioPlayer.play();
            } catch (Exception e) {
                System.err.println("Scene2 audio error: " + e.getMessage());
            }
        }).start();

        timer = new Timer(1000 / 60, event -> gameCycle());
        timer.start();
    }


    private void gameCycle() {
        frame++;
        updateGame();
        repaint();
    }

    private void updateGame() {
        player.act();
        updateEnemies();
        updateShots();
        updateEnemyShots();
        updatePowerUps();
        spawnRandomPowerUps();
        checkWinCondition();
    }

    private void updateEnemies() {
        List<LaserEnemy> toRemove = new ArrayList<>();
        for (LaserEnemy le : enemies) {
            le.act(-1);
            if (le.isLaserActive() && le.laserCollidesWith(player)) {
                if (!player.isInvulnerable()) {
                    handlePlayerHit();
                }
            }
            if (le.isDying()) {
                toRemove.add(le);
            }
        }
        enemies.removeAll(toRemove);
        laserEnemiesKilled += toRemove.size();
    }

    private void handlePlayerHit() {
        if (player.isInvulnerable() || player.isDying()) return;

        player.setInvulnerable(true);
        lives--;

        explosions.add(new Explosion(
            player.getX() + player.getImage().getWidth()/2 - 16,
            player.getY() + player.getImage().getHeight()/2 - 16,
            false
        ));

        try {
            new AudioPlayer("src/audio/explosion.wav").play();
        } catch (Exception e) {
            System.err.println("Explosion sound error: " + e.getMessage());
        }

        if (lives <= 0) {
            handlePlayerDeath();
        }
    }

    private void handlePlayerDeath() {
        if (!player.isDying()) {
            player.setDying(true);
            explosions.add(new Explosion(
                player.getX() + player.getImage().getWidth()/2 - 32,
                player.getY() + player.getImage().getHeight()/2 - 32,
                true
            ));

            inGame = false;
            timer.stop();

            try {
                if (audioPlayer != null) audioPlayer.stop();
                new AudioPlayer("src/audio/gameover.wav").play();
            } catch (Exception e) {
                System.err.println("Game over sound error: " + e.getMessage());
            }
        }
    }

    private void updateShots() {
        List<Shot> toRemove = new ArrayList<>();
        for (Shot s : shots) {
            s.act();
            for (LaserEnemy e : enemies) {
                if (s.collidesWith(e)) {
                    e.takeDamage((ArrayList<Explosion>) explosions);
                    if (e.isDying()) {
                        deaths += 10;
                    }
                    toRemove.add(s);
                    break;
                }
            }
        }
        shots.removeAll(toRemove);
    }

    private void updateEnemyShots() {
        List<EnemyShot> toRemove = new ArrayList<>();
        for (EnemyShot s : enemyShots) {
            s.act();
            if (!s.isVisible()) {
                toRemove.add(s);
                continue;
            }
            if (s.collidesWith(player)) {
                // Handle player damage
                toRemove.add(s);
            }
        }
        enemyShots.removeAll(toRemove);
    }

    private void updatePowerUps() {
        List<PowerUp> toRemove = new ArrayList<>();
        for (PowerUp p : powerUps) {
            p.act();

            if (p.collidesWith(player)) {
                if (p instanceof ExtraLife) {
                    lives++;
                } else {
                    p.upgrade(player); // Upgrade logic already in PowerUp subclasses
                }

                toRemove.add(p);
                powerUpOnScreen = false;

                try {
                    new AudioPlayer("src/audio/powerup.wav").play();
                } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
                    System.err.println("PowerUp sound failed");
                }
            } else if (p.getX() < -50) { // Off-screen
                toRemove.add(p);
                powerUpOnScreen = false;
            }
        }
        powerUps.removeAll(toRemove);
    }


    private void spawnRandomPowerUps() {
        if (!powerUpOnScreen && frame % 300 == 0 && random.nextDouble() < 0.5) {
            int x = BOARD_WIDTH + 50;
            int y = 50 + random.nextInt(BOARD_HEIGHT - 100);

            PowerUp newPowerUp = null;
            int type = random.nextInt(4); 

            try {
                switch (type) {
                    case 0:
                        newPowerUp = new ExtraLife(x, y);
                        break;
                    case 1:
                        newPowerUp = new SpeedUp(x, y);
                        break;
                    case 2:
                        newPowerUp = new MultiShot(x, y);
                        break;
                    case 3:
                        newPowerUp = new ThreeWayShot(x, y);
                        break;
                }
                if (newPowerUp != null) {
                    powerUps.add(newPowerUp);
                    powerUpOnScreen = true;
                }
            } catch (IOException e) {
                System.err.println("Failed to spawn power-up: " + e.getMessage());
            }
        }
}


    private void checkWinCondition() {
        if (laserEnemiesKilled >= LASER_ENEMY_COUNT) {
            inGame = false;
            timer.stop();
            try {
                audioPlayer.stop();
                new AudioPlayer("src/audio/gamewon.wav").play();
            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
                System.out.println("Win sound error: " + e);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawMap(g);
        if (inGame) {
            drawGame(g);
            drawDashboard(g);
        } else {
            drawWinScreen(g);
        }
    }

    private void drawMap(Graphics g) {
        int scrollOffset = frame % BLOCK_WIDTH;
        int baseColumn = (frame / BLOCK_WIDTH) % MAP[0].length; // columns: 24
        int columnsNeeded = (BOARD_WIDTH / BLOCK_WIDTH) + 2;

        for (int screenCol = 0; screenCol < columnsNeeded; screenCol++) {
            int mapCol = (baseColumn + screenCol) % MAP[0].length; // MAP[0].length = 24
            int x = BOARD_WIDTH - ((screenCol * BLOCK_WIDTH) - scrollOffset);

            if (x < -BLOCK_WIDTH || x > BOARD_WIDTH) continue;

            for (int row = 0; row < MAP.length; row++) { // rows: 12
                if (MAP[row][mapCol] == 1) {
                    int y = row * BLOCK_HEIGHT;
                    drawStarCluster(g, x, y, BLOCK_WIDTH, BLOCK_HEIGHT);
                }
            }
        }
    }


    private void drawStarCluster(Graphics g, int x, int y, int width, int height) {
        g.setColor(Color.WHITE);
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        g.fillOval(centerX - 2, centerY - 2, 4, 4); // Main star
        g.fillOval(centerX - 10, centerY + 5, 2, 2);
        g.fillOval(centerX + 8, centerY - 8, 2, 2);
        g.fillOval(centerX - 5, centerY - 12, 1, 1);
        g.fillOval(centerX + 10, centerY + 12, 1, 1);
    }


    private void drawGame(Graphics g) {
        g.drawImage(player.getImage(), player.getX(), player.getY(), this);

        for (LaserEnemy e : enemies) {
            e.drawLaser((Graphics2D) g);
            e.drawChargingEffect((Graphics2D) g);
            e.drawHealthBar((Graphics2D) g);
            g.drawImage(e.getImage(), e.getX(), e.getY(), this);
        }

        for (Shot s : shots) {
            g.drawImage(s.getImage(), s.getX(), s.getY(), this);
        }

        for (EnemyShot s : enemyShots) {
            g.drawImage(s.getImage(), s.getX(), s.getY(), this);
        }

        for (PowerUp p : powerUps) {
            g.drawImage(p.getImage(), p.getX(), p.getY(), this);
        }

        for (Explosion ex : explosions) {
            if (ex.isVisible()) {
                g.drawImage(ex.getImage(), ex.getX(), ex.getY(), this);
                ex.act();
            }
        }
    }

    private void drawDashboard(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawString("LEVEL: 4", 10, 15);
        g.drawString("SCORE: " + deaths, 10, 35);
        g.drawString("SPEED: " + player.getSpeed(), 10, 55);
        g.drawString("LIVES: " + Math.max(0, lives), 10, 75);

        if (player.isMultiShotEnabled()) {
            g.setColor(Color.YELLOW);
            g.drawString("MULTI-SHOT ACTIVE!", 10, 95);
        }
        if (player.isThreeWayShotEnabled()) {
            g.setColor(Color.CYAN);
            g.drawString("THREE-WAY ACTIVE!", 10, 115);
        }

        g.setColor(Color.WHITE);

        // Level progress bar is always full in level 4
        g.setColor(Color.DARK_GRAY);
        g.fillRect(LEVEL_BAR_X, LEVEL_BAR_Y, LEVEL_BAR_WIDTH, LEVEL_BAR_HEIGHT);
        g.setColor(Color.GREEN);
        g.fillRect(LEVEL_BAR_X, LEVEL_BAR_Y, LEVEL_BAR_WIDTH, LEVEL_BAR_HEIGHT);
    }


    private void drawWinScreen(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        // Background rectangle box
        g.setColor(new Color(0, 48, 32));
        g.fillRect(50, BOARD_HEIGHT / 2 - 30, BOARD_WIDTH - 100, 50);

        // Border
        g.setColor(Color.WHITE);
        g.drawRect(50, BOARD_HEIGHT / 2 - 30, BOARD_WIDTH - 100, 50);

        // Text
        Font messageFont = new Font("Helvetica", Font.BOLD, 16);
        g.setFont(messageFont);
        String winText = "Congratulations! You Won! Press ESC to Exit";
        int textWidth = g.getFontMetrics(messageFont).stringWidth(winText);
        g.drawString(winText, (BOARD_WIDTH - textWidth) / 2, BOARD_HEIGHT / 2);
    }


    private class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            player.keyPressed(e);

            if (e.getKeyCode() == KeyEvent.VK_SPACE && inGame) {
                shots.addAll(player.createShots());
                try {
                    new AudioPlayer("src/audio/shot.wav").play();
                } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
                    System.err.println("Shot sound failed");
                }
            } else if (!inGame && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            player.keyReleased(e);
        }
    }

    public void addExplosion(Explosion e) {
        explosions.add(e);
    }

    public void addEnemyShots(ArrayList<EnemyShot> shots) {
        enemyShots.addAll(shots);
    }

    public void enemyKilled(LaserEnemy e) {
        enemies.remove(e);
        laserEnemiesKilled++;
    }
}