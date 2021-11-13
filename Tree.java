import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class Tree {
	static Random RNG = new Random();


    public static void main(String[] args) throws IOException {
		int dimension_x = 2000;
		int dimension_y = dimension_x;
        BufferedImage image = new BufferedImage(dimension_x, dimension_y, BufferedImage.TYPE_INT_ARGB);
		
		BufferedImage[] sources = {
			//ImageIO.read(new File("cherryTreeColorSample.jpg")),
			ImageIO.read(new File("cheneColorSample.jpg")),
			ImageIO.read(new File("sapinColorSample.jpg")),
			ImageIO.read(new File("autumnColorSample1.jpg")),
			ImageIO.read(new File("autumnColorSample2.jpg"))
		};
		
		
		int skyHeight = RNG.nextInt(dimension_y/2);
		int groundHeight = dimension_y - skyHeight;
		paintRectangle(image, 0xFF4B9EF4, 0, dimension_x, 0, skyHeight); // paints the sky
		paintRectangle(image, 0xFF1E391C, 0, dimension_x, skyHeight, dimension_y); // paints the ground

		int n = groundHeight / 50;
		for(int i = 0; i < n; ++i) {
			for(int j = 0; j < 4*n - 4*i; ++j){
				Vector position = new Vector().set(RNG.nextInt(dimension_x), groundHeight * i / n + RNG.nextInt(groundHeight / n) + skyHeight);		
				TreeDrawer drawer = new TreeDrawer(image, sources[RNG.nextInt(sources.length)], position, dimension_x / 60 + i*dimension_x / (n * 40));		
				drawer.drawTree();
			}
		}
       
		
        ImageIO.write(image, "png", new File("result.png"));
    }

    public static void paintRectangle(BufferedImage image, int color, int x1, int x2, int y1, int y2){
		//x2, y2 excluded
        for(int x = x1; x < x2; ++x) {
            for(int y = y1; y < y2; ++y) {
                image.setRGB(x, y, color);
            }
        }
    }
}

class TreeDrawer {
	static Random RNG = new Random();

    /*static Vector[] directions = {
            new Vector().set(1, 0),
            new Vector().set(-1, 0),
            new Vector().set(-1,-1),
            new Vector().set(0, -1),
            new Vector().set(1, -1),
    };*/
	
	static Vector[] directions = {
            new Vector().set(1, 0),
			new Vector().set(1, 1),
			new Vector().set(0, 1),
			new Vector().set(-1, 1),
            new Vector().set(-1, 0),
            new Vector().set(-1,-1),
            new Vector().set(0, -1),
            new Vector().set(1, -1),
    };
	
	BufferedImage colorSource;
	BufferedImage hostImage;

    int MIN_BRANCH_LENGTH; //commented if must depend on tree size
    int MIN_BRANCH_LENGTH_2; //
    static double BRANCH_PROBABILITY = 0.004;

    int LEAF_SIZE; // 180 for 500 first branch
    static int LEAF_DEPTH = 2;
	
	static int a = 15;
	static int b = -3*a;
	
	static int BRANCH_COLOR = 0xFFb4b09a;
	
	int initLength;
	Vector initPosition;
	
	//---------------CONSTRUCTORS--------------------------
	public TreeDrawer(BufferedImage image, BufferedImage colorSource, Vector position, int length) {
		this.colorSource = colorSource;
		this.hostImage = image;
		initLength = length;
		
		LEAF_SIZE = 2 *length / 5;
		MIN_BRANCH_LENGTH = length / 40;
		MIN_BRANCH_LENGTH_2 = length / 20;
		
		this.initPosition = position;
		//drawTree();
	}
	
	//---------------METHODS-------------------------------
	
	public void drawTree() {
		//initially, 1st branch towards up, and depth is zero
		branch(initPosition, 6, initLength, 0);
    }
	
    void branch(Vector pos,
                int dirIndex,
                int length,
                int depth) {

        if (length < MIN_BRANCH_LENGTH || length == 0) {
			leaf(pos);
			return;
		}			

        Vector position = new Vector().set(pos.x, pos.y);
        int color = BRANCH_COLOR;

        for(int i = 0; i < length; ++i){
            if(position.x < 0 || position.x >= hostImage.getWidth() ||
                    position.y < 0 || position.y >= hostImage.getHeight()) return;
			//if K is the range k below takes
			//for the moment, K(L, i) = L/10   [doesn't depend on i]
			//K(i) at the end for L must be equal to K(i) at the beginning for 2*L / 3
			// K(L, i) = a * L + b * i (hypothesis that K is linear in L and i)
			// K(L, L) = a * L + b * L = a * 2L/3 = K(2L/3, 0)
			// => a * L / 3 + b * L = 0 => b = -a/3  ;  a is the width factor of a branch at it's beginning in function of it's length, set to 1/10
			// so b = -1/30  => k ranges from -L/20 + i/60 to L/20 - i/60
            for(int k = -length/a - i/b; k < length/a + i/b; ++k){
				if(position.x + k < 0 || position.x + k >= hostImage.getWidth()) break;
                hostImage.setRGB(position.x + k, position.y, color);
            }
            position.add(directions[dirIndex]);
            if(RNG.nextFloat() < BRANCH_PROBABILITY)
                branch(position, addDir(dirIndex, RNG.nextInt(3) - 1), 2 * length / 3, depth + 1);
        }
		
		//if(depth > LEAF_DEPTH) leaf(image, position);

        if (length < MIN_BRANCH_LENGTH_2) {
			leaf(position);
			return;
		}

        branch(position, addDir(dirIndex, 1), 2 * length / 3, depth + 1);
        branch(position, addDir(dirIndex, -1), 2 * length / 3, depth + 1);
        branch(position, dirIndex, length / 2, depth + 1);
    }

    void leaf(Vector pos) {
        int color = randomColor();
        for(int x = -LEAF_SIZE; x < LEAF_SIZE; ++x) {
            for(int y = (int)-Math.sqrt(LEAF_SIZE - x*x); y < (int)Math.sqrt(LEAF_SIZE - x*x); ++y) {
                int x1 = pos.x + x;
                int y1 = pos.y + y;
                if(x1 < 0 || x1 >= hostImage.getWidth() ||
                        y1 < 0 || y1 >= hostImage.getHeight()) return; //to put in an auxialiary method
                hostImage.setRGB(x1, y1, color);
            }
        }
    }
	
	int randomColor() {
        int x = RNG.nextInt(colorSource.getWidth());
        int y = RNG.nextInt(colorSource.getHeight());
        return colorSource.getRGB(x, y);
    }
	
    static int addDir(int a, int b) {
        return Math.floorMod((a + b), directions.length);
    }
}

class Vector {
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
