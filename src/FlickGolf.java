import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/*
 * FlickGolf is the main class for the game.
 * 
 * Handles the initiation of all related classes.
 * Handles the game's event listeners.
 */
public class FlickGolf extends JPanel implements MouseListener,MouseMotionListener,KeyListener{
	
	private TileMap tilemap;
	private Ball ball;
	
	private boolean playing;
	
	private boolean clicked;
	private Point mouseClick;
	private Point mouseCurrent;
	
	public void init() {
		tilemap = new TileMap();
		int tS = tilemap.tileSize;
		int r = tS/2;
		Point start = tilemap.getStart();
		ball = new Ball(new Point(start.x*tS + r,start.y*tS + r),r);
		clicked = false;
		playing = false;
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
	}
	
	
	public void playGame() {
		playing = true;
		
		
		long lastLoopTime = System.nanoTime();
		final int TARGET_FPS = 30;
		final long TARGET_IN_NANO = 1000000000 / TARGET_FPS;
		
		while(playing) {
			long now = System.nanoTime();
			long timeElapsed = now - lastLoopTime;
			
			/* Change in time RELATIVE to the target fps.
			 * (i.e. if target = 60fps, 
			 * 		delta 0.5 = 1/120 sec passed
			 * 		delta 2 = 1/30 sec passed) 
			 */
			double delta = timeElapsed / TARGET_IN_NANO;
			if (delta >= 1) {
				lastLoopTime = now;
				gameUpdate(delta);
				repaint();
			}
			
			try {
				Thread.sleep((TARGET_IN_NANO - (System.nanoTime() - lastLoopTime))/1000000);
			} catch (Exception e) {
				
			}
		}
	}
	
	/*
	 * Update game logic.
	 * 
	 * @param	delta		Number of frame(s) passed.
	 */
	public void gameUpdate(double delta) {
		
	}
	
	/*
	 * Pause functionality not implemented!
	 */
	public void pauseGame() {
		//playing = false;
	}
	
	public void render() {
		Graphics g = this.getGraphics();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		tilemap.draw(g);
		ball.draw(g);
		if(clicked) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.drawLine(mouseClick.x, mouseClick.y, mouseCurrent.x, mouseCurrent.y);
		}
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
		frame.getContentPane().setPreferredSize(new Dimension(TileMap.width, TileMap.height));
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
