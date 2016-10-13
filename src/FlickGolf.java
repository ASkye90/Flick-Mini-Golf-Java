import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

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
	private static int TARGET_FPS = 300;
	private static int TARGET_IN_MILLI = 1000 / TARGET_FPS;
	
	private boolean showDevLines;
	private ArrayList<Line2D> collLines;
	private ArrayList<Ellipse2D> collCircs;
	private ArrayList<Point2D> pointsOfInterest;
	private Line2D impactLine;
	private Point2D impact;
	
	public FlickGolf() {
		map = new Map();
		map.loadLevel("Assets/level1.csv");
		background = new BufferedImage(Map.WIDTH,Map.HEIGHT,BufferedImage.TYPE_INT_ARGB);
		map.draw(background.getGraphics());
		int tS = Map.TILESIZE;
		int r = (int)(tS/4);
		Point start = map.getStart();
		ball = new Ball(new Point(start.x*tS + r,start.y*tS + r),r);
		clicked = false;
		playing = false;
		addMouseListener(this);
		addMouseMotionListener(this);
		
		showDevLines = false;
		collLines = new ArrayList<Line2D>();
		collCircs = new ArrayList<Ellipse2D>();
		pointsOfInterest = new ArrayList<Point2D>();
				
		taskPerformer = new ActionListener() {
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
		while (delta > 0) {
			Point2D vel = ball.getVelocity();
			ArrayList<Line2D> lines = map.getPossibleLines(ball, delta);
			if (lines.isEmpty()) {
				ball.move(vel.getX()*delta, vel.getY()*delta);
				delta = 0;
			} else {
				if(timer.isRunning()) {
					collLines.clear();
					collCircs.clear();
					pointsOfInterest.clear();
				}
				//Collision handling
				double smallestDist = Map.HEIGHT;
				ArrayList<Point2D> collisions = new ArrayList<Point2D>();
				double fullDist = Math.sqrt(Math.pow(vel.getX(), 2) + Math.pow(vel.getY(), 2));
				
				//R
				Line2D save = lines.get(0);
				for (Line2D line : lines) {
					double before = smallestDist;
					smallestDist = checkLines(line,smallestDist,collisions);
					smallestDist = checkCircles(line,smallestDist,collisions);
					
					//R
					if (smallestDist < before) {
						save = line;
					}
				}
						
				if(smallestDist < Map.HEIGHT) {
					
					Point2D lineP1 = collisions.get(0);
					Point2D lineP2 = collisions.get(1);
					Point2D intersect = collisions.get(2);
					double collDelta = smallestDist/fullDist;
					if (collDelta > 0 && collDelta <= delta) {
						Point2D newVel = getReflectVel(ball.getVelocity(),lineP1,lineP2);

						ball.setCenter(intersect.getX(), intersect.getY());
						
						//Handle any simultaneous collisions here.
						if (collisions.size() >= 6) {
							for(int i=3; i<collisions.size(); i+=3) {
								newVel = getReflectVel(newVel,collisions.get(i),collisions.get(i+1));
							}
						}
						
						ball.setVelocity(newVel);
						
						//Remove later
						impactLine = save;
						impact = intersect;
						//timer.stop();
						//System.out.println("Stop");
						
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
	private double checkCircles(Line2D line, double currSmallest, ArrayList<Point2D> collisions) {
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
			
			//Calculate point(s) of intersection using quadratic formula and equations for both circle and line.
			if (vel.getX() == 0) {
				//Special case if line is vertical (cannot be represented by equation y=mx+b.
				m = 0;
				b2 = 0;
				a = 1;
				b = -2*center.getY();
				c = Math.pow(ball.getCircle().getCenterX()-center.getX(),2)+Math.pow(center.getY(), 2)-(r*r);
			} else {
				m = vel.getY()/vel.getX();
				b2 = start.getY()-m*start.getX();
				a = m*m + 1;
				b = 2*((m*b2)-(m*center.getY())-center.getX());
				c = Math.pow(center.getY(),2)-(r*r)+Math.pow(center.getX(), 2)-(2*b2*center.getY())+(b2*b2);
			}
			det = Math.pow(b, 2)- (4*a*c);
			int loop = 1;
			//Use determinant for quadratic formula to find number of points of intersection.
			if (det < 0) {
				loop = -2;
			} else if (det == 0) {
				loop = -1;
			} else {
				loop = 1;
			}
			for (int j=-1; j<=loop; j+=2) {
				if (vel.getX() == 0) {
					//Special case if line was vertical.
					collPoint.setLocation(ball.getCircle().getCenterX(), (-b+j*Math.sqrt(det))/(2*a));
				} else {
					double x = (-b+(j*Math.sqrt(det)))/(2*a);
					collPoint.setLocation(x, (x*m)+b2);
				}
				//Make sure the ball is traveling towards the collision point.
				Point2D dir = new Point2D.Double(collPoint.getX()-start.getX(),collPoint.getY()-start.getY());
				if (Math.signum(vel.getX()) == Math.signum(dir.getX()) && Math.signum(vel.getY()) == Math.signum(dir.getY())) {
					dist = collPoint.distance(start);
					if (dist > 1E-5 && dist <= smallest) {
						if (dist != smallest) {
							collisions.clear();
						}
						smallest = dist;
						tangent = getTangent(center,collPoint);
						//Remove overlapping circles.
						if(!collisions.contains(collPoint)) {
							collisions.add(tangent.getP1());
							collisions.add(tangent.getP2());
							collisions.add((Point2D)collPoint.clone());
							if(timer.isRunning()) collLines.add(tangent);
						}
					}
				}
				if(timer.isRunning()) pointsOfInterest.add(collPoint);
			}
			if(timer.isRunning()) collCircs.add(new Ellipse2D.Double(center.getX()-r, center.getY()-r, r*2, r*2));
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
			return new Line2D.Double(pX,pY-20,pX,pY+20);
		} else if (dX == 0) {
			return new Line2D.Double(pX-20,pY,pX+20,pY);
		} else {
			if(timer.isRunning()) {
				pointsOfInterest.add(point);
				pointsOfInterest.add(center);
			}
			return new Line2D.Double(pX-dY,pY+dX,pX+dY,pY-dX);
		}
	}
	
	/*
	 * Helper method for physicsUpdate method.
	 * Projects lines on either side of given line at ball's radius distance away to check for intersections.
	 * Updates current collision array accordingly if a shorter collision is found.
	 * 
	 * @param	line			Line segment to project lines from.
	 * @param	currSmallest	Current smallest distance to a known collision point.
	 * @param 	current			Array representation of the line and point for current collision.
	 * 
	 * @return	smallest distance between given and collisions with generated circles.
	 */
	private double checkLines(Line2D line, double currSmallest, ArrayList<Point2D> collisions) {
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
				if (u>0 && u<1 && t>=0 && t<=1) {
					point.setLocation(start.getX()+(t*dX1), start.getY()+(t*dY1));
					dist = point.distance(start);
					if(timer.isRunning()) pointsOfInterest.add(point);
					if (dist > 1E-5 && dist <= smallest) {
						if (dist != smallest) {
							collisions.clear();
						}
						smallest = dist;
						collisions.add(newLine.getP1());
						collisions.add(newLine.getP2());
						collisions.add((Point2D)point.clone());
					}
				}
			}
			if(timer.isRunning()) collLines.add(newLine);
		}
		return smallest;
	}
	
	/*
	 * Helper method for physicsUpdate method.
	 * Calculates the reflection vector for velocity assumed to hit line P1 -> P2.
	 * 
	 * @param	vel		Given starting velocity.
	 * @param	lineP1	First point on line.
	 * @param	lineP2	Second point on line. 
	 * 
	 * @return	Reflection vector over line.
	 */
	private Point2D getReflectVel(Point2D vel, Point2D lineP1, Point2D lineP2) {
		double dotProd;
		double dX = lineP2.getX()-lineP1.getX();
		double dY = lineP2.getY()-lineP1.getY();
		double[] projLine = new double[2];
		double[] projLineNorm = new double[2];
		dotProd = ((vel.getX()*dX)+(vel.getY()*dY))/((dX*dX)+(dY*dY));
		projLine[0] = dotProd*dX;
		projLine[1] = dotProd*dY;
		
		dotProd = ((vel.getX()*dY)+(vel.getY()*-dX))/((dY*dY)+(-dX*-dX));
		projLineNorm[0] = dotProd*dY;
		projLineNorm[1] = dotProd*-dX;
		
		return new Point2D.Double(projLine[0]-projLineNorm[0],projLine[1]-projLineNorm[1]);
		
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
		
		if(showDevLines) {
		g2d.setColor(Color.cyan);
			for (Ellipse2D e:collCircs) {
				g2d.draw(e);
			}
			g2d.setColor(Color.red);
			for (Line2D l:collLines) {
				g2d.draw(l);
			}
			g2d.setColor(Color.green);
			g2d.fillOval((int)ball.getCircle().getCenterX()-1,(int)ball.getCircle().getCenterY()-1,3,3);
			
			g2d.setColor(Color.orange);
			for (Point2D point:pointsOfInterest) {
				g2d.drawOval((int)point.getX()-1, (int)point.getY()-1, 3, 3);
			}
		}
	}
	
	@Override
	public void keyPressed(KeyEvent key) {
		int kc = key.getKeyCode();
		if(kc == KeyEvent.VK_A) {
			ball.setVelocity(-1,0);
		} else if (kc == KeyEvent.VK_D) {
			ball.setVelocity(1,0);
		} else if (kc == KeyEvent.VK_W) {
			ball.setVelocity(0,-1);
		} else if (kc == KeyEvent.VK_S) {
			ball.setVelocity(0,1);
		} else if (kc == KeyEvent.VK_F1) {
			showDevLines = !showDevLines;
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			ball.setCenter(e.getPoint().getX(), e.getPoint().getY());
		} else {
			clicked = true;
			mouseClick = e.getPoint();
			mouseCurrent = e.getPoint();
		}
	}


	@Override
	public void mouseReleased(MouseEvent e) {
		if(clicked) {
			clicked = false;
			ball.setVelocity(new Point2D.Double((mouseCurrent.getX()-mouseClick.getX())/50,(mouseCurrent.getY()-mouseClick.getY())/50));
		}
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
		
		frame.addKeyListener(game);
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
