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
	public static int WIDTH = 960;
	public static int HEIGHT = 640;
	public static int TILESIZE = 20;
	
	// All integers at or above IS_SOLID contain collidable parts.
	private static int IS_SOLID = 10;
	/* Two-dimensional map as single array stored from top-left to bottom-right.
	 * Current assigned values:
	 * 		0 = empty
	 * 		1 = empty + starting position
	 * 		10 = square
	 * 		30 = triangle (top-left)
	 * 		35 = triangle (top-right)
	 *		40 = triangle (bottom-left)
	 *		45 = triangle (bottom-right) 		
	 */
	private static int SQUARE = 10;
	private static int TRIANGLE_TL = 30;
	private static int TRIANGLE_TR = 35;
	private static int TRIANGLE_BL = 40;
	private static int TRIANGLE_BR = 45;
	
	private int[] map;
	private Shape[] tiles;
	
	//Starting position for ball.
	private Point start;
	
	public Map() {
		int aWidth = WIDTH/TILESIZE;
		int aHeight = HEIGHT/TILESIZE;
		int arraySize = aWidth*aHeight;
		map = new int[arraySize];
		tiles = new Shape[arraySize];
		
		int pos, val;
		for (int i=0; i<aWidth; i++) {
			for (int j=0; j<aHeight; j++) {
				pos = i+j*aWidth;
				val = 0;
				tiles[pos] = new Rectangle2D.Double(i*TILESIZE, j*TILESIZE, TILESIZE, TILESIZE);
				if (j == 0 || j == aHeight-1 || i == 0 || i == aWidth-1) {
					val = SQUARE;
				} else if (i <= 3) {
					val = 0;
				} else {
					double freq = .015;
					double rand = Math.random();
					if (rand < freq*4) {
						val = SQUARE;
					} else if (rand < freq*5) {
						val = TRIANGLE_TL;
						Polygon triangle = new Polygon();
						triangle.addPoint(i*TILESIZE,j*TILESIZE);
						triangle.addPoint((i+1)*TILESIZE, j*TILESIZE);
						triangle.addPoint(i*TILESIZE, (j+1)*TILESIZE);
						tiles[pos] = triangle;
					} else if (rand < freq*6) {
						val = TRIANGLE_TR;
						Polygon triangle = new Polygon();
						triangle.addPoint(i*TILESIZE,j*TILESIZE);
						triangle.addPoint((i+1)*TILESIZE, j*TILESIZE);
						triangle.addPoint((i+1)*TILESIZE, (j+1)*TILESIZE);
						tiles[pos] = triangle;
					} else if (rand < freq*7) {
						val = TRIANGLE_BL;
						Polygon triangle = new Polygon();
						triangle.addPoint(i*TILESIZE,j*TILESIZE);
						triangle.addPoint(i*TILESIZE, (j+1)*TILESIZE);
						triangle.addPoint((i+1)*TILESIZE, (j+1)*TILESIZE);
						tiles[pos] = triangle;
					} else if (rand < freq*8) {
						val = TRIANGLE_BR;
						Polygon triangle = new Polygon();
						triangle.addPoint((i+1)*TILESIZE,j*TILESIZE);
						triangle.addPoint((i+1)*TILESIZE, (j+1)*TILESIZE);
						triangle.addPoint(i*TILESIZE, (j+1)*TILESIZE);
						tiles[pos] = triangle;
					}
				}
				map[pos]=val;
			}
		}
		
		//Force mid-way left hand side starting position.
		start = new Point(1,aHeight/2);
		map[start.x+start.y*aWidth] = 1;
		tiles[start.x+start.y*aWidth] = new Rectangle2D.Double(start.x*TILESIZE,start.y*TILESIZE,TILESIZE,TILESIZE);
		
	}
	
	public void draw(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g.setColor(Color.BLACK);
		int aWidth = WIDTH/TILESIZE;
		int aHeight = HEIGHT/TILESIZE;
		int pos, tileType;
		for (int i=0; i<aWidth; i++) {
			for (int j=0; j<aHeight; j++) {
				pos = i+j*aWidth;
				tileType = map[pos];
				if (tileType == 0) {
					//g2d.draw(tiles[pos]);
				} else if (tileType == SQUARE) {
					g2d.fill(tiles[pos]);
				} else if (tileType == TRIANGLE_TL || tileType == TRIANGLE_TR || tileType == TRIANGLE_BL || tileType == TRIANGLE_BR) {
					g2d.fillPolygon((Polygon)tiles[pos]);
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
	 * ASSUMPTIONS: delta <= 1.0, ball smaller than tile size, ball cannot completely pass a tile in one delta time step.
	 */
	public ArrayList<Line2D> getPossibleLines(Ball ball, double delta) {
		ArrayList<Line2D> lines = new ArrayList<Line2D>();
		
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
					tileType = map[tilePos];
					if (tileType >= IS_SOLID) {
						Line2D line;
						double top = tileY*TILESIZE;
						double bottom = (tileY+1)*TILESIZE;
						double left = tileX*TILESIZE;
						double right = (tileX+1)*TILESIZE;
						
						if (tileType == TRIANGLE_TL) {
							lines.add(new Line2D.Double(left,top,right,top));
							lines.add(new Line2D.Double(right,top,left,bottom));
							lines.add(new Line2D.Double(left,bottom,left,top));
						} else if (tileType == TRIANGLE_TR) {
							lines.add(new Line2D.Double(left,top,right,top));
							lines.add(new Line2D.Double(right,top,right,bottom));
							lines.add(new Line2D.Double(right,bottom,left,top));
						} else if (tileType == TRIANGLE_BL) {
							lines.add(new Line2D.Double(left,top,right,bottom));
							lines.add(new Line2D.Double(right,bottom,left,bottom));
							lines.add(new Line2D.Double(left,bottom,left,top));
						} else if (tileType == TRIANGLE_BR) {
							lines.add(new Line2D.Double(right,top,right,bottom));
							lines.add(new Line2D.Double(right,bottom,left,bottom));
							lines.add(new Line2D.Double(left,bottom,right,top));
						} else {
							lines.add(new Line2D.Double(left,top,right,top));
							lines.add(new Line2D.Double(right,top,right,bottom));
							lines.add(new Line2D.Double(right,bottom,left,bottom));
							lines.add(new Line2D.Double(left,bottom,left,top));
						}
					}
				}
			}
		}
		return lines;
	}
	
	/*
	 * Helper method for getPossibleLines method.
	 * 
	 * Checks if a line intersects with given circle.
	 * 
	 * @return	true if there is an intersection.
	 */
	private boolean checkLine(Line2D line, Point2D center, int radius) {
		Point2D closest = closestPointOnLine(line,center);
		double dist = closest.distance(center);
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
		double[] AP = new double[2];
		double[] AB = new double[2];
		
		AP[0] = point.getX() - line.getX1();
		AP[1] = point.getY() - line.getY1();
		AB[0] = line.getX2() - line.getX1();
		AB[1] = line.getY2() - line.getY1();
		
		double t = ((AP[0]*AB[0])+(AP[1]*AB[1]))/((AB[0]*AB[0]) + (AB[1]*AB[1]));
		
		if (t < 0) t = 0;
		else if (t > 1) t = 1;
		return new Point2D.Double(line.getX1()+AB[0]*t,line.getY1()+AB[1]*t);
	}
}
