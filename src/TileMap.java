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
	 * Currently only 2 assigned values:
	 * 		0 = empty
	 * 		1 = filled
	 */
	private int[] map;
	
	
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
				} else {
					if (Math.random() < .2) {
						val = 1;
					}
				}
				map[pos]=val;
			}
		}
		
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
					g2d.drawRect(i*tileSize, j*tileSize, tileSize, tileSize);
				} else if (map[pos] == 1) {
					g2d.fillRect(i*tileSize, j*tileSize, tileSize, tileSize);
				}
			}
		}
	}
}
