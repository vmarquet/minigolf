package fr.atis_lab.physicalworld;

import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.util.Scanner;
import java.io.*;
import org.jbox2d.common.*;
import sources.*;
import java.lang.Math;
import java.util.ArrayList;
import sources.Model;
import sources.HighScore;

/**
 * Custom JPanel to paint the PhysicalWorld and its PhysicalObject <br/>
 * The DrawingPanel is a "window" on the world (it can show only a part of it) <br/>
 * The scale determine the ratio between the simulated world and the painted world
 * @author A. Gademer
 * @version 12/2013
 */
public class DrawingPanel extends JPanel implements Serializable {
       
   private Model model;
   
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

      this.model = Model.getInstance();
    }
    
    public DrawingPanel(Dimension dimension, float scale) {
      this.scale = scale;
      this.backgroundColor = null;
      this.backgroundIcon = null;
      this.setPreferredSize(dimension);
      // The cameraPosition in the simulation referential
      this.cameraPosition = new Vec2(0,0);

      this.model = Model.getInstance();
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
     	  
     	  // Get the Graphics context from the image (different from the Graphics context from the JPanel)
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

        // HUD DRAWING
        if (model.gameMode == 2) {
        
           //ImageIcon ATH = new ImageIcon("./img/ATH2.png");
           int step = 140;  // space in pixels between player's HUD
           for (Player player : model.getPlayers()) {
              int i = player.getNumber();
              
              int leftCornerX = 50+i*step;
              int leftCornerY = 50;
              imageGraphics.setColor(player.getColor());
              imageGraphics.drawRect(leftCornerX,leftCornerY,115,38);
              //ATH.paintIcon(this, imageGraphics, leftCornerX, leftCornerY);
              
              // we draw some Strings: the name of the player, and his score
              //imageGraphics.setColor(Color.WHITE);
              imageGraphics.drawString(player.getName(), leftCornerX,leftCornerY);
              //imageGraphics.setColor(Color.WHITE);
              imageGraphics.drawString("Total Score", leftCornerX+5,leftCornerY+16);
              String totalScore = Integer.toString(player.getTotalScore());
              imageGraphics.drawString(totalScore, leftCornerX+90,leftCornerY+16);
              imageGraphics.drawString("Level Score", leftCornerX+5,leftCornerY+31);
              String levelScore = Integer.toString(player.getLevelScore());
              imageGraphics.drawString(levelScore, leftCornerX+90,leftCornerY+31);
              
           }

           // drawing of the power bar:
           imageGraphics.setColor(Color.BLUE);
           imageGraphics.drawRect(30, 197, 30, 204);

           float power = ((float)(model.currentPlayer.getPower()))/100f; // between 0 and 1
           float R, G, B=0;
           R = Math.min(power*2, 1);
           G = Math.min(2f-(power*2), 1);
           imageGraphics.setColor(new Color(R,G,B));
           imageGraphics.fillRect(33, 400-2*model.currentPlayer.getPower(), 26, 2*model.currentPlayer.getPower());
           
           // draw an icon to show whoose turn it is
           imageGraphics.setColor(Color.WHITE);
           int var = model.currentPlayer.number*step;
           // if we want the symbol to be a ball:
           //imageGraphics.drawOval(40+var, 64, 10,10);
           // if we want the symbol to be a triangle:
           imageGraphics.setColor(Color.RED);
           int[] pointsX = new int[3];
           pointsX[0] = 40 + var; pointsX[1] = 40 + var; pointsX[2] = 47 + var;
           int[] pointsY = new int[3];
           pointsY[0] = 62; pointsY[1] = 70; pointsY[2] = 66;
           imageGraphics.fillPolygon(pointsX, pointsY, 3);
           
           
           // drawing of the level par:
           imageGraphics.setColor(Color.WHITE);
           imageGraphics.drawString("Level Par :", 590, 25);
           String par = Integer.toString(world.getPar());
           imageGraphics.drawString(par, 660, 25);
        }
        
        // high scores
        if (model.gameMode == 3) {
           int step = 30;
           imageGraphics.setColor(Color.WHITE);
           imageGraphics.drawString("This game's player's high scores", 300, 100);
           //imageGraphics.drawString("(the closest to 0, the better it is)", 300, 100+step);
           for (int i=0; i<model.numberOfPlayer; i++) {
             imageGraphics.drawString( Integer.toString(i+1) + ". " + HighScore.localHighScoresNames[i] + 
             ", score = " + Integer.toString(HighScore.localHighScores[i]), 300, 100 + step*(i+2));
           }
           
           imageGraphics.setColor(Color.WHITE);
           imageGraphics.drawString("Global player's high scores", 700, 100);
           //imageGraphics.drawString("(the closest to 0, the better it is)", 300, 100+step);
           for (int i=0; i<10; i++) {
             imageGraphics.drawString( Integer.toString(i+1) + ". " + HighScore.globalHighScoresNames[i] + 
             ", score = " + Integer.toString(HighScore.globalHighScores[i]), 700, 100 + step*(i+2));
           }
           
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





