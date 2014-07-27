package sources;

import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import org.jbox2d.collision.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.dynamics.contacts.*;
import org.jbox2d.callbacks.*;
import java.awt.*;  // for JPanel

public class Player
{
   public int number;  // start from 0, not 1
   private String name;
   private Color color;
   public Body ball;   // called "ballPlayerN"
   public boolean isBallSet;  // to make the ball appear when the player play for the first time in the hole
   public boolean isBallRolling;  // to now when to make next player play 
   public boolean hasFinishedHole;
   private Vec2 previousPos;
   private int totalScore;
   private int levelScore;
   private int angle;  // between 0 and 180
   private int power;  // between 10 and 100
   //private int rotation;
   //Panel reticule;
   
   
   public Player(int number, String name, Color color) {
      this.number = number;
      this.name = name;
      this.color = color;
      this.totalScore = 0;
      this.levelScore = 0;
      this.angle = 40;
      this.power = 50;
      previousPos = new Vec2(0,0);
      isBallRolling = true;
      hasFinishedHole = false;
      //reticule = new Panel();
   }

   // to reset flags and score between holes
   public void resetBetweenHoles() {
      this.hasFinishedHole = false;
      this.isBallRolling = false;
      this.isBallSet = false;
      this.resetLevelScore();
      this.resetPower();
      this.resetAngle();
   }
   
   private void resetAngle() {
      this.angle = 45;
   }
   public void increaseAngle() {
      if (this.angle < 180)
        this.angle += 5;
      if (this.angle > 180)
        this.angle = 180;
      return;
   }
   public void decreaseAngle() {
      if (this.angle > 0)
        this.angle -= 5;
      if (this.angle < 0)
        this.angle = 0;
      return;
   }
   public double getAngleRadian() {
      return (this.angle/180.0)*java.lang.Math.PI;
   }
   public void increasePower() {
      if (this.power < 100)
        this.power += 2;
      if (this.power > 100)
        this.power = 100;
      return;
   }
   public void decreasePower() {
      if (this.power > 10)
        this.power -= 2;
      if (this.power < 10)
        this.power = 10;
      return;
   }
   public int getPower() {
      return this.power;
   }
   private void resetPower() {
      this.power = 50;
   }
   public Vec2 getPreviousPos() {
      return this.previousPos;
   }
   public void setPreviousPos(Vec2 pos) {
      this.previousPos.x = pos.x;
      this.previousPos.y = pos.y;
      return;
   }
   public String getName() {
      return this.name;
   }
   public int getTotalScore() {
      return this.totalScore;
   }
   public void updateTotalScore(int diffWithPar){
      this.totalScore += diffWithPar;
   }
   public int getLevelScore() {
      return this.levelScore;
   }
   public void addShot() {
      this.levelScore += 1;
   }
   private void resetLevelScore() {
      this.levelScore = 0;
   }
   public Color getColor() {
      return this.color;
   }
   public int getNumber() {
      return this.number;
   }
}




