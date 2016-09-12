import java.awt.*;
/*
 * TileMap provides storage and drawing for the current map.
 * 
 * Handles the collision detection for the game. 
 */
public class TileMap {
	public static int width = 960;
	public static int height = 640;
	public static int tileSize = 20;
	
	/* Two-dimensional map as single array stored from top-left to bottom-right.
	 * Current assigned values:
	 * 		0 = empty
	 * 		1 = filled
	 * 		2 = empty + starting position
	 */
	private int[] map;
	
	//Starting position for ball.
	private Point start;
	
	public TileMap() {
		int aWidth = width/tileSize;
		int aHeight = height/tileSize;
		int arraySize = aWidth*aHeight;
		map = new int[arraySize];
		
		int pos, val;
		for (int i=0; i<aWidth; i++) {
			for (int j=0; j<aHeight; j++) {
				pos = i+j*aWidth;
				val = 0;
				if (j == 0 || j == aHeight-1 || i == 0 || i == aWidth-1) {
					val = 1;
				} else if (i <= 3) {
					val = 0;
				} else {
					if (Math.random() < .1) {
						val = 1;
					}
				}
				map[pos]=val;
			}
		}
		
		//Force (1,30) to be starting position.
		start = new Point(1,aHeight/2);
		map[start.x+start.y*aWidth] = 2;
		
	}
	
	public void draw(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		int aWidth = width/tileSize;
		int aHeight = height/tileSize;
		int pos;
		for (int i=0; i<aWidth; i++) {
			for (int j=0; j<aHeight; j++) {
				pos = i+j*aWidth;
				if (map[pos] == 0) {
					//g2d.drawRect(i*tileSize, j*tileSize, tileSize, tileSize);
				} else if (map[pos] == 1) {
					g2d.fillRect(i*tileSize, j*tileSize, tileSize, tileSize);
				}
			}
		}
	}
	
	/* 
	 * @return		Starting tile for Ball.
	 */
	public Point getStart() {
		return start;
	}
}
