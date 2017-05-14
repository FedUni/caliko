package au.edu.federation.utils;

/**
 * Simple vec3i class to hold three integer components. This class is by the
 * au.edu.federation.caliko.visualisation.Model class to store model vertex, normal and face indices.
 * 
 * @version 0.6 - 01/01/2016
 */
public class Vec3i
{
	// --- Properties ---

	/** Public x, y and z properties */
	public int x, y, z;

	// --- Methods ---

	/** Default constructor - default values for the x, y and z components are 0.0f. */ 
	public Vec3i() { }

	/**
	 * Three parameter constructor.
	 * 
	 * @param	x	The x value to set.
	 * @param	y	The y value to set.
	 * @param	z	The z value to set. 
	 */
	public Vec3i(int x, int y, int z) {	this.x = x;	this.y = y;	this.z = z;	}

	/* Deliberately no getters and setters for performance reasons - access member properties directly! */

	/** Provide a concise, human-readable description of this object. */
	@Override
	public String toString() { return "(" + x + ", " + y + ", " + z + ")"; }

}
