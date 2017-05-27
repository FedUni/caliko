package au.edu.federation.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.util.Random;

/**
 * Class  : A series of static utility / helper methods to perform common operations.
 * Version: 0.4
 * Date   : 04/12/2015
 */
public final class Utils
{
	// Constants to translate values from degrees to radians and vice versa
	private static final float DEGS_TO_RADS = (float)Math.PI / 180.0f;
	private static final float RADS_TO_DEGS = 180.0f / (float)Math.PI;

	// Define a private static DecimalFormat to be used by our toString() method.
	// Note: '0' means put a 0 there if it's zero, '#' means omit if zero.
	public static final DecimalFormat df = new DecimalFormat("0.000");
	
	/** Newline character for this system. */
	public static final String NEW_LINE = System.lineSeparator();
	
	/**
	 * Some colours with which to draw things.
	 * <p>
	 * These colours are neither final nor 'constant', so they can be user modified at runtime if desired.
	 */
	public static final Colour4f RED       = new Colour4f(1.0f, 0.0f, 0.0f, 1.0f);
	public static final Colour4f GREEN     = new Colour4f(0.0f, 1.0f, 0.0f, 1.0f);
	public static final Colour4f BLUE      = new Colour4f(0.0f, 0.0f, 1.0f, 1.0f);
	public static final Colour4f MID_RED   = new Colour4f(0.6f, 0.0f, 0.0f, 1.0f);
	public static final Colour4f MID_GREEN = new Colour4f(0.0f, 0.6f, 0.0f, 1.0f);
	public static final Colour4f MID_BLUE  = new Colour4f(0.0f, 0.0f, 0.6f, 1.0f);
	public static final Colour4f BLACK     = new Colour4f(0.0f, 0.0f, 0.0f, 1.0f);
	public static final Colour4f GREY      = new Colour4f(0.5f, 0.5f, 0.5f, 1.0f);
	public static final Colour4f WHITE     = new Colour4f(1.0f, 1.0f, 1.0f, 1.0f);
	public static final Colour4f YELLOW    = new Colour4f(1.0f, 1.0f, 0.0f, 1.0f);
	public static final Colour4f CYAN      = new Colour4f(0.0f, 1.0f, 1.0f, 1.0f);
	public static final Colour4f MAGENTA   = new Colour4f(1.0f, 0.0f, 1.0f, 1.0f);
	
	/**
	 * A Random object used to generate random numbers in the randRange methods.
	 * <p>
	 * If you want a reproducable sequence of events, then set a seed value using Utils.setRandomSeed(someValue).
	 * 
	 * @see #setRandomSeed(int)
	 * @see #randRange(float, float)
	 * @see #randRange(int, int)
	 */ 
	public static Random random = new Random();
	
	/** The maximum length in characters of any names which may be used for bones, chains or structures. */
	public static final int MAX_NAME_LENGTH = 100;
	
	private Utils() {}

	/**
	 * Set a fixed seed value - call this with any value before starting the inverse kinematics runs to get a repeatable sequence of events.
	 * 
	 * @param	seedValue	The seed value to set.
	 */
	public static void setRandomSeed(int seedValue)
	{
		random = new Random(seedValue);
	}
	
	/**
	 * Return a random floating point value between the half-open range [min..max).
	 * <p>
	 * This means that, for example, a call to {@code randRange(-5.0f, 5.0f)} may return a value between -5.0f up to a maximum of 4.999999f.
	 * 
	 * @param	min	The minimum value
	 * @param	max The maximum value
	 * @return		A random float within the specified half-open range.
	 */
	public static float randRange(float min, float max) { return random.nextFloat() * (max - min) + min; }
	
	/**
	 * Return a random integer value between the half-open range (min..max]
	 * <p>
	 * This means that, for example, a call to {@code randRange(-5, 5)} will return a value between -5 up to a maximum of 4.
	 * 
	 * @param	min	The minimum value
	 * @param	max The maximum value
	 * @return		A random int within the specified half-open range.
	 */
	public static int randRange(int min, int max) {	return random.nextInt(max - min) + min; }

	/**
	 * Return the co-tangent of an angle specified in radians.
	 * 
	 * @param	angleRads	The angle specified in radians to return the co-tangent of.
	 * @return				The co-tangent of the specified angle.
	 */  
	public static float cot(float angleRads) { return (float)( 1.0f / Math.tan(angleRads) ); }
		
	/**
	 * Convert an radians to degrees.
	 * 
	 * @param	angleRads	The angle in radians.
	 * @return				The converted angle in degrees.
	 */
	public static float radiansToDegrees(float angleRads) { return angleRads * RADS_TO_DEGS; }

	/**
	 * Convert an angle from degrees to radians.
	 * 
	 * @param	angleDegs	The angle in degrees.
	 * @return				The converted angle in radians.
	 */
	public static float degreesToRadians(float angleDegs) { return angleDegs * DEGS_TO_RADS; }	

	/**
	 * Return a FloatBuffer which can hold the specified number of floats.
	 *
	 * @param	numFloats	The number of floats this FloatBuffer should hold.
	 * @return				A float buffer which can hold the specified number of floats.
	 */
	public static FloatBuffer createFloatBuffer(int numFloats)
	{	
		return ByteBuffer.allocateDirect(numFloats * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
	}
	
	/**
	 * Determine the sign of a float value.
	 * 
	 * @param	value	The value to return the sign of.
	 * @return			1.0f if the provided float value is positive, -1.0f otherwise.
	 */ 
	public static float sign(float value)
	{
		if (value >= 0.0f) { 
		  return 1.0f; 
		}		
		return -1.0f;
	}
	
	/**
	 * Method to set a provided seed value to be used for random number generation.
	 * <p>
	 * This allows you to have a repoducable sequence of pseudo-random numbers which are used by
	 * the visualisation MovingTarget class.
	 * 
	 * @param	seed	The seed value
	 */
	public static void setSeed(int seed) { random = new Random(seed); }
	
	/**
	 * Validate a direction unit vector (Vec2f) to ensure that it does not have a magnitude of zero.
	 * <p>
	 * If the direction unit vector has a magnitude of zero then an IllegalArgumentException is thrown.
	 * @param	directionUV	The direction unit vector to validate
	 */
	public static void validateDirectionUV(Vec2f directionUV)
	{
		// Ensure that the magnitude of this direction unit vector is greater than zero
		if ( directionUV.length() <= 0.0f )
		{
			throw new IllegalArgumentException("Vec2f direction unit vector cannot be zero.");
		}
	}
	
	/**
	 * Validate a direction unit vector (Vec3f) to ensure that it does not have a magnitude of zero.
	 * <p>
	 * If the direction unit vector has a magnitude of zero then an IllegalArgumentException is thrown.
	 * @param	directionUV	The direction unit vector to validate
	 */
	public static void validateDirectionUV(Vec3f directionUV)
	{
		// Ensure that the magnitude of this direction unit vector is greater than zero
		if ( directionUV.length() <= 0.0f )
		{
			throw new IllegalArgumentException("Vec3f direction unit vector cannot be zero.");
		}
	}
	
	/**
	 * Validate the length of a bone to ensure that it's a positive value.
	 * <p>
	 * If the provided bone length is not greater than zero then an IllegalArgumentException is thrown.
	 * @param	length	The length value to validate.
	 */
	public static void validateLength(float length)
	{
		// Ensure that the magnitude of this direction unit vector is not zero
		if (length < 0.0f)
		{
			throw new IllegalArgumentException("Length must be a greater than or equal to zero.");
		}
	}
	
	/**
	 * Convert a value in one range into a value in another range.
	 * <p>
	 * If the original range is approximately zero then the returned value is the
	 * average value of the new range, that is: (newMin + newMax) / 2.0f
	 * 
	 * @param	origValue	The original value in the original range.
	 * @param	origMin		The minimum value in the original range.
	 * @param	origMax		The maximum value in the original range.
	 * @param	newMin		The new range's minimum value.
	 * @param	newMax		The new range's maximum value.
	 * @return				The original value converted into the new range.
	 */
	public static float convertRange(float origValue, float origMin, float origMax, float newMin, float newMax)
	{	
		float origRange = origMax - origMin;
		float newRange  = newMax  - newMin;
		
		float newValue;
		if (origRange > -0.000001f && origRange < 0.000001f)
		{
		    newValue = (newMin + newMax) / 2.0f;
		}
		else
		{
			newValue = (((origValue - origMin) * newRange) / origRange) + newMin;				
		}
		
		return newValue;
	}
	
	/**
	 * Return a boolean indicating whether a float approximately equals another to within a given tolerance.
	 * 
	 * @param	a		The first value
	 * @param	b		The second value
	 * @param	tolerance	The difference within the <strong>a</strong> and <strong>b</strong> values must be within to be considered approximately equal.
	 * @return			Whether the a and b values are approximately equal or not.
	 */
	public static boolean approximatelyEquals(float a, float b, float tolerance)
	{
		return (Math.abs(a - b) <= tolerance) ? true : false; 
	}
	
	/** Ensure we have a legal line width with which to draw.
	 * <p>
	 * Valid line widths are between 1.0f and 32.0f pixels inclusive.
	 * <p>
	 * Line widths outside this range will cause an IllegalArgumentException to be thrown.
	 * 
	 * @param	lineWidth	The width of the line we are validating. 
	 */
	public static void validateLineWidth(float lineWidth)
	{
		if (lineWidth < 1.0f || lineWidth > 32.0f)
		{
			throw new IllegalArgumentException("Line widths must be within the range 1.0f to 32.0f - but only 1.0f is guaranteed to be supported.");
		}
	}
	
	/**
	 * Return the given name capped at 100 characters, if necessary.
	 * 
	 * @param	name	The name to validate.
	 * @return			The given name capped at 100 characters, if necessary.
	 */
	public static String getValidatedName(String name)
	{
		if (name.length() >= Utils.MAX_NAME_LENGTH)
		{
			return name.substring(0, Utils.MAX_NAME_LENGTH);
		} 
		else
		{	
			return name;
		}
	}

} // End of Utils clas
