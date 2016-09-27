import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.*;

/*
 * FlickGolf is the main class for the game.
 * 
 * Handles the initiation of all related classes.
 * Handles the game's event listeners.
 */
public class FlickGolf extends JPanel implements MouseListener,MouseMotionListener,KeyListener{
	
	private Map map;
	private Ball ball;
	
	private boolean playing;
	
	private boolean clicked;
	private Point mouseClick;
	private Point mouseCurrent;
	
	//TEMPORARY CODE
	private ArrayList<Line2D> _TEMPORARY;
	
	public void init() {
		map = new Map();
		int tS = Map.tileSize;
		int r = tS/2 - 1;
		Point start = map.getStart();
		ball = new Ball(new Point(start.x*tS + r,start.y*tS + r),r);
		clicked = false;
		playing = false;
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		
		//TEMPORARY CODE
		_TEMPORARY = new ArrayList<Line2D>();
	}
	
	
	public void playGame() {
		playing = true;
		
		
		long lastLoopTime = System.nanoTime();
		final int TARGET_FPS = 30;
		final long TARGET_IN_NANO = 1000000000 / TARGET_FPS;
		
		//Percentage leniency in delta for fixed time step.
		final double DELTA_PADDING = .05;
		
		while(playing) {
			long now = System.nanoTime();
			long timeElapsed = now - lastLoopTime;
			
			/* Change in time RELATIVE to the target fps.
			 * (i.e. if target = 60fps, 
			 * 		delta 0.5 = 1/120 sec passed
			 * 		delta 2 = 1/30 sec passed) 
			 */
			double delta = timeElapsed / TARGET_IN_NANO;
			if (delta >= 1 - DELTA_PADDING && delta <= 1 + DELTA_PADDING) {
				lastLoopTime = now;
				gameUpdate();
				repaint();
			} else if (delta >= 2) {
				//Attempt to account for severe lag.
				
				lastLoopTime = now;
				for (int i=0; i<(int)Math.floor(delta); i++) {
					gameUpdate();
				}
				repaint();
			}
			
			try {
				Thread.sleep((TARGET_IN_NANO - (System.nanoTime() - lastLoopTime))/1000000);
			} catch (Exception e) {
				
			}
		}
	}
	
	/*
	 * Update game logic and check for game end conditions.
	 * 
	 */
	public void gameUpdate() {
		physicsUpdate();
	}
	
	/*
	 * Check and update physics of the game using fixed time step. 
	 * (collision detection/handling)
	 */
	public void physicsUpdate() {
		double delta = 1.0;
		while (delta >= 0.01) {
			ArrayList<Line2D> collisions = map.getPossibleLines(ball, delta);
			Point2D vel = ball.getVelocity();
			if (collisions.isEmpty()) {
				ball.move(vel.getX()*delta, vel.getY()*delta);
				delta = 0;
			} else {
				System.out.println("Collisions : " + collisions.size());
				//Collision handling
				double smallestDist = -1;
				Line2D firstLine = collisions.get(0);
				Point2D firstIntersect = getPointOfIntersection(firstLine);
				if(collisions.size() > 1) {
					double dist;
					for (Line2D line : collisions) {
						Point2D intersect = getPointOfIntersection(line);
						Point2D ballStart = new Point2D.Double(ball.getCircle().getCenterX(),ball.getCircle().getCenterY());
						
											
						//Handles case where ball's starting position is closer than projected line for intersection.
						if (sameSign(vel.getX(),intersect.getX()-ballStart.getX())&&sameSign(vel.getY(),intersect.getY()-ballStart.getY())) {
							dist = ballStart.distance(intersect);
							if (dist < smallestDist || smallestDist == -1) {
								smallestDist = dist;
								firstLine = line;
								firstIntersect = intersect;
							}
						}
						
						
					}
				}
				
				//Calculate percentage of given delta time until collision.
				Point2D start = new Point2D.Double(ball.getCircle().getCenterX(), ball.getCircle().getCenterY());
				Point2D endPoint = new Point2D.Double(start.getX()+ball.getVelocity().getX()*delta,start.getY()+ball.getVelocity().getY()*delta);
				double perc = start.distance(firstIntersect)/start.distance(endPoint);
				System.out.println("Velocity: " + vel);
				System.out.println(firstLine + " : (" + firstLine.getX1() + ", " + firstLine.getY1()+"), (" + firstLine.getX2() + ", " + firstLine.getY2() +")");
				System.out.println(start + " : " + endPoint + " : " + firstIntersect);
				if(perc > 1) {
					ball.move(vel.getX()*delta, vel.getY()*delta);
					delta = 0;
				} else {
					double dX,dY, dotProd;
					double[] projLine = new double[2];
					double[] projLineNorm = new double[2];
					
					//Split ball's velocity into projection on line and normal of line.
					dX = firstLine.getX2() - firstLine.getX1();
					dY = firstLine.getY2() - firstLine.getY1();
					dotProd = ((vel.getX()*dX)+(vel.getY()*dY))/((dX*dX)+(dY*dY));
					projLine[0] = dotProd*dX;
					projLine[1] = dotProd*dY;
					
					dotProd = ((vel.getX()*dY)+(vel.getY()*-dX))/((dY*dY)+(-dX*-dX));
					projLineNorm[0] = dotProd*dY;
					projLineNorm[1] = dotProd*-dX;
					
					Point2D newVel = new Point2D.Double(projLine[0]-projLineNorm[0],projLine[1]-projLineNorm[1]);
					
					ball.move(vel.getX()*delta*perc, vel.getY()*delta*perc);
					ball.setVelocity(newVel);
					delta *= (1-perc);
				}
				//TEMPORARY CODE
				//ball.setVelocity(newVel);
				System.out.println();
				_TEMPORARY.addAll(collisions);
				//TEMPORARY CODE
			}
		}
	}
	
	/* Helper method for collision handling part of physicsUpdate method.
	 * 
	 */
	private boolean sameSign(double a, double b) {
		if (a > 0 && b > 0) {
			return true;
		} else if (a < 0 && b < 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * Helper method for collision handling part of physicsUpdate method
	 * Calculate the point of intersection between line and ball.
	 * 
	 * @param	line1	Line of static object
	 * 
	 * @return Center point of ball where intersection occurs.
	 * ASSUMPTION: ball will intersect with line given current velocity vector.
	 */
	private Point2D getPointOfIntersection(Line2D line) {
		
		Point2D start = new Point2D.Double(ball.getCircle().getCenterX(), ball.getCircle().getCenterY());
		Point2D end = new Point2D.Double(start.getX()+ball.getVelocity().getX(),start.getY()+ball.getVelocity().getY());
		
		double dY1 = start.getY() - end.getY();
		double dX1 = start.getX() - end.getX();
	
		double dY2 = line.getY1() - line.getY2();
		double dX2 = line.getX1() - line.getX2();
		
		//Calculate the determinant for which side of the line our ball is currently at.
		int detSide = getDeterminantSide(line);
				
		//Calculate determinant for which direction we're calculating our line in the x-direction.
		int detX = 0;
		if (dX2 > 0)	{ 
			detX = 1; 
		} else if (dX2 < 0)	{ 
			detX = -1; 
		} else {
			detX = 0;
		}
		
		double x, y, m1, b1, m2, b2;
		boolean isVertMove = false;
		boolean isVertLine = false;
		
		//Convert ball movement line into slope-intercept form:
		// y = (m1)x + (b1)
		if (dX1 == 0) {
			isVertMove = true;
			m1 = 0;
		} else {
			m1 = dY1 / dX1;
		}
		b1 = start.getY() - (m1*start.getX());
		
		//Convert given static line into slope-intercept form:
		// y = (m2)x + (b2)
		if (dX2 == 0) {
			isVertLine = true;
			m2 = 0;
		} else {
			m2 = dY2 / dX2;
		}
		b2 = line.getY1() - (m2 * line.getX1());
		
		//Offset static line by radius of ball.
		b2 = b2 + (detX * detSide * ball.getRadius());
		
		//Calculate & return point of intersection.
		if (isVertMove && isVertLine) {
			x = start.getX();
			if (Math.abs(line.getY1() - start.getY()) < Math.abs(line.getY2() - start.getY())) {
				y = line.getY1();
			} else {
				y = line.getY2();
			}
			if (line.getY1() < start.getY()) {
				y -= ball.getRadius();
			} else {
				y += ball.getRadius();
			}
		} else if (isVertMove) {
			x = start.getX();
			y = (m2*x) + b2;
		} else if (isVertLine) {
			x = line.getX1() + (detSide)*ball.getRadius();
			y = (m1*x) + b1;
		} else {
			x = (b2 - b1)/(m1 - m2);
			y = (m1*x) + b1;
		}
		return new Point2D.Double(x,y);
	}
	
	/* Helper method for getPointOfIntersection and physicsUpdate methods.
	 * Return determinant for which side of the line the ball's center is currently at,
	 * relative to the line moving from point (x1,y1) to point (x2,y2).
	 * 
	 * @return	int		-1 if to the right, +1 if to the left, 0 if ball's centered on line.
	 * 
	 */
	private int getDeterminantSide(Line2D line) {
		double detVal = (ball.getCircle().getCenterX() - line.getX1())*(line.getY2()-line.getY1()) - (ball.getCircle().getCenterY()-line.getY1())*(line.getX2()-line.getX1());
		if (detVal > 0) { 
			return 1;
		} else if (detVal < 0) { 
			return -1; 
		} else {
			return 0;
		}
	}
	
	/*
	 * Pause functionality not implemented!
	 */
	public void pauseGame() {
		//playing = false;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		super.paintComponent(g);
		map.draw(g);
		ball.draw(g);
		if(clicked) {
			g2d.drawLine(mouseClick.x, mouseClick.y, mouseCurrent.x, mouseCurrent.y);
		}
		
		//TEMPORARY CODE
		g.setColor(Color.red);
		for (Line2D line: _TEMPORARY) {
			g2d.draw(line);
			//g2d.drawLine((int)Math.round(line.getX1()), (int)Math.round(line.getY1()), (int)Math.round(line.getX2()), (int)Math.round(line.getY2()));
		}
		g.setColor(Color.black);
		//TEMPORARY CODE
	}
	
	@Override
	public void keyPressed(KeyEvent key) {
		if(key.getKeyChar() == KeyEvent.VK_ESCAPE) {
			pauseGame();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		clicked = true;
		mouseClick = e.getPoint();
		mouseCurrent = e.getPoint();
	}


	@Override
	public void mouseReleased(MouseEvent e) {
		clicked = false;
		ball.setVelocity(new Point2D.Double((mouseCurrent.getX()-mouseClick.getX())/50,(mouseCurrent.getY()-mouseClick.getY())/50));
		//Insert ball action here.
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if (clicked) {
			mouseCurrent = e.getPoint();
		}
	}

	
	
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		FlickGolf game = new FlickGolf();
		frame.setTitle("Flick Mini-Golf!");
		frame.getContentPane().setPreferredSize(new Dimension(Map.width, Map.height));
		frame.add(game);
		game.init();
		
		frame.pack();		
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		game.playGame();
	}


	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
