package au.edu.federation.caliko;

import au.edu.federation.caliko.FabrikJoint3D.JointType;
import au.edu.federation.utils.Colour4f;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Utils;
import au.edu.federation.utils.Vec3f;

/**
 * A class to represent a FabrikBone3D object.
 * <p>
 * A FabrikBone3D consists of a start location, an end location and a FabrikJoint3D which can constrain
 * the rotation of the bone with regard to a previous bone in an IK chain either as a ball joint or as
 * a hinge joint constrained about a local or global axis.
 * 
 * @version 0.3.1 - 20/07/2016
 * @see FabrikJoint3D
 */
public class FabrikBone3D implements FabrikBone<Vec3f,FabrikJoint3D>
{
	/** A line separator for the current system running this code. */
	private static final String NEW_LINE = System.lineSeparator();
	
	/** The minimum valid line width with which to draw a bone as a line is 1.0f pixels wide. */
	private static final float MIN_LINE_WIDTH = 1.0f;
	
	/** The maximum valid line width with which to draw a bone as a line is 64.0f pixels wide. */
	private static final float MAX_LINE_WIDTH = 64.0f;
	
	/** Options for if a bone in another chain is connected to this bone, should it connect at the start or end location? */
	public enum BoneConnectionPoint3D { START, END };
	
	/**
	 * If this chain is connected to a bone in another chain, should this chain connect to the start or the end of that bone?
	 * <p>
	 * The default is to connect to the end of the specified bone.
	 * <p>
	 * This property can be set via the {#link #setBoneConnectionPoint(BoneConnectionPoint)} method, or when attaching this chain
	 * to another chain via the {@link au.edu.federation.caliko.FabrikStructure3D#addConnectedChain(FabrikChain3D, int, int, BoneConnectionPoint)} method.
	 */
	private BoneConnectionPoint3D mBoneConnectionPoint = BoneConnectionPoint3D.END;
	
	/**
	 * mJoint	The joint attached to this FabrikBone3D.
	 * <p>
	 * Each bone has a single FabrikJoint3D which controls the angle to which the bone is
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
	 * be constrained around a global axis via the
	 * {@link FabrikChain3D#constrainBaseBoneToDirectionUV(Vec3f)} method).
	 * <p>
	 * When a second bone is added to the chain, the joint at the start of this second bone
	 * controls the rotational constraints with regard to the first ('base') bone, and so on.
	 * <p>
	 * During the forward (tip-to-base) pass of the FABRIK algorithm, the end effector is
	 * unconstrained and snapped to the target. As we then work from tip-to-base each
	 * previous bone is constrained by the joint in the outer bone until we reach the base,
	 * at which point, if we have a fixed base location, then we snap the base bone start
	 * location to it, or if we do not have a fixed base location we project the new start
	 * location along the reverse direction of the bone by its length.
	 * <p>
	 * During the backward (base-to-tip) pass, each bone is constrained by the joint angles
	 * relative to the bone before it, ensuring that all constraints are enforced.
	 */
	private FabrikJoint3D mJoint = new FabrikJoint3D();

	/**
	 * mStartLocation	The start location of this FabrikBone3D object.
	 * <p>
	 * The start location of a bone may only be set through a constructor or via an 'addBone'
	 * method provided by the {@link FabrikChain3D} class.
	 */
	private Vec3f mStartLocation = new Vec3f();
	
	/**
	 * mEndLocation	The end location of this FabrikBone3D object.
	 * <p>
	 * The end location of a bone may only be set through a constructor or indirectly via an
	 * 'addBone' method provided by the {@link FabrikChain3D} class.
	 */
	private Vec3f mEndLocation = new Vec3f();

	/**
	 * mName	The name of this FabrikBone3D object.
	 * <p>
	 * It is not necessary to use this property, but it is provided to allow for easy identification
	 * of a bone, such as when used in a map such as {@code Map<String, FabrikBone3D>}.
	 * <p>
	 * The maximum allowable length of the name String is 100 characters - names exceeding this length
	 * will be truncated.
	 * 
	 * @see #setName(String)
	 * @see #FabrikBone3D(Vec3f, Vec3f, String)
	 * @see #FabrikBone3D(Vec3f, Vec3f, float, String)
	 */
	private String mName;
	
	/**
	 * The length of this bone from its start location to its end location. This is is calculated
	 * in the constructor and remains constant for the lifetime of the object.
	 */
	private float mLength;

	/**
	 * The colour used to draw the bone in the {@link draw} method specified as a {@link #Colour4f} object.
	 * <p>
	 * The default colour to draw a bone is white at full opacity i.e. Colour4f(1.0f, 1.0f, 1.0f, 1.0f).
	 */
	private Colour4f mColour = new Colour4f();

	/**
	 * The width of the line drawn to represent this bone, specified in pixels.
	 * <p>
	 * This property can be changed via the {@link #setLineWidth(float)} method, or alternatively,
	 * line widths may be specified as arguments to the {@link #draw(float, Mat4f)} or
	 * {@link #draw(Colour4f, float, Mat4f) methods.
	 * <p>
	 * The default line width is 1.0f, which is the only value guaranteed to render correctly for
	 * any given hardware/driver combination. The maximum line width that can be drawn depends on
	 * the graphics hardware and drivers on the host machine, but is typically up to 64.0f (pixels)
	 * on modern hardware.
	 * 
	 * @default	1.0f
	 * @see		#setLineWidth(float)
	 * @see		<a href="https://www.opengl.org/sdk/docs/man3/xhtml/glLineWidth.xml">glLineWidth(float)</a>
	 */
	private float mLineWidth = 1.0f;

	// ---------- Constructors ----------

	/**
	 * Default constructor */
	FabrikBone3D() { }

	/**
	 * Create a new FabrikBone3D from a start and end location as provided by a pair of Vec3fs.
	 * <p>
	 * The {@link #mLength} property is calculated and set from the provided locations. All other properties
	 * are set to their default values.
	 * <p>
	 * Instantiating a FabrikBone3D with the exact same start and end location, and hence a length of zero,
	 * will result in an IllegalArgumentException being thrown.
	 * 
	 * @param	startLocation	The start location of this bone.
	 * @param	endLocation		The end location of this bone.
	 */
	public FabrikBone3D(Vec3f startLocation, Vec3f endLocation)
	{
		mStartLocation.set(startLocation);
		mEndLocation.set(endLocation);
		
		// Set the length of the bone - if the length is not a positive value then an InvalidArgumentException is thrown
		setLength( Vec3f.distanceBetween(startLocation, endLocation) );
	}
	
	/**
	 * Create a new FabrikBone3D from a start and end location and a String.
	 * <p>
	 * This constructor is merely for convenience if you intend on working with named bones, and internally
	 * calls the {@link #FabrikBone3D(Vec3f, Vec3f)} constructor.
	 * 
	 * @param	startLocation	The start location of this bone.
	 * @param	endLocation		The end location of this bone.
	 * @param	name			The name of this bone.
	 */
	public FabrikBone3D(Vec3f startLocation, Vec3f endLocation, String name)
	{
		// Call the start/end location constructor - which also sets the length of the bone
		this(startLocation, endLocation);
		setName(name);
	}
	
	/**
	 * Create a new FabrikBone3D from a start location, a direction unit vector and a length.
	 * <p>
	 * The end location of the bone is calculated as the start location plus the direction unit
	 * vector multiplied by the length (which must be a positive value). All other properties
	 * are set to their default values.	 * 
	 * <p>
	 * If this constructor is provided with a direction unit vector of magnitude zero, or with a 
	 * length less than or equal to zero then an {@link IllegalArgumentException} is thrown.
	 * 
	 * @param	startLocation	The start location of this bone.
	 * @param	directionUV		The direction unit vector of this bone.
	 * @param	length			The length of this bone.
	 */
	public FabrikBone3D(Vec3f startLocation, Vec3f directionUV, float length)
	{
		// Sanity checking
		setLength(length); // Throws IAE if < zero
		if ( !(directionUV.length() > 0.0f) ) { throw new IllegalArgumentException("Direction cannot be a zero vector"); }
		
		// Set the length, start and end locations
		setLength(length);
		mStartLocation.set(startLocation);
		mEndLocation.set( mStartLocation.plus( directionUV.normalised().times(length) ) );
	}
	
	/**
	 * Create a new FabrikBone3D from a start location, a direction unit vector, a length and
	 * a pair of constraint angles.
	 * <p>
	 * If the direction has a magnitude of zero, the length is not a positive value, or* if either
	 * constraint angle is not within their valid ranges then an then an {@link IllegalArgumentException}
	 * is thrown.
	 * 
	 * @see #setAnticlockwiseConstraintDegs(float)
	 * @see #setClockwiseConstraintDegs(float)
	 * @see #mColour
	 * @see #mJoint
	 * @see #mLineWidth
	 * @see #mName
	 */
	/*public FabrikBone3D(Vec3f startLocation, Vec3f directionUV, float length, float clockwiseConstraintDegs, float anticlockwiseConstraintDegs)
	{
		// Set up as per previous constructor - IllegalArgumentExceptions will be thrown for invalid directions or lengths
		this(startLocation, directionUV, length);
		
		// Set the constraint angles - IllegalArgumentExceptions will be thrown for invalid constraint angles
		setClockwiseConstraintDegs(clockwiseConstraintDegs);
		setAnticlockwiseConstraintDegs(anticlockwiseConstraintDegs);
	}*/
	
	/*public FabrikBone3D(Vec3f startLocation, Vec3f directionUV, float length, float clockwiseConstraintDegs, float anticlockwiseConstraintDegs, Colour4f colour)
	{
		// Set up as per previous constructor - IllegalArgumentExceptions will be thrown for bad values
		this(startLocation, directionUV, length, clockwiseConstraintDegs, anticlockwiseConstraintDegs);
		
		mColour.set(colour);
	}*/
	
	/**
	 * Create a named FabrikBone3D from a start location, a direction unit vector, a bone length and a name.
	 * <p>
	 * This constructor is merely for convenience if you intend on working with named bones, and internally
	 * calls the {@link #FabrikBone3D(Vec3f, Vec3f, float)} constructor.
	 * <p>
	 * If the provided length argument is zero or if the direction is a zero vector then an IllegalArgumentException is thrown.
	 * 
	 * @param	startLocation	The start location of this bone.
	 * @param	directionUV		The direction unit vector of this bone.
	 * @param	length			The length of this bone.
	 * @param	name			The name of this bone.
	 */
	public FabrikBone3D(Vec3f startLocation, Vec3f directionUV, float length, String name)
	{
		this(startLocation, directionUV, length);
		setName(name);
	}
	
	/**
	 * Create a new FabrikBone3D from a start location, a direction unit vector, a bone length and a colour.
	 * <p>
	 * This constructor is merely for convenience if you intend on working with named bones, and internally
	 * calls the {@link #FabrikBone3D(Vec3f, Vec3f, float)} constructor.
	 * 
	 * @param	startLocation	The start location of this bone.
	 * @param	directionUV		The direction unit vector of this bone.
	 * @param	length			The length of this bone.
	 * @param	colour			The colour to draw this bone.
	 */
	public FabrikBone3D(Vec3f startLocation, Vec3f directionUV, float length, Colour4f colour)
	{
		this(startLocation, directionUV, length);
		setColour(colour);
	}

	/**
	 * Copy constructor.
	 * <p>
	 * Takes a source FabrikBone3D object and copies all properties into the new FabrikBone3D by value.
	 * Once this is done, there are no shared references between the source and the new object, and they are
	 * exact copies of each other.
	 * 
	 * @param	source	The bone to use as the basis for this new bone.
	 */
	public FabrikBone3D(FabrikBone3D source)
	{
		// Set all Vec3f properties by value via their set method
		mStartLocation.set(source.mStartLocation);
		mEndLocation.set(source.mEndLocation);
		mJoint.set(source.mJoint);
		mColour.set(source.mColour);
		
		// Set the remaining properties by value via simple assignment
		mName                = source.mName;
		mLength              = source.mLength;
		mLineWidth           = source.mLineWidth;
		mBoneConnectionPoint = source.mBoneConnectionPoint;
	}
	
	// ---------- Methods ----------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float length() {	return mLength;	}
	
	/**
	 * Return the live (i.e. live calculated) length of this bone from its current start and end locations.
	 *
	 * @return	The 'live' calculated distance between the start and end locations of this bone.
	 */
	public float liveLength() { return Vec3f.distanceBetween(mStartLocation, mEndLocation);	}
	
	/**
	 * Specify the bone connection point of this bone.
	 * <p>
	 * This connection point property controls whether, when THIS bone connects to another bone in another chain, it does so at
	 * the start or the end of the bone we connect to.
	 * <p>
	 * The default is BoneConnectionPoint3D.END.
	 * 
	 * @param	bcp	The bone connection point to use (BoneConnectionPOint3D.START or BoneConnectionPoint3D.END).
	 * 
	 */
	public void setBoneConnectionPoint(BoneConnectionPoint3D bcp) { mBoneConnectionPoint = bcp; }
	
	/** 
	 * Return the bone connection point for THIS bone, which will be either BoneConnectionPoint.START or BoneConnectionPoint.END.
	 * <p>
	 * This connection point property controls whether, when THIS bone connects to another bone in another chain, it does so at
	 * the start or the end of the bone we connect to.
	 *
	 * @return	The bone connection point for this bone.
	 */
	public BoneConnectionPoint3D getBoneConnectionPoint() { return mBoneConnectionPoint; }	

	/**
	 * Return the colour of this bone.
	 *
	 * @return	The colour to draw this bone, as stored in the mColour property.
	 */
	public Colour4f getColour() { return mColour; }
	
	/**
	 * Set the colour used to draw this bone.
	 *
	 * @param	colour	The colour (used to draw this bone) to set on the mColour property.
	 */
	public void setColour(Colour4f colour) { mColour.set(colour); }
	
	/**
	 * Return the line width in pixels used to draw this bone.
	 *
	 * @return	The line width in pixels used to draw this bone.
	 */
	public float getLineWidth()	{ return mLineWidth; }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vec3f getStartLocation() { return mStartLocation; }
	
	/**
	 * Return the start location of this bone as an array of three floats.
	 *
	 * @return	The start location of this bone as an array of three floats.
	 */
	public float[] getStartLocationAsArray() { return new float[] { mStartLocation.x, mStartLocation.y, mStartLocation.z }; }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vec3f getEndLocation() { return mEndLocation; }
	
	/**
	 * Return the end location of this bone as an array of three floats.
	 *
	 * @return	The end location of this bone as an array of three floats.
	 */
	public float[] getEndLocationAsArray() { return new float[] { mEndLocation.x, mEndLocation.y, mEndLocation.z };	}
	
	/**
	 * Set the FabrikJoint3D of this bone to match the properties of the provided FabrikJoint3D argument.
	 *
	 * @param	joint	The joint to use as the source for all joint properties on this bone. 
	 */
	public void setJoint(FabrikJoint3D joint) { mJoint.set(joint); }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FabrikJoint3D getJoint()	{ return mJoint; }
	
	/**
	 * Return the type of FabrikJoint3D.JointType associated with this bone.
	 * 
	 * @return	The type of FabrikJoint3D.JointType associated with this bone.
	 */
	public JointType getJointType()	{ return mJoint.getJointType(); }

	/**
	 * Set the clockwise constraint angle of this bone's joint in degrees (0.0f to 180.0f inclusive).
	 * <p>
	 * If a constraint angle outside of this range is provided, then an IllegalArgumentException is
	 * thrown.
	 * 
	 * @param  angleDegs  The relative clockwise constraint angle specified in degrees.	
	 */
	public void setHingeJointClockwiseConstraintDegs(float angleDegs) {	mJoint.setHingeJointClockwiseConstraintDegs(angleDegs);	}
	
	/**
	 * Return the clockwise constraint angle of this bone's hinge joint in degrees.
	 *
	 * @return	 The clockwise constraint angle of this bone's hinge joint in degrees.
	 */
	public float getHingeJointClockwiseConstraintDegs()	{ return mJoint.getHingeClockwiseConstraintDegs(); }
	
	/**
	 * Set the anticlockwise constraint angle of this bone's joint in degrees (0.0f to 180.0f inclusive).
	 * <p>	 
	 * If a constraint angle outside of this range is provided, then an {@link IllegalArgumentException}
	 * is thrown.
	 * 
	 * @param  angleDegs  The relative anticlockwise constraint angle specified in degrees.
	 */
	public void setHingeJointAnticlockwiseConstraintDegs(float angleDegs) { mJoint.setHingeJointAnticlockwiseConstraintDegs(angleDegs); }
	
	/**
	 * Return the anticlockwise constraint angle of this bone's hinge joint in degrees.
	 *
	 * @return	 The anticlockwise constraint angle of this bone's hinge joint in degrees. 
	 */
	public float getHingeJointAnticlockwiseConstraintDegs() { return mJoint.getHingeAnticlockwiseConstraintDegs(); }
	
	/**
	 * Set the rotor constraint angle of this bone's joint in degrees (0.0f to 180.0f inclusive).
	 * <p>	 
	 * If a constraint angle outside of this range is provided, then an {@link IllegalArgumentException}
	 * is thrown.
	 * 
	 * @param  angleDegs  The angle in degrees relative to the previous bone which this bone is constrained to.
	 */
	public void setBallJointConstraintDegs(float angleDegs)
	{	
		if (angleDegs < 0.0f || angleDegs > 180.0f)
		{
			throw new IllegalArgumentException("Rotor constraints for ball joints must be in the range 0.0f to 180.0f degrees inclusive.");
		}
		
		mJoint.setBallJointConstraintDegs(angleDegs);
	}
	
	/**
	 * Return the anticlockwise constraint angle of this bone's joint in degrees.
	 * 
	 * @return	The anticlockwise constraint angle of this bone's joint in degrees.
	 */
	public float getBallJointConstraintDegs() { return mJoint.getBallJointConstraintDegs();	}

	/**
	 * Get the direction unit vector between the start location and end location of this bone.
	 * <p>
	 * If the opposite (i.e. end to start) location is required then you can simply negate the provided direction.
	 * 
	 * @return  The direction unit vector of this bone.
	 * @see		Vec3f#negate()
	 * @see		Vec3f#negated()
	 */
	public Vec3f getDirectionUV()
	{
		return Vec3f.getDirectionUV(mStartLocation, mEndLocation);
	}
	
	/**
	 * Set the line width with which to draw this bone.
	 * <p>
	 * If the provided parameter is outside the valid range of 1.0f to 64.0f inclusive then an
	 * IllegalArgumentException is thrown.
	 * 
	 * @param	lineWidth	The value to set on the mLineWidth property.
	 */
	public void setLineWidth(float lineWidth)
	{
		// If the specified argument is within the valid range then set it...
		if (lineWidth >= FabrikBone3D.MIN_LINE_WIDTH && lineWidth <= FabrikBone3D.MAX_LINE_WIDTH)
		{
			mLineWidth = lineWidth;
		}
		else // ...otherwise throw an IllegalArgumentException.
		{
			throw new IllegalArgumentException("Line width must be between " +
		                                       FabrikBone3D.MIN_LINE_WIDTH + " and " +
		                                       FabrikBone3D.MAX_LINE_WIDTH + " inclusive.");
		}		
	}
	
	/** 
	 * Set the name of this bone, capped to 100 characters if required.
	 * 
	 * @param	name	The name to set.
	 */
	public void setName(String name) { mName = Utils.getValidatedName(name); }
	
	/**
	 * Get the name of this bone.
	 * <p>
	 * If the bone has not been specifically named through a constructor or by using the {@link #setName(String)} method,
	 * then the name will be the default of "UnnamedFabrikBone3D".
	 * @return String
	 */
	public String getName() { return mName; }
		
	/**
	 * Return a concise, human readable description of this FabrikBone3D as a String.
	 */
	@Override
	public String toString()
	{
		//Vec3f direction = this.getDirectionUV();
		StringBuilder sb = new StringBuilder();
		sb.append("Start joint location : " + mStartLocation  + NEW_LINE);
		sb.append("End   joint location : " + mEndLocation    + NEW_LINE);
		sb.append("Bone length          : " + mLength         + NEW_LINE);
		sb.append("Colour               : " + mColour         + NEW_LINE);
		return sb.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStartLocation(Vec3f location)
	{
		mStartLocation.set(location);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEndLocation(Vec3f location)
	{
		mEndLocation.set(location);               
	}
	
	/**
	 * Set the length of the bone.
	 * <p>
	 * This method validates the length argument to ensure that it is greater than zero.
	 * <p>
	 * If the length argument is not a positive value then an {@link IllegalArgumentException} is thrown.
	 * 
	 * @param	length	The value to set on the {@link #mLength} property.
	 */
	private void setLength(float length)
	{
		if (length > 0.0f)
		{
			mLength = length;
		}
		else
		{
			throw new IllegalArgumentException("Bone length must be a positive value.");
		}
	}
	
	
	
	
} // End of FabrikBone3D class
