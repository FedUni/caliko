package au.edu.federation.caliko;

public interface FabrikJoint<T> {
	
	/**
	 * Set the constraint angles of this joint to match those of a source joint, essentially making a copy of the source joint.
	 * @param	sourceJoint	The source joint from which to copy values.
	 */
	void set(T sourceJoint);	

}
