package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.*;
import gdd.SpawnDetails;
import gdd.powerup.MultiShot;
import gdd.powerup.PowerUp;
import gdd.powerup.SpeedUp;
import gdd.powerup.ThreeWayShot;
import gdd.sprite.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Scene1 extends JPanel {

    private int frame = 0;     
    private int lives = 5;
                 
    private int deaths = 0;                    
    private boolean inGame = true;              
    private String message = "Game Over!";      

    private List<PowerUp> powerups;             
    private List<Enemy> enemies;              
    private List<Explosion> explosions;      
    private List<Shot> shots;                
    private List<EnemyShot> enemyShots;      
    private List<Alien1.Bomb> bombs;           
    private Player player;            
    private Game game;  
    private int lastLevel = 0;    
    private boolean initialPowerupSpawned = false;
    private int MAX_LASER_ENEMIES = 1;
    private int laserEnemySpawned = 0;

    private final int BLOCKHEIGHT = 50;       
    private final int BLOCKWIDTH = 50;          
    private final Dimension d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
    private final Random randomizer = new Random();

    private Timer timer;                     
    private AudioPlayer audioPlayer;
    private AudioPlayer shotAudio;
    private AudioPlayer explosionAudio;
    private boolean gameOverSoundPlayed = false;

    private HashMap<Integer, List<SpawnDetails>> scoreSpawnMap = new HashMap<>();
    private Set<Integer> spawnedScores = new HashSet<>();

    private boolean showPlayAgain = false;
    private Rectangle playAgainButton = new Rectangle(
            BOARD_WIDTH/2 - 100,
            BOARD_HEIGHT/2 + 30,
            200,
            40
    );

    // background scrolling
    private int mapOffset = 0;
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

    private HashMap<Integer, SpawnDetails> spawnMap = new HashMap<>(); // Enemy spawn schedule


    public Scene1(Game game) {
        this.game = game;
        spawnedScores = new HashSet<>();
        loadSpawnDetails();
        addMouseListener(new MouseHandler());
    }

    private void initAudio() {
        try {
            // Background music
            audioPlayer = new AudioPlayer("src/audio/scene1.wav");
            audioPlayer.setLoop(true);
            audioPlayer.play();
        } catch (Exception e) {
            System.err.println("Error initializing audio player: " + e.getMessage());
        }
    }

    private void loadSpawnDetails() {
        scoreSpawnMap.put(0, List.of(new SpawnDetails("PowerUp-SpeedUp", BOARD_WIDTH, 150)));

        scoreSpawnMap.put(10, List.of(
                new SpawnDetails("PowerUp-MultiShot", BOARD_WIDTH, 180)
        ));

        scoreSpawnMap.put(20, List.of(
                new SpawnDetails("PowerUp-ThreeWayShot", BOARD_WIDTH, 200),
                new SpawnDetails("PowerUp-ThreeWayShot", BOARD_WIDTH, 300)
        ));

        scoreSpawnMap.put(30, List.of(
                new SpawnDetails("PowerUp-MultiShot", BOARD_WIDTH, 200),
                new SpawnDetails("PowerUp-ThreeWayShot", BOARD_WIDTH, 500)
        ));

        scoreSpawnMap.put(50, List.of(
                new SpawnDetails("PowerUp-ThreeWayShot", BOARD_WIDTH/2, 180)
        ));
    }


   
    public void start() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        requestFocusInWindow();
        setBackground(Color.black);

        timer = new Timer(1000 / 60, new GameCycle()); 
        timer.start();

        gameInit();
        initAudio();
        lastLevel = 1;
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }
        try {
            if (audioPlayer != null) {
                audioPlayer.stop();
            }
        } catch (Exception e) {
            System.err.println("Error closing audio player.");
        }
    }

    private void gameInit() {
        enemies = new ArrayList<>();
        powerups = new ArrayList<>();
        explosions = new ArrayList<>();
        shots = new ArrayList<>();
        enemyShots = new ArrayList<>();
        bombs = new ArrayList<>();

        player = new Player(); // Create player 
    }

    private void drawMap(Graphics g) {
        int scrollOffset = (frame) % BLOCKWIDTH;
        int baseColumn = (frame) / BLOCKWIDTH;
        int columnsNeeded = (BOARD_WIDTH / BLOCKWIDTH) + 2;

        for (int screenCol = 0; screenCol < columnsNeeded; screenCol++) {
            int mapCol = (baseColumn + screenCol) % MAP.length;
            int x = BOARD_WIDTH - ((screenCol * BLOCKWIDTH) - scrollOffset);

            if (x < -BLOCKWIDTH || x > BOARD_WIDTH) continue;

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

        // Main star (larger)
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        g.fillOval(centerX - 2, centerY - 2, 4, 4);

        // Smaller surrounding stars
        g.fillOval(centerX - 15, centerY - 10, 2, 2);
        g.fillOval(centerX + 12, centerY - 8, 2, 2);
        g.fillOval(centerX - 8, centerY + 12, 2, 2);
        g.fillOval(centerX + 10, centerY + 15, 2, 2);

        // Tiny stars for more detail
        g.fillOval(centerX - 20, centerY + 5, 1, 1);
        g.fillOval(centerX + 18, centerY - 15, 1, 1);
        g.fillOval(centerX - 5, centerY - 18, 1, 1);
        g.fillOval(centerX + 8, centerY + 20, 1, 1);
    }

    private void drawAliens(Graphics g) {
        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                if (enemy.getExhaust().isActive()) {
                    g.drawImage(
                            enemy.getExhaust().getCurrentFrame(),
                            enemy.getExhaust().getX(enemy),
                            enemy.getExhaust().getY(enemy),
                            this
                    );
                }
                g.drawImage(enemy.getImage(), enemy.getX(), enemy.getY(), this);

                // Special enemy effects
                if (enemy instanceof MissileEnemy) {
                    ((MissileEnemy) enemy).drawWarningEffect(g);
                } else if (enemy instanceof LaserEnemy) {
                    Graphics2D g2d = (Graphics2D) g;
                    LaserEnemy laserEnemy = (LaserEnemy) enemy;
                    laserEnemy.drawLaser(g2d);
                    laserEnemy.drawChargingEffect(g2d);
                    laserEnemy.drawHealthBar(g2d);
                }
            }

            if (enemy.isDying()) {
                enemy.die();
            }
        }
    }

    private void drawPowerUps(Graphics g) {
        for (PowerUp p : powerups) {
            if (p.isVisible()) {
                g.drawImage(p.getImage(), p.getX(), p.getY(), this);
                
            }

            if (p.isDying()) {
                p.die();
            }
        }
    }

    private void drawPlayer(Graphics g) {
        if (player.isVisible()) {
            if (player.getExhaust().isActive()) {
                g.drawImage(
                        player.getExhaust().getCurrentFrame(),
                        player.getExhaust().getX(player),
                        player.getExhaust().getY(player),
                        this
                );
            }

            g.drawImage(player.getImage(), player.getX(), player.getY(), this);
        }

        if (player.isDying()) {
            player.die();
            // Check if all explosion animations are finished
            boolean explosionOngoing = explosions.stream().anyMatch(e -> e.isVisible());
            if (!explosionOngoing) {
                inGame = false;
            }
        }
    }

    private void drawShot(Graphics g) {
        for (Shot shot : shots) {
            if (shot.isVisible()) {
                g.drawImage(shot.getImage(), shot.getX(), shot.getY(), this);
            }
        }
    }

    private void drawExplosions(Graphics g) {
        List<Explosion> toRemove = new ArrayList<>();

        for (Explosion explosion : explosions) {
            if (explosion.isVisible()) {
                g.drawImage(explosion.getImage(), explosion.getX(), explosion.getY(), this);
                explosion.act();
                if (!explosion.isVisible()) {
                    toRemove.add(explosion);
                }
            }
        }

        explosions.removeAll(toRemove);
    }

    private void drawEnemyShots(Graphics g) {
        for (EnemyShot shot : enemyShots) {
            if (shot.isVisible()) {
                g.drawImage(shot.getImage(), shot.getX(), shot.getY(), this);
            }
        }
    }

    private void drawBombs(Graphics g) {
        for (Alien1.Bomb bomb : bombs) {
            if (bomb.isVisible()) {
                g.drawImage(bomb.getImage(), bomb.getX(), bomb.getY(), this);
            }
        }
    }

    private void drawDashboard(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawString("LEVEL: " + getCurrentLevel(), 10, 15);
        g.drawString("SCORE: " + deaths, 10, 35);
        g.drawString("SPEED: " + player.getSpeed(), 10, 55);
        g.drawString("LIVES: " + lives, 10, 75);


        // Highlight power-ups
        if (player.isMultiShotEnabled()) {
            g.setColor(Color.YELLOW);
            g.drawString("MULTI-SHOT ACTIVE!", 10, 95);
        }
        if (player.isThreeWayShotEnabled()) {
            g.setColor(Color.CYAN);
            g.drawString("THREE-WAY ACTIVE!", 10, 115);
        }
        g.setColor(Color.WHITE);

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }

    private void doDrawing(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, d.width, d.height);

        if (inGame) {
            drawMap(g);
            drawExplosions(g);
            drawPowerUps(g);
            drawAliens(g);
            drawPlayer(g);
            drawShot(g);
            drawEnemyShots(g);
            drawBombs(g);
            drawDashboard(g);
        } else {
            gameOver(g);
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private void gameOver(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        g.setColor(new Color(0, 32, 48));
        g.fillRect(50, BOARD_HEIGHT / 2 - 30, BOARD_WIDTH - 100, 50);
        g.setColor(Color.white);
        g.drawRect(50, BOARD_HEIGHT / 2 - 30, BOARD_WIDTH - 100, 50);

        Font small = new Font("Helvetica", Font.BOLD, 14);
        g.setFont(small);
        String displayMessage = message + " Your Score: " + deaths;
        g.drawString(displayMessage,
                (BOARD_WIDTH - getFontMetrics(small).stringWidth(displayMessage)) / 2,
                BOARD_HEIGHT / 2);

 
        showPlayAgain = true;
        g.setColor(new Color(70, 70, 200));
        g.fillRect(playAgainButton.x, playAgainButton.y, playAgainButton.width, playAgainButton.height);
        g.setColor(Color.white);
        g.drawRect(playAgainButton.x, playAgainButton.y, playAgainButton.width, playAgainButton.height);

        Font buttonFont = new Font("Helvetica", Font.BOLD, 16);
        g.setFont(buttonFont);
        String buttonText = "PLAY AGAIN";
        g.drawString(buttonText,
                playAgainButton.x + (playAgainButton.width - g.getFontMetrics().stringWidth(buttonText))/2,
                playAgainButton.y + 25);

        if (!gameOverSoundPlayed) {
            try {
                new AudioPlayer("src/audio/gameover.wav").play();
                audioPlayer.stop();
                gameOverSoundPlayed = true;
            } catch (Exception e) {
                System.out.println("Game over sound error: " + e);
            }
        }
    }

    private void update() {
        spawnEntities();         
        checkWinCondition();    
        updatePlayer();         
        updatePowerUps();       
        updateEnemies();        
        updateShots();          
        updateEnemyShots();     
        updateBombs();          

        if (frame % 150 == 0) {
            int randomY = 80 + randomizer.nextInt(BOARD_HEIGHT - 160);
            enemies.add(new Alien1(BOARD_WIDTH, randomY));

            if (getCurrentLevel() >= 2) {
                int missileY = 80 + randomizer.nextInt(BOARD_HEIGHT - 160);
                enemies.add(new MissileEnemy(BOARD_WIDTH, missileY));
            }
        }

        if (getCurrentLevel() >= 3 && laserEnemySpawned < MAX_LASER_ENEMIES) {
            enemies.add(new LaserEnemy(BOARD_WIDTH, 300, this));
            laserEnemySpawned++;
        }

        if (frame % 400 == 0 && getCurrentLevel() >= 2) {
            int quadY = 80 + randomizer.nextInt(BOARD_HEIGHT - 160);
            enemies.add(new QuadShotEnemy(BOARD_WIDTH, quadY));
        }

        int currentLevel = getCurrentLevel();
        
        if (currentLevel > lastLevel) {
            powerups.clear();
            int randomY = 100 + randomizer.nextInt(BOARD_HEIGHT - 200);
            powerups.add(new SpeedUp(BOARD_WIDTH, randomY));
            lastLevel = currentLevel;

        }

        if (!initialPowerupSpawned && currentLevel == 1) {
            powerups.add(new SpeedUp(BOARD_WIDTH, 150));
            initialPowerupSpawned = true;
            System.out.println("[DEBUG] Initial SpeedUp spawned");

        }

    }

    private int getCurrentLevel() {
        if (deaths < 25) return 1;         // Level 1: Normal enemies only
        else if (deaths < 50) return 2;    // Level 2: Add Missile enemies
        else return 3;                     // Level 3: Add Laser enemies
    }

    
    private void spawnEntities() {
    if (spawnedScores.contains(deaths)) return;

    List<SpawnDetails> spawns = scoreSpawnMap.get(deaths);
        

        if (spawns != null) {
            int level = getCurrentLevel();
            for (SpawnDetails sd : spawns) {
                switch (sd.type) {
                    case "Alien1":
                        enemies.add(new Alien1(sd.x, sd.y));
                        break;
                    case "MissileEnemy":
                        if (level >= 2) enemies.add(new MissileEnemy(sd.x, sd.y));
                        break;
                    case "QuadShotEnemy":
                        enemies.add(new QuadShotEnemy(sd.x, sd.y));
                        break;
                    case "LaserEnemy":
                        if (level >= 3) enemies.add(new LaserEnemy(sd.x, sd.y, this));
                        break;
                    case "PowerUp-SpeedUp":
                        powerups.add(new SpeedUp(sd.x, sd.y));
                        break;
                    case "PowerUp-MultiShot":
                        powerups.add(new MultiShot(sd.x, sd.y));
                        break;
                    case "PowerUp-ThreeWayShot":
                        powerups.add(new ThreeWayShot(sd.x, sd.y));
                        break;
                    default:
                        System.out.println("Unknown spawn type: " + sd.type);
                }
            }
            spawnedScores.add(deaths);
        }
    }



    private void checkWinCondition() {
        if (deaths == NUMBER_OF_ALIENS_TO_DESTROY) {
            inGame = false;
            timer.stop();
            message = "Game won!";
            try {
                new AudioPlayer("src/audio/gamewon.wav").play();
            } catch (Exception e) {
                System.out.println("Game won sound error: " + e);
            }
        }
    }


    private void updatePlayer() {
        player.act(); 
    }

    private void updatePowerUps() {
        List<PowerUp> powerupsToRemove = new ArrayList<>();
        for (PowerUp powerup : powerups) {
            if (powerup.isVisible()) {
                powerup.act(); 


                if (powerup.collidesWith(player)) {
                    try {
              
                        new AudioPlayer("src/audio/powerup.wav").play();
                    } catch (Exception ex) {
                        System.err.println("Power up sound failed: " + ex.getMessage());
                    }

                    powerup.upgrade(player);
                    powerupsToRemove.add(powerup);
                }


                if (powerup.getX() < -50) {
                    powerupsToRemove.add(powerup);
                }
            }
        }
        powerups.removeAll(powerupsToRemove);
    }

    private void updateEnemies() {
        List<Enemy> enemiesToRemove = new ArrayList<>();
        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                updateSpecialEnemies(enemy); 

                if (player.isVisible() && enemy.collidesWith(player)) {
                    player.setDying(true);
                    explosions.add(new Explosion(
                            player.getX() + player.getImage().getWidth()/2 - 32, 
                            player.getY() + player.getImage().getHeight()/2 - 32, 
                            true
                    ));               
                }

                // enemy off screen
                if (enemy.getX() < -50) {
                    enemiesToRemove.add(enemy);
                    lives--;

                    if (lives <= 0) {
                        handlePlayerDeath();
                    }
                }
            }
        }
        enemies.removeAll(enemiesToRemove);
    }

    private void updateSpecialEnemies(Enemy enemy) {
        if (enemy instanceof MissileEnemy) {
            MissileEnemy missile = (MissileEnemy) enemy;
            missile.update(player);
            missile.act(-1);      
        } else if (enemy instanceof LaserEnemy) {
            LaserEnemy laserEnemy = (LaserEnemy) enemy;
            if (laserEnemy.isLaserActive() && laserEnemy.laserCollidesWith(player)) {
                player.setDying(true);
                explosions.add(new Explosion(
                        player.getX() + player.getImage().getWidth()/2 - 32, 
                        player.getY() + player.getImage().getHeight()/2 - 32, 
                        true
                ));            }
            laserEnemy.act(-1);
        } else if (enemy instanceof Alien1) {

            Alien1 alien = (Alien1) enemy;
            if (randomizer.nextInt(360) == 1) { 
                handleAlienShooting(alien);
            }
            enemy.act(-1);
        } else if (enemy instanceof QuadShotEnemy) {
            QuadShotEnemy quadEnemy = (QuadShotEnemy) enemy;
            quadEnemy.act(-1);

           
            enemyShots.addAll(quadEnemy.getPendingShots());
        }
    }

    private void handleAlienShooting(Alien1 alien) {

        Alien1.Bomb bomb = alien.getBomb();
        if (bomb.isDestroyed()) {
            bomb = alien.new Bomb(alien.getX(), alien.getY() + 20);
            alien.setBomb(bomb);
            bombs.add(bomb);
        }

        enemyShots.add(new EnemyShot(alien.getX(), alien.getY() + 20, 1));
    }

    private void updateShots() {
        List<Shot> shotsToRemove = new ArrayList<>();

        for (Shot shot : shots) {
            if (shot.isVisible()) {
                // Check collision with enemies
                for (Enemy enemy : enemies) {
                    if (enemy.isVisible() && shot.collidesWith(enemy)) {
                        handleEnemyHit(enemy, shot);
                        shotsToRemove.add(shot);
                        break;
                    }
                }

                // Move shot and check if off-screen
                shot.act();
                if (shot.getX() > BOARD_WIDTH) {
                    shotsToRemove.add(shot);
                }
            }
        }
        shots.removeAll(shotsToRemove);
    }

    private void handleEnemyHit(Enemy enemy, Shot shot) {
        if (enemy instanceof LaserEnemy) {
            LaserEnemy laserEnemy = (LaserEnemy) enemy;
            laserEnemy.takeDamage((ArrayList<Explosion>) explosions);

            if (laserEnemy.isDying()) {
                // Give 10 points for killing a LaserEnemy
                deaths += 10;
                explosions.add(new Explosion(
                        enemy.getX() + enemy.getImage().getWidth()/2 - 24, 
                        enemy.getY() + enemy.getImage().getHeight()/2 - 24, 
                        false
                ));
            }
        } else {
            enemy.setDying(true);
            explosions.add(new Explosion(
                    enemy.getX() + enemy.getImage().getWidth()/2 - 24, 
                    enemy.getY() + enemy.getImage().getHeight()/2 - 24, 
                    false
            ));            deaths++;
        }
        shot.die();

        // Play explosion sound
        try {
            new AudioPlayer("src/audio/explosion.wav").play();
        } catch (Exception ex) {
            System.err.println("Explosion sound failed: " + ex.getMessage());
        }
    }

    private void updateEnemyShots() {
        List<EnemyShot> enemyShotsToRemove = new ArrayList<>();

        for (EnemyShot shot : enemyShots) {
            if (shot.isVisible()) {
                shot.act();

                // Check collision with player
                if (player.isVisible() && shot.collidesWith(player)) {
                    handlePlayerDeath();
                    enemyShotsToRemove.add(shot);
                }
            } else {
                enemyShotsToRemove.add(shot);
            }
            }
            enemyShots.removeAll(enemyShotsToRemove);
        }

    private void updateBombs() {
        List<Alien1.Bomb> bombsToRemove = new ArrayList<>();

        for (Alien1.Bomb bomb : bombs) {
            if (bomb.isVisible()) {
                bomb.act();

                // Check collision with player
                if (player.isVisible() && bomb.collidesWith(player)) {
                    handlePlayerDeath();

                    bombsToRemove.add(bomb);
                }

                // Remove off-screen bombs
                if (bomb.getX() < -50) {
                    bombsToRemove.add(bomb);
                }
            } else {
                bombsToRemove.add(bomb);
            }
        }
        bombs.removeAll(bombsToRemove);
    }

    public void enemyKilled(Enemy enemy) {

    }

    public void addEnemyShots(List<EnemyShot> shots) {
        enemyShots.addAll(shots);
    }


    private void doGameCycle() {
        frame++;
        update();
        repaint();
    }

    private class GameCycle implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            doGameCycle();
        }
    }

    private class TAdapter extends KeyAdapter {
        @Override
        public void keyReleased(KeyEvent e) {
            player.keyReleased(e);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            player.keyPressed(e);

            // Handle shooting
            if (e.getKeyCode() == KeyEvent.VK_SPACE && inGame) {
                int maxShots = 20;
                int shotsToCreate = player.isThreeWayShotEnabled() ? 3 : 1;

                if (shots.size() <= maxShots - shotsToCreate) {
                    shots.addAll(player.createShots());

                    try {
                        new AudioPlayer("src/audio/shot.wav").play();
                    } catch (Exception ex) {
                        System.err.println("Shot sound failed: " + ex.getMessage());
                    }
                }
            }
        }
    }

    private class MouseHandler extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (!inGame && showPlayAgain && playAgainButton.contains(e.getPoint())) {
                resetGame();
            }
        }
    }

    private void resetGame() 
    {
        frame = 0;
        lives = 5;
        deaths = 0;
        inGame = true;
        showPlayAgain = false;
        message = "Game Over!";
        spawnedScores.clear();

        enemies.clear();
        powerups.clear();
        explosions.clear();
        shots.clear();
        enemyShots.clear();
        bombs.clear();

        gameInit();

        if (!timer.isRunning()) {
            timer.start();
        }

        try {
            audioPlayer = new AudioPlayer("src/audio/scene1.wav");
            audioPlayer.setLoop(true);
            audioPlayer.play();
        } catch (Exception e) {
            System.err.println("Error restarting audio: " + e.getMessage());
        }

        gameOverSoundPlayed = false;
        laserEnemySpawned = 0;
    }

    private void handlePlayerDeath() {
    if (!player.isDying()) {
        player.setDying(true);
        lives--;

        explosions.add(new Explosion(
            player.getX() + player.getImage().getWidth()/2 - 32, 
            player.getY() + player.getImage().getHeight()/2 - 32, 
            true
        ));

        if (lives > 0) {
            player = new Player();
        }
    }
}

}
