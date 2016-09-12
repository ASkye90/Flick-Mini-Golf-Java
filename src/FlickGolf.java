import java.awt.*;
import javax.swing.*;

/*
 * FlickGolf is the main class for the game.
 * 
 * Handles the initiation of all related classes.
 * Handles the game's event listeners.
 */
public class FlickGolf extends JPanel {
	
	private TileMap tilemap;
	
	public void init() {
		tilemap = new TileMap();
	}
	
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		tilemap.draw(g);
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
	}

}
