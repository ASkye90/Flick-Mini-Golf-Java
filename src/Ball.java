import java.awt.*;

/*
 * Ball handles storage and drawing for the player's ball object.
 */
public class Ball {
	private Point center;
	private int radius;
	
	//Velocity stored as change in x and y position per instance.
	private Point velocity;
	
	public Ball(Point c, int rad) {
		center = c;
		radius = rad;
		velocity = new Point(0,0);
	}
	
	public Point getCenter() {
		return center;
	}

	public void setCenter(Point center) {
		this.center = center;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public Point getVelocity() {
		return velocity;
	}

	public void setVelocity(Point velocity) {
		this.velocity = velocity;
	}
	
	public void draw(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawOval(center.x-radius, center.y-radius, (radius*2)-1, (radius*2)-1);
	}
}
