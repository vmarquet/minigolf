package fr.atis_lab.physicalworld;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.io.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import org.jbox2d.collision.*;
import org.jbox2d.collision.shapes.*;
import org.box2d.proto.Box2D.*;

/**
 * Sprite is their to allow painting capacities and unique name to JBox2D Bodies
 * @author A. Gademer
 * @version 12/2013
 */
public class Sprite implements Comparable<Sprite>, Serializable {

    private String name;
    private Color color;
    private ImageIcon icon;
    private int layerIndex;
    private transient Body body = null; // This link is transient, i.e. not saved through serialization (you must recall linkToBody method after deserialization)

	/**
	 * Create a new Sprite
	 * @param name the Sprite name (must be unique in the PhysicalWorld)
	 * @param layerIndex the layerIndex allow to sort PhysicalObject from front (higher values) to bottom (lower values)
	 * @param color the color of the geometrical shape drawn (null if you want an invisible object)
	 * @param icon an image to represent the object (null if no image)
	 */
	public Sprite(String name, int layerIndex, Color color, ImageIcon icon) {
		this.name = name;
		this.layerIndex = layerIndex;
		this.color = color;
		this.icon = icon;
	}

/**
	 * Accessor to get the name of the Sprite
	 * @return the name of the Sprite
	 */
    public String getName() {
        return name;
    }

	/**
	 * Accessor to get the Color of the Sprite
	 * @return the color of the Sprite
	 */
    public Color getColor() {
        return color;
    }
    
    /**
	 * Accessor to set the Color of the Sprite
	 * @param color the new color
	 */
    public void setColor(Color color) {
        this.color = color;
    }
    
    
    /**
	 * Accessor to set the ImageIcon of the Sprite
	 * @param icon the new ImageIcon
	 */
    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }
    
    /**
	 * Accessor to get the ImageIcon of the Sprite
	 * @return the ImageIcon of the Sprite
	 */
    public ImageIcon getImageIcon() {
        return icon;
    }
    
    /**
     * Return a string showing the Sprite parameters
     * @return a string showing the Sprite parameters
     */
    @Override
    public String toString() {
     return "["+this.name+" "+this.color+" "+this.icon+"]";
    }
    
    /**
     * Set the link to the associated Body <br/>
     * (this link is not saved by serialization)
     * @param body the Body to be associated with
     */
    public void linkToBody(Body body) {
    	 this.body = body;
    }

	/**
	 * Paint the Sprite in the given Graphics context. <br/>
	 * @param g the Graphics context where to draw
	 * @param panel the containing DrawingPanel (for the scale purpose)
	 */
    public void paint(Graphics g, DrawingPanel panel) {
   	   Fixture fixture;
       Shape shape;
       Point p1 = new Point();
       Point p2 = new Point();
       int x, y, xOff, yOff, radius;
       float scaleX, scaleY;

	       if(body == null) {
	       	return;
	       }
        Transform t = body.getTransform();
        fixture = body.getFixtureList();
        while(fixture != null) {
        shape = fixture.getShape();

	       // Change the drawing method depending of the shape of the Sprite
        switch(shape.getType()) {
        default:
            break;
        // Circular Sprite
        case CIRCLE :

            p1.setLocation(panel.convert4draw(body.getPosition())); // Center of the circle in the panel orientation
            radius = panel.toScale(shape.getRadius());

		  // Geometric drawing
            if(color != null) {
                g.setColor(this.color);
                g.drawOval(p1.x-radius, p1.y-radius, 2*radius, 2*radius);
            }

		  // Image drawing
            if(icon != null) {
                scaleX = (2.0f*radius) / icon.getIconWidth(); // Diameter of the circle to iconHeight ratio
                scaleY = (2.0f*radius) / icon.getIconHeight(); // Diameter of the circle to iconHeight ratio
                rotatedPaint(g, icon, p1.x, p1.y, scaleX, scaleY, -icon.getIconWidth()/2, -icon.getIconHeight()/2, -body.getAngle());
            }

            break;
            
        // Rectangular or Polygonal Sprite
        case POLYGON :
		          // Geometric drawing
            if(color != null) {

                g.setColor(this.color);

			             // Draw the shape
                PolygonShape pshape = ((PolygonShape)shape);
                for (int i = 0; i < pshape.getVertexCount() - 1; i++) {
                    p1.setLocation(panel.convert4draw(Transform.mul(t, pshape.getVertex(i))));
                    p2.setLocation(panel.convert4draw(Transform.mul(t, pshape.getVertex(i+1))));
                    g.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
                p1.setLocation(panel.convert4draw(Transform.mul(t, pshape.getVertex(0))));
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }

		          // Image drawing
            if(icon != null) {

			             // Compute the bounding box of the shape and fit the image to the bounding box
                AABB aabb = new AABB();
                shape.computeAABB(aabb, new Transform(), 0);
                p1.setLocation(panel.convert4draw(Transform.mul(t, aabb.lowerBound)));
                scaleX = ((float)panel.toScale((aabb.upperBound.x - aabb.lowerBound.x))) / icon.getIconWidth();
                scaleY = ((float)panel.toScale((aabb.upperBound.y - aabb.lowerBound.y))) / icon.getIconHeight();
                rotatedPaint(g, icon, p1.x, p1.y, scaleX, scaleY, 0, -icon.getIconHeight(), -body.getAngle());
            }
            break;
          }
        	fixture = fixture.getNext();
        }
        
        // TEST: pour l'ATH -> c'est le serpent qui se mord la queue:
        // utiliser imageicon -> shape -> fixture -> body -> world -> AUTANT CREER LE TRUC DANS LA SIMU PHYSIQUE
        /*ImageIcon iconTest = new ImageIcon("./img/emosmile.png");
        AABB aabb = new AABB();
        Body bodyTest;
        fixture = bodyTest.getFixtureList();
        shape = fixture.getShape();
        shape.computeAABB(aabb, new Transform(), 0);
        p1.setLocation(panel.convert4draw(Transform.mul(t, aabb.lowerBound)));
        scaleX = ((float)panel.toScale((aabb.upperBound.x - aabb.lowerBound.x))) / iconTest.getIconWidth();
        scaleY = ((float)panel.toScale((aabb.upperBound.y - aabb.lowerBound.y))) / iconTest.getIconHeight();
        rotatedPaint(g, iconTest, p1.x, p1.y, scaleX, scaleY, 0, -icon.getIconHeight(), -body.getAngle());*/
        
        // AUTRE METHODE: voir doc java sur la classe Graphics 
        //drawImage(Image img, int x, int y, ImageObserver observer)
        /*
        BufferedImage img = new BufferedImage();
        img = ImageIO.read("./emosmile.png");
        ImageObserver observer = new ImageObserver(); 
        g.drawImage(img, 50, 50, observer);
        */
    }
    
    /**
     * Mystic function to rotate, translate and scale the ImageIcon before painting
     *
     * @param g the Graphics context where to draw
     * @param icon the Icon to be painted
     * @param x the x-coordinate for translation
     * @param y the y-coordinate for translation
     * @param scaleX the horizontal scale factor
     * @param scaleY the vertical scale factor
     * @param offX x-coordinate of the offset to the rotation center
     * @param offY y-coordinate of the offset to the rotation center
     * @param angle the rotation angle
     */
    public static void rotatedPaint(Graphics g, Icon icon, int x, int y, float scaleX, float scaleY, int offX, int offY, float angle) {

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        AffineTransform original = g2.getTransform();
        AffineTransform at = new AffineTransform();
        at.concatenate(original);
        at.translate(x,y); // Translate the Graphics2D topleft corner to the reference point
        at.rotate(angle); // Rotate
        at.scale(scaleX, scaleY); // Scale
        g2.setTransform(at);
        icon.paintIcon(null, g2, offX, offY); // Paint Icon relative the reference point
        //DEBUG : Print a crossed rectangle corresponding to the transform
        /*g2.setColor(Color.RED);
        g2.drawRect(0,0, 400, 600);
        g2.drawLine(0, 0, 400, 600);
        g2.drawLine(0, 600, 400, 0);*/
        g2.setTransform(original);
        //DEBUG : Print a crossed rectangle corresponding to the orignal position
        /*g2.setColor(Color.GREEN);
         g2.drawLine(0, 0, 400, 600);
         g2.drawLine(0, 600, 400, 0);*/
    }
    
    /**
     * Compare the layerIndex between this Sprite and another Sprite. <br/>
     * We sort the Sprite by their layer higher layerIndex means in front of other Sprite <br/>
     * Implementation of the Comparable<Sprite> interface
     * @param other the other Sprite
     * @return -1 if the current object is in front of the other, 1 if the current object is behind the other, 0 is they are at the same level.
     */
    public int compareTo(Sprite other) {
    	if(this.layerIndex==other.layerIndex) 
    		return 0;
    	if(this.layerIndex<other.layerIndex)
    		return -1;
    	else
    		return 1;
    }
    
    /**
     * Static method to extract the Sprite from a Body instance <br/>
     * It is a static method of Sprite because we cannot modify the Body class
     * @param body the targetted Body
     * @return the associated Sprite
     */
    public static Sprite extractSprite(Body body) {
    		return (Sprite)body.getUserData();
    }
}
