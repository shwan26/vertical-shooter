package gdd;

import gdd.scene.InfoScreen;
import gdd.scene.Scene1;
import gdd.scene.Scene2;
import gdd.scene.TitleScene;
import gdd.scene.TutorialScene;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Game extends JFrame {

    private TitleScene titleScene;
    private Scene1 scene1;
    private Scene2 scene2; 
    private TutorialScene tutorialScene;

    public Game() {
        titleScene = new TitleScene(this);
        scene1 = new Scene1(this);
        initUI();
        loadTitle();
    }

    private void initUI() {
        setTitle("Space Invaders");
        setSize(Global.BOARD_WIDTH, Global.BOARD_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    public void loadTitle() {
        if (scene1 != null) scene1.stop();
        getContentPane().removeAll();
        add(titleScene);
        titleScene.start();
        revalidate();
        repaint();
    }

    public void loadScene1() {
        titleScene.stop(); 
        getContentPane().removeAll();

        InfoScreen infoScreen = new InfoScreen(() -> {
            tutorialScene = new TutorialScene(this);
            setScene(tutorialScene);
        });

        setScene(infoScreen);
    }

    public void setScene(JPanel panel) {
        getContentPane().removeAll();
        setContentPane(panel);
        revalidate();
        repaint();
        panel.requestFocusInWindow();

      
        if (panel instanceof Scene1 s1) {
            s1.start();
        } else if (panel instanceof TutorialScene ts) {
            ts.start();
        } else if (panel instanceof TitleScene ts) {
            ts.start();
        }
    }

    public void loadScene2() {
        scene1 = new Scene1(this); // fresh game instance
        setScene(scene1);
    }

    public void loadScene3() {
        scene2 = new Scene2(this); // fresh game instance
        setScene(scene2);
    }
}
