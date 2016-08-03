package au.edu.federation.caliko;

import java.util.ArrayList;
import java.util.List;

import au.edu.federation.caliko.FabrikChain3D.BaseboneConstraintType3D;
import au.edu.federation.utils.Mat3f;
import au.edu.federation.utils.Utils;
import au.edu.federation.utils.Vec2f;
import au.edu.federation.utils.Vec3f;
import static au.edu.federation.caliko.FabrikBone3D.BoneConnectionPoint3D;

/** 
 * A FabrikStructure3D contains one or more FabrikChain3D objects, which we can solve using the FABRIK (Forward And
 * Backward Reaching Inverse Kinematics) algorithm for specified target locations.
 * <p>
 * The FabrikStructure3D class is merely a convenient holder for a list of FabrikChain3D objects which allows
 * multiple chains to have their target location updated, as well as solving and drawing the multiple chains
 * attached to the FabrikStructure3D object using one method call per structure.
 * <p>
 * If you do not intend on attaching multiple FabrikChain3D objects into a complex structure, for example one with
 * multiple effectors, then you may be better served by creating individual FabrikChain3D objects and using those
 * objects directly.
 * 
 * @author Al Lansley
 * @version 0.4 - 29/12/2015
 **/
public class FabrikStructure3D
{
	// ---------- Private Properties ----------
	
	private static final String NEW_LINE = System.lineSeparator();
	
	/** Max name of structure in characters.*/
	private static final int MAX_NAME_LENGTH = 100;

	/** The string name of this FabrikStructure3D - can be used for creating Maps, if required. */
	private String mName;
	
	/**
	 * The main substance of a FabrikStructure3D is a List of FabrikChain3D objects.
	 * <p>
	 * Each FabrikChain3D in the mChains vector is independent of all others, but shares the same target location as any/all other chains
	 * which exist in this structure.
	 */
	private List<FabrikChain3D> mChains = new ArrayList<FabrikChain3D>();

	/** Property to keep track of how many chains exist in this structure. */
	private int mNumChains = 0;

	// --------- Public Methods ----------

	/** Default constructor. */
	public FabrikStructure3D() { }
	
	/**
	 * Naming constructor.
	 * <p>
	 * Name lengths are truncated to a maximum of 100 characters, if necessary.
	 *  
	 *  @param	name	The name you wish to call the structure.
	 */
	public FabrikStructure3D(String name)
	{
		mName = name.length() > MAX_NAME_LENGTH ? name = name.substring(0, MAX_NAME_LENGTH) : name;
	}

	/**
	 * Solve the structure for the given target location.
	 * <p>
	 * All chains in this structure are solved for the given target location EXCEPT those which have embedded targets enabled, which are
	 * solved for the target location embedded in the chain.
	 * <p>
	 * After this method has been executed, the configuration of all IK chains attached to this structure will have been updated.
	 *  
	 * @param   newTargetLocation	The location of the target for which we will attempt to solve all chains attached to this structure.
	 */
	public void solveForTarget(Vec3f newTargetLocation)
	{
		int numChains = mChains.size();
		int connectedChainNumber;
		
		// Loop over all chains in this structure...
		for (int loop = 0; loop < numChains; ++loop)
		{
			// Get this chain, and get the number of the chain in this structure it's connected to (if any)
			FabrikChain3D thisChain = mChains.get(loop);
			connectedChainNumber    = thisChain.getConnectedChainNumber();
			
			// If this chain isn't connected to another chain then update as normal...
			if (connectedChainNumber == -1)
			{	
				thisChain.solveForTarget(newTargetLocation);
			}
			else // ...however, if this chain IS connected to another chain...
			{	
				// ... get the host chain and bone which this chain is connected to
				FabrikChain3D hostChain = mChains.get(connectedChainNumber);
				FabrikBone3D hostBone   = hostChain.getBone( thisChain.getConnectedBoneNumber() );
				if (hostBone.getBoneConnectionPoint() == BoneConnectionPoint3D.START) { thisChain.setBaseLocation( hostBone.getStartLocation() ); }
				else                                                                  { thisChain.setBaseLocation( hostBone.getEndLocation()   ); }
				
				// Now that we've clamped the base location of this chain to the start or end point of the bone in the chain we are connected to, it's
				// time to deal with any base bone constraints...
				
				// What type of base bone constraint is this (connected to another) chain using? 
				BaseboneConstraintType3D constraintType = thisChain.getBaseboneConstraintType();
				switch (constraintType)
				{
					// None or global basebone constraints? Nothing to do, because these will be handled in FabrikChain3D.solveIK() as we do not
					// need information from another chain to handle them.
					case NONE:         // Nothing to do because there's no basebone constraint
					case GLOBAL_ROTOR: // Nothing to do because the basebone constraint is not relative to bones in other chains in this structure
					case GLOBAL_HINGE: // Nothing to do because the basebone constraint is not relative to bones in other chains in this structure
						break;
						
					// If we have a local rotor or hinge constraint then we must calculate the relative basebone constraint before calling updateTarget
					case LOCAL_ROTOR:
					case LOCAL_HINGE: {
						// Get the direction of the bone this chain is connected to and create a rotation matrix from it.
						Mat3f connectionBoneMatrix = Mat3f.createRotationMatrix( hostBone.getDirectionUV() );
						
						// We'll then get the basebone constraint UV and multiply it by the rotation matrix of the connected bone 
						// to make the basebone constraint UV relative to the direction of bone it's connected to.
						Vec3f relativeBaseboneConstraintUV = connectionBoneMatrix.times( thisChain.getBaseboneConstraintUV() ).normalised();
							
						// Update our basebone relative constraint UV property
						thisChain.setBaseboneRelativeConstraintUV(relativeBaseboneConstraintUV);
						
						// Updat the relative reference constraint UV if we hav a local hinge
						if (constraintType == BaseboneConstraintType3D.LOCAL_HINGE)
						{
							thisChain.setBaseboneRelativeReferenceConstraintUV( connectionBoneMatrix.times( thisChain.getBone(0).getJoint().getHingeReferenceAxis() ) );
						}
						break;
					}	
					
					// No need for a default - constraint types are enums and we've covered them all.
				}
						
				// NOTE: If the base bone constraint type is NONE then we don't do anything with the base bone constraint of the connected chain.
				
				// Finally, update the target and solve the chain
				// Update the target and solve the chain
				if ( !thisChain.getEmbeddedTargetMode() )
				{
					thisChain.solveForTarget(newTargetLocation);	
				}
				else
				{
					thisChain.solveForEmbeddedTarget();
				}
				
			} // End of if chain is connected to another chain section
			
		} // End of loop over chains

	} // End of updateTarget method

	/**
	 * Solve the structure for the given target location.
	 * <p>
	 * All chains in this structure are solved for the given target location EXCEPT those which have embedded targets enabled, which are
	 * solved for the target location embedded in the chain.
	 * <p>
	 * After this method has been executed, the configuration of all IK chains attached to this structure will have been updated.
	 * 
	 * @param  targetX	The target x location.
	 * @param  targetY	The target y location.
	 * @param  targetZ	The target z location.
	 **/
	public void solveForTarget(float targetX, float targetY, float targetZ)
	{
		// Call our Vec3f version of updateTarget using a constructed Vec3f target location
		// Note: This will loop over all chains, attempting to solve each for the same target location
		solveForTarget( new Vec3f(targetX, targetY, targetZ) );
	}
	
	/**
	 * Add a FabrikChain3D to this FabrikStructure3D.
	 * <p>
	 * In effect, the chain is added to the mChains list of FabrikChain3D objects, and the mNumChains property is incremented.
	 * <p>
	 * Adding a chain using this method adds the chain to the structure, but does not connect it to any existing chain
	 * in the structure. If you wish to connect a chain, use one of the connectChain methods instead.
	 * <p>
	 * All chains in a structure share the same target, and all chains in the structure can be solved for the target location
	 * via a single call to updateTarget. 
	 *  
	 * @param	chain	(FabrikChain3D)	The FabrikChain3D to add to this structure.
	 * @see		#connectChain(au.edu.federation.caliko.FabrikChain3D, int, int)
	 * @see		#connectChain(au.edu.federation.caliko.FabrikChain3D, int, int, au.edu.federation.caliko.FabrikBone3D.BoneConnectionPoint3D)
	 **/
	public void addChain(FabrikChain3D chain)
	{
		mChains.add(chain);		
		++mNumChains;
	}
	
	/**
	 * Remove a FabrikChain3D from this FabrikStructure3D by its index.
	 * <p>
	 * In effect, the chain is removed from the mChains list of FabrikChain3D objects, and the mNumChains property is decremented.
	 * 
	 * @param	chainIndex	The index of the chain to remove from the mChains list of FabrikChain3D objects.
	 **/
	public void removeChain(int chainIndex)
	{
		mChains.remove(chainIndex);		
		--mNumChains;
	}
	
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
	public void connectChain(FabrikChain3D newChain, int existingChainNumber, int existingBoneNumber)
	{	
		// Does this chain exist? If not throw an IllegalArgumentException
		if (existingChainNumber > mNumChains)
		{
			throw new IllegalArgumentException("Cannot connect to chain " + existingChainNumber + " - no such chain (remember that chains are zero indexed).");
		}
		
		// Do we have this bone in the specified chain? If not throw an IllegalArgumentException
		if (existingBoneNumber > mChains.get(existingChainNumber).getNumBones() )
		{
			throw new IllegalArgumentException("Cannot connect to bone " + existingBoneNumber + " of chain " + existingChainNumber + " - no such bone (remember that bones are zero indexed).");
		}
			
		// Make a copy of the provided chain so any changes made to the original do not affect this chain
		FabrikChain3D relativeChain = new FabrikChain3D(newChain);
		
		// Connect the copy of the provided chain to the specified chain and bone in this structure
		relativeChain.connectToStructure(this, existingChainNumber, existingBoneNumber);
		
		// The chain as we were provided should be centred on the origin, so we must now make it
		// relative to the start location of the given bone in the given chain.
		
		
		// Get the connection point so we know to connect at the start or end location of the bone we're connecting to
		BoneConnectionPoint3D connectionPoint = this.getChain(existingChainNumber).getBone(existingBoneNumber).getBoneConnectionPoint();
		Vec3f connectionLocation;
		if (connectionPoint == BoneConnectionPoint3D.START)
		{
			connectionLocation = mChains.get(existingChainNumber).getBone(existingBoneNumber).getStartLocation();
		}
		else // If it's BoneConnectionPoint.END then we set the connection point to be the end location of the bone we're connecting to
		{
			connectionLocation = mChains.get(existingChainNumber).getBone(existingBoneNumber).getEndLocation();
		}		
		relativeChain.setBaseLocation(connectionLocation);
		
		// When we have a chain connected to a another 'host' chain, the chain is which is connecting in
		// MUST have a fixed base, even though that means the base location is 'fixed' to the connection
		// point on the host chain, rather than a static location.
		relativeChain.setFixedBaseMode(true);
		
		// Translate the chain we're connecting to the connection point
		for (int loop = 0; loop < relativeChain.getNumBones(); ++loop)
		{
			Vec3f origStart = relativeChain.getBone(loop).getStartLocation();
			Vec3f origEnd   = relativeChain.getBone(loop).getEndLocation();
			
			Vec3f translatedStart = origStart.plus(connectionLocation);
			Vec3f translatedEnd   = origEnd.plus(connectionLocation);
			
			relativeChain.getBone(loop).setStartLocation(translatedStart);
			relativeChain.getBone(loop).setEndLocation(translatedEnd);
		}
		
		this.addChain(relativeChain);
	}
	
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
	public void connectChain(FabrikChain3D newChain, int existingChainNumber, int existingBoneNumber, BoneConnectionPoint3D boneConnectionPoint)
	{
		// Does this chain exist? If not throw an IllegalArgumentException
		if (existingChainNumber > mNumChains)
		{
			throw new IllegalArgumentException("Cannot connect to chain " + existingChainNumber + " - no such chain (remember that chains are zero indexed).");
		}
		
		// Do we have this bone in the specified chain? If not throw an IllegalArgumentException
		if (existingBoneNumber > mChains.get(existingChainNumber).getNumBones() )
		{
			throw new IllegalArgumentException("Cannot connect to bone " + existingBoneNumber + " of chain " + existingChainNumber + " - no such bone (remember that bones are zero indexed).");
		}
			
		// Make a copy of the provided chain so any changes made to the original do not affect this chain
		FabrikChain3D relativeChain = new FabrikChain3D(newChain);
		
		// Connect the copy of the provided chain to the specified chain and bone in this structure
		relativeChain.connectToStructure(this, existingChainNumber, existingBoneNumber);
		
		// The chain as we were provided should be centred on the origin, so we must now make it
		// relative to the start location of the given bone in the given chain.
		
		
		// Set the connection point and use it to get the connection location
		this.getChain(existingChainNumber).getBone(existingBoneNumber).setBoneConnectionPoint(boneConnectionPoint);
		Vec3f connectionLocation;
		if (boneConnectionPoint == BoneConnectionPoint3D.START)
		{
			connectionLocation = mChains.get(existingChainNumber).getBone(existingBoneNumber).getStartLocation();
		}
		else // If it's BoneConnectionPoint.END then we set the connection point to be the end location of the bone we're connecting to
		{
			connectionLocation = mChains.get(existingChainNumber).getBone(existingBoneNumber).getEndLocation();
		}		
		relativeChain.setBaseLocation(connectionLocation);
		
		// When we have a chain connected to a another 'host' chain, the chain is which is connecting in
		// MUST have a fixed base, even though that means the base location is 'fixed' to the connection
		// point on the host chain, rather than a static location.
		relativeChain.setFixedBaseMode(true);
		
		// Translate the chain we're connecting to the connection point
		for (int loop = 0; loop < relativeChain.getNumBones(); ++loop)
		{
			Vec3f origStart = relativeChain.getBone(loop).getStartLocation();
			Vec3f origEnd   = relativeChain.getBone(loop).getEndLocation();
			
			Vec3f translatedStart = origStart.plus(connectionLocation);
			Vec3f translatedEnd   = origEnd.plus(connectionLocation);
			
			relativeChain.getBone(loop).setStartLocation(translatedStart);
			relativeChain.getBone(loop).setEndLocation(translatedEnd);
		}
		
		this.addChain(relativeChain);
	}
	
	/**
	 * Return the number of chains in this structure.
	 *
	 * @return	The number of chains in this structure.
	 */
	public int getNumChains() { return mNumChains; }
	
	/**
	 * Return the specified chain from this structure.
	 * <p>
	 * Chain numbers are zero indexed. If the specified chain does not exist in this structure
	 * then an IllegalArgumentException is thrown.
	 * 
	 * @param	chainNumber	The specified chain from this structure.
	 * @return	The specified FabrikChain3D from this chain.
	 */
	public FabrikChain3D getChain(int chainNumber) { return mChains.get(chainNumber); }
	
	/**
	 * Set the fixed base mode on all chains in this structure.
	 *
	 * @param	fixedBaseMode	Whether all chains should operate in fixed base mode (true) or not (false).
	 */
	public void setFixedBaseMode(boolean fixedBaseMode)
	{
		for (int loop = 0; loop < mNumChains; ++loop)
		{
			mChains.get(loop).setFixedBaseMode(fixedBaseMode);
		}
	}
	
	/** 
	 * Set the name of this structure, capped to 100 characters if required.
	 * 
	 * @param	name	The name to set.
	 */
	public void setName(String name) { mName = Utils.getValidatedName(name); }

	/**
	 * Return a concise, human readable description of this FabrikStructure3D. 
	 * <p>
	 * If further details on a specific chain are required, then you should get and print each chain individually.
	 *
	 * @return A concise, human readable description of this FabrikStructure3D.
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("----- FabrikStructure3D: " + mName + " -----" + NEW_LINE);
		
		sb.append("Number of chains: " + mNumChains + NEW_LINE);

		for (int loop = 0; loop < mNumChains; ++loop)
		{
			sb.append(mChains.get(loop).toString() );
		}

		return sb.toString();
	}

} // End of FabrikStructure3D class
