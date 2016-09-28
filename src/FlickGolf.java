import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
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
	private BufferedImage background;
	private Ball ball;
	
	private boolean playing;
	
	private boolean clicked;
	private Point mouseClick;
	private Point mouseCurrent;
	
	private Timer timer;
	private ActionListener taskPerformer;
	private static int TARGET_FPS = 30;
	private static int TARGET_IN_MILLI = 1000 / TARGET_FPS;
	
	public FlickGolf() {
		map = new Map();
		background = new BufferedImage(Map.WIDTH,Map.HEIGHT,BufferedImage.TYPE_INT_ARGB);
		map.draw(background.getGraphics());
		int tS = Map.tileSize;
		int r = tS/2 - 1;
		Point start = map.getStart();
		ball = new Ball(new Point(start.x*tS + r,start.y*tS + r),r);
		clicked = false;
		playing = false;
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
				
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				gameUpdate();
				repaint();
			}
		};
		
		timer = new Timer(TARGET_IN_MILLI,taskPerformer);
		timer.setInitialDelay(0);
	}
	
	
	public void playGame() {
		playing = true;		
		timer.start();
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
			ArrayList<Line2D> lines = map.getPossibleLines(ball, delta);
			Point2D vel = ball.getVelocity();
			if (lines.isEmpty()) {
				ball.move(vel.getX()*delta, vel.getY()*delta);
				delta = 0;
			} else {
				//Collision handling
				double smallestDist = map.HEIGHT;
				Point2D[] collision = new Point2D.Double[3];
				
				for (Line2D line : lines) {
					smallestDist = checkCircles(line,smallestDist,collision);
					smallestDist = checkLines(line,smallestDist,collision);
				}
						
				if(smallestDist < map.HEIGHT) {
					Point2D lineP1 = collision[0];
					Point2D lineP2 = collision[1];
					Point2D intersect = collision[2];
					double dX = lineP2.getX()-lineP1.getX();
					double dY = lineP2.getY()-lineP1.getY();
					double collDelta = (intersect.getY()-ball.getCircle().getCenterY())/vel.getY();
					if (collDelta < delta) {
						double dotProd;
						double[] projLine = new double[2];
						double[] projLineNorm = new double[2];
						dotProd = ((vel.getX()*dX)+(vel.getY()*dY))/((dX*dX)+(dY*dY));
						projLine[0] = dotProd*dX;
						projLine[1] = dotProd*dY;
						
						dotProd = ((vel.getX()*dY)+(vel.getY()*-dX))/((dY*dY)+(-dX*-dX));
						projLineNorm[0] = dotProd*dY;
						projLineNorm[1] = dotProd*-dX;
						
						Point2D newVel = new Point2D.Double(projLine[0]-projLineNorm[0],projLine[1]-projLineNorm[1]);
						
						ball.move(vel.getX()*collDelta, vel.getY()*collDelta);
						ball.setVelocity(newVel);
						delta -= collDelta;
					} else {
						ball.move(vel.getX()*delta, vel.getY()*delta);
						delta = 0;
					}
					
				} else {
					ball.move(vel.getX()*delta, vel.getY()*delta);
					delta = 0;
				}
			}
		}
	}
	
	/*
	 * Helper method for physicsUpdate method.
	 * Creates circles around edge points of line to check for intersections.
	 * Updates current collision array accordingly if a shorter collision is found.
	 * 
	 * @param	line			Line segment to project circles around.
	 * @param	currSmallest	Current smallest distance to a known collision point.
	 * @param 	current			Array representation of the line and point for current collision.
	 * 
	 * @return	smallest distance between given and collisions with generated circles.
	 */
	private double checkCircles(Line2D line, double currSmallest, Point2D[] current) {
		double smallest = currSmallest;
		Point2D collPoint = new Point2D.Double(-1,-1);
		Point2D vel = ball.getVelocity();
		Point2D start = new Point2D.Double(ball.getCircle().getCenterX(),ball.getCircle().getCenterY());
		double r = ball.getRadius();
		
		for (int  i=0; i<2; i++) {
			Line2D tangent;
			double a, b, c, m, b2, det, dist;
			Point2D center;
			if (i==0) {
				center = line.getP1();
			} else {
				center = line.getP2();
			}
			if (vel.getX() == 0) {
				m = 0;
				b2 = 0;
				a = 1;
				b = -2*center.getY();
				c = Math.pow(vel.getX()-center.getX(),2)+Math.pow(center.getY(), 2)-r;
			} else {
				//Change to equation of line: y = mx + b2
				m = vel.getY()/vel.getX();
				b2 = start.getY()-m*start.getX();
				a = m*m + 1;
				b = 2*((m*b2)-(m*center.getY())-center.getX());
				c = Math.pow(center.getY(),2)-(r*r)+Math.pow(center.getX(), 2)-(2*b2*center.getY())+(b2*b2);
			}		
			det = Math.pow(b, 2)- (4*a*c);
			int loop = 1;
			if (det < 0) {
				loop = -2;
			} else if (det == 0) {
				loop = -1;
			} else {
				loop = 1;
			}
			for (int j=-1; j<=loop; j+=2) {
				if (vel.getX() == 0) {
					collPoint.setLocation(vel.getX(), (-b+j*Math.sqrt(det))/(2*a));
				} else {
					double x = (-b+(j*Math.sqrt(det)))/(2*a);
					collPoint.setLocation(x, x*m+b2);
				}
				dist = collPoint.distance(start);
				if (dist < smallest) {
					smallest = dist;
					tangent = getTangent(center,collPoint);
					current[0] = tangent.getP1();
					current[1] = tangent.getP2();
					current[2] = (Point2D)collPoint.clone();
				}
			}
		}
		return smallest;
	}
	
	/*
	 * Helper method for checkCircles method.
	 * 
	 * @param	center	Center of circle
	 * @param	point	Point on circle to find tangential line.
	 * 
	 * @return Any line tangential to circle at given point.
	 */
	private Line2D getTangent(Point2D center, Point2D point) {
		double pX = point.getX();
		double pY = point.getY();
		double dY = pY - center.getY();
		double dX = pX - center.getX();
		if (dY == 0) {
			return new Line2D.Double(pX-1,pY,pX+1,pY);
		} else if (dX == 0) {
			return new Line2D.Double(pX,pY-1,pX,pY+1);
		} else {
			double m = -dX/dY;
			double b = pY-(m*pX);
			return new Line2D.Double(pX+1,m*(pX+1)+b,pX-1,m*(pX-1)+b);
		}
	}
	
	/*
	 * Helper method for physicsUpdate method.
	 * Projects lines on either side of given at ball's radius distance away to check for intersections.
	 * Updates current collision array accordingly if a shorter collision is found.
	 * 
	 * @param	line			Line segment to project circles around.
	 * @param	currSmallest	Current smallest distance to a known collision point.
	 * @param 	current			Array representation of the line and point for current collision.
	 * 
	 * @return	smallest distance between given and collisions with generated circles.
	 */
	private double checkLines(Line2D line, double currSmallest, Point2D[] current) {
		double smallest = currSmallest;
		double dist, mag, dX, dY, dX_PA, dY_PA;
		double r = ball.getRadius();
		Point2D point;
		dY = line.getY2()-line.getY1();
		dX = line.getX2()-line.getX1();
		dist = Math.sqrt((dY*dY)+(dX*dX));
		mag = r / dist;
		point = new Point2D.Double(line.getX1()+(mag*dX),line.getY1()+(mag*dY));
		dY_PA = point.getY()-line.getY1();
		dX_PA = point.getX()-line.getX1();
		
		double a,b,c,dX1,dX2,dY1,dY2,t,u;
		double[] d = new double[2];
		Line2D newLine;
		Point2D start = new Point2D.Double(ball.getCircle().getCenterX(),ball.getCircle().getCenterY());
		for (int i=-1; i<=1; i+=2) {
			newLine = new Line2D.Double(line.getX1()+(i*dY_PA),line.getY1()-(i*dX_PA),line.getX2()+(i*dY_PA),line.getY2()-(i*dX_PA));
			dX1 = ball.getVelocity().getX();
			dY1 = ball.getVelocity().getY();
			dX2 = newLine.getX2()-newLine.getX1();
			dY2 = newLine.getY2()-newLine.getY1();
			d[0] = newLine.getX1()-start.getX();
			d[1] = newLine.getY1()-start.getY();
			a = (dX1*dY2)-(dY1*dX2);
			b = (d[0]*dY1)-(d[1]*dX1);
			c = (d[0]*dY2)-(d[1]*dX2);
			if( a != 0 ) {
				t = c / a;
				u = b / a;
				if (t>=0 && t<=1 && u>=0 && u<=1) {
					point.setLocation(start.getX()+(t*dX1), start.getY()+(t*dY1));
					dist = point.distance(start);
					if (dist < smallest) {
						smallest = dist;
						current[0] = newLine.getP1();
						current[1] = newLine.getP2();
						current[2] = (Point2D)point.clone();
					}
				}
			}
		}
		return smallest;
	}
	
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		super.paintComponent(g);
		g.drawImage(background,0,0,null);
		ball.draw(g);
		if(clicked) {
			g2d.drawLine(mouseClick.x, mouseClick.y, mouseCurrent.x, mouseCurrent.y);
		}
	}
	
	@Override
	public void keyPressed(KeyEvent key) {
		if(key.getKeyChar() == KeyEvent.VK_ESCAPE) {
			
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
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if (clicked) {
			mouseCurrent = e.getPoint();
		}
	}

	
	
	
	public static void main(String[] args) {
		FlickGolf game = new FlickGolf();
		JFrame frame = new JFrame();
		frame.setTitle("Flick Mini-Golf!");
		frame.getContentPane().setPreferredSize(new Dimension(Map.WIDTH, Map.HEIGHT));
		frame.add(game);
		
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
