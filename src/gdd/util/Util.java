package gdd.util;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static gdd.Global.SCALE_FACTOR;

public class Util {

    public static BufferedImage[] loadAnimationFrames(String basePath, int count, int dScale) {
        BufferedImage[] frames = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            try {
                BufferedImage raw = ImageIO.read(new File(basePath + (i + 1) + ".png"));
                int scaledWidth = raw.getWidth() * (SCALE_FACTOR - dScale);
                int scaledHeight = raw.getHeight() * (SCALE_FACTOR - dScale);
                frames[i] = scaleImage(raw, scaledWidth, scaledHeight);
            } catch (IOException e) {
                e.printStackTrace();
                frames[i] = null;
            }
        }

        return frames;
    }

    public static BufferedImage scaleImage(BufferedImage src, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resized.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(src, 0, 0, width, height, null);
        g2.dispose();
        return resized;
    }

    public static Image loadImage(String path) {
    return new ImageIcon(path).getImage();
}

}
