package au.edu.federation.utils;

/**
 * Interface for a Vector
 * 
 * @author jsalvo
 *
 * @param <T>
 */
public interface Vectorf<T> {
	
	/**
	 * Check if this vector is approximately equal to another vector.
	 * <p>
	 * If a tolerance of less than zero is provided then an IllegalArgumentException is thrown.
	 * 
	 * @param	v			The vector to compare to
	 * @param   tolerance	The value which both components of each vector must be less than to be considered equal
	 * @return				A true or false value indicating if the vectors are approximately equal.
	 */
	boolean approximatelyEquals(T v, float tolerance);
	
	/**
	 * Return a vector which is the result of component-wise division of by the given scalar value. This vector remains unchanged.
	 * 
	 * @param	value	The value to divide each component of this vector by.
	 * @return			A vector which is the result of dividing each component of this vector by the provided argument.
	 **/	
	T dividedBy(float value);
	
	/**
	 * Return the length of this vector.
	 *
	 * @return	The length of this vector.
	 */	
	float length();
	
	/**
	 * Subtract the provided vector from this vector and return the result in a new vector.
	 * <p>
	 * The {@code minus} method does not modify 'this' vector. 
	 * @param	v	The vector to subtract.
	 * @return		A vector which is the result of component-wise subtraction of the provided Vec2f from this vector
	 */	
	T minus(T v);
	
	/**
	 * Return a negated version of this vector - no changes are made to this vector itself.
	 * 
	 * @return A negated version of this vector.
	 */	
	T negated();
	
	/**
	 * Normalise and return this vector.
	 * <p>
	 * Note: This vector itself is normalised and returned, not a copy / clone.
	 *
	 * @return	A normalised version of this vector.
	 */	
	T normalise();
	
	/**
	 * Return a T which is the result of adding another T to this T. This T remains unchanged.
	 * 
	 * @param	v	The T to add to this T
	 * @return		The result of adding the 'v' vector to this T.
	 **/	
	T plus(T v);
	
	/** Set this T to have identical values to a provided source Vec3f.
	 *
	 * @param	source	The source T from which to get the z properties to set on this T.
	 */	
	void set(T source);
	
	/** Return a T which is the result of multiplying this this T by a scalar value. This T remains unchanged.
	 * 
	 * @param	scale	The amount to scale each component of this vector.
	 * @return			The result of scaling this T by the providing argument.
	 **/	
	T times(float value);

}
