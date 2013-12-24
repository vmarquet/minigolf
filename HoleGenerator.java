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

public class HoleGenerator
{
   
   public HoleGenerator(int holeNumber, PhysicalWorld world) {
      try {
        FileReader file = new FileReader("./holes/hole"+Integer.toString(holeNumber)+".txt");
        Scanner in = new Scanner(file);
        Body pointer;
        
        try {
           while(in.hasNextLine())
           {
              LinkedList<Vec2> ground = new LinkedList<Vec2>();
              String line = in.nextLine();
              String[] word = line.split("#");
              //System.out.println(word[0] + " SPLIT " + word[1]);
              if (word.length != 2)
                invalidFileExit();
              String[] vect = word[0].split(" ");
              int n = vect.length;
              for (int i=0; i<n-2; i++) {
                //System.out.print(vect[i] + " SPLIT ");
                String[] coord = vect[i].split(",");
                ground.add(new Vec2(Integer.parseInt(coord[0]), Integer.parseInt(coord[1])));  // IL FAUT LES CONVERTIR EN ENTIER
              }
              try {
                switch(vect[n-1])
                {
                   case "GREEN":
                     pointer = world.addPolygonalObject(ground, BodyType.STATIC, new Vec2(0, 0), 0, 
                               new Sprite(vect[n-2], 0, Color.GREEN, null)); break;
                   case "RED":
                     pointer = world.addPolygonalObject(ground, BodyType.STATIC, new Vec2(0, 0), 0, 
                               new Sprite(vect[n-2], 0, Color.RED, null)); break;
                   case "BLUE":
                     pointer = world.addPolygonalObject(ground, BodyType.STATIC, new Vec2(0, 0), 0, 
                               new Sprite(vect[n-2], 0, Color.BLUE, null)); break;
                   case "YELLOW":
                     pointer = world.addPolygonalObject(ground, BodyType.STATIC, new Vec2(0, 0), 0, 
                               new Sprite(vect[n-2], 0, Color.YELLOW, null)); break;
                   default:
                     pointer = world.addPolygonalObject(ground, BodyType.STATIC, new Vec2(0, 0), 0, 
                               new Sprite(vect[n-2], 0, null, null)); break;
                 }
                 if (vect[n-2].equals("flag") || vect[n-2].equals("flagStake") || vect[n-2].equals("holeSensor") )
                   pointer.getFixtureList().setSensor(true);
              }
              catch(InvalidPolygonException ex) {
                System.err.println(ex.getMessage());
                System.exit(-1);
              }
              
           }
        }
        catch (InvalidSpriteNameException ex) {
           ex.printStackTrace();
           System.exit(-1);
        }
        
        
        
        in.close();
      }
      catch (FileNotFoundException ex) {
        System.out.println(ex.getMessage());
        System.exit(-1);
      }
      
   }
   public static void main(String[] args) {  // juste pour visualiser le terrain (fournir le numéro en console)
      Scanner scan = new Scanner(System.in);
      System.out.print("Enter the number of the ground you wanna see: ");
      // ATTENTION: si l'utilisateur ne rentre pas un nombre, problème
      int n = scan.nextInt();
      PhysicalWorld world = new PhysicalWorld(new Vec2(0,-9.81f), -64, 64, 0, 72, Color.WHITE);
      new HoleGenerator(n, world);
      // gestion de l'affichage du terrain
      DrawingPanel panel;
      panel = new DrawingPanel(world, new Dimension(1280,720), 10f);  // 10f: scale
      panel.setBackGroundColor(Color.BLACK);
      panel.setBackGroundIcon(new ImageIcon("./img/holegenerator.png"));
      JFrame frame = new JFrame("Virtual Minigolf  [1280*720]");
      Image icon = new ImageIcon("./img/icon2.png").getImage();
      frame.setIconImage(icon);
      frame.setMinimumSize(panel.getPreferredSize());
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setLayout(new BorderLayout());
      frame.add(panel, BorderLayout.CENTER);
      frame.pack();
      frame.setVisible(true);
      panel.requestFocus();
      panel.setCameraPosition(new Vec2(0,36));
      return;
   }
   private void invalidFileExit() {
      System.out.println("ERROR: hole.txt file corrupted, impossible to construct the ground");
      System.exit(-1);
   }
   
}
