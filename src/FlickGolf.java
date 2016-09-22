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
	
	//Offset to ease working with lines that are perfectly vertical or horizontal -- creating an artificial slope.
	private static double OFFSET = .001;
	
	//TEMPORARY CODE
	private ArrayList<Line2D> _TEMPORARY;
	
	public void init() {
		map = new Map();
		int tS = Map.tileSize;
		int r = tS/2;
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
			if (collisions.isEmpty()) {
				Point2D vel = ball.getVelocity();
				ball.move(vel.getX()*delta, vel.getY()*delta);
				delta = 0;
			} else {
				//Collision handling
				double dist;
				double smallestDist = -1;
				Line2D firstLine = collisions.get(0);
				for (Line2D line : collisions) {
					Point2D intersect = getPointOfIntersection(line);
					Point2D ballStart = new Point2D.Double(ball.getCircle().getCenterX(),ball.getCircle().getCenterY());
					dist = Math.sqrt(Math.pow(ballStart.getX()-intersect.getX(), 2) + Math.pow(ballStart.getY()-intersect.getY(),2));
					if (dist < smallestDist || smallestDist == -1) {
						smallestDist = dist;
						firstLine = line;
					}
				}
				
				//TEMPORARY CODE
				Point2D vel = ball.getVelocity();
				ball.move(vel.getX()*delta, vel.getY()*delta);
				_TEMPORARY.addAll(collisions);
				delta = 0;
				//TEMPORARY CODE
			}
		}
	}
	
	/*
	 * Helper method for collision handling part of physicsUpdate method
	 * Calculate the point of intersection between line and ball.
	 * 
	 * @param	line1	Line of static object
	 * 
	 * @return Point of intersection
	 */
	private Point2D getPointOfIntersection(Line2D line) {
		
		//Convert ball movement line into slope-intercept form:
		// y = (m1)x + (b1)
		Point2D start = new Point2D.Double(ball.getCircle().getCenterX(), ball.getCircle().getCenterY());
		Point2D end = new Point2D.Double(start.getX()+ball.getVelocity().getX(),start.getY()+ball.getVelocity().getY());
		
		double dY = start.getY() - end.getY();
		double dX = start.getX() - end.getX();
		if (dY == 0) {
			dY = this.OFFSET;
		}
		if (dX == 0) {
			dX = this.OFFSET;
		}
		double m1 = dY/dX;
		double b1 = start.getY() - (m1*start.getX());
		
		//Convert given static line into slope-intercept form:
		// y = (m2)x + (b2)
		dY = line.getY1() - line.getY2();
		dX = line.getX1() - line.getX2();
		if (dY == 0) {
			dY = this.OFFSET;
		}
		if (dX == 0) {
			dX = this.OFFSET;
		}
		double m2 = dY / dX;
		double b2 = line.getY1() - (m2 * line.getX1());
		
		//Calculate the determinant for which side of the line our ball is currently at.
		int detSide = getDeterminantSide(line);
		
		//Calculate determinant for which direction we're calculating our line in the x-direction.
		int detX = 0;
		if (dX > 0)	{ 
			detX = 1; 
		}
		if (dX < 0)	{ 
			detX = -1; 
		}
		
		//Offset static line by radius of ball.
		b2 = b2 + (detX * detSide * ball.getRadius());
		
		//Calculate & return point of intersection.
		double x = (b2 - b1)/(m1 - m2);
		double y = (m1*x) + b1;
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
		System.out.println(mouseClick);
	}


	@Override
	public void mouseReleased(MouseEvent e) {
		clicked = false;
		
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
