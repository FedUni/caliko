package au.edu.federation.utils;

import java.io.Serializable;

import java.text.DecimalFormat;

//FIXME: Need to incorporate the following into Vec2f / FabrikChain2D:
// - NORMALISE corrected constraint angle to stop jitter / flip-out
// - Stop Vec2f dot product producing NAN by capping to -1..+1

/**
 * Class  : Simple vec3 class with common operations and utility / helper methods.
 *
 * Version: 0.9
 * Date   : 19/06/2019
 */

public class Vec3f implements Vectorf<Vec3f>, Serializable
{
	private static final long serialVersionUID = 1L;
	
	// ----- Static Properties -----
	
	// Define a DecimalFormat to be used by our toString() method.
	// Note: '0' means put a 0 there if it's zero, '#' means omit if zero.
	private static DecimalFormat df = new DecimalFormat("0.000");
	
	// Conversion constants to/from degrees and radians
	private static final float DEGS_TO_RADS = (float)Math.PI / 180.0f;
	private static final float RADS_TO_DEGS = 180.0f / (float)Math.PI;

	// Cardinal axes
	private static Vec3f X_AXIS = new Vec3f(1.0f, 0.0f, 0.0f);
	private static Vec3f Y_AXIS = new Vec3f(0.0f, 1.0f, 0.0f);
	private static Vec3f Z_AXIS = new Vec3f(0.0f, 0.0f, 1.0f);
	
	// ----- Properties -----

	// A Vec3 simply has three properties called x, y and z - these are public so we can access them directly for speed
	public float x, y, z;

	// ----- Methods -----
	
	/** Default constructor - x, y, and z are initialised to zero. */
	public Vec3f() { /* x, y and z are initialised to zero by default as that's how primitives are initialised in Java*/ }

	/**
	 * Single parameter constructor sets the same value across all three components.
	 *
	 * @param	value	The value to set on the x, y and z components of this vector.
	 */
	public Vec3f(float value) { x = y = z = value; }

	/**
	 * Three parameter constructor.
	 * 
	 * @param	x	The x component value to set.
	 * @param	y	The y component value to set.
	 * @param	z	The z component value to set. 
	 */
	public Vec3f(float x, float y, float z)	{ this.x = x; this.y = y; this.z = z; }

	/**
	 * Copy constructor.
	 * 
	 * @param	source	The vector used to set component values on this newly created vector.
	 */
	public Vec3f(Vec3f source) { this.x = source.x; this.y = source.y; this.z = source.z; }

	/**
	 * Return an identical copy of the provided Vec3f.
	 *
	 * @param	source	The vector to clone. 
	 * @return		An identical copy of the provided Vec3f.
	 */
	public static Vec3f clone(Vec3f source) { return new Vec3f(source.x, source.y, source.z); }

	/** 
	 * Float setter for convenience. Note: x/y/z properties are public.
	 *
	 * @param	x	The x value to set.
	 * @param	y	The y value to set.
	 * @param	z	The z value to set.
	 */
	public void set(float x, float y, float z) { this.x = x; this.y = y; this.z = z; }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void set(Vec3f source) {	x = source.x; y = source.y; z = source.z; }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean approximatelyEquals(Vec3f v, float tolerance)
	{	
		if (tolerance < 0.0f)
		{
			throw new IllegalArgumentException("Equality threshold must be greater than or equal to 0.0f");
		}
		
		// Get the absolute differences between the components
		float xDiff = Math.abs(this.x - v.x);
		float yDiff = Math.abs(this.y - v.y);
		float zDiff = Math.abs(this.z - v.z);
		
		// Return true or false
		return (xDiff < tolerance && yDiff < tolerance && zDiff < tolerance);
	}
	
	/**
	 * Return whether the two provided vectors are perpendicular (to a dot-product tolerance of 0.01f).
	 *
	 * @param	a	The first vector.
	 * @param	b	The second vector.
	 * @return		Whether the two provided vectors are perpendicular (true) or not (false).
	 */
	public static boolean perpendicular(Vec3f a, Vec3f b)
	{
		return Utils.approximatelyEquals( Vec3f.dotProduct(a, b), 0.0f, 0.01f ) ? true : false;
	}
	
	/**
	 * Return whether the length of this Vec3f is approximately equal to a given value to within a given tolerance.
	 * 
	 * @param	value		The value to compare the length of this vector to.
	 * @param	tolerance	The tolerance within which the values must be to return true.
	 * @return				A boolean indicating whether the length of this vector is approximately the same as that of the provided value.
	 */
	public boolean lengthIsApproximately(float value, float tolerance)
	{
		// Check for a valid tolerance
		if (tolerance < 0.0f)
		{
			throw new IllegalArgumentException("Comparison tolerance cannot be less than zero.");
		}
		
		if ( Math.abs(this.length() - value) < tolerance)
		{
			return true;
		}
		
		return false;
	}
	
	/** Set all components of this vector to 0.0f */
	public void zero() { x = y = z = 0.0f; }

	/**
	 * Negate and return this vector.
	 * <p>
	 * Note: It is actually <em>this</em> vector which is negated and returned, not a copy / clone.
	 * 
	 * @return	This vector negated.
	 */
	public Vec3f negate()
	{
		x = -x;
		y = -y;
		z = -z;

		// Return this for chaining
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vec3f negated() { return new Vec3f(-x, -y, -z); }
	
	/**
	 * Return whether two vectors are approximately equal to within a given tolerance.
	 *
	 * @param	a		The first vector.
	 * @param	b		The second vector.
	 * @param	tolerance	The value which each component of each vector must be within to be considered approximately equal.
	 * @return			Whether the two provided vector arguments are approximately equal (true) or not (false).
	 */
	public static boolean approximatelyEqual(Vec3f a, Vec3f b, float tolerance)
	{
		if ( (Math.abs(a.x - b.x) < tolerance) &&
		     (Math.abs(a.y - b.y) < tolerance) &&
		     (Math.abs(a.z - b.z) < tolerance) )
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vec3f normalise()
	{
		// Calculate the magnitude of our vector
		float magnitude = (float)Math.sqrt(x * x + y * y + z * z);

		// As long as the magnitude is greater then zero, divide each element by the
		// magnitude to get the normalised value between -1 and +1.
		// Note: If the vector has a magnitude of zero we simply return it - we
		// could instead throw a RuntimeException here... but it's better to continue.
		if (magnitude > 0.0f)
		{
			x /= magnitude;
			y /= magnitude;
			z /= magnitude;
		}
		
		// Return this for chaining
		return this;
	}
	
	/**
	 * Return a normalised version of this vector without modifying 'this' vector.
	 *
	 * @return	A normalised version of this vector.
	 */
	public Vec3f normalised() { return new Vec3f(this).normalise(); }
	
	/**
	 * Return the scalar product of two vectors.
	 * <p>
	 * If the provided vectors are normalised then this will be the same as the dot product.
	 * 
	 * @param	v1	The first vector.
	 * @param	v2	The second vector.
	 * @return		The scalar product of the two vectors.
	 */
	public static float scalarProduct(Vec3f v1, Vec3f v2) {	return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;	}

	/**
	 * Return the scalar product of two vectors.
	 * <p>
	 * Normalised versions of the provided vectors are used in the dot product operation.
	 * 
	 * @param	v1	The first vector.
	 * @param	v2	The second vector.
	 * @return		The dot product of the two vectors.
	 */
	public static float dotProduct(Vec3f v1, Vec3f v2)
	{
		Vec3f v1Norm = v1.normalised();
		Vec3f v2Norm = v2.normalised();
		
		return v1Norm.x * v2Norm.x + v1Norm.y * v2Norm.y + v1Norm.z * v2Norm.z;
	}

	/**
	 * Calculate and return a vector which is the cross product of the two provided vectors.
	 * <p>
	 * The returned vector is not normalised.
	 * 
	 * @param	v1	The first vector.
	 * @param	v2	The second vector.
	 * @return		The non-normalised cross-product of the two vectors v1-cross-v2.
	 */
	public static Vec3f crossProduct(Vec3f v1, Vec3f v2) { return new Vec3f(v1.y * v2.z - v1.z * v2.y, v1.z * v2.x - v1.x * v2.z, v1.x * v2.y - v1.y * v2.x); }

	/**
	 * Calculate and return a vector which is the cross product of this vector and another vector.
	 * <p>
	 * The returned vector is not normalised.
	 * 
	 * @param	v	The Vec3f with which we will cross product this Vec3f.
	 * @return		The non-normalised cross product if the two vectors this-cross-v.
	 */
	public Vec3f cross(Vec3f v) { return new Vec3f(y * v.z - z * v.y,	z * v.x - x * v.z, x * v.y - y * v.x); }

	/**
	 * Calculate and return the distance between two points in 3D space.
	 * 
	 * @param	v1	The first point.
	 * @param	v2	The second point.
	 * @return		The distance between the two points.
	 */
	public static float distanceBetween(Vec3f v1, Vec3f v2)
	{
		float dx = v2.x - v1.x;
		float dy = v2.y - v1.y;
		float dz = v2.z - v1.z;
		return (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	/**
	 * Calculate and return the Manhattan distance between two Vec3f objects.
	 * <p>
	 * The Manhattan distance is an approximate distance between two points, but
	 * can be calculated faster than the exact distance.
	 * <p>
	 * Further reading:
	 *     http://en.wikipedia.org/wiki/floataxicab_geometry
	 *     http://stackoverflow.com/questions/3693514/very-fast-3d-distance-check
	 *  
	 * @param	v1	The first point.
	 * @param	v2	The second point.
	 * @return		The Manhattan distance between the two points.
	 */
	public static float manhattanDistanceBetween(Vec3f v1, Vec3f v2) { return Math.abs(v2.x - v1.x) + Math.abs(v2.x - v1.x) + Math.abs(v2.x - v1.x); }
	
	/**
	 * Return whether two locations are within a given manhattan distance of each other.
	 * <p>
	 * The manhattan distance is an approximate distance between two points, but
	 * can be calculated faster than the exact distance.
	 * <p>
	 * Further reading:
	 *     http://en.wikipedia.org/wiki/floataxicab_geometry
	 *     http://stackoverflow.com/questions/3693514/very-fast-3d-distance-check
	 *  
	 * @param	v1	The first location vector
	 * @param	v2	The second location vector
	 * @return	boolean
	 */
	boolean withinManhattanDistance(Vec3f v1, Vec3f v2, float distance)
	{	
		if (Math.abs(v2.x - v1.x) > distance) return false; // Too far in x direction
		if (Math.abs(v2.y - v1.y) > distance) return false; // Too far in y direction
		if (Math.abs(v2.z - v1.z) > distance) return false; // Too far in z direction	
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public float length() { return (float)Math.sqrt(x * x + y * y + z * z); }
	
	/**
	 * Return a component-wise absolute version (i.e. all components are positive) of this vector.
	 * <p>
	 * Note: This vector itself is not modified - a new vector is created, each component is made positive, and the new vector is returned.
	 *
	 * @param	source	The vector to make absolute.
	 * @return		A component-wise absolute version of this vector.
	 */
	public static Vec3f abs(Vec3f source)
	{
		Vec3f absVector = new Vec3f();

		if (source.x < 0.0f) { absVector.x = -source.x; } else { absVector.x = source.x; }
		if (source.y < 0.0f) { absVector.y = -source.y; } else { absVector.y = source.y; }
		if (source.z < 0.0f) { absVector.z = -source.z; } else { absVector.z = source.z; }

		return absVector;
	}

	/**
	 * Return a normalised Vec3f which is perpendicular to the vector provided.
	 * <p>
	 * This is a very fast method of generating a perpendicular vector that works for any vector
	 * which is 5 degrees or more from vertical 'up'.
	 * <p>
	 * The code in this method is adapted from: http://blog.selfshadow.com/2011/10/17/perp-vectors/
	 *
	 * @param	u	The vector to use as the basis for generating the perpendicular vector.
	 * @return		A normalised vector which is perpendicular to the provided vector argument.
	 */
	public static Vec3f genPerpendicularVectorQuick(Vec3f u)
	{
		Vec3f perp;
		
		if (Math.abs(u.y) < 0.99f)
		{
			perp = new Vec3f(-u.z, 0.0f, u.x); // cross(u, UP)
		}
		else
		{
			perp = new Vec3f(0.0f, u.z, -u.y); // cross(u, RIGHT)
		}
		
		return perp.normalise();
	}
	
	/**
	 * Method to generate a vector perpendicular to another one using the Hughes-Muller method.
	 * <p>
	 * The returned vector is normalised.
	 * <p>
	 * The code in this method is adapted from: http://blog.selfshadow.com/2011/10/17/perp-vectors/
	 * <p>
	 * Further reading: Hughes, J. F., Muller, T., "Building an Orthonormal Basis from a Unit Vector", Journal of Graphics Tools 4:4 (1999), 33-35.
	 * 
	 * @param	u	The vector with regard to which we will generate a perpendicular unit vector.
	 * @return		A normalised vector which is perpendicular to the provided vector argument.
	 */
	public static Vec3f genPerpendicularVectorHM(Vec3f u)
	{
		// Get the absolute source vector
		Vec3f a = Vec3f.abs(u);

		if (a.x <= a.y && a.x <= a.z)
		{
			return new Vec3f(0.0f, -u.z, u.y).normalise();
		}
		else if (a.y <= a.x && a.y <= a.z)
		{
			return new Vec3f(-u.z, 0.0f, u.x).normalise();
		}
		else
		{
			return new Vec3f(-u.y, u.x, 0.0f).normalise();
		}
	}
	
	// TODO: Fix up and document properly
	// Further reading: Stark, M. M., "Efficient Construction of Perpendicular Vectors without Branching", Journal of Graphics Tools 14:1 (2009), 55-61.
//	Vec3f genPerpendicularVectorStark(Vec3f u)
//	{
//		// Get the absolute source vector
//	    Vec3f a = Vec3f.abs(u);
//
//	    unsigned int = SIGNBIT(a.x - a.y);
//	    uint uzx = SIGNBIT(a.x - a.z);
//	    uint uzy = SIGNBIT(a.y - a.z);
//
//	    uint xm = uyx & uzx;
//	    uint ym = (1^xm) & uzy;
//	    uint zm = 1^(xm & ym);
//
//	    float3 v = cross(u, float3(xm, ym, zm));
//	    return v;
//	}
	

	//TODO: Test if better than Quick version and document.
	/**
	 * Method to generate a vector perpendicular to another one using the Frisvad method.
	 * <p>
	 * The returned vector is normalised.
	 * 
	 * @param	u	The vector with regard to which we will generate a perpendicular unit vector.
	 * @return		A normalised vector which is perpendicular to the provided vector argument.
	 */
	public static Vec3f genPerpendicularVectorFrisvad(Vec3f u)
	{
	    if (u.z < -0.9999999f) // Handle the singularity
	    {
	      return new Vec3f(0.0f, -1.0f, 0.0f);
	      //b2 = Vec3f(-1.0f,  0.0f, 0.0f);
	      //return;
	    }
	    
	    float a = 1.0f/(1.0f + u.z);
	    //float b = -n.x*n.y*a;
	    return new Vec3f(1.0f - u.x * u.x * a, -u.x * u.y * a, -u.x).normalised();
	    //b2 = Vec3f(b, 1.0f - n.y*n.y*a, -n.y);
	}

	/**
	 * Return the unit vector between two provided vectors.
	 *
	 * @param	v1	The first vector.
	 * @param	v2	The second vector.
	 * @return		The unit vector between the two provided vector arguments.
	 */
	public static Vec3f getUvBetween(Vec3f v1, Vec3f v2) { return new Vec3f( v2.minus(v1) ).normalise(); }

	/**
	 * Calculate and return the angle between two vectors in radians.
	 * <p>
	 * The result will always be a positive value between zero and pi (3.14159f) radians.
	 * <p>
	 * This method does not modify the provided vectors, but does use normalised versions of them in the calculations.
	 * 
	 * @param	v1	The first vector.
	 * @param	v2	The second vector.
	 * @return		The angle between the vector in radians.
	 */
	public static float getAngleBetweenRads(Vec3f v1, Vec3f v2)
	{
		// Note: a and b are normalised within the dotProduct method.
		return (float)Math.acos( Vec3f.dotProduct(v1,  v2) );
	}

	/**
	 * Calculate and return the angle between two vectors in degrees.
	 * <p>
	 * The result will always be a positive value between [0..180) degrees.
	 * <p>
	 * This method does not modify the provided vectors, but does use normalised versions of them in the calculations.
	 * 
	 * @param	v1	The first vector.
	 * @param	v2	The second vector.
	 * @return		The angle between the vector in degrees.
	 */
	public static float getAngleBetweenDegs(Vec3f v1, Vec3f v2) { return Vec3f.getAngleBetweenRads(v1, v2) * RADS_TO_DEGS; }
	
	/**
	 * Return a signed angle between two vectors within the range -179.9f..180.0f degrees.
	 *
	 * @param	referenceVector	The baseline vector which we consider to be at zero degrees.
	 * @param	otherVector		The vector we will use to calculate the signed angle with respect to the reference vector.
	 * @param	normalVector	The normal vector (i.e. vector perpendicular to) both the reference and 'other' vectors.
	 * @return					The signed angle from the reference vector to the other vector in degrees.
	 **/
	public static float getSignedAngleBetweenDegs(Vec3f referenceVector, Vec3f otherVector, Vec3f normalVector)
	{
		float unsignedAngle = Vec3f.getAngleBetweenDegs(referenceVector, otherVector);
		float sign          = Utils.sign( Vec3f.dotProduct(Vec3f.crossProduct(referenceVector, otherVector), normalVector));		
		return unsignedAngle * sign;
	}
  
	/**
	 * Return an angle limited vector with regard to another vector.
	 * <p>
	 * @param	vecToLimit		The vector which we will limit to a given angle with regard to the the baseline vector.
	 * @param	vecBaseline		The vector which will be used as the baseline / frame-of-reference when rotating the vecToLimit.
	 * @param	angleLimitDegs	The maximum angle which the vecToLimit may be rotated away from the vecBaseline, in degrees.
	 * @return					The rotated vecToLimit, which is constraint to a maximum of the angleLimitDegs argument.
	 */
    public static Vec3f getAngleLimitedUnitVectorDegs(Vec3f vecToLimit, Vec3f vecBaseline, float angleLimitDegs)
    {
    	// Get the angle between the two vectors
    	// Note: This will ALWAYS be a positive value between 0 and 180 degrees.
        float angleBetweenVectorsDegs = Vec3f.getAngleBetweenDegs(vecBaseline, vecToLimit);
        
        if (angleBetweenVectorsDegs > angleLimitDegs)
        {        	
        	// The axis which we need to rotate around is the one perpendicular to the two vectors - so we're
            // rotating around the vector which is the cross-product of our two vectors.
        	// Note: We do not have to worry about both vectors being the same or pointing in opposite directions
        	// because if they bones are the same direction they will not have an angle greater than the angle limit,
        	// and if they point opposite directions we will approach but not quite reach the precise max angle
        	// limit of 180.0f (I believe).
            Vec3f correctionAxis = Vec3f.crossProduct(vecBaseline.normalised(), vecToLimit.normalised() ).normalise();
            
            // Our new vector is the baseline vector rotated by the max allowable angle about the correction axis
            return Vec3f.rotateAboutAxisDegs(vecBaseline, angleLimitDegs, correctionAxis).normalised();
        }
        else // Angle not greater than limit? Just return a normalised version of the vecToLimit
        {
        	// This may already BE normalised, but we have no way of knowing without calcing the length, so best be safe and normalise.
        	// TODO: If performance is an issue, then I could get the length, and if it's not approx. 1.0f THEN normalise otherwise just return as is.
            return vecToLimit.normalised();
        }
    }

	/**
	 * Return the global pitch of this vector about the global X-Axis. The returned value is within the range -179.9f..180.0f 
degrees.
	 *
	 * @return	The pitch of the vector in degrees.
	 **/
	public float getGlobalPitchDegs()
	{
		Vec3f xProjected = this.projectOntoPlane(X_AXIS);
		float pitch = Vec3f.getAngleBetweenDegs( Z_AXIS.negated(), xProjected);
		return xProjected.y < 0.0f ? -pitch : pitch;
	}

	/**
	 * Return the global yaw of this vector about the global Y-Axis. The returned value is within the range -179.9f..180.0f 
degrees.
	 *
	 * @return	The yaw of the vector in degrees.
	 **/
	public float getGlobalYawDegs()
	{
		Vec3f yProjected = this.projectOntoPlane(Y_AXIS);
		float yaw = Vec3f.getAngleBetweenDegs( Z_AXIS.negated(), yProjected);
		return yProjected.x < 0.0f ? -yaw : yaw;
	}



    /**
     * Rotate a Vec3f about the world-space X-axis by a given angle specified in radians.
     * 
     * @param	source		The vector to rotate.
     * @param	angleRads	The angle to rotate the vector in radians.
     * @return				A rotated version of the vector.
     */ 
 	public static Vec3f rotateXRads(Vec3f source, float angleRads)
 	{
 		// Rotation about the x-axis:
 		// x' = x
 		// y' = y*cos q - z*sin q
 		// z' = y*sin q + z*cos q

 		float cosTheta = (float)Math.cos(angleRads);
 		float sinTheta = (float)Math.sin(angleRads);
 		
 		return new Vec3f(source.x, source.y * cosTheta - source.z * sinTheta, source.y * sinTheta + source.z * cosTheta);
 	}
 	
 	/**
     * Rotate a Vec3f about the world-space X-axis by a given angle specified in degrees.
     * 
     * @param	source		The vector to rotate.
     * @param	angleDegs	The angle to rotate the vector in degrees.
     * @return				A rotated version of the vector.
     */
 	public static Vec3f rotateXDegs(Vec3f source, float angleDegs) { return Vec3f.rotateXRads(source, angleDegs * DEGS_TO_RADS); }
	
 	/**
     * Rotate a Vec3f about the world-space Y-axis by a given angle specified in radians.
     * 
     * @param	source		The vector to rotate.
     * @param	angleRads	The angle to rotate the vector in radians.
     * @return				A rotated version of the vector.
     */
	public static Vec3f rotateYRads(Vec3f source, float angleRads)
	{
		// Rotation about the y axis:
		// x' = z*sin q + x*cos q
		// y' = y
		// z' = z*cos q - x*sin q

		float cosTheta = (float)Math.cos(angleRads);
		float sinTheta = (float)Math.sin(angleRads);
		
		return new Vec3f(source.z * sinTheta + source.x * cosTheta, source.y, source.z * cosTheta - source.x * sinTheta); 
	}

	/**
     * Rotate a Vec3f about the world-space Y-axis by a given angle specified in degrees.
     * 
     * @param	source		The vector to rotate.
     * @param	angleDegs	The angle to rotate the vector in degrees.
     * @return				A rotated version of the vector.
     */
	public static Vec3f rotateYDegs(Vec3f source, float angleDegs) { return Vec3f.rotateYRads(source, angleDegs * DEGS_TO_RADS); }
	
	/**
     * Rotate a Vec3f about the world-space Z-axis by a given angle specified in radians.
     * 
     * @param	source		The vector to rotate.
     * @param	angleRads	The angle to rotate the vector in radians.
     * @return				A rotated version of the vector.
     */
	public static Vec3f rotateZRads(Vec3f source, float angleRads)
	{
		// Rotation about the z-axis:
		// x' = x*cos q - y*sin q
		// y' = x*sin q + y*cos q
		// z' = z

		float cosTheta = (float)Math.cos(angleRads);
		float sinTheta = (float)Math.sin(angleRads);
		
		return new Vec3f(source.x * cosTheta - source.y * sinTheta, source.x * sinTheta + source.y * cosTheta, source.z); 
	}
		
	/**
     * Rotate a Vec3f about the world-space Z-axis by a given angle specified in degrees.
     * 
     * @param	source		The vector to rotate.
     * @param	angleDegs	The angle to rotate the vector in degrees.
     * @return				A rotated version of the vector.
     */
	public static Vec3f rotateZDegs(Vec3f source, float angleDegs) { return Vec3f.rotateZRads(source, angleDegs * DEGS_TO_RADS); }
		
	/**
	 * Rotate a source vector an amount in radians about an arbitrary axis.
	 * 
	 * @param source		The vector to rotate.
	 * @param angleRads		The amount of rotation to perform in radians.
	 * @param rotationAxis	The rotation axis.
	 * @return				The source vector rotated about the rotation axis.
	 */
	public static Vec3f rotateAboutAxisRads(Vec3f source, float angleRads, Vec3f rotationAxis)
	{
		Mat3f rotationMatrix = new Mat3f();

		float sinTheta         = (float)Math.sin(angleRads);
		float cosTheta         = (float)Math.cos(angleRads);
		float oneMinusCosTheta = 1.0f - cosTheta;
		
		// It's quicker to pre-calc these and reuse than calculate x * y, then y * x later (same thing).
		float xyOne = rotationAxis.x * rotationAxis.y * oneMinusCosTheta;
		float xzOne = rotationAxis.x * rotationAxis.z * oneMinusCosTheta;
		float yzOne = rotationAxis.y * rotationAxis.z * oneMinusCosTheta;
		
		// Calculate rotated x-axis
		rotationMatrix.m00 = rotationAxis.x * rotationAxis.x * oneMinusCosTheta + cosTheta;
		rotationMatrix.m01 = xyOne + rotationAxis.z * sinTheta;
		rotationMatrix.m02 = xzOne - rotationAxis.y * sinTheta;

		// Calculate rotated y-axis
		rotationMatrix.m10 = xyOne - rotationAxis.z * sinTheta;
		rotationMatrix.m11 = rotationAxis.y * rotationAxis.y * oneMinusCosTheta + cosTheta;
		rotationMatrix.m12 = yzOne + rotationAxis.x * sinTheta;

		// Calculate rotated z-axis
		rotationMatrix.m20 = xzOne + rotationAxis.y * sinTheta;
		rotationMatrix.m21 = yzOne - rotationAxis.x * sinTheta;
		rotationMatrix.m22 = rotationAxis.z * rotationAxis.z * oneMinusCosTheta + cosTheta;

		// Multiply the source by the rotation matrix we just created to perform the rotation
		return rotationMatrix.times(source);
	}

	/**
	 * Rotate a source vector an amount in degrees about an arbitrary axis.
	 * 
	 * @param source		The vector to rotate.
	 * @param angleDegs		The amount of rotation to perform in degrees.
	 * @param rotationAxis	The rotation axis.
	 * @return				The source vector rotated about the rotation axis.
	 */
	public static Vec3f rotateAboutAxisDegs(Vec3f source, float angleDegs, Vec3f rotationAxis)
	{
		return Vec3f.rotateAboutAxisRads(source, angleDegs * DEGS_TO_RADS, rotationAxis);
	}
	
	// Overloaded toString method
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("x: " + df.format(x) + ", y: " + df.format(y) + ", z: " + df.format(z) );
		return sb.toString();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vec3f plus(Vec3f v) { return new Vec3f(this.x + v.x, this.y + v.y, this.z + v.z); }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vec3f minus(Vec3f v) { return new Vec3f(this.x - v.x, this.y - v.y, this.z - v.z); }
	
	/**
	 * Return a vector which is the result of multiplying this vector by another vector. This vector remains unchanged.
	 * 
	 * @param	v	The vector to multiply this vector by.
	 * @return		The result of multiplying this vector by the 'v' vector.
	 **/
	public Vec3f times(Vec3f v) { return new Vec3f(this.x * v.x, this.y * v.y, this.z * v.z); }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vec3f times(float scale) { return new Vec3f(this.x * scale, this.y * scale, this.z * scale); }
	
	/**
	 * Multiply the value of a Vec3f in place by a given scaling factor.
	 * 
	 * @param	v		The vector to scale.
	 * @param	scale	The value used to scale each component of the 'v' vector.
	 **/
	public static void times(Vec3f v, float scale) { v.x *= scale; v.y *= scale; v.z *= scale; }
	
	/**
	 * Add a vector to a source vector - the source vector is modified.
	 * <p>
	 * This method does not perform any memory allocations - it merely adds 'other' to 'source'.
	 * 
	 *  @param	source	The vector to which we will add a vector.
	 *  @param	other	The vector we will add to the 'source' vector.
	 */
	public static void add(Vec3f source, Vec3f other) { source.x += other.x; source.y += other.y; source.z += other.z; }
	
	/**
	 * Subtract a vector from a source vector - the source vector is modified.
	 * <p>
	 * This method does not perform any memory allocations - it merely subtracts 'other' from 'source'.
	 * 
	 *  @param	source	The vector to which we will subtract a vector.
	 *  @param	other	The vector we will suctract from the 'source' vector.
	 */
	public static void subtract(Vec3f source, Vec3f other) { source.x -= other.x; source.y -= other.y; source.z -= other.z; }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vec3f dividedBy(float value) { return new Vec3f(this.x / value, this.y / value, this.z / value);	}
	
	/**
	 * Return a vector which is the result of projecting this vector onto a plane described by the provided surface normal.
	 * <p>
	 * Neither the vector on which this method is called or the provided plane normal vector are modified.
	 * <p>
	 * If the plane surface normal has a magnitude of zero then an IllegalArgumentException is thrown.
	 *  
	 * @param	planeNormal	The normal that describes the plane onto which we will project this vector.
	 * @return				A projected version of this vector.
	 */
	public Vec3f projectOntoPlane(Vec3f planeNormal)
	{
		if ( !(planeNormal.length() > 0.0f) ) {	throw new IllegalArgumentException("Plane normal cannot be a zero vector."); }
		
		// Projection of vector b onto plane with normal n is defined as: b - ( b.n / ( |n| squared )) * n
		// Note: |n| is length or magnitude of the vector n, NOT its (component-wise) absolute value		
		Vec3f b = this.normalised();
		Vec3f n = planeNormal.normalised();		
		return b.minus( n.times( Vec3f.dotProduct(b, planeNormal) ) ).normalise();
		
		/** IMPORTANT: We have to be careful here - even code like the below (where dotProduct uses normalised
		 *             versions of 'this' and planeNormal is off by enough to make the IK solutions oscillate:
		 *
		 *             return this.minus( planeNormal.times( Vec3f.dotProduct(this, planeNormal) ) ).normalised();
		 *             
		 */
				
		// Note: For non-normalised plane vectors we can use:
		// float planeNormalLength = planeNormal.length();
		// return b.minus( n.times( Vec3f.dotProduct(b, n) / (planeNormalLength * planeNormalLength) ).normalised();
	}
	
	/**
	 * Calculate and return the direction unit vector from point a to point b.
	 * <p>
	 * If the opposite direction is required then the argument order can be swapped or the the result can simply be negated.
	 * 
	 * @param	v1	The first location.
	 * @param	v2	The second location.
	 * @return		The normalised direction unit vector between point v1 and point v2.
	 */
	public static Vec3f getDirectionUV(Vec3f v1, Vec3f v2) { return v2.minus(v1).normalise(); }	
	
	/**
	 * Randomise the components of this vector to be random values between the provided half-open range as described by the minimum and maximum value arguments.
	 *
	 * @param	min	The minimum value for any given component (inclusive).
	 * @param	max	The maximum value for any given component (exclusive, i.e. a max of 5.0f will be assigned values up to 4.9999f or such).
	 **/
	public void randomise(float min, float max)
	{
		this.x = Utils.randRange(min, max);
		this.y = Utils.randRange(min, max);
		this.z = Utils.randRange(min, max);
	}

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Float.floatToIntBits(x);
    result = prime * result + Float.floatToIntBits(y);
    result = prime * result + Float.floatToIntBits(z);
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
    Vec3f other = (Vec3f) obj;
    if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x)) {
      return false;
    }
    if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y)) {
      return false;
    }
    if (Float.floatToIntBits(z) != Float.floatToIntBits(other.z)) {
      return false;
    }
    return true;
  }	
	
} // End of Vec3f class
