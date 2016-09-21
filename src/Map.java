import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
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
	private Shape[] tiles;
	
	//Starting position for ball.
	private Point start;
	
	public Map() {
		int aWidth = width/tileSize;
		int aHeight = height/tileSize;
		int arraySize = aWidth*aHeight;
		map = new int[arraySize];
		tiles = new Shape[arraySize];
		
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
					if (Math.random() < .4) {
						val = 10;
					}
				}
				tiles[pos] = new Rectangle2D.Double(i*tileSize, j*tileSize, tileSize, tileSize);
				map[pos]=val;
			}
		}
		
		//Force mid-way left hand side starting position.
		start = new Point(1,aHeight/2);
		map[start.x+start.y*aWidth] = 1;
		tiles[start.x+start.y*aWidth] = new Rectangle2D.Double(start.x*tileSize,start.y*tileSize,tileSize,tileSize);
		
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
					g2d.draw(tiles[pos]);
				} else if (map[pos] == 10) {
					g2d.fill(tiles[pos]);
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
	 * Checks balls for intersections with map elements after delta time step.
	 * 
	 * @param	ball	Ball to check.
	 * @param	delta	what fraction of a fixed time step to calculate.
	 * 
	 * @return	All lines that will intersect with ball.
	 * 
	 * ASSUMPTION: delta <= 1.0, ball smaller than tile size, ball cannot completely pass a tile in one delta time step.
	 */
	public ArrayList<Line2D> getPossibleLines(Ball ball, double delta) {
		ArrayList<Line2D> lines = new ArrayList<Line2D>();
		
		/* BROAD SEARCH 
		 * Use AABB to find which tiles to check.
		 */
		
		//Create AABB for resultant ball after movement represented by top-left and bottom-right points.
		Ellipse2D c = ball.getCircle();
		Point2D vel = ball.getVelocity();
		Point2D newCenter = new Point2D.Double((c.getCenterX() + vel.getX() * delta),(c.getCenterY() + vel.getY() * delta));
		int r = ball.getRadius();
		
		int centerTileX = (int)(newCenter.getX()/tileSize);
		int centerTileY = (int)(newCenter.getY()/tileSize);
		
		int gridWidth = width/tileSize;
		int gridHeight = height/tileSize;
		int tilePos, tileX, tileY;
		for (int i=-1; i<=1; i++) {
			for (int j=-1; j<=1; j++) {
				tileX = centerTileX + i;
				tileY = centerTileY + j;
				
				//Add check for redundant lines here if necessary
				if(tileX >= 0 && tileY >= 0 && tileX < gridWidth && tileY < gridHeight) {
					tilePos = tileX + tileY * gridWidth;
					if (map[tilePos] >= IS_SOLID) {
						
						/*
						 * NARROW SEARCH
						 * Check sphere against all lines in non-empty tiles.
						 */
						if (map[tilePos] == 10) {
							Line2D line;
							Rectangle2D tile = (Rectangle2D) tiles[tilePos];
							double top = tile.getY();
							double bottom = tile.getMaxY();
							double left = tile.getX();
							double right = tile.getMaxX();
							// If traveling in upwards direction and the highest point on
							//  the AABB is above the tile's bottom line, check bottom line.
							if(vel.getY() < 0 && newCenter.getY() - r <= bottom) {
								line = new Line2D.Double(new Point2D.Double(left,bottom),new Point2D.Double(right,bottom));
								if (checkLine(line,newCenter,r)) {
									lines.add(line);
								}
							}
							
							// If traveling in downwards direction and the lowest point on
							//  the AABB is below the tile's top line, check top line.
							if(vel.getY() > 0 && newCenter.getY() + r >= top) {
								line = new Line2D.Double(new Point2D.Double(left,top),new Point2D.Double(right,top));
								if (checkLine(line,newCenter,r)) {
									lines.add(line);
								}
							}
							
							// If traveling in the rightwards direction and the far right point on
							//  the AABB is right of the tile's left-most line, check left line.
							if (vel.getX() > 0 && newCenter.getX() + r >= left) {
								line = new Line2D.Double(new Point2D.Double(left,top),new Point2D.Double(left,bottom));
								if (checkLine(line,newCenter,r)) {
									lines.add(line);
								}
							}
							
							// If traveling in the leftwards direction and the far right point on
							//  the AABB is left of the tile's right-most line, check right line.
							if (vel.getX() < 0 && newCenter.getX() - r <= right) {
								line = new Line2D.Double(new Point2D.Double(right,top),new Point2D.Double(right,bottom));
								if (checkLine(line,newCenter,r)) {
									lines.add(line);
								}
							}
							
						}
					}
				}
			}
		}
		
		return lines;
	
	}
	
	/*
	 * 
	 * Checks if a line intersects with given circle.
	 * 
	 * @return	true if there is an intersection.
	 */
	private boolean checkLine(Line2D line, Point2D center, int radius) {
		Point2D closest = closestPointOnLine(line,center);
		double dist = Math.sqrt(Math.pow(closest.getX()-center.getX(), 2) + Math.pow(closest.getY() - center.getY(), 2));
		if (dist <= radius) {
			return true;
		}
		return false;
	}
	
	/*
	 * Helper method for checkLine method.
	 * 
	 * @return closest point on line to any given point.
	 */
	private Point2D closestPointOnLine(Line2D line, Point2D point){
		double lx1 = line.getX1();
		double lx2 = line.getX2();
		double ly1 = line.getY1();
		double ly2 = line.getY2();
		double A1 = ly2 - ly1; 
		double B1 = lx1 - lx2; 
		double C1 = (ly2 - ly1)*lx1 + (lx1 - lx2)*ly1; 
		double C2 = -B1*point.getX() + A1*point.getY(); 
		double det = A1*A1 - -B1*B1; 
		double cx = 0; 
		double cy = 0; 
		if(det != 0){ 
            cx = (A1*C1 - B1*C2)/det; 
            cy = (A1*C2 - -B1*C1)/det; 
		} else { 
            cx = point.getX(); 
            cy = point.getY(); 
		} 
		return new Point2D.Double(cx, cy); 
	}
}