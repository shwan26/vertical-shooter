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
    private AudioPlayer audioPlayer;

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
                // Handle damage if needed
            }
            if (le.isDying()) {
                toRemove.add(le);
            }
        }
        enemies.removeAll(toRemove);
        laserEnemiesKilled += toRemove.size();
    }

    private void updateShots() {
        List<Shot> toRemove = new ArrayList<>();
        for (Shot s : shots) {
            s.act();
            for (LaserEnemy e : enemies) {
                if (s.collidesWith(e)) {
                    e.takeDamage((ArrayList<Explosion>) explosions);
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
                    player.addLife(); // Or your actual method to add a life
                }
                toRemove.add(p);
                try {
                    new AudioPlayer("src/audio/powerup.wav").play();
                } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
                    System.err.println("PowerUp sound failed");
                }
            }

        }
        powerUps.removeAll(toRemove);
    }

    private void spawnRandomPowerUps() {
        if (frame % 300 == 0 && random.nextDouble() < 0.4) {
            int x = BOARD_WIDTH + 50;
            int y = 50 + random.nextInt(BOARD_HEIGHT - 100);
            try {
                powerUps.add(new ExtraLife(x, y));
            } catch (IOException e) {
                e.printStackTrace();
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
        if (inGame) {
            drawGame(g);
        } else {
            drawWinScreen(g);
        }
    }

    private void drawGame(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawString("FINAL STAGE", 10, 20);
        g.drawString("Laser Enemies Left: " + (LASER_ENEMY_COUNT - laserEnemiesKilled), 10, 40);
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

    private void drawWinScreen(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        g.setColor(Color.GREEN);
        g.drawString("YOU WON! Press ESC to Exit", BOARD_WIDTH / 2 - 80, BOARD_HEIGHT / 2);
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