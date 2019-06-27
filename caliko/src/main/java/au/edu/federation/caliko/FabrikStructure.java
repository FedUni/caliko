package au.edu.federation.caliko;

import java.io.Serializable;

import au.edu.federation.utils.Vectorf;

/**
 * Structure interface
 * 
 * @author jsalvo
 *
 */
//@SuppressWarnings("rawtypes")
public interface FabrikStructure<T extends FabrikChain,V extends Vectorf>
{
	
  /**
   * Add a FabrikChain object to a FabrikStructure object.
   * <p>
   * Adding a chain using this method adds the chain to the structure, but does not connect it to any existing chain in the structure.
   * However, all chains in a structure share the same target, and all chains in the structure can be solved for the target location
   * via a single call to updateTarget on this structure.
   *  
   * @param  chain	The chain to add to this structure.
   **/
  public void addChain(T chain);
  
  /**
   * Connect a chain to an existing chain in this structure.
   * <p>
   * Both chains and bones are are zero indexed.
   * <p>
   * If the existingChainNumber or existingBoneNumber specified to connect to does not exist in this structure
   * then an IllegalArgumentExeception is thrown.
   * 
   * @param	newChain			The chain to connect to this structure
   * @param	existingChainNumber	The index of the chain to connect the new chain to.
   * @param	existingBoneNumber	The index of the bone to connect the new chain to within the existing chain.
   */
  public void connectChain(T newChain, int existingChainNumber, int existingBoneNumber);
  
  /**
   * Connect a chain to an existing chain in this structure.
   * <p>
   * Both chains and bones are are zero indexed.
   * <p>
   * If the existingChainNumber or existingBoneNumber specified to connect to does not exist in this structure
   * then an IllegalArgumentExeception is thrown.
   * 
   * @param	newChain		The chain to connect to this structure
   * @param	existingChainNumber	The index of the chain to connect the new chain to.
   * @param	existingBoneNumber	The index of the bone to connect the new chain to within the existing chain.
   * @param	boneConnectionPoint	Whether the new chain should connect to the START or END of the specified bone in the specified chain.
   */
  public void connectChain(T newChain, int existingChainNumber, int existingBoneNumber, BoneConnectionPoint boneConnectionPoint);	
  
  /**
   * Return a chain which exists in this structure.
   * <p>
   * Note: Chains are zero-indexed.
   * 
   * @param	chainNumber	The zero-indexed chain in this structure to return.
   * @return				The desired chain.
   */
  public T getChain(int chainNumber);
  
  /**
   * @return The name for this structure
   */
  public String getName();
  
  /**
   * Return the number of chains in this structure.
   * 
   * @return  The number of chains in this structure.
   */
  public int getNumChains();
  
  /** 
   * Set the name of this structure, capped to 100 characters if required.
   * 
   * @param name  The name to set.
   */
  public void setName(String name);
  
  /**
   * Solve the structure for the given target location.
   * <p>
   * All chains in this structure are solved for the given target location EXCEPT those which have embedded targets enabled, which are
   * solved for the target location embedded in the chain.
   * <p>
   * After this method has been executed, the configuration of all IK chains attached to this structure will have been updated.
   *  
   * @param   newTargetLocation The location of the target for which we will attempt to solve all chains attached to this structure.
   */
  public void solveForTarget(V newTargetLocation);

}
