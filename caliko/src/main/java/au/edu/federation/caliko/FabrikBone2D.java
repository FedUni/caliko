package au.edu.federation.caliko;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import au.edu.federation.utils.Colour4f;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Utils;
import au.edu.federation.utils.Vec2f;

/**
 * A class to represent a FabrikBone2D object.
 * <p>
 * A FabrikBone2D consists of a start location, an end location and a FabrikJoint2D which can constrain
 * the rotation of the bone with regard to either the previous bone in the same chain or with regard
 * to the direction of a bone in another chain which this bone is connected to.
 * 
 * @author Al Lansley
 * @version 0.9.1 - 20/07/2016
 */
@XmlRootElement(name="2dbone")
@XmlAccessorType(XmlAccessType.NONE)
public class FabrikBone2D implements FabrikBone<Vec2f,FabrikJoint2D>
{
	/**
	 * mJoint	The joint attached to this FabrikBone2D.
	 * <p>
	 * Each bone has a single FabrikJoint2D which controls the angle to which the bone is
	 * constrained with regard to the previous (i.e. earlier / closer to the base) bone in its chain.
	 * <p>
	 * By default, a joint is not constrained (that is, it is free to rotate up to 180
	 * degrees in a clockwise or anticlockwise direction), however a joint may be
	 * constrained by specifying constraint angles via the
	 * {@link #setClockwiseConstraintDegs(float)} and {@link #setAnticlockwiseConstraintDegs(float)}
	 * methods.
	 * <p>
	 * You might think that surely a bone has two joints, one at the beginning and one at
	 * the end - however, consider an empty chain to which you add a single bone: It has
	 * a joint at its start, around which the bone may rotate (and which it may optionally
	 * be constrained). In this way the single joint which can be considered to be at the
	 * start location of each bone controls the allowable range of motion for that bone alone.
	 */
	private FabrikJoint2D mJoint = new FabrikJoint2D();

	/**
	 * mStartLocation	The start location of this FabrikBone2D object.
	 * <p>
	 * The start location of a bone may only be set through a constructor or via an 'addBone'
	 * or 'addConsecutiveBone' method provided by the {@link FabrikChain2D} class.
	 */
	@XmlElement(name="startLocation")
	private Vec2f mStartLocation = new Vec2f();
	
	/**
	 * mEndLocation	The end location of this FabrikBone2D object.
	 * <p>
	 * The end location of a bone may only be set through a constructor or indirectly via an
	 * 'addBone' method provided by the {@link FabrikChain2D} class.
	 */
	@XmlElement(name="endLocation")
	private Vec2f mEndLocation = new Vec2f();

	/**
	 * mName	The name of this FabrikBone2D object.
	 * <p>
	 * It is not necessary to use this property, but it is provided to allow for easy identification
	 * of a bone, such as when used in a map or such.
	 * <p>
	 * Names exceeding 100 characters will be truncated.
	 */
	private String mName;
	
	/**
	 * mLength	The length of this bone from its start location to its end location.
	 * <p>
	 * In the typical usage scenario of a FabrikBone2D the length of the bone remains constant.
	 * <p>
	 * The length may be set explicitly through a value provided to a constructor, or implicitly
	 * when it is calculated as the distance between the {@link #mStartLocation} and {@link mEndLocation}
	 * of a bone.
	 * <p>
	 * Attempting to set a bone length of less than zero, either explicitly or implicitly, will result
	 * in an IllegalArgumentException or 
	 */
	private float mLength;

	/**
	 * The colour used to draw the bone.
	 * <p>
	 * The default colour is white at full opacity.
	 * <p>
	 * This colour property does not have to be used, for example when using solved IK chains for purposes
	 * other than drawing the solutions to screen.
	 */
	private Colour4f mColour = new Colour4f();

	/**
	 * mLineWidth	The width of the line drawn to represent this bone, specified in pixels.
	 * <p>
	 * This property can be changed via the {@link #setLineWidth(float)} method, or alternatively, line widths
	 * can be specified as arguments to the {@link #draw(float, Mat4f)} or {@link #draw(Colour4f, float, Mat4f) methods.
	 * <p>
	 * The default line width is 1.0f, which is the only value guaranteed to render correctly for any given
	 * hardware/driver combination. The maximum line width that can be drawn depends on the graphics hardware and drivers
	 * on the host machine, but is typically up to 64.0f on modern hardware.
	 * @see		#setLineWidth(float)
	 * @see		<a href="https://www.opengl.org/sdk/docs/man3/xhtml/glLineWidth.xml">glLineWidth(float)</a>
	 */
	private float mLineWidth = 1.0f;

	// ---------- Constructors ----------

	/** Default constructor */
	FabrikBone2D() { }

	/**
	 * Constructor to create a new FabrikBone2D from a start and end location as provided by a pair of Vec2fs.
	 * <p>
	 * The {@link #mLength} property is calculated and set from the provided locations. All other properties
	 * are set to their default values.
	 * <p>
	 * Instantiating a FabrikBone2D with the exact same start and end location, and hence a length of zero,
	 * may result in undefined behaviour.
	 * 
	 * @param	startLocation	The start location of the bone in world space.
	 * @param	endLocation		The end location of the bone in world space.
	 */
	public FabrikBone2D(Vec2f startLocation, Vec2f endLocation)
	{
		mStartLocation.set(startLocation);
		mEndLocation.set(endLocation);		
		setLength( Vec2f.distanceBetween(startLocation, endLocation) );
	}
	
	/**
	 * Constructor to create a new FabrikBone2D from a start and end location as provided by a four floats.
	 * <p>
	 * The {@link #mLength} property is calculated and set from the provided locations. All other properties
	 * are set to their default values.
	 * <p>
	 * Instantiating a FabrikBone2D with the exact same start and end location, and hence a length of zero,
	 * may result in undefined behaviour.
	 * 
	 * @param	startX	The horizontal start location of the bone in world space.
	 * @param	startY	The vertical   start location of the bone in world space.
	 * @param	endX	The horizontal end   location of the bone in world space.
	 * @param	endY	The vertical   end   location of the bone in world space.
	 */
	public FabrikBone2D(float startX, float startY, float endX, float endY)
	{
		this( new Vec2f(startX, startY), new Vec2f(endX, endY) );
	}
	
	/**
	 * Constructor to create a new FabrikBone2D from a start location, a direction unit vector and a length.
	 * <p>
	 * A normalised version of the direction unit vector is used to calculate the end location.
	 * <p>
	 * If the provided direction unit vector is zero then an IllegalArgumentException is thrown.
	 * If the provided length argument is less than zero then an IllegalArgumentException is thrown.
	 * <p>
	 * Instantiating a FabrikBone3D with a length of precisely zero may result in undefined behaviour.
	 * 
	 * @param	startLocation	The start location of the bone in world-space.
	 * @param	directionUV		The direction unit vector of the bone in world-space.
	 * @param	length			The length of the bone in world-space units.
	 */
	public FabrikBone2D(Vec2f startLocation, Vec2f directionUV, float length)
	{
		// Sanity checking
		Utils.validateDirectionUV(directionUV);
		
		// Set the start and end locations
		mStartLocation.set(startLocation);
		mEndLocation.set( mStartLocation.plus( Vec2f.normalised(directionUV).times(length) ) );
		
		// Set the bone length via the setLength method rather than directly on the mLength property so that validation is performed
		setLength(length);
	}
	
	/**
	 * Constructor to create a new FabrikBone2D from a start location, a direction unit vector, a length and
	 * a pair of constraint angles specified in degrees.
	 * <p>
	 * The clockwise and anticlockwise constraint angles can be considered to be relative to the previous bone
	 * in the chain which this bone exists in UNLESS the bone is a basebone (i.e. the first bone in a chain)
	 * in which case, the constraint angles can optionally be made relative to either a world-space direction,
	 * the direction of the bone to which this bone may be connected, or to a direction relative to the coordinate
	 * space of the bone to which this bone may be connected. 
	 * <p>
	 * If the direction unit vector argument is zero then an IllegalArgumentException is thrown.
	 * If the length argument is less than zero then an IllegalArgumentException is thrown.
	 * If either the clockwise or anticlockwise constraint angles are outside of the range 0.0f degrees
	 * to 180.0f degrees then an IllegalArgumentException is thrown.
	 * <p>
	 * Instantiating a FabrikBone3D with a length of precisely zero may result in undefined behaviour.
	 * 
	 * @param	startLocation		The start location of the bone in world-space.
	 * @param	directionUV			The direction unit vector of the bone in world-space.
	 * @param	length				The length of the bone in world-space units.
	 * @param	cwConstraintDegs	The clockwise constraint angle in degrees.
	 * @param	acwConstraintDegs	The anticlockwise constraint angle in degrees.
	 * 
	 * @see FabrikChain2D.BaseboneConstraintType2D
	 */
	public FabrikBone2D(Vec2f startLocation, Vec2f directionUV, float length, float cwConstraintDegs, float acwConstraintDegs)
	{
		// Set up as per previous constructor - IllegalArgumentExceptions will be thrown for invalid directions or lengths
		this(startLocation, directionUV, length);
		
		// Set the constraint angles - IllegalArgumentExceptions will be thrown for invalid constraint angles
		setClockwiseConstraintDegs(cwConstraintDegs);
		setAnticlockwiseConstraintDegs(acwConstraintDegs);
	}
	
	/**
	 * Constructor to create a new FabrikBone2D from a start location, a direction unit vector, a length, a pair
	 * of constraint angles specified in degrees and a colour.
	 * <p>
	 * The clockwise and anticlockwise constraint angles can be considered to be relative to the previous bone
	 * in the chain which this bone exists in UNLESS the bone is a basebone (i.e. the first bone in a chain)
	 * in which case, the constraint angles can optionally be made relative to either a world-space direction,
	 * the direction of the bone to which this bone may be connected, or to a direction relative to the coordinate
	 * space of the bone to which this bone may be connected. 
	 * <p>
	 * If the direction unit vector argument is zero then an IllegalArgumentException is thrown.
	 * If the length argument is less than zero then an IllegalArgumentException is thrown.
	 * If either the clockwise or anticlockwise constraint angles are outside of the range 0.0f degrees
	 * to 180.0f degrees then an IllegalArgumentException is thrown.
	 * <p>
	 * Instantiating a FabrikBone3D with a length of precisely zero may result in undefined behaviour.
	 * 
	 * @param	startLocation		The start location of the bone in world-space.
	 * @param	directionUV			The direction unit vector of the bone in world-space.
	 * @param	length				The length of the bone in world-space units.
	 * @param	cwConstraintDegs	The clockwise constraint angle in degrees.
	 * @param	acwConstraintDegs	The anticlockwise constraint angle in degrees.
	 * @param	colour				The colour with which to draw the bone.
	 * 
	 * @see FabrikChain2D.BaseboneConstraintType2D
	 */
	public FabrikBone2D(Vec2f startLocation, Vec2f directionUV, float length, float cwConstraintDegs, float acwConstraintDegs, Colour4f colour)
	{
		this(startLocation, directionUV, length, cwConstraintDegs, acwConstraintDegs);
		mColour.set(colour);
	}
	
	/**
	 * Copy constructor.
	 * <p>
	 * Takes a source FabrikBone2D object and copies all properties into the new FabrikBone2D by value.
	 * Once this is done, there are no shared references between the source and the new object, and they are
	 * exact copies of each other.
	 * 
	 * @param	source	The FabrikBone2D to clone.
	 */
	public FabrikBone2D(FabrikBone2D source)
	{
		// Set all custom classes via their set methods to avoid new memory allocations
		mStartLocation.set(source.mStartLocation);
		mEndLocation.set(source.mEndLocation);
		mJoint.set(source.mJoint);
		mColour.set(source.mColour);
		
		// Set the remaining properties by value via assignment
		mName      = source.mName;
		mLength    = source.mLength;
		mLineWidth = source.mLineWidth;		
	}
	
	// ---------- Methods ----------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float length() {	return mLength; }

	/**
	 * Get the colour of this bone as a Colour4f.
	 * 
	 * @return  The colour of the bone.
	 */
	public Colour4f getColour() { return mColour; }
	
	/**
	 * Set the colour used to draw this bone.
	 * <p>
	 * Any colour component values outside the valid range of 0.0f to 1.0f inclusive are clamped to that range.
	 *  
	 * @param	colour	The colour with which to draw this bone.
	 */
	public void setColour(Colour4f colour) { mColour.set(colour); }
	
	/**
	 * Get the line width with which to draw this line
	 * 
	 * @return  The width of the line in pixels which should be used to draw this bone.
	 */
	public float getLineWidth()	{ return mLineWidth; }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vec2f getStartLocation() { return mStartLocation; }
	
	/**
	 * Get the start location of this bone in world-space as an array of two floats.
	 * 
	 * @return  The start location of this bone.                 
	 */
	public float[] getStartLocationAsArray() { return new float[] { mStartLocation.x, mStartLocation.y }; }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vec2f getEndLocation() { return mEndLocation; }
	
	/** Get the end location of the bone in world-space as an array of two floats.
	 * 
	 * @return  The end location of this bone.
	 */
	public float[] getEndLocationAsArray() { return new float[] { mEndLocation.x, mEndLocation.y }; }
	
	/**
	 * Set the FabrikJoint2D object of this bone.
	 * 
	 * @param  joint  The FabrikJoint2D which this bone should use.
	 */
	public void setJoint(FabrikJoint2D joint) {	mJoint.set(joint); }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FabrikJoint2D getJoint() { return mJoint; }

	/**
	 * Set the clockwise constraint angle of this bone's joint in degrees.
	 * <p>
	 * The valid range of constraint angle is 0.0f degrees to 180.0f degrees inclusive, angles outside this range are clamped. 
	 * 
	 * @param  angleDegs  The clockwise constraint angle specified in degrees.	
	 */
	public void setClockwiseConstraintDegs(float angleDegs) { mJoint.setClockwiseConstraintDegs(angleDegs); }
	
	/**
	 * Get the clockwise constraint angle of this bone's joint in degrees.
	 *  
	 * @return  the clockwise constraint angle in degrees.	
	 */
	public float getClockwiseConstraintDegs() { return mJoint.getClockwiseConstraintDegs(); }
	
	/**
	 * Set the anticlockwise constraint angle of this bone's joint in degrees.
	 * <p>
	 * The valid range of constraint angle is 0.0f degrees to 180.0f degrees inclusive.
	 * <p>
	 * If a constraint angle outside of this range is provided then an IllegalArgumentException is thrown.
	 * 
	 * @param  angleDegs  The anticlockwise constraint angle specified in degrees.	
	 */
	public void setAnticlockwiseConstraintDegs(float angleDegs) { mJoint.setAnticlockwiseConstraintDegs(angleDegs); }
	
	/**
	 * Get the anticlockwise constraint angle of this bone's joint in degrees.
	 *  
	 * @return  the anticlockwise constraint angle in degrees.	
	 */
	public float getAnticlockwiseConstraintDegs() { return mJoint.getAnticlockwiseConstraintDegs(); }

	/**
	 * Get the direction unit vector between the start location and end location of this bone.
	 * <p>
	 * If the opposite (i.e. end to start) location is required then you can simply negate the provided direction.
	 * 
	 * @return  The direction unit vector of this bone.
	 * @see		Vec2f#negated()
	 */
	public Vec2f getDirectionUV() {	return Vec2f.getDirectionUV(mStartLocation, mEndLocation); }
	
	/**
	 * Set the line width with which to draw this bone.
	 * <p>
	 * The value set is clamped to be between 1.0f and 64.0f inclusive, if necessary.
	 * 
	 * @param	lineWidth	The width of the line to draw this bone in pixels.
	 */
	public void setLineWidth(float lineWidth)
	{
		if (lineWidth < 1.0f ) { 
		  mLineWidth = 1.0f;  
		} else if (lineWidth > 64.0f) { 
		  mLineWidth = 64.0f; 
		} else {
	    mLineWidth = lineWidth;
		}
	}
	
	/** 
	 * Set the name of this bone, capped to 100 characters if required.
	 * 
	 * @param	name	The name to set.
	 */
	public void setName(String name) { mName = Utils.getValidatedName(name); }
	
	/**
	 * Return the name of this bone.
	 *
	 * @return	The name of this bone.
	 */
	public String getName()	{ return mName; }
		
	/**
	 * Return a concise, human readable description of this FabrikBone2D as a String.
	 * 
	 * The colour and line-width are not included in this output, but can be queried separately
	 * via the getColour and getLineWidth methods.
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("Start joint location : " + mStartLocation                                     + Utils.NEW_LINE);
		sb.append("End   joint location : " + mEndLocation                                       + Utils.NEW_LINE);
		sb.append("Bone direction       : " + Vec2f.getDirectionUV(mStartLocation, mEndLocation) + Utils.NEW_LINE);
		sb.append("Bone length          : " + mLength                                            + Utils.NEW_LINE);
		sb.append( mJoint.toString() );
		
		return sb.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStartLocation(Vec2f location) { mStartLocation.set(location); }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEndLocation(Vec2f location) { mEndLocation.set(location); }
	
	/**
	 * Set the length of the bone.
	 * <p>
	 * If the length argument is not greater then zero an IllegalArgumentException is thrown.
	 * If the length argument is precisely zero then
	 * 
	 * @param	length	The value to set on the {@link #mLength} property.
	 */
	void setLength(float length)
	{
		if (length >= 0.0f)
		{
			mLength = length;
		}
		else
		{
			throw new IllegalArgumentException("Bone length must be a positive value.");
		}
	}
	
} // End of FabrikBone2D class
