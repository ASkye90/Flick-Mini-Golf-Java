import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
/*
 * Map provides storage and drawing for the current map.
 * 
 * Handles the collision detection for the game. 
 */
public class Map {
	public static int WIDTH = 960;
	public static int HEIGHT = 640;
	public static int TILESIZE = 32;
	
	private static int TILESET_WIDTH = 132;
	private static String TILESET_PATH = "Assets/tileset.png";
	private static String MAPPING_PATH = "Assets/mapping.csv";
	
	private int[] level;
	private ArrayList<Line2D>[] lines;
	private BufferedImage tileset;
	
	//Starting position for ball.
	private Point start;
	
	/*
	 * Constructor for Map object.
	 * Loads in tileset image and initiates empty arrays in preparation for loading a level.
	 */
	public Map() {
		int aWidth = WIDTH/TILESIZE;
		int aHeight = HEIGHT/TILESIZE;
		int arraySize = aWidth*aHeight;
		level = new int[arraySize];
		lines = new ArrayList[arraySize];

		//Force mid-way left hand side starting position.
		start = new Point(aWidth/2,aHeight/2);

		try {
			tileset = ImageIO.read(this.getClass().getResourceAsStream(TILESET_PATH));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
	}
	
	/*
	 * Loads a level into the Map from Asset folder based on level name.
	 * 
	 * @param	levelName	Name of level to load (CSV file)
	 */
	public void loadLevel(String levelName) {				
		HashMap<Integer,double[]> mapping = new HashMap<Integer,double[]>();
		BufferedReader CSVFile;
		
		//Load the mapping file used to associate set(s) of lines for each tile.
		try {
			CSVFile = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(MAPPING_PATH)));
			String dataRow = CSVFile.readLine();
			while (dataRow != null) {
				if (!dataRow.isEmpty()) {
					String[] data = dataRow.split(",");
					int pos = Integer.parseInt(data[0]);
					double[] item = new double[data.length-1];
					for (int i=1; i<data.length; i++) {
						item[i-1] = Integer.parseInt(data[i]);
					}
					mapping.put(pos,item);
				}
				dataRow = CSVFile.readLine();
			}
		} catch (Exception e) {
			System.out.println("Failed to load mapping");
			e.printStackTrace();
		}
		
		//Load the level into a singular array for drawing
		//Create a set of lines for each tile in the level based on the mapping. 
		try {
			CSVFile = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(levelName)));
			String[] mapData = CSVFile.readLine().split(",");
			int x, y, tile;
			int lvlCols = WIDTH / TILESIZE;
			double[] mappingItem;
			ArrayList<Line2D> newLines;
			for (int i=0; i<mapData.length; i++) {
				tile = Integer.parseInt(mapData[i]);
				mappingItem = mapping.get(tile);
				x = (i % lvlCols)*TILESIZE;
				y = (int) (Math.floor(i / lvlCols))*TILESIZE;
				newLines = new ArrayList<Line2D>();
				for (int j=0; j<mappingItem.length; j+=4) {
					newLines.add(new Line2D.Double(x+mappingItem[j],y+mappingItem[j+1],x+mappingItem[j+2],y+mappingItem[j+3]));
				}
				lines[i] = newLines;
				level[i] = tile;
			}
		} catch (Exception e) {
			System.out.println("Failed to load level.");
			e.printStackTrace();
		}
	}
	
	/*
	 * Draw the map.
	 */
	public void draw(Graphics g) {
		int drawX, drawY, tileX, tileY;
		int tileCols = TILESET_WIDTH / (TILESIZE+1);
		int lvlCols = WIDTH / TILESIZE;
		for (int i=0; i<level.length; i++) {
			drawX = (i % lvlCols)*TILESIZE;
			drawY = (int) (Math.floor(i / lvlCols))*TILESIZE;
			tileX = (level[i] % tileCols) * (TILESIZE+1) + 1;
			tileY = (int) (Math.floor(level[i] / tileCols)) * (TILESIZE+1) + 1;
			g.drawImage(tileset.getSubimage(tileX, tileY, TILESIZE, TILESIZE), drawX, drawY, null);
		}
	}
	
	/* 
	 * @return		Starting tile for Ball.
	 */
	public Point getStart() {
		return start;
	}
	
	/*
	 * Checks balls for intersections with map elements after delta time step.
	 * 
	 * @param	ball	Ball to check.
	 * @param	delta	what fraction of a fixed time step to calculate.
	 * 
	 * @return	All lines that will intersect with ball.
	 * 
	 * ASSUMPTIONS: delta <= 1.0, ball smaller than tile size, ball cannot completely pass a tile in one delta time step.
	 */
	public ArrayList<Line2D> getPossibleLines(Ball ball, double delta) {
		ArrayList<Line2D> checkLines = new ArrayList<Line2D>();
		
		/* BROAD SEARCH 
		 * Checking all tiles within map around ball.
		 */
		
		Ellipse2D c = ball.getCircle();
		Point2D vel = ball.getVelocity();
		Point2D newCenter = new Point2D.Double((c.getCenterX() + vel.getX() * delta),(c.getCenterY() + vel.getY() * delta));
		int r = ball.getRadius();
		
		int centerTileX = (int)(newCenter.getX()/TILESIZE);
		int centerTileY = (int)(newCenter.getY()/TILESIZE);
		
		int gridWidth = WIDTH/TILESIZE;
		int gridHeight = HEIGHT/TILESIZE;
		int tilePos, tileType, tileX, tileY;
		for (int i=-15; i<=15; i++) {
			for (int j=-15; j<=15; j++) {
				tileX = centerTileX + i;
				tileY = centerTileY + j;
				
				if(tileX >= 0 && tileY >= 0 && tileX < gridWidth && tileY < gridHeight) {
					tilePos = tileX + tileY * gridWidth;
					tileType = level[tilePos];
					checkLines.addAll(lines[tilePos]);
				}
			}
		}
		return checkLines;
	}
}
