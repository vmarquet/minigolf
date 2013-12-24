package fr.atis_lab.physicalworld;

import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.io.*;
import org.jbox2d.common.*;

/**
 * Custom JPanel to paint the PhysicalWorld and its PhysicalObject <br/>
 * The DrawingPanel is a "window" on the world (it can show only a part of it) <br/>
 * The scale determine the ratio between the simulated world and the painted world
 * @author A. Gademer
 * @version 12/2013
 */
public class DrawingPanel extends JPanel implements Serializable {

       private float scale;
	      private PhysicalWorld world;
	      private Color backgroundColor;
   	   private ImageIcon backgroundIcon;
   	   private Vec2 cameraPosition;
	   
	   /**
	    * Create a new DrawingPanel with a given dimension and scale
	    * @param world the PhysicalWorld to paint
	    * @param dimension the dimension of the "window" <br/> (dimension of the world in pixel is given by the scale)
	    * @param scale the scale ratio between the simulation coordinate and the pixel size <br/> (with scale 10f, a circle with a 3f radius will be printed with a 30 pixels radius)
	    */
        public DrawingPanel(PhysicalWorld world, Dimension dimension, float scale) {
          this.world = world;
		        this.scale = scale;
		        this.backgroundColor = null;
		        this.backgroundIcon = null;
		        this.setPreferredSize(dimension);
		        // The cameraPosition in the simulation referential
		        this.cameraPosition = new Vec2(0,0);
        }
        
        public DrawingPanel(Dimension dimension, float scale) {
          this.scale = scale;
		        this.backgroundColor = null;
		        this.backgroundIcon = null;
		        this.setPreferredSize(dimension);
		        // The cameraPosition in the simulation referential
		        this.cameraPosition = new Vec2(0,0);
        }
        
        public void setPhysicalWorld(PhysicalWorld world) {
          this.world = world;
        } 
       
        
        
        /**
         * Set the camera position (in the simulation referential)
         * @param cameraPosition the cameraPosition (in the simulation referential)
         */
        public void setCameraPosition(Vec2 cameraPosition) {
        	this.cameraPosition.set(cameraPosition);
        }
        
        /**
         * Set the background color
         * @param backgroundColor the new Color for the background
         */
        public void setBackGroundColor(Color backgroundColor) {
        	this.backgroundColor = backgroundColor;
        }
        
        /**
         * Set the background image
         * @param backgroundIcon the new ImageIcon for the background
         */
        public void setBackGroundIcon(ImageIcon backgroundIcon) {
        	this.backgroundIcon = backgroundIcon;
        }
        
        /**
         * Convert a simulation's size into pixel'size
         * @param value the value in the simulation
         * @return the value in pixel
         */
        public int toScale(float value) {
        	return Math.round(value *scale);
        }
        
        /**
         * Convert simulation coordinate (Origin centered, Positive ordinate up) into JPanel coordinate (Top-left origin, Positive ordinate down)
         * @param v a Vec2 vector coordinate in simulation referential
         * @return a Point vector in JPanel referential
         */
        public Point convert4draw(Vec2 v) { // Change orientation of the referentiel and put to scale
        	return  new Point(toScale(v.x - world.getXMin()), toScale(world.getYMax() - (v.y)));
        }

	   /**
	    * Drawing method, overrided from JPanel
	    * @param g the Graphics context
	    */
        @Override
        public void paint(Graphics g) {
        
         	  /* Painting the whole world in the buffer image */
         	  
          	 // The buffer is an image containing the painting of the whole world
		          // The painting of the DrawingPanel will be a crop of this image, centered around the camera
		          BufferedImage buffer = new BufferedImage(toScale(world.getWidth()), toScale(world.getHeight()), BufferedImage.TYPE_INT_RGB);
         	  
         	  // Get the Graphics context from the image (different from the Graphics context from the JPanel
         	  Graphics imageGraphics = buffer.getGraphics();
         	  // Clear the image
            imageGraphics.clearRect(0, 0, buffer.getWidth(), buffer.getHeight());
            // Fill background with color
            if(backgroundColor!=null) {
            	imageGraphics.setColor(backgroundColor);
            	imageGraphics.fillRect(0,0, buffer.getWidth(), buffer.getHeight());
            }
            // Paint the background image (the image is scaled to fit the PhysicalWorld dimension)
            if(backgroundIcon != null) {
            	Sprite.rotatedPaint(imageGraphics, backgroundIcon, 0, 0 , ((float)buffer.getWidth())/backgroundIcon.getIconWidth(), ((float)buffer.getHeight())/backgroundIcon.getIconHeight(), 0, 0, 0);
            }
            // If the DrawingPanel is linked to a PhysicalWorld
            if(world != null) {
            	 world.paint(imageGraphics, this);
            	 // world.paint appelle Sprite.paint() sur tous les sprites du tableau de sprites
            }
            
            /* Painting the JPanel as a crop from the buffer image */
            
            // Clear the JPanel
            g.clearRect(0, 0, getWidth(), getHeight());
            // Get the camera's coordinate in JPanel referential
            Point cam = convert4draw(cameraPosition);
            // Center the JPanel on the camera and print the buffer image in the JPanel
            g.drawImage(buffer, this.getWidth()/2 - cam.x, this.getHeight()/2 -cam.y , null);
        }
    }
