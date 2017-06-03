package au.edu.federation.caliko;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/** 
 * Class to represent a joint used to constrain the relative angle between FabrikBone2D objects in an IK chain.
 * <p>
 * A FabrikJoint2D consists of a pair of constraint angles, which are:
 * <ul><li>A clockwise constraint angle, and</li>
 * <li>An anticlockwise constraint angle.</li></ul>
 * <p>
 * Both of these angles are specified in degrees, and are kept within the range 0 degrees to 180 degrees inclusive. The default
 * value of these angles is 180 degrees, which means that the joint does not enforce any rotational
 * constraints. To enforce joint constraints, the {@link #mClockwiseConstraintDegs} and {@link #mAnticlockwiseConstraintDegs}
 * properties may be set via the {@link #setClockwiseConstraintDegs} and {@link #setAnticlockwiseConstraintDegs} methods,
 * or alternatively on a FabrikBone2D through identically named methods.
 * <p>
 * Each FabrikBone2D contains precisely one FabrikJoint2D object, which does not have a specific location, but can be considered
 * to be attached to the {@code mStartLocation} of the bone. That a bone only contains a single joint may seem unintuitive at
 * first, as when you think about bones in your arms or legs, most bones are connected at two points i.e. with a connection at each
 * end. However, if you imagine working from a blank slate and adding a single bone to an IK chain, then that first bone has a
 * single joint at its base (i.e. start location) and there is no joint at it's tip (i.e. end location).
 * <p>
 * Following on from this, adding a second bone to the chain adds with it a second joint, which again can be thought of as
 * being located at the start location of that second bone (which itself is at the end location of the first bone). In this way
 * we avoid having redundant joints (i.e. 2 bones? 4 joints!), and by using the joint of the outer bone to constrain any inner
 * bone during the FABRIK algorithm's 'forward pass', and the bone's own joint when traversing the IK chain during the
 * 'backward pass', the correct constraints are enforced between the relative angles of any pair of adjacent bones.
 * <p>
 * As there is only one type of joint available when working with a FabrikBone2D, it would have been perfectly possible to
 * incorporate the joint functionality directly into the FabrikBone2D class. However, as the FabrikBone3D class allows
 * joints to be of different types, to keep the conventions the same across the 2D and 3D aspects of the Caliko library,
 * this separate joint class was created. 
 * 
 * @author Al Lansley
 * @version 0.8 - 16/12/2015
 */
@XmlRootElement(name="joint2d")
@XmlAccessorType(XmlAccessType.NONE)
public class FabrikJoint2D implements FabrikJoint<FabrikJoint2D>
{
	/** The minimum valid constraint angle for both clockwise and anticlockwise rotation is 0 degrees. */
	public static final float MIN_2D_CONSTRAINT_ANGLE_DEGS = 0.0f;

	/** The maximum valid constraint angle for both clockwise and anticlockwise rotation is 180 degrees. */
	public static final float MAX_2D_CONSTRAINT_ANGLE_DEGS = 180.0f;
	
	/**
	 * mClockwiseConstraintDegs	The angle (specified in degrees) up to which this FabrikJoint2D is allowed to
	 * rotate in a clockwise direction with regard to either the previous bone in the chain, or if this is
	 * a basebone then either a world-space direction or the direction of a bone that the bone containing
	 * this joint may be connected to.
	 * <p>
	 * The valid range of this property is 0.0f degrees to 180.0f degrees.
	 * <p>
	 * Although this property is positive from a users perspective for the sake of simplicity, internally it
	 * is treated as being a negative value as, in accordance with the right-hand rule, when rotating around
	 * the z-axis (which points outwards from the screen) clockwise rotation is negative.
	 * 
	 * @default 180.0f
	 */
	@XmlAttribute(name="clockwiseConstraintDegrees")
	private float mClockwiseConstraintDegs = MAX_2D_CONSTRAINT_ANGLE_DEGS;

	/**
	 * mAntiClockwiseContraintDegs	The angle (specified in degrees) up to which this FabrikJoint2D is allowed to
	 * rotate in an anticlockwise direction with regard to either the previous bone in the chain, or if this is
	 * a basebone then either a world-space direction or the direction of a bone that the bone containing
	 * this joint may be connected to.
	 * <p>
	 *  The valid range of this property is 0.0f degrees to 180.0f degrees.
	 * 
	 * @default 180.0f
	 */
	@XmlAttribute(name="anticlockwiseConstraintDegrees")
	private float mAnticlockwiseConstraintDegs = MAX_2D_CONSTRAINT_ANGLE_DEGS;

	// ---------- Constructors ----------
	
	/**
	 * Default constructor.
	 * <p>
	 * The clockwise and anticlockwise constraint angles are set to default values of 180 degrees, which means the bone is not
	 * constrained in its rotation. To specify joint constraints, use the {@link #setClockwiseConstraintDegs(float)} and
	 * {@link #setAnticlockwiseConstraintDegs(float)} methods.
	 */
	public FabrikJoint2D() {
	  //
	}
	
	/**
	 * Two parameter constructor which sets the constraint angles.
	 * <p>
	 * Constraint angles should be specified in the valid range 0.0f to 180.0f inclusive. Values outside of this range will
	 * be clamped to be within it.
	 * @param clockwiseConstraintDegs		The clockwise constraint angle specified in degrees.
	 * @param antiClockwiseConstraintDegs	The anticlockwise constraint angle specified in degrees.
	 */
	public FabrikJoint2D(float clockwiseConstraintDegs, float antiClockwiseConstraintDegs)
	{
		setClockwiseConstraintDegs(clockwiseConstraintDegs);
		setAnticlockwiseConstraintDegs(antiClockwiseConstraintDegs);
	}

	// ---------- Methods ----------
	
	/**
	 * Set the constraint angles of this FabrikJoint2D to match those of a source FabrikJoint2D, essentially making a copy of the source joint.
	 * @param	sourceJoint	The source joint from which to copy values.
	 */
	@Override
	public void set(FabrikJoint2D sourceJoint)
	{	
		setClockwiseConstraintDegs(sourceJoint.mClockwiseConstraintDegs);
		setAnticlockwiseConstraintDegs(sourceJoint.mAnticlockwiseConstraintDegs);
	}

	/**
	 * Set the clockwise constraint angle of this joint in degrees.
	 * <p>
	 * The constraint angle is clamped to be within the valid range of 0.0 degrees (fully constrained)
	 * to 180.0 degrees (unconstrained) inclusive.
	 * @param	angleDegs	The clockwise constraint angle specified in degrees.
	 */
	public void setClockwiseConstraintDegs(float angleDegs)
	{		
		if (angleDegs < MIN_2D_CONSTRAINT_ANGLE_DEGS) { 
		  mClockwiseConstraintDegs = MIN_2D_CONSTRAINT_ANGLE_DEGS; 
		} else if (angleDegs > MAX_2D_CONSTRAINT_ANGLE_DEGS) { 
		  mClockwiseConstraintDegs = MAX_2D_CONSTRAINT_ANGLE_DEGS; 
		} else {
	    mClockwiseConstraintDegs = angleDegs;
		}
	}
 
	/**
	 * Set the anticlockwise constraint angle of this joint in degrees.
	 * <p>
	 * The constraint angle is clamped to be within the valid range of 0.0 degrees (fully constrained)
	 * to 180.0 degrees (unconstrained) inclusive.
	 * 
	 * @param	angleDegs	The anticlockwise constraint angle specified in degrees.
	 */
	public void setAnticlockwiseConstraintDegs(float angleDegs)
	{		
		if (angleDegs < MIN_2D_CONSTRAINT_ANGLE_DEGS) { 
		  mAnticlockwiseConstraintDegs = MIN_2D_CONSTRAINT_ANGLE_DEGS; 
		}
		else if (angleDegs > MAX_2D_CONSTRAINT_ANGLE_DEGS) { 
		  mAnticlockwiseConstraintDegs = MAX_2D_CONSTRAINT_ANGLE_DEGS; 
		} else {
	    mAnticlockwiseConstraintDegs = angleDegs;
		}
	}
	
	/**
	 * Get the clockwise constraint angle of this joint in degrees.
	 * 
	 * @return	The clockwise constraint angle of this joint in degrees.
	 */
	public float getClockwiseConstraintDegs() {	return mClockwiseConstraintDegs; }

	/**
	 * Get the anticlockwise constraint angle of this joint in degrees.
	 * 
	 * @return	The anticlockwise constraint angle of this joint in degrees.
	 */
	public float getAnticlockwiseConstraintDegs() {	return mAnticlockwiseConstraintDegs; }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Float.floatToIntBits(mAnticlockwiseConstraintDegs);
    result = prime * result + Float.floatToIntBits(mClockwiseConstraintDegs);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    FabrikJoint2D other = (FabrikJoint2D) obj;
    if (Float.floatToIntBits(mAnticlockwiseConstraintDegs) != Float
        .floatToIntBits(other.mAnticlockwiseConstraintDegs)) {
      return false;
    }
    if (Float.floatToIntBits(mClockwiseConstraintDegs) != Float.floatToIntBits(other.mClockwiseConstraintDegs)) {
      return false;
    }
    return true;
  }

} // End of FabrikJoint2D class