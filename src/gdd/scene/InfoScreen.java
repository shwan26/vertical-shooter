package gdd.scene;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class InfoScreen extends JPanel {

    private final HashMap<String, BufferedImage> images = new HashMap<>();
    private final Runnable onEnterPressed;

    public InfoScreen(Runnable onEnterPressed) {
        this.onEnterPressed = onEnterPressed;
        setBackground(Color.BLACK);
        setFocusable(true);
        loadImages();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onEnterPressed.run();
                }
            }
        });
    }

    private void loadImages() {
        load("Alien", "src/images/enemyt/enemy1_1.png");
        load("Bomb", "src/images/bomb/bomb3.png");
        load("MissileEnemy", "src/images/enemyt/enemy2_1.png");
        load("QuadShotEnemy", "src/images/enemyt/enemy3_1.png");
        load("LaserEnemy", "src/images/enemyt/enemy6_1.png");
        load("SpeedUp", "src/images/powerup-s.png");
        load("MultiShot", "src/images/powerup-multishot.png");
        load("ThreeWayShot", "src/images/powerup-threeway.png");
    }

    private void load(String key, String path) {
        try {
            images.put(key, ImageIO.read(new File(path)));
        } catch (IOException e) {
            System.err.println("Failed to load image for " + key + ": " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawInfoScreen(g);
    }

    private void drawInfoScreen(Graphics g) {
        g.setColor(Color.ORANGE);
        g.setFont(new Font("VCR OSD Mono", Font.BOLD, 30));
        g.drawString("Enemy & Power-Up Guide", 150, 50);

        g.setFont(new Font("Arial", Font.PLAIN, 14));
        int y = 100;
        drawEntry(g, "", "Press SPACE to shoot", 80, y); 
        y += 50;

        drawEntry(g, "Alien", "Alien - Drops bombs randomly.", 80, y); 
        y += 50;
        drawEntry(g, "Bomb", "Bomb - Move towards player (cannot be destroyed)", 80, y); 
        y += 50;
        drawEntry(g, "MissileEnemy", "MissileEnemy - Speeds up and tracks you.", 80, y); 
        y += 50;
        drawEntry(g, "QuadShotEnemy", "QuadShotEnemy - Change position randomly and fires 4 bullets", 80, y); 
        y += 50;
        drawEntry(g, "LaserEnemy", "LaserEnemy - Charges a deadly laser beam.", 80, y); 
        y += 80;

        drawEntry(g, "SpeedUp", "SpeedUp - Increases movement speed.", 80, y); 
        y += 50;
        drawEntry(g, "MultiShot", "MultiShot - Fire multiple shots at once.", 80, y); 
        y += 50;
        drawEntry(g, "ThreeWayShot", "ThreeWayShot - Fire in 3 directions.", 80, y);

        g.setFont(new Font("Arial", Font.ITALIC, 16));
        g.setColor(Color.LIGHT_GRAY);
        g.drawString("Press ENTER to begin...", 280, 600);
    }

    private void drawEntry(Graphics g, String key, String text, int x, int y) {
        BufferedImage img = images.get(key);
        if (img != null) {
            g.drawImage(img, x, y - 20, 40, 40, this);
        }
        g.setColor(Color.WHITE);
        g.drawString(text, x + 60, y);
    }
}
