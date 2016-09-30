import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/*
 * Ball handles storage and drawing for the player's ball object.
 */
public class Ball {
	
	private Ellipse2D.Double circle;
	
	//Velocity stored as change in x and y position per instance.
	private Point2D velocity;
	private int radius;
	
	public Ball(Point center, int rad) {
		radius = rad;
		circle = new Ellipse2D.Double(center.getX()-radius, center.getY()-radius, radius*2, radius*2);
		velocity = new Point2D.Double(0,0);
	}
	
	public void move(double dX, double dY) {
		circle.setFrame(circle.getX()+dX, circle.getY()+dY, circle.getWidth(), circle.getHeight());
	}
	
	public void setCenter(double x, double y) {
		circle = new Ellipse2D.Double(x-radius, y-radius, radius*2, radius*2);
	}
	
	public Ellipse2D.Double getCircle() {
		return circle;
	}

	public int getRadius() {
		return radius;
	}

	public Point2D getVelocity() {
		return velocity;
	}

	public void setVelocity(Point2D velocity) {
		this.velocity = velocity;
	}
	
	public void setVelocity(double x, double y) {
		velocity.setLocation(x, y);
	}
	
	public void draw(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g.setColor(Color.gray);
		g2d.fill(circle);
	}
}
