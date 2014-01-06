package fr.atis_lab.physicalworld;

import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.callbacks.*;
import org.jbox2d.serialization.*;
import org.jbox2d.serialization.pb.*;
import org.box2d.proto.Box2D.*;
import java.awt.Color;
import java.awt.Graphics;
import java.util.*;
import java.awt.image.*;
import javax.swing.*;
import java.io.*;
import java.text.*;

/**
 * PhysicalWorld is a wrapper around JBox2D World, adding some walls and a PhysicalObject list <br/>
 * PhysicalWorld use org.jbox2d.common.Vec2 for its coordinate
 * @author A. Gademer
 * @version 12/2013
 */
public class PhysicalWorld implements Serializable, JbSerializer.ObjectSigner, JbDeserializer.ObjectListener {

    private transient World jBox2DWorld;
    private Vec2 axisMin;
    private Vec2 axisMax;
    private float timeStep = 1.0f / 60.0f;
    private int velocityIterations = 8;
    private int positionIterations = 3;
    private float time;
    private long stepIdx;
    private ImageIcon wallIcon;
    private LinkedList<Sprite> spriteList;
    private int par;
    private String levelName;
    /**
     * Create a world from gravity, dimensions and wall colors
     * @param gravity Gravity vector
     * @param xmin minimum coordinate for x axis
     * @param xmax maximum coordinate for x axis
     * @param ymin minimum coordinate for y axis
     * @param ymax maximum coordinate for y axis
     * @param borderColor color of the border, null if invisible border
     */
    public PhysicalWorld(Vec2 gravity, float xmin, float xmax, float ymin, float ymax, Color borderColor) {

        spriteList = new LinkedList<Sprite>();
        this.jBox2DWorld = new World(gravity);
        this.axisMin = new Vec2(xmin, ymin);
        this.axisMax = new Vec2(xmax, ymax);
        this.time = 0;
        this.stepIdx = 0;
        if(borderColor != null) { // Create an 1x1 icon filled with one color
            BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            Graphics g = img.getGraphics();
            g.setColor(borderColor);
            g.fillRect(0, 0, img.getWidth(), img.getHeight());
            wallIcon = new ImageIcon(img);
        } else {
            wallIcon = null;
        }

	   try {
        //Bottom wall -> si on veut être plus haut que le bas de la fenêtre, mettre             axisMin.y +7
        addRectangularObject(getWidth(), 0.1f, BodyType.STATIC, new Vec2(axisMin.x+getWidth()/2, axisMin.y), 0, new Sprite("bottomWall", -1, null, wallIcon));
        //Top wall
        addRectangularObject(getWidth(), -0.1f, BodyType.STATIC, new Vec2(axisMin.x+getWidth()/2, axisMax.y), 0, new Sprite("topWall", -1, null, wallIcon));
        //Left wall
        addRectangularObject(0.1f, getHeight(), BodyType.STATIC, new Vec2(axisMin.x, axisMin.y+getHeight()/2), 0, new Sprite("leftWall", -1, null, wallIcon));
        //Right wall
        addRectangularObject(-0.1f, getHeight(), BodyType.STATIC, new Vec2(axisMax.x, axisMin.y+getHeight()/2), 0, new Sprite("rightWall", -1, null, wallIcon));
        } catch (InvalidSpriteNameException ex) {
        	ex.printStackTrace();
        	System.exit(-1);
        }

    }

    /**
     * Get the current time spent from the creation of the PhysicalWorld
     * @return the time spent in the simulation
     */
    public float getTime() {
        return time;
    }

    /**
     * Get the step index from the creation of the PhysicalWorld
     * @return the step index
     */    
    public long getStepIdx() {
    	   return stepIdx;
    }

    /**
    * Get an object matching a given name
    * @param name a given String to find
    * @return the first PhysicalObject corresponding to the name or null if none
    * @throws ObjectNameNotFoundException when the object name cannot be found
    */
    public Body getObject(String name) throws ObjectNameNotFoundException {
        Body body = jBox2DWorld.getBodyList();
        // Affectation of the Body to the Collection
        while(body != null) {
            if(Sprite.extractSprite(body).getName().equals(name)) {
                return body;
            }
            body = body.getNext();
        }
        throw new ObjectNameNotFoundException(name+" was not a correct object name !");
    }

    /**
    * Accessor to the JBox2D World
    * @return the internal JBox2D World
    */
    public World getJBox2DWorld() {
        return jBox2DWorld;
    }

    /**
    	* Accessor to the x-axis minimum coordinate
    	* @return the x-axis minimum coordinate
    	*/
    public float getXMin() {
        return axisMin.x;
    }
    /**
    * Accessor to the y-axis minimum coordinate
    * @return the y-axis minimum coordinate
    */
    public float getYMin() {
        return axisMin.y;
    }

    /**
    * Accessor to the x-axis maximum coordinate
    * @return the x-axis maximum coordinate
    */
    public float getXMax() {
        return axisMax.x;
    }
    /**
    * Accessor to the y-axis maximum coordinate
    * @return the y-axis maximum coordinate
    */
    public float getYMax() {
        return axisMax.y;
    }

    /**
    * Accessor to the width of the PhysicalWorld
    * @return the width of the PhysicalWorld
    */
    public float getWidth() {
        return axisMax.x - axisMin.x;
    }

    /**
    	* Accessor to the height of the PhysicalWorld
    	* @return the height of the PhysicalWorld
    	*/
    public float getHeight() {
        return axisMax.y - axisMin.y;
    }

    /**
     * Advance the physical simulation from a time step
     */
    public void step() {
        jBox2DWorld.step(timeStep, velocityIterations, positionIterations);
        time+=timeStep;
        stepIdx++;
    }

    /**
    * Set the ContactListener of the World
    * @param listener the new ContactListener
    */
    public void setContactListener(ContactListener listener) {
        jBox2DWorld.setContactListener(listener);
    }

    /**
    	* Is the world currently locked (during simulation process)
    	* @return true if the world is currently locked
    	*/
    public boolean isLocked() {
        return jBox2DWorld.isLocked();
    }

    /**
     * Fix the time step of the simulation
     * @param timeStep the new time step
     */
    public void setTimeStep(float timeStep) {
        this.timeStep = timeStep;
    }

	/**
	 * Return a String showing the state of the PhysicalWorld
	 * @return the currentTime then the object's list
	 */
    public String toString() {
	    DecimalFormat df = new DecimalFormat("#.##");
		StringBuffer buf = new StringBuffer();
		buf.append("Time : ").append(df.format(time)).append(" s\n");
		 Body body = jBox2DWorld.getBodyList();
        // Affectation of the Body to the Collection
        while(body != null) {
            buf.append("- ").append(Sprite.extractSprite(body).getName());
            buf.append(" (").append(df.format(body.getPosition().x)).append(", ").append(df.format(body.getPosition().y)).append(")");
            buf.append(" ").append(df.format(Math.toDegrees(body.getAngle()))).append("°");
            buf.append("\n");
            body = body.getNext();
        }
        return buf.toString();
    }

	/**
	 * Paint all objects in the PhysicalWorld
	 * @param g the Graphics context
	 * @param panel the DrawingPanel panel (for scale, etc.)
	 */
    public void paint(Graphics g, DrawingPanel panel) {
        Collections.sort(spriteList);
        for(Sprite s:spriteList) {
        	s.paint(g, panel);
        }
    }

    /**
     * Add a circular object to the PhysicalWorld
     * @param radius the radius of the circular shape
     * @param type determine if the object is mobile (BodyType.DYNAMIC) or static (BodyType.STATIC)
     * @param position the coordinate of the PhysicalObject
     * @param orientation the orientation angle (in radians) of the PhysicalObject
     * @param sprite the associated Sprite
     * @throws InvalidSpriteNameException if the name was already used
     */
    public Body addCircularObject(float radius, BodyType type, Vec2 position, float orientation, Sprite sprite) throws InvalidSpriteNameException{
        CircleShape circ = new CircleShape();
        circ.setRadius(radius);
        return createObject(circ, type, position, orientation, sprite);
    }

    /**
     * Add a rectangular object to the PhysicalWorld
     * @param width the width of the rectangular shape
     * @param height the height of the rectangular shape
     * @param type determine if the object is mobile (BodyType.DYNAMIC) or static (BodyType.STATIC)
     * @param position the coordinate of the PhysicalObject
     * @param orientation the orientation angle (in radians) of the PhysicalObject
     * @param sprite the associated Sprite
     * @throws InvalidSpriteNameException if the name was already used
     */
    public Body addRectangularObject(float width, float height, BodyType type, Vec2 position, float orientation, Sprite sprite) throws InvalidSpriteNameException {
        PolygonShape poly = new PolygonShape();
        // The rectangle center is at the crossing of diagonals
        poly.setAsBox(width/2, height/2, new Vec2(0,0), 0);
        return createObject(poly, type, position, orientation, sprite);
    }

    /**
     * Add a polygonal object to the PhysicalWorld
     * @param vertices a LinkedList of coordinates in COUNTERCLOCKWISE order.
     * @param type determine if the object is mobile (BodyType.DYNAMIC) or static (BodyType.STATIC)
     * @param position the coordinate of the PhysicalObject
     * @param orientation the orientation angle (in radians) of the PhysicalObject
     * @param sprite the associated Sprite
     * @throws InvalidPolygonException if the vertices list is too short (<3) or too long
     * @throws InvalidSpriteNameException if the name was already used
     */
    public Body addPolygonalObject(LinkedList<Vec2> vertices, BodyType type, Vec2 position, float orientation, Sprite sprite) throws InvalidPolygonException, InvalidSpriteNameException {
        if(vertices.size() < 3) {
            throw new InvalidPolygonException("Not enough points");
        }
        if(vertices.size() > Settings.maxPolygonVertices) {
            throw new InvalidPolygonException("Too much points (max : "+Settings.maxPolygonVertices+")");
        }
        PolygonShape poly = new PolygonShape();
        poly.set(vertices.toArray(new Vec2[0]), vertices.size());
        return createObject(poly, type, position, orientation, sprite);
    }

    /**
     * Internal initialisation of the object, common to all three previous methods
     */
    private Body createObject(Shape shape, BodyType type, Vec2 position, float orientation, Sprite sprite) throws InvalidSpriteNameException {
    	   for(Sprite s:spriteList) {
    	   	if(s.getName().equals(sprite.getName())) {
    	   		throw new InvalidSpriteNameException(sprite.getName()+" already used.");
    	   	}
    	   }
        Body body = null;
        FixtureDef fixDef = new FixtureDef();
        fixDef.shape = shape;
        fixDef.density = 0.1f;
        fixDef.isSensor = false;
        fixDef.restitution = 0.1f;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = type;

        bodyDef.angularDamping = 0.1f;
        bodyDef.linearDamping = 0.1f;

        bodyDef.fixedRotation = false;
        bodyDef.gravityScale = 1f;

        bodyDef.linearVelocity = new Vec2(0,0);
        bodyDef.angularVelocity = 0;
        bodyDef.position = new Vec2(position);
        bodyDef.angle = orientation;
        bodyDef.allowSleep = true;
        spriteList.add(sprite); // Save the sprite to the list (sprites must be serialiazed in the PhysicalWorld)
        bodyDef.userData = sprite; // Link the body and the sprite

        do {
            body = jBox2DWorld.createBody(bodyDef);
        } while(body== null); // Wait until the object is really created
        sprite.linkToBody(body); // Link the body to the sprite (this link is not serialiazed)
        body.createFixture(fixDef);
        return body;
    }
    
   	/**
     * Remove the body corresponding to the name from the World and destroy it
     * @param body the body to destroy
     * @throws ObjectNameNotFoundException if the name does not correspond to an object
     * @throws LockedWorldException if you try to destroy the object during simulation step
     */
    public void destroyObject(String name) throws ObjectNameNotFoundException,LockedWorldException {
		destroyObject(getObject(name));
    }
    
	/**
     * Remove the given body from the World and destroy it
     * @param body the body to destroy
     * @throws LockedWorldException if you try to destroy the object during simulation step
     */
    public void destroyObject(Body body) throws LockedWorldException {
    		if(jBox2DWorld.isLocked()) {
    			throw new LockedWorldException("The world is currently locked, you cannot destroyObject during collission callbacks");
    		}
		// Remove the Sprite from the list
		spriteList.remove(Sprite.extractSprite(body));
		// Destroy the Body
		jBox2DWorld.destroyBody(body);
    }

    /**
     * Write the object during the serialization step
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        // DEBUG: System.err.println("writeObject PhysicalWorld");
        // Write all non-transient fields
        out.defaultWriteObject(); 
        // Write the transient (i.e. not serialisable) JBox2D world
        PbSerializer jbox2Dserialiser = new PbSerializer(this);
        out.writeObject(jbox2Dserialiser.serializeWorld(jBox2DWorld).build());

    }
    
    /**
     * Read the object during the deserialization step
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        // DEBUG: System.err.println("readObject PhysicalWorld");
        // Read all non-transient fields
        in.defaultReadObject(); 
        // Read the transient (i.e. not serialisable) JBox2D world
        PbDeserializer jbox2Ddeserialiser = new PbDeserializer(this);
        PbWorld pbworld = (PbWorld) in.readObject();
        jBox2DWorld = jbox2Ddeserialiser.deserializeWorld(pbworld);
    }

   /**
    * Before serialization, mark all Body with the hashCode of their Sprite's name for future recognition
    */
    public Long getTag(org.jbox2d.dynamics.Body body)  {
        Long l = new Long(Sprite.extractSprite(body).getName().hashCode());
        // DEBUG: System.err.println(Sprite.extractSprite(body).getName() + " is associated to "+l);
        return l;
    }
    /**
    * Before serialization, mark a specific Fixture for identification (not used yet)
    */
    public  Long 	getTag(org.jbox2d.dynamics.Fixture fixture)  {
        return null;
    }
    /**
     * Before serialization, mark a specific Joint for identification (not used yet)
     */
    public  Long 	getTag(org.jbox2d.dynamics.joints.Joint joint)  {
        return null;
    }
    /**
	* Before serialization, mark a specific Shape for identification (not used yet)
	*/
    public  Long 	getTag(org.jbox2d.collision.shapes.Shape shape)  {
        return null;
    }
    /**
     * Before serialization, mark a specific World for identification (not used yet)
     */
    public  Long getTag(org.jbox2d.dynamics.World world)  {
        return null;
    }
    /**
      * After deserialization, Body marked with specific tag are relinked to their Sprite
      */
    public void processBody(org.jbox2d.dynamics.Body body, Long tag) {
        for(Sprite s : spriteList) {
            if(tag.equals(new Long(s.getName().hashCode()))) {
                body.setUserData(s);
                s.linkToBody(body);
                // DEBUG: System.err.println(tag+" is found an linked to "+s.getName());
                break;
            }
        }
    }

    /**
     * After deserialization, does specific Fixture need to set some property ? (not used yet)
     */
    public void 	processFixture(org.jbox2d.dynamics.Fixture fixture, Long tag)  {}
    /**
     * After deserialization, does specific Joint need to set some property ? (not used yet)
     */
    public void 	processJoint(org.jbox2d.dynamics.joints.Joint joint, Long tag)  {}
    /**
     * After deserialization, does specific Shape need to set some property ? (not used yet)
     */
    public void 	processShape(org.jbox2d.collision.shapes.Shape shape, Long tag)  {}
    /**
     * After deserialization, does specific World need to set some property ? (not used yet)
     */
    public void 	processWorld(org.jbox2d.dynamics.World world, Long tag)  {}
    
    public void setPar(int par) {
       this.par = par;
    }
    public int getPar() {
       return this.par;
    }
    public void setLevelName(String levelName) {
       this.levelName = levelName;
    }
    public String getLevelName() {
       return this.levelName;
    }
}




