package gdd.util;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static gdd.Global.SCALE_FACTOR;

public class Util {

    public static BufferedImage[] loadAnimationFrames(String basePath, int count, int dScale, boolean flipHorizontal) {
        BufferedImage[] frames = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            try {
                BufferedImage raw = ImageIO.read(new File(basePath + (i + 1) + ".png"));
                if (flipHorizontal) {
                    raw = flipImageHorizontally(raw);
                }
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

    public static BufferedImage[] loadAnimationFrames(String basePath, int count, int dScale, int[] reduceSize, boolean flipHorizontal) {
        BufferedImage[] frames = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            try {
                BufferedImage raw = ImageIO.read(new File(basePath + (i + 1) + ".png"));
                if (flipHorizontal) {
                    raw = flipImageHorizontally(raw);
                }
                int scaledWidth = (raw.getWidth() * (SCALE_FACTOR - dScale)) - reduceSize[0];
                int scaledHeight = (raw.getHeight() * (SCALE_FACTOR - dScale)) - reduceSize[1];
                frames[i] = scaleImage(raw, scaledWidth, scaledHeight);
            } catch (IOException e) {
                e.printStackTrace();
                frames[i] = null;
            }
        }
        return frames;
    }

    public static BufferedImage flipImageHorizontally(BufferedImage original) {
        BufferedImage flipped = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                original.getType()
        );

        Graphics2D g = flipped.createGraphics();
        g.drawImage(original, original.getWidth(), 0, -original.getWidth(), original.getHeight(), null);
        g.dispose();

        return flipped;
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
