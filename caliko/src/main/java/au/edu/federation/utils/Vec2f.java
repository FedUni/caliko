package au.edu.federation.utils;

import java.text.DecimalFormat;

/**
 * A two-dimensional vector consisting of x and and y values stored as floats.
 * <p>
 * The x and y properties are declared publicly for performance reasons.
 *  @author Al Lansley
 *  @version 0.4 - 19/06/2019
 */

public class Vec2f implements Vectorf<Vec2f>
{
	// Conversion constants to/from degrees and radians
	private static final float DEGS_TO_RADS = (float)Math.PI / 180.0f;
	private static final float RADS_TO_DEGS = 180.0f / (float)Math.PI;

	/** Decimal format which prints values to three decimal places, used in the toString method. */
	// Note: '0' means put a 0 there if it's zero, '#' means omit if zero.
	private static DecimalFormat df = new DecimalFormat("0.000");

	public float x, y;

	// ---------- Constructors ----------

	/**
	 * Default constructor.
	 * <p>
	 * The x and y properties of the Vec2f are implicitly set to 0.0f by Java as that's the default value for
	 * primitives of type float. This allows for fast creation of large numbers of Vec2f objects without the
	 * need to have the x and y properties explicitly set to zero.
	 */
	public Vec2f() { }

	/**
	 * Copy-constructor.
	 * <p>
	 * @param	source	The source Vec2f to copy the x and y values from.
	 */
	public Vec2f(Vec2f source) { x = source.x; y = source.y; }

	/**
	 * Two parameter constructor which allows for creation of a Vec2f from two separate float values.
	 * <p>
	 * @param   x	The x value of this Vec2f.
	 * @param   y	The y value of this Vec2f.
	 */
	public Vec2f(float x, float y) { this.x = x; this.y = y; }

	// ---------- Methods ----------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean approximatelyEquals(Vec2f v, float tolerance)
	{
		if (tolerance < 0.0f) {	throw new IllegalArgumentException("Equality threshold must be greater than or equal to 0.0f");	}
		return (Math.abs(this.x - v.x) < tolerance &&  Math.abs(this.y - v.y) < tolerance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vec2f plus(Vec2f v) { return new Vec2f(x + v.x, y + v.y); }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vec2f minus(Vec2f v) { return new Vec2f(x - v.x, y - v.y); }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vec2f times(float value) { return new Vec2f(x * value, y * value); }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vec2f dividedBy(float value) { return new Vec2f(x / value, y / value); }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vec2f negated() { return new Vec2f(-x, -y); }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void set(Vec2f source) { x = source.x; y = source.y; }

	/**
	 * Set the x and y values of a given Vec2f object from two floats.
	 * <p>
	 * This method isn't necessarily required as the x and y properties are public, but it doesn't hurt either.
	 *
	 * @param   x	The x value to set.
	 * @param   y	The y value to set.
	 */
	public void set(float x, float y) { this.x = x; this.y = y; }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float length() { return (float)Math.sqrt(x * x + y * y);	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vec2f normalise()
	{
		float magnitude = (float)Math.sqrt(this.x * this.x + this.y * this.y);

		// If the magnitude is greater than zero then normalise the vector, otherwise simply return it as it is
		if (magnitude > 0.0f)
		{
			this.x /= magnitude;
			this.y /= magnitude;
		}

		return this;
	}

	/**
	 * Return a normalised version of the provided vector. The provided vector argument itself remains unchanged.
	 * <p>
	 * If the magnitude of the vector is zero then the original vector is returned.
	 *
	 * @param	source	The vector to normalise.
	 * @return		A normalised version of this vector.
	 */
	public static Vec2f normalised(Vec2f source) { return new Vec2f(source).normalise(); }

	/**
	 * Calculate and return the direction between two points as a unit vector.
	 * <p>
	 * The direction returned is the direction <strong>from</strong> {@code a} <strong>to</strong> {@code b}. If the
	 * opposite direction is required then the result can simply be negated.
	 * @param	a	The first location.
	 * @param	b	The second location.
	 * @return		FIX THIS
	 * @see		Vec2f#negated()
	 */
	public static Vec2f getDirectionUV(Vec2f a, Vec2f b) { return b.minus(a).normalise(); }

	/**
	 * Calculate and return the distance between two Vec2f objects.
	 * <p>
	 * The distance is calculated as the square root of the horizontal distance squared plus the vertical distance squared.
	 *
	 * @param	v1	The first vector location.
	 * @param	v2	The second vector location.
	 * @return		The distance between the two vector arguments.
	 */
	public static float distanceBetween(Vec2f v1, Vec2f v2)	{ return (float)Math.sqrt( (v2.x - v1.x) * (v2.x - v1.x) + (v2.y - v1.y) * (v2.y - v1.y) ); }

	/**
	 * Calculate and return the dot product (i.e. scalar product) of this Vec2f and another Vec2f.
	 *
	 * @param	v1	The first  Vec2f with which to calculate the dot product.
	 * @param	v2	The second Vec2f with which to calculate the dot product.
	 * @return		The dot product (i.e. scalar product) of v1 and v2.
	 */
	public static float dot(Vec2f v1, Vec2f v2) { return v1.x * v2.x + v1.y * v2.y;	}

	/**
	 * Calculate and return the dot product (i.e. scalar product) of this Vec2f and another Vec2f.
	 *
	 * @param	v	The Vec2f with which to calculate the dot product.
	 * @return		The dot product (i.e. scalar product) of this Vec2f and the 'v' Vec2f.
	 */
	public float dot(Vec2f v) { return x * v.x + y * v.y; }

	/**
	 * Calculate and return the unsigned angle between two Vec2f objects.
	 * <p>
	 * The returned angle will be within the half-open range [0.0f..180.0f)
	 * @param	v1	The first  Vec2f
	 * @param	v2	The second Vec2f
	 * @return	float
	 */
	public static float getUnsignedAngleBetweenVectorsDegs(Vec2f v1, Vec2f v2)
	{
		return (float)Math.acos( Vec2f.normalised(v1).dot( Vec2f.normalised(v2) ) ) * RADS_TO_DEGS;
	}

	/**
	 * Method to determine the sign of the angle between two Vec2f objects.
	 * <p>
	 * @param	u	The first vector.
	 * @param	v	The second vector.
	 * @return		An indication of whether the angle is positive (1), negative (-1) or that the vectors are parallel (0).
	 * @see <a href="https://stackoverflow.com/questions/7785601/detecting-if-angle-is-more-than-180-degrees">https://stackoverflow.com/questions/7785601/detecting-if-angle-is-more-than-180-degrees</a>
	 */
	public static int zcross(Vec2f u, Vec2f v)
	{
		float p = u.x * v.y - v.x * u.y;

		if      (p > 0.0f) { return 1; }
		else if (p < 0.0f) { return -1;	}
		return 0;
	}

	/**
	 * Get the signed angle in degrees between this vector and another vector.
	 * <p>
	 * The signed angle, if not zero (i.e. vectors are parallel), will be either positive or negative:
	 * - If positive (between 0.0f and 180.0f), then to rotate this vector to the other vector requires a rotation in
	 * an anti-clockwise direction,
	 * - If negative (between 0.0f and -180.0f), then to rotate this vector to the other vector requires a rotation in
	 * a clockwise direction.
	 * <p>
	 * Internally, once the unsigned angle between the vectors is calculated via the arc-cosine of the dot-product of
	 * the vectors, the {@link Vec2f#zcross(Vec2f, Vec2f)} method is used to determine if the angle is positive or negative.
	 * @param	otherVector The Vec2f that we are looking to find the angle we must rotate this vector about to reach.
	 * @return	float
	 */
	public float getSignedAngleDegsTo(Vec2f otherVector)
	{
		// Normalise the vectors that we're going to use
		Vec2f thisVectorUV  = Vec2f.normalised(this);
		Vec2f otherVectorUV = Vec2f.normalised(otherVector);

		// Calculate the unsigned angle between the vectors as the arc-cosine of their dot product
		float unsignedAngleDegs = (float)Math.acos( thisVectorUV.dot(otherVectorUV) ) * RADS_TO_DEGS;

		// Calculate and return the signed angle between the two vectors using the zcross method
		if ( Vec2f.zcross(thisVectorUV, otherVectorUV) == 1)
		{
			return unsignedAngleDegs;
		}
		else
		{
			return -unsignedAngleDegs;
		}
	}

	/**
	 * Constrain a direction unit vector to be within the clockwise and anti-clockwise rotational constraints of a baseline unit vector.
	 * <p>
	 * By default, the FABRIK algorithm solves an IK chain without constraints being applied between adjacent bones in the IK chain.
	 * However, when simulating real-world objects, such angles between bones would be unrealistic - and we should constrain the bones
	 * to (in this case, in 2D) specified clockwise and anti-clockwise rotational limits with regard to adjacent bones in the chain.
	 * <p>
	 * This method takes a direction unit vector and a baseline unit vector, and should the difference between those vectors be
	 * greater than the allowable clockwise or anti-clockwise constraint angles, then it clamps the returned direction unit vector
	 * so that it cannot exceed the clockwise or anti-clockwise rotational limits.
	 * @param	directionUV	The direction unit vector to constrain
	 * @param	baselineUV	The baseline unit vector with which to constrain the direction
	 * @param	clockwiseConstraintDegs	The maximum clockwise rotation that may be applied to the direction unit vector
	 * @param	antiClockwiseConstraintDegs	The maximum anti-clockwise rotation that may be applied to the direction unit vector
	 * @return	Vec2f
	 */
	public static Vec2f getConstrainedUV(Vec2f directionUV, Vec2f baselineUV, float clockwiseConstraintDegs, float antiClockwiseConstraintDegs)
	{
		// Get the signed angle from the baseline UV to the direction UV.
		// Note: In our signed angle ranges:
		//       0...180 degrees represents anti-clockwise rotation, and
		//       0..-180 degrees represents clockwise rotation
		float signedAngleDegs = baselineUV.getSignedAngleDegsTo(directionUV);

		// If we've exceeded the anti-clockwise (positive) constraint angle...
		if (signedAngleDegs > antiClockwiseConstraintDegs)
		{
			// ...then our constrained unit vector is the baseline rotated by the anti-clockwise constraint angle.
			// Note: We could do this by calculating a correction angle to apply to the directionUV, but it's simpler to work from the baseline.
			return Vec2f.rotateDegs(baselineUV, antiClockwiseConstraintDegs);
		}

		// If we've exceeded the clockwise (negative) constraint angle...
		if (signedAngleDegs < -clockwiseConstraintDegs)
		{
			// ...then our constrained unit vector is the baseline rotated by the clockwise constraint angle.
			// Note: Again, we could do this by calculating a correction angle to apply to the directionUV, but it's simpler to work from the baseline.
			return Vec2f.rotateDegs(baselineUV, -clockwiseConstraintDegs);
		}

		// If we have not exceeded any constraint then we simply return the original direction unit vector
		return directionUV;
	}

	/**
	 * Method to rotate this Vec2f by a given angle as specified in radians.
	 * <p>
	 * Positive values rotate this Vec2f in an anti-clockwise direction.
	 * Negative values rotate this Vec2f in a clockwise direction.
	 * <p>
	 * The changes are applied to 'this' vector, and 'this' is returned for chaining.
	 * @param   angleRads	The angle to rotate this vector specified in radians.
	 * @return	Vec2f
	 */
	public Vec2f rotateRads(float angleRads)
	{
		// Rotation about the z-axis:
		// x' = x*cos q - y*sin q
		// y' = x*sin q + y*cos q
		// z' = z

		// Pre-calc any expensive calculations we use more than once
		float cosTheta = (float)Math.cos(angleRads);
		float sinTheta = (float)Math.sin(angleRads);

		// Create a new vector which is the rotated vector
		// Note: This calculation cannot be performed 'inline' because each aspect (x and y) depends on
		// the other aspect to get the correct result. As such, we must create a new rotated vector, and
		// then assign it back to the original vector.
		Vec2f rotatedVector = new Vec2f(x * cosTheta - y * sinTheta,  // x
				                        x * sinTheta + y * cosTheta); // y

		// Set the rotated vector coords on this Vec2f
		x = rotatedVector.x;
		y = rotatedVector.y;

		// Return this Vec2f for chaining
		return this;
	}

	/**
	 * Method to rotate this Vec2f by a given angle as specified in degrees.
	 * <p>
	 * This static method does not modify the source Vec2f - it returns a new Vec2f rotated by the specified angle.
	 * Positive values rotate the source Vec2f in an anti-clockwise direction.
	 * Negative values rotate the source Vec2f in a clockwise direction.
	 * <p>
	 * This method does not convert degrees to radians and call rotateRads() - instead it performs a degrees to radians
	 * conversion and then uses identical code from the rotateRads() method to rotate the vector whilst avoiding the
	 * additional function call overhead.
	 *
	 * @param	source		The vector to rotate.
	 * @param	angleDegs	The angle to rotate the source vector specified in degrees.
	 * @return	Vec2f
	 */
	public static Vec2f rotateDegs(Vec2f source, float angleDegs)
	{
		// Rotation about the z-axis:
		// x' = x*cos q - y*sin q
		// y' = x*sin q + y*cos q
		// z' = z

		// Convert the rotation angle from degrees to radians
		float angleRads = angleDegs * DEGS_TO_RADS;

		// Pre-calc any expensive calculations we use more than once
		float cosTheta = (float)Math.cos(angleRads);
		float sinTheta = (float)Math.sin(angleRads);

		// Create a new vector which is the rotated vector
		// Note: This calculation cannot be performed 'inline' because each aspect (x and y) depends on
		// the other aspect to get the correct result. As such, we must create a new rotated vector, and
		// then assign it back to the original vector.
		return new Vec2f(source.x * cosTheta - source.y * sinTheta,  // x
				         source.x * sinTheta + source.y * cosTheta); // y
	}

	/**
	 * Return a concise, human-readable description of this Vec2f as a String.
	 * <p>
	 * The x and y values are formatted to three decimal places - if you want the exact values with no
	 * formatting applied then you can access and print them manually (they're declared as public).
	 * @return String
	 */
	@Override
	public String toString()
	{
		return df.format(x) + ", " + df.format(y);
	}

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Float.floatToIntBits(x);
    result = prime * result + Float.floatToIntBits(y);
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
    Vec2f other = (Vec2f) obj;
    if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x)) {
      return false;
    }
    if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y)) {
      return false;
    }
    return true;
  }

} // End of Vec2f class
