import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/*
 * Ball handles storage and drawing for the player's ball object.
 */
public class Ball {
	
	private Ellipse2D.Double circle;
	
	//Velocity stored as change in x and y position per instance.
	private Point2D velocity;
	
	public Ball(Point center, int radius) {
		circle = new Ellipse2D.Double(center.getX()-radius, center.getY()-radius, radius*2, radius*2);
		velocity = new Point2D.Double(.7,0.3);
	}
	
	public void move(double dX, double dY) {
		circle.setFrame(circle.getX()+dX, circle.getY()+dY, circle.getWidth(), circle.getHeight());
	}
	
	public Ellipse2D.Double getCircle() {
		return circle;
	}

	public int getRadius() {
		return (int)circle.getHeight()/2;
	}

	public Point2D getVelocity() {
		return velocity;
	}

	public void setVelocity(Point2D velocity) {
		this.velocity = velocity;
	}
	
	public void draw(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.draw(circle);
		//g2d.draw(new Rectangle2D.Double(circle.getX(),circle.getY(),circle.getWidth(),circle.getHeight()));
	}
}
