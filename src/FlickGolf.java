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
		int tS = map.tileSize;
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
		while (delta >= 1.0) {
			ArrayList<Line2D> collisions = map.getCollisionLines(ball, delta);
			if (collisions.isEmpty()) {
				Point vel = ball.getVelocity();
				ball.move((int)Math.round(vel.getX()*delta), (int)Math.round(vel.getY()*delta));
				delta = 0;
			} else {
				//Collision handling
				
				//TEMPORARY CODE
				Point vel = ball.getVelocity();
				ball.move((int)Math.round(vel.getX()*delta), (int)Math.round(vel.getY()*delta));
				delta = 0;
				_TEMPORARY.addAll(collisions);
				//TEMPORARY CODE
			}
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
			g2d.drawLine((int)Math.round(line.getX1()), (int)Math.round(line.getY1()), (int)Math.round(line.getX2()), (int)Math.round(line.getY2()));
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
