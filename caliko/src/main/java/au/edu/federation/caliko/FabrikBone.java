package au.edu.federation.caliko;

import au.edu.federation.utils.Vectorf;

/**
 * Interface for a bone
 * 
 * @author jsalvo
 */
@SuppressWarnings("rawtypes")
public interface FabrikBone<V extends Vectorf, J extends FabrikJoint> {
	
	/**
	 * Return the start location of this bone.
	 *
	 * @return	The start location of this bone.
	 */
	V getStartLocation();
	
	/**
	 * Return the end location of this bone.
	 *
	 * @return	The end location of this bone.
	 */
	V getEndLocation();
	
	/**
	 * Set the start location of this bone from a provided vector.
	 * <p>
	 * No validation is performed on the value of the start location - be aware
	 * that adding a bone with identical start and end locations will result in
	 * undefined behaviour. 
	 * @param	location	The bone start location specified as a vector.
	 */
	void setStartLocation(V location);
	
	/**
	 * Set the end location of this bone from a provided vector.
	 * <p>
	 * No validation is performed on the value of the end location - be aware
	 * that adding a bone with identical start and end locations will result in
	 * undefined behaviour. 
	 * @param	location	The bone end location specified as a vector.
	 */
	void setEndLocation(V location);
	

	/**
	 * Return the length of this bone. This value is calculated when the bone is constructed
	 * and used throughout the lifetime of the bone.
	 * 
	 * @return	The length of this bone, as stored in the mLength property.
	 */
	float length();
	
	/**
	 * Get the joint associated with this bone.
	 * <p>
	 * Each bone has precisely one joint. Although the joint does not
	 * have a location, it can conceptually be thought of to be located at the start location
	 * of the bone.
	 * 
	 * @return  The joint associated with this bone.
	 */	
	J getJoint();

}
