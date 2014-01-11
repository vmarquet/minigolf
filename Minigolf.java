// idées pour les niveaux: pour simuler de l'eau, mettre des petites boules bleu très légères.
// si la balle de golf tombe dedans, elle coule au fond et atteint un sensor 

// gestion de l'effet ?

import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import org.jbox2d.collision.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.dynamics.contacts.*;
import org.jbox2d.callbacks.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.Math.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import fr.atis_lab.physicalworld.*;
import sources.*;

// Compilation:
// javac -cp ./lib/*:. Minigolf.java
// java  -cp ./lib/*:. Minigolf

public class Minigolf implements KeyListener, ContactListener, Serializable {

    /* PhysicalWorld => contains the World and walls (as PhysicalObject) */
    private PhysicalWorld world;
    /* PhysicalObject => contains the Body, Shape & Fixture */
    transient private Body blob, pointer;  // blob is a temporary pointer
    LinkedList<Vec2> powerJaugeVariable;
    
    /* Custom Panel for drawing purpose */
    private DrawingPanel panel;
    private JFrame frame;
    
    private int gameMode;  // 1 = menu; 2 = playing; 3 = high scores;  this flag is set in this.menu() and this.showHighScores
    private int numberOfPlayer;
    private int numberOfPlayerMax;
    private boolean isNumberOfPlayerSet;
    private Body[] menuball;
    private Player[] player;
    private Player currentPlayer;  // pointer
    private Player previousPlayer; // pointer
    private int tourDeBoucle;
    private boolean hasEverybodyFinishedHole;  // updated in beginContact
    
    private boolean updatePowerJauge;

    public Minigolf() {
       this.numberOfPlayerMax = 6;
       this.tourDeBoucle = 0;
       this.panel = new DrawingPanel(new Dimension(1280,720), 10f);
       frame = new JFrame("Virtual Minigolf  [1280*720]");
       Image icon = new ImageIcon("./img/icon2.png").getImage();
       frame.setIconImage(icon);
       frame.setMinimumSize(this.panel.getPreferredSize());
       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       frame.setLayout(new BorderLayout());  // choisir un autre layout pour faire des choses plus précises
                                             // borderLayout ne permet que centre, haut, bas, gauche, droite
       this.menu();  // to show titlescreen and get the number of players
    }
    
    public static void main(String[] args) {
       System.out.println("Welcome to Virtual Minigolf !");
       Minigolf golf = new Minigolf();
       for(int i=1; i<2; i++)  // i<3
         golf.playHole(i);
       golf.showHighScores();
       System.out.println("Bye-bye !");
       System.exit(-1);
       //golf.menu();  // in order to do that, there are a few changes to make to menu()
    }
    
    public void menu() {
       gameMode = 1; panel.setGameMode(1);
       // we create a temporary world just to do the background of the titlescreen
       world = new PhysicalWorld(new Vec2(0,-9.81f), -64, 64, 0, 72, Color.WHITE);
       world.setContactListener(this);
       try {
          menuball = new Body[numberOfPlayerMax];
          for (int i=0; i<numberOfPlayerMax; i++) {
            menuball[i] = world.addCircularObject(0.5f, BodyType.STATIC, new Vec2(13+2*i, -10), 0, 
                          new Sprite("menuball"+Integer.toString(i), 1, Color.WHITE, null));
                          // -10 because by default we want to hide the ball for players 2 3 4
          }
          menuball[0].setTransform(new Vec2(13,32), 0);  // we show 1 ball because default is 1 player
       }
       catch (InvalidSpriteNameException e) {
          e.getMessage();
       }
       HoleGenerator hg = new HoleGenerator(1, world);
       this.panel.setPhysicalWorld(world);
       this.panel.setBackGroundColor(Color.BLACK);
       this.panel.setBackGroundIcon(new ImageIcon("./img/menu.png"));
       panel.setCameraPosition(new Vec2(0,36));
       this.frame.add(this.panel, BorderLayout.CENTER);
       this.frame.pack();
       this.frame.setVisible(true);
       this.panel.requestFocus();
       this.panel.addKeyListener(this);
       
       // we get the number of players
       isNumberOfPlayerSet = false;
       numberOfPlayer = 1;  panel.setNumberOfPlayer(numberOfPlayer); // default value
       
       try {
         float timeStep = 1/60.0f;  // 60 FPS
         int msSleep = Math.round(1000*timeStep); // timeStep in milliseconds
         world.setTimeStep(timeStep); // Set the timeStep of the PhysicalWorld
         
         while( ! isNumberOfPlayerSet ) {
            world.step(); // Move all objects
            //panel.setCameraPosition(ball.getPosition().add(new Vec2(0,20))); // The camera will follow the ball
            panel.setCameraPosition(new Vec2(0,36));
            Thread.sleep(msSleep); // Synchronize the simulation with real time
            this.panel.updateUI(); // Update graphical interface
          }
       } 
       catch(InterruptedException ex) {
           System.err.println(ex.getMessage());
       }
       
       // creation of an object Player for each player
       System.out.println("You choose " + Integer.toString(numberOfPlayer) + " player(s)");
       player = new Player[numberOfPlayer];
       for (int i=0; i<numberOfPlayer; i++) {
         // we display a popup to get the name of the player
         String num = Integer.toString(i+1);
         String name = JOptionPane.showInputDialog(null, 
         "Please choose a name for player " + num, "Player "+num, JOptionPane.INFORMATION_MESSAGE);
         // color of the ball
         Color color;
         switch(i) {
           case 0:
             color = Color.WHITE;
             break;
           case 1:
             color = Color.CYAN;
             break;
           case 2:
             color = Color.MAGENTA;
             break;
           case 3:
             color = Color.ORANGE;
             break;
           case 4:
             color = Color.PINK;
             break;
           case 5:
             color = Color.YELLOW;
             break;
           default:
             color = Color.WHITE;
             break;
         }
         if ( name == null || name.equals("") )
           player[i] = new Player(i, "Player " + Integer.toString(i+1), color);
         else
           player[i] = new Player(i, name, color);
         System.out.println("New player: " + player[i].getName());
         
       }
       panel.setPlayer(this.player);
       
       gameMode = 2; panel.setGameMode(2);
       return;
    }
       
    public void playHole(int n) {
       
       System.out.println("Playing hole number " + Integer.toString(n) );
       
       // we reset each player's flags for this hole
       for (int i=0; i<numberOfPlayer; i++) {
         player[i].hasFinishedHole = false;
         player[i].isBallRolling = false;
         player[i].isBallSet = false;
         player[i].resetLevelScore();
         player[i].resetPower();
         player[i].resetAngle();
       }
       hasEverybodyFinishedHole = false;
       int par = 0;  // to avoid the "variable might not have been initialized" due to the try catch
       
       // public PhysicalWorld(Vec2 gravity, float xmin, float xmax, float ymin, float ymax, Color borderColor)
       world = new PhysicalWorld(new Vec2(0,-9.81f), -64, 64, 0, 72, Color.WHITE); // 64 - (-64) = 128 = 1280/10
       world.setContactListener(this);  // ne pas confondre avec le addKeyListener
       this.panel.setBackGroundIcon(null);
       
       try {
         /* Allocation of the ball : radius of 3, position (0, 10), yellow, with an Image */
         /* PhysicalObject are automatically added to the PhysicalWorld */
         for (int i=0; i<numberOfPlayer; i++)
         {
           player[i].ball = world.addCircularObject(0.5f, BodyType.STATIC, 
                            new Vec2(0, -10), 0, new Sprite("ballPlayer"+Integer.toString(i), 1, player[i].getColor(), null));
                            // or new ImageIcon("./img/golf_ball.png"))) instead of null
           // STATIC -> to prevent the ball from falling when set to sensor  
           player[i].ball.getFixtureList().setSensor(true);  // to prevent from colliding with other player's balls
           player[i].ball.getFixtureList().setRestitution(0.2f);  // bouncing propertie
           player[i].ball.getFixtureList().setFriction(10000000000000f);  // friction propertie (adhérence)
           // on peut aussi fixer la densité: density
           player[i].ball.setAngularDamping(3); 
         }
         // we prepair player[0] to play
         currentPlayer = player[0];  panel.setCurrentPlayer(currentPlayer);
         currentPlayer.ball.setTransform(new Vec2(-50,8), 0);
         currentPlayer.ball.getFixtureList().setSensor(false);
         currentPlayer.ball.setType(BodyType.DYNAMIC);
         currentPlayer.isBallSet = true;
         
         // we read the .txt file which contain the configuration of the hole:
         HoleGenerator hg = new HoleGenerator(n, world);
         par = world.getPar();
         

         // affichage du pointeur de tir
         pointer = world.addCircularObject(0.2f, BodyType.STATIC, new Vec2(-50, 10), 0, 
                   new Sprite("pointer", 1, Color.RED, new ImageIcon("./img/pointer.png")));
         pointer.getFixtureList().setSensor(true);


       } catch (InvalidSpriteNameException ex) {
           ex.printStackTrace();
           System.exit(-1);
       }
       
       
       
       /* The DrawingPanel is a window on the world (with scale x10, the world is currently 1280*720) */
       /* The DrawingPanel panel is centered around the camera position (0,0) by default */
       //this.panel = new DrawingPanel(world, new Dimension(1280,720), 10f);  // 10f: scale
       this.panel.setPhysicalWorld(world);
       this.panel.setBackGroundColor(Color.BLACK);
       //this.panel.setBackGroundIcon(new ImageIcon("./holegenerator.png"));
       this.panel.addKeyListener(this);
       
       this.frame.add(this.panel, BorderLayout.CENTER); // Add DrawingPanel Panel to the frame
       this.frame.pack();
       this.frame.setVisible(true);
       this.panel.requestFocus();	  // must be last line
       
       /* POUR L'ATH: MARCHE A PEU PRES MAIS TRES MOCHE (BARRE GRISE EN HAUT)
       JLabel test1 = new JLabel("My text");
       frame.add(test1, BorderLayout.PAGE_START); */
       
       this.run();
       
       for (int i=0; i<numberOfPlayer; i++) {
          player[i].updateTotalScore( player[i].getLevelScore() - par );
       }
    }

    // Simulation loop
    public void run() {
    		//System.out.println("You may use the arrows of the keyboard !");
        try {
          float timeStep = 1/60.0f;  // 60 FPS
          int msSleep = Math.round(1000*timeStep); // timeStep in milliseconds
          world.setTimeStep(timeStep); // Set the timeStep of the PhysicalWorld
          
          
          /* Launch the simulation */
          while( !hasEverybodyFinishedHole ) {
             
             frame.setTitle("Virtual Minigolf  [1280*720]  -  " + world.getLevelName() );
             
             // sometimes, due to the physical engine, the ball can pass through walls if it go to quicky
             // if it happens, we putt the ball at the beginning again (but the shot count for the player as a penalty)
             if (currentPlayer.ball.getPosition().x < -65 || currentPlayer.ball.getPosition().x > 65
             || currentPlayer.ball.getPosition().y > 75 || currentPlayer.ball.getPosition().y < 1 ) {
                currentPlayer.isBallRolling = false;
                currentPlayer.isBallSet = false;
                this.nextPlayer();
             }
             //System.out.println( currentPlayer.ball.getPosition().y );
             
             // we actualize ball.previousPos once per second
             if (tourDeBoucle == 60) {
               double ecart_x = (double)(currentPlayer.ball.getPosition().x - currentPlayer.getPreviousPos().x);
               double ecart_y = (double)(currentPlayer.ball.getPosition().y - currentPlayer.getPreviousPos().y);
               double dist = java.lang.Math.sqrt((double)ecart_x*(double)ecart_x + ecart_y*ecart_y);
               
               // we check if the ball has stopped or nearly, and next player
               if (dist < 0.1 && currentPlayer.isBallRolling==true) {  // if the ball is nearly stopped
                  currentPlayer.ball.setLinearVelocity(new Vec2(0,0));  // we stop completely the ball
                  // we search for the next player, we skip those who already finished the hole
             	  	 this.nextPlayer();
             	  	 
               }
               currentPlayer.setPreviousPos(currentPlayer.ball.getPosition());  // don't change this line, else dist never < 0.1 !!
               tourDeBoucle = 0;
             }
             tourDeBoucle++;
          
             // we check if we have to draw the pointer which show the angle
             if ( currentPlayer.isBallRolling == false ) {
                Vec2 ball_pos = currentPlayer.ball.getPosition();
                double cos = java.lang.Math.cos((double)currentPlayer.getAngleRadian());
           	    double sin = java.lang.Math.sin((double)currentPlayer.getAngleRadian());
                Vec2 pointer_pos = ball_pos.add(new Vec2((int)(10*cos), (int)(10*sin)));
                pointer.setTransform(pointer_pos, 0);
             }
             else  // si ce n'est à aucun des joueurs de jouer (balle en mouvement)
                pointer.setTransform(new Vec2(0,-10), 0);  // we hide the pointer
                
                
            world.step(); // Move all objects
            //panel.setCameraPosition(ball.getPosition().add(new Vec2(0,20))); // The camera will follow the ball
            panel.setCameraPosition(new Vec2(0,36));
            Thread.sleep(msSleep); // Synchronize the simulation with real time
            this.panel.updateUI(); // Update graphical interface
          }
        } catch(InterruptedException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    public void showHighScores() {
       gameMode = 3; panel.setGameMode(3);
       panel.computeHighScore();
       this.panel.updateUI();
       
       boolean useless = true;
       while(useless) {  }
       
       
       gameMode = 1; panel.setGameMode(1); // if we want to return to the menu
    }
    
    
    /* Event when object are touching */
    public void beginContact(Contact contact) {
        int n = currentPlayer.number;
        //if current player's ball touch the ground of the hole:
        if ( ( Sprite.extractSprite(contact.getFixtureA().getBody()).getName().equals("ballPlayer"+Integer.toString(n)) &&
           Sprite.extractSprite(contact.getFixtureB().getBody()).getName().equals("holeSensor") ) ||
           ( Sprite.extractSprite(contact.getFixtureB().getBody()).getName().equals("ballPlayer"+Integer.toString(n)) &&
           Sprite.extractSprite(contact.getFixtureA().getBody()).getName().equals("holeSensor") ) ) {
              currentPlayer.hasFinishedHole = true;
              System.out.println(currentPlayer.getName() + " has finished");
              //this.nextPlayer();  // useless if it's the last player to finish the hole
              // WARNING: the if(dist<0.1 ...) code is not read, maybe it's why there is a bug 
        }
        
        // we check if all player have finished the hole
        // if yes, we change the flag:
        int m=0;
        for (int i=0; i<numberOfPlayer; i++) {
          if (player[i].hasFinishedHole == true)
            m++;
        }
        if (m==numberOfPlayer) {
          hasEverybodyFinishedHole = true;
          System.out.println("Everybody has finished this hole");
        }
        
        /*System.out.println("Objects are touching "+Sprite.extractSprite(contact.getFixtureA().getBody()).getName()
        +" "+Sprite.extractSprite(contact.getFixtureB().getBody()).getName() ); */
    }

    /* Event when object are leaving */
    public void endContact(Contact contact) {
        /*System.out.println("Objects are leaving "+Sprite.extractSprite(contact.getFixtureA().getBody()).getName() 
        +" "+Sprite.extractSprite(contact.getFixtureB().getBody()).getName() ); */
    }
    /* unused advanced stuff */
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    public void preSolve(Contact contact, Manifold oldManifold) {}
    
    
    public void keyPressed(KeyEvent e) {
       //System.out.println("keyPressed "+ e.getKeyCode());
       boolean ctrl_pressed = false;
       if ((KeyEvent.CTRL_MASK & e.getModifiers()) != 0)  // Detect the CTRL modifier
         ctrl_pressed = true;
       switch (e.getKeyCode()) {
         case KeyEvent.VK_Q:
         	  if(ctrl_pressed) { // If Q AND CTRL are both pressed
         	  	 System.out.println("Bye bye");
         	   	System.exit(-1);
         	 	}
            break;
         case KeyEvent.VK_ENTER:
            if (gameMode == 1)
              isNumberOfPlayerSet = true;
            break;
         case KeyEvent.VK_SPACE:
            if (gameMode == 2) {
           	  if( !currentPlayer.isBallRolling ) {
           	    // we apply the force to the ball
           	    double cos = java.lang.Math.cos((double)currentPlayer.getAngleRadian());
           	    double sin = java.lang.Math.sin((double)currentPlayer.getAngleRadian());
           	    int power = currentPlayer.getPower();
           	  	 currentPlayer.ball.applyForceToCenter(new Vec2((int)(3*power*cos),(int)(3*power*sin))); // en Newton
           	  	 currentPlayer.isBallRolling = true;
           	  	 System.out.println(currentPlayer.getName() + " shot");
           	  	 currentPlayer.addShot();
           	  }
         	  }
            break;
         case KeyEvent.VK_RIGHT:
            if (gameMode == 1) {
              if(numberOfPlayer < numberOfPlayerMax) {
                numberOfPlayer++;  panel.setNumberOfPlayer(numberOfPlayer);
                menuball[numberOfPlayer-1].setTransform(new Vec2(13+2*(numberOfPlayer-1),32), 0);
              }
            }
            if (gameMode == 2)
         	    currentPlayer.decreaseAngle();
            break;
         case KeyEvent.VK_LEFT:
            if (gameMode == 1) {
              if(numberOfPlayer > 1) {
                numberOfPlayer--;  panel.setNumberOfPlayer(numberOfPlayer);
                menuball[numberOfPlayer].setTransform(new Vec2(0,-10), 0);
              }
            }
            if (gameMode == 2)
         	    currentPlayer.increaseAngle();
            break;
         case KeyEvent.VK_DOWN:
            if (gameMode == 1) {
              if(numberOfPlayer > 1) {
                numberOfPlayer--;  panel.setNumberOfPlayer(numberOfPlayer);
                menuball[numberOfPlayer].setTransform(new Vec2(0,-10), 0);
              }
            }
            if (gameMode == 2) {
           	  currentPlayer.decreasePower();
           	}
            break;
         case KeyEvent.VK_UP:
            if (gameMode == 1) {
              if(numberOfPlayer < numberOfPlayerMax) {
                numberOfPlayer++;  panel.setNumberOfPlayer(numberOfPlayer);
                menuball[numberOfPlayer-1].setTransform(new Vec2(13+2*(numberOfPlayer-1),32), 0);
              }
            }
            if (gameMode == 2) {
           	  currentPlayer.increasePower();
           	}
            break;
       }
    }
    public void keyTyped(KeyEvent e) {
    }
    public void keyReleased(KeyEvent e) {
    }
    private void nextPlayer() {  // change the current player to the next who hasn't finished the hole
       // operations on the ball
       currentPlayer.ball.setType(BodyType.STATIC); // we set to static, else the ball will fall when we set it to sensor
       currentPlayer.ball.getFixtureList().setSensor(true); // we set to sensor to avoid collisions with other balls
       currentPlayer.isBallRolling = false;
       // we search for the next player
       int n = currentPlayer.number;
  	  	 do {
  	  	   if (n == numberOfPlayer-1)
  	  	     n = 0;
  	  	   else
  	  	     n++;
  	  	   // to avoid infinite loop:
  	  	   if ( hasEverybodyFinishedHole == true )
  	  	     return;
  	  	 } while ( player[n].hasFinishedHole == true );  // don't put currentPlayer instead of player[n] !!!
  	  	 // here, we can actualize current player
  	  	 currentPlayer = player[n];  panel.setCurrentPlayer(currentPlayer);
  	  	 // we prepair him to play 
	  	   currentPlayer.ball.getFixtureList().setSensor(false);
	  	   currentPlayer.ball.setType(BodyType.DYNAMIC);
  	  	 System.out.println("Now it's the turn of " + currentPlayer.getName() );
  	  	 if ( ! currentPlayer.isBallSet ) { // if it's the player first shot, we put the ball at the beginning of the hole
  	  	   currentPlayer.ball.setTransform(new Vec2(-50,8), 0);
  	  	   currentPlayer.isBallSet = true;
  	  	 }
  	  	 currentPlayer.setPreviousPos(currentPlayer.ball.getPosition());  // new player, so we must actualize coord of the ball
    }
}

