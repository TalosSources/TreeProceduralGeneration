package ImageTransformations;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class Tree {
    static Random RNG = new Random();

    static Vector[] directions = {
            new Vector().set(1, 0),
            new Vector().set(-1, 0),
            new Vector().set(-1,-1),
            new Vector().set(0, -1),
            new Vector().set(1, -1),
    };

    static BufferedImage colorSource = null;

    static int MIN_BRANCH_LENGTH = 4;
    static int MIN_BRANCH_LENGTH_2 = 12;
    static double BRANCH_PROBABILITY = 0.01;

    static int LEAF_SIZE = 60;
    static int LEAF_DEPTH = 4;

    public static void main(String[] args) throws IOException {
        BufferedImage image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
        colorSource = ImageIO.read(new File("cherryTreeColorSample.jpg"));
        paintEntirely(image, 0xFFBFF7FF);

        drawTree(image);
        ImageIO.write(image, "png", new File("result.png"));
    }

    public static void drawTree(BufferedImage image) {
        Vector position = new Vector().set(image.getWidth() / 2, image.getHeight() - 1);

        branch(image, position, 3,250, 0);

    }

    public static void paintEntirely(BufferedImage image, int color){
        for(int x = 0; x < image.getWidth(); ++x) {
            for(int y = 0; y < image.getHeight(); ++y) {
                image.setRGB(x, y, color);
            }
        }
    }

    public static void branch(BufferedImage image,
                              Vector pos,
                              int dirIndex,
                              int length,
                              int depth) {

        if (length < MIN_BRANCH_LENGTH) return;

        if(depth > LEAF_DEPTH) leaf(image, pos);

        Vector position = new Vector().set(pos.x, pos.y);
        int color = 0xFFC9C8C8;

        for(int i = 0; i < length; ++i){
            if(position.x < 0 || position.x >= image.getWidth() ||
                    position.y < 0 || position.y >= image.getHeight()) return;
            for(int k = 0; k < length / 10; ++k){
                image.setRGB(position.x + k, position.y, color);
            }
            position.add(directions[dirIndex]);
            if(RNG.nextFloat() < BRANCH_PROBABILITY)
                branch(image, position, addDir(dirIndex, RNG.nextInt(3) - 1), 2 * length / 3, depth + 1);
        }

        if (length < MIN_BRANCH_LENGTH_2) return;

        branch(image, position, addDir(dirIndex, 1), 2 * length / 3, depth + 1);
        branch(image, position, addDir(dirIndex, -1), 2 * length / 3, depth + 1);
        branch(image, position, dirIndex, length / 2, depth + 1);
    }

    public static void leaf(BufferedImage image, Vector pos) {
        int color = randomColor();
        for(int x = -LEAF_SIZE; x < LEAF_SIZE; ++x) {
            for(int y = (int)-Math.sqrt(LEAF_SIZE - x*x); y < (int)Math.sqrt(LEAF_SIZE - x*x); ++y) {
                int x1 = pos.x + x - LEAF_SIZE / 2;
                int y1 = pos.y + y - LEAF_SIZE / 2;
                if(x1 < 0 || x1 >= image.getWidth() ||
                        y1 < 0 || y1 >= image.getHeight()) return;
                image.setRGB(x1, y1, color);
            }
        }
    }

    static int addDir(int a, int b) {
        return Math.floorMod((a + b), directions.length);
    }

    static int randomColor() {
        int x = RNG.nextInt(colorSource.getWidth());
        int y = RNG.nextInt(colorSource.getHeight());
        return colorSource.getRGB(x, y);
    }

    static class Vector {
        int x;
        int y;

        Vector set(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        Vector add(Vector other){
            this.x += other.x;
            this.y += other.y;
            return this;
        }
    }
}
