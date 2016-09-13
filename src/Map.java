import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.sound.sampled.Line;
/*
 * Map provides storage and drawing for the current map.
 * 
 * Handles the collision detection for the game. 
 */
public class Map {
	public static int width = 960;
	public static int height = 640;
	public static int tileSize = 20;
	
	// All integers at or above IS_SOLID contain collidable parts.
	private static int IS_SOLID = 10;
	/* Two-dimensional map as single array stored from top-left to bottom-right.
	 * Current assigned values:
	 * 		0 = empty
	 * 		1 = empty + starting position
	 * 		10 = filled
	 */
	private int[] map;
	
	//Starting position for ball.
	private Point start;
	
	public Map() {
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
					val = 10;
				} else if (i <= 3) {
					val = 0;
				} else {
					if (Math.random() < .1) {
						val = 10;
					}
				}
				map[pos]=val;
			}
		}
		
		//Force (1,30) to be starting position.
		start = new Point(1,aHeight/2);
		map[start.x+start.y*aWidth] = 1;
		
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
				} else if (map[pos] == 10) {
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
	
	/*
	 * Check ball for intersections with map after delta .
	 * 
	 * @param	ball	Ball to check.
	 * @param	delta	what fraction of a fixed time step to calculate.
	 * 
	 * @return	All lines that intersect with ball.
	 * 
	 * ASSUMPTION: delta <= 1.0, ball smaller than tile size, ball cannot completely pass a tile in one time step.
	 */
	public ArrayList<Line2D> getCollisionLines(Ball ball, double delta) {
		ArrayList<Line2D> lines = new ArrayList<Line2D>();
		
		/* BROAD SEARCH 
		 * Use AABB to find which tiles to check.
		 */
		
		//Create AABB for resultant ball after movement represented by top-left and bottom-right points.
		Point c = ball.getCenter();
		Point vel = ball.getVelocity();
		Point2D newC = new Point2D.Double((c.x + vel.x * delta),(c.y + vel.y *delta));
		int r = ball.getRadius();
		Point2D min = new Point2D.Double(newC.getX() - r, newC.getY() - r);
		Point2D max = new Point2D.Double(newC.getX() + r, newC.getY() + r);
		
		int aWidth = width/tileSize;
		
		//Check only 4 tiles (max) based on ball size assumption.
		int tile, tileX, tileY;
		for (int i=0; i<2; i++) {
			for (int j=0; j<2; j++) {
				if (i==0) {
					tileX = (int) Math.floor(min.getX()/tileSize);
				} else {
					tileX = (int) Math.floor(max.getX()/tileSize);
				}
				if (j == 0) {
					tileY = (int) Math.floor(min.getY()/tileSize);
				} else {
					tileY = (int) Math.floor(max.getY()/tileSize);
				}
				tile = tileX + tileY * aWidth;
				
				if (map[tile] >= IS_SOLID) {
					
					/*
					 * NARROW SEARCH
					 * Check sphere against all lines in non-empty tiles.
					 */
					if (map[tile] == 10) {
						Line2D line;
						boolean check = false;
						double top = tileY * tileSize;
						double bottom = tileY * (tileSize+1);
						double left = tileX * tileSize;
						double right = tileX * (tileSize+1);
						if (newC.getX() < left) {
							line = new Line2D.Double(new Point2D.Double(left,top),new Point2D.Double(left,bottom));
							if (checkLine(line,newC,r)) {
								lines.add(line);
							}
						} else if (newC.getX() > right) {
							line = new Line2D.Double(new Point2D.Double(right,top),new Point2D.Double(right,bottom));
							if (checkLine(line,newC,r)) {
								lines.add(line);
							}
						}
						if (newC.getY() < top) {
							line = new Line2D.Double(new Point2D.Double(left,top),new Point2D.Double(right,top));
							if (checkLine(line,newC,r)) {
								lines.add(line);
							}
						} else if (newC.getY() > bottom) {
							line = new Line2D.Double(new Point2D.Double(left,bottom),new Point2D.Double(right,bottom));
							if (checkLine(line,newC,r)) {
								lines.add(line);
							}
						}
					}
					
				}	
			}
		}
		
		return lines;
	
	}
	
	/*
	 * Helper method for getCollisionLines().
	 * 
	 * Checks if a line intersects with given circle.
	 * 
	 * @return	true if there is an intersection.
	 */
	public boolean checkLine(Line2D line, Point2D center, int radius) {
		return true;
	}
}
