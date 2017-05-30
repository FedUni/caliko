package au.edu.federation.caliko;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import au.edu.federation.caliko.FabrikChain2D.BaseboneConstraintType2D;
import au.edu.federation.utils.Utils;
import au.edu.federation.utils.Vec2f;

/** 
 * A FabrikStructure2D contains one or more FabrikChain2D objects, which we can solve using the FABRIK (Forward And
 * Backward Reaching Inverse Kinematics) algorithm for specified target locations.
 * <p>
 * The FabrikStructure2D class is merely a convenient holder for an ArrayList of FabrikChain2D objects which allows
 * multiple chains to have their target location updated, as well as drawing the multiple chains attached to the
 * FabrikStructure2D object, using a single method call.
 * <p>
 * Chains in a structure may be connected to other chains in the same structure in a variety of ways, if desired.
 * 
 * @author Al Lansley
 * @version 1.0 - 02/08/2016
 **/
@XmlRootElement(name="2dstructure")
@XmlAccessorType(XmlAccessType.NONE)
public class FabrikStructure2D implements FabrikStructure<FabrikChain2D,Vec2f>
{	
	private static final Vec2f UP = new Vec2f(0.0f, 1.0f);
	
	// ---------- Private Properties ----------
	
	/** The string name of this FabrikStructure2D - can be used for creating Maps, if required. */
	@XmlAttribute(name="name")
	private String mName;

	/** The main substance of a FabrikStructure2D is an ArrayList of FabrikChain2D objects. */
	@XmlElementWrapper(name="2dchains")
	@XmlElement(name="2dchain")
	private List<FabrikChain2D> mChains = new ArrayList<>();

	/** Property to indicate if the first chain (chain zero) in this structure has its basebone fixed in place or not. */
	private boolean mFixedBaseMode = true;

	// --------- Public Methods ----------

	/** Default constructor. */
	public FabrikStructure2D() { }
	
	/**
	 * Naming constructor.
	 * <p>
	 * Names lengths are limited to 100 characters and are truncated if necessary.
	 * 
	 *  @param	name	The name you wish to call the structure.
	 */
	public FabrikStructure2D(String name) {	setName(name); }

	/** 
	 * Set the name of this structure, capped to 100 characters if required.
	 * 
	 * @param	name	The name to set.
	 */
	@Override
	public void setName(String name) { mName = Utils.getValidatedName(name); }
	
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
	@Override
	public void solveForTarget(Vec2f newTargetLocation)
	{
		int numChains = mChains.size();
		int hostChainNumber;
		FabrikChain2D thisChain;		
		
		
		for (int loop = 0; loop < numChains; ++loop)
		{
			thisChain = mChains.get(loop);
			
			// Is this chain connected to another chain?
			hostChainNumber = thisChain.getConnectedChainNumber();
			
			// Get the basebone constraint type of the chain we're working on
			BaseboneConstraintType2D constraintType = thisChain.getBaseboneConstraintType();
			
			// If this chain is not connected to another chain and the basebone constraint type of this chain is not global absolute
			// then we must update the basebone constraint UV for LOCAL_RELATIVE and the basebone relative constraint UV for LOCAL_ABSOLUTE connection types.
			// Note: For NONE or GLOBAL_ABSOLUTE we don't need to update anything before calling updateTarget().
			if (hostChainNumber != -1 && constraintType != BaseboneConstraintType2D.GLOBAL_ABSOLUTE)
			{	
				// Get the bone which this chain is connected to in the 'host' chain
				FabrikBone2D hostBone = mChains.get(hostChainNumber).getBone( mChains.get(loop).getConnectedBoneNumber() );
				
				// If we're connecting this chain to the start location of the bone in the 'host' chain...
				if (thisChain.getBoneConnectionPoint() == BoneConnectionPoint.START)
				{
					// ...set the base location of this bone to be the start location of the bone it's connected to.
					thisChain.setBaseLocation( hostBone.getStartLocation() );
				}
				else // If the bone connection point is BoneConnectionPoint.END...
				{	
					// ...set the base location of the chain to be the end location of the bone we're connecting to.
					thisChain.setBaseLocation( hostBone.getEndLocation() );
				}
				
				// If the basebone is constrained to the direction of the bone it's connected to...
				Vec2f hostBoneUV = hostBone.getDirectionUV();
				if (constraintType == BaseboneConstraintType2D.LOCAL_RELATIVE)
				{	
					// ...then set the basebone constraint UV to be the direction of the bone we're connected to.
					mChains.get(loop).setBaseboneConstraintUV(hostBoneUV);
				}				
				else if (constraintType == BaseboneConstraintType2D.LOCAL_ABSOLUTE)
				{	
					// Note: LOCAL_ABSOLUTE directions are directions which are in the local coordinate system of the host bone.
					// For example, if the baseboneConstraintUV is Vec2f(-1.0f, 0.0f) [i.e. left], then the baseboneConnectionConstraintUV
					// will be updated to be left with regard to the host bone.
					
					// Get the angle between UP and the hostbone direction
					float angleDegs = UP.getSignedAngleDegsTo(hostBoneUV);
					
					// ...then apply that same rotation to this chain's basebone constraint UV to get the relative constraint UV... 
					Vec2f relativeConstraintUV = Vec2f.rotateDegs( thisChain.getBaseboneConstraintUV(), angleDegs);
					
					// ...which we then update.
					thisChain.setBaseboneRelativeConstraintUV(relativeConstraintUV);					
				}
				
				// NOTE: If the basebone constraint type is NONE then we don't do anything with the basebone constraint of the connected chain.
				
			} // End of if chain is connected to another chain section
			
			// Update the target and solve the chain
			if ( !thisChain.getEmbeddedTargetMode() )
			{
				thisChain.solveForTarget(newTargetLocation);	
			}
			else
			{
				thisChain.solveForEmbeddedTarget();
			}			
			
		} // End of loop over chains
		
	} // End of updateTarget method

	/**
	 * Add a FabrikChain2D object to a FabrikStructure2D object.
	 * <p>
	 * Adding a chain using this method adds the chain to the structure, but does not connect it to any existing chain in the structure.
	 * However, all chains in a structure share the same target, and all chains in the structure can be solved for the target location
	 * via a single call to updateTarget on this structure.
	 *  
	 * @param  chain	(FabrikChain2D)	The FabrikChain2D to add to this structure.
	 **/
	@Override
	public void addChain(FabrikChain2D chain)
	{
		mChains.add(chain);		
	}	
	
	/**
	 * Add a chain to this structure which is connected to a specific bone of an existing chain in this structure.
	 * <p>
	 * When connecting a chain to an existing chain in a structure, the multiple chains will all share the same
	 * target location / end effector.
	 * <p>
	 * The location of the bones in the chain to be connected should be specified relative to the origin (0.0f, 0.0f),
	 * not relative to the current location of connection point in the chain being connected to. On connection, all
	 * bones are translated (that is, moved horizontally and vertically) so that they're positioned relative to the
	 * end location of the specified bone in the specified chain.
	 * <p> 
	 * Both chains within the structure and bones within a chain are zero. If the chainNumber or boneNumber specified
	 * do not exist then an IllegalArgumentException is thrown.
	 * 
	 * @param	chain		The chain to connect to this structure
	 * @param	chainNumber The zero indexed number of the chain to connect the provided chain to.
	 * @param	boneNumber	The zero indexed number of the bone within the specified chain to connect the provided chain to.
	 */
	@Override
	public void connectChain(FabrikChain2D chain, int chainNumber, int boneNumber)
	{	
		// Does this chain exist? If not throw an IllegalArgumentException
		if (chainNumber >= this.mChains.size())
		{
			throw new IllegalArgumentException("Cannot connect to chain " + chainNumber + " - no such chain (remember that chains are zero indexed).");
		}
		
		// Do we have this bone in the specified chain? If not throw an IllegalArgumentException
		if ( boneNumber >= mChains.get(chainNumber).getNumBones() )
		{
			throw new IllegalArgumentException("Cannot connect to bone " + boneNumber + " of chain " + chainNumber + " - no such bone (remember that bones are zero indexed).");
		}
				
		// Note: Any basebone constraint type is fine for a connected chain
		
		// The chain as we were provided should be centred on the origin, so we must now make it
		// relative to the connection point in the given chain.		
		FabrikChain2D relativeChain = new FabrikChain2D(chain);		
		relativeChain.setConnectedChainNumber(chainNumber);
		relativeChain.setConnectedBoneNumber(boneNumber);
		
		// Get the connection point so we know to connect at the start or end location of the bone we're connecting to
		BoneConnectionPoint connectionPoint = chain.getBoneConnectionPoint();		
		Vec2f connectionLocation;
		if (connectionPoint == BoneConnectionPoint.START)
		{
			connectionLocation = mChains.get(chainNumber).getBone(boneNumber).getStartLocation();
		}
		else // If it's BoneConnectionPoint.END then we set the connection point to be the end location of the bone we're connecting to
		{
			connectionLocation = mChains.get(chainNumber).getBone(boneNumber).getEndLocation();
		}		
		relativeChain.setBaseLocation(connectionLocation);
		
		// When we have a chain connected to a another 'host' chain, the chain is which is connecting in
		// MUST have a fixed base, even though that means the base location is 'fixed' to the connection
		// point on the host chain, rather than a static location.
		relativeChain.setFixedBaseMode(true);
		
		// Translate the chain we're connecting to the connection point
		for (int loop = 0; loop < chain.getNumBones(); ++loop)
		{
			Vec2f origStart = relativeChain.getBone(loop).getStartLocation();
			Vec2f origEnd   = relativeChain.getBone(loop).getEndLocation();
			
			Vec2f translatedStart = origStart.plus(connectionLocation);
			Vec2f translatedEnd   = origEnd.plus(connectionLocation);
			
			relativeChain.getBone(loop).setStartLocation(translatedStart);
			relativeChain.getBone(loop).setEndLocation(translatedEnd);
		}
		
		this.addChain(relativeChain);
	}
	
	/**
	 * Connect a chain to an existing chain in the structure.
	 * 
	 * @param	chain				The chain which we will connect to this structure.
	 * @param	chainNumber			The zero-indexed number of the pre-existing chain in this structure that the new chain will attach to.
	 * @param	boneNumber			The zero-indexed number of bone in the pre-existing chain in this structure that the new chain will attach to.
	 * @param	boneConnectionPoint	Whether to attach the new chain to the start or end point of the bone we are connecting to.
	 */
	@Override
	public void connectChain(FabrikChain2D chain, int chainNumber, int boneNumber, BoneConnectionPoint boneConnectionPoint)
	{
		// Set the bone connection point so we'll connect to the start or the end point of the specified connection bone
		chain.setBoneConnectionPoint(boneConnectionPoint);
		
		// Call the standard addConnectedChain method to perform the connection
		connectChain(chain, chainNumber, boneNumber);		
	}

	/**
	 * Return the number of chains in this structure.
	 * 
	 * @return	The number of chains in this structure.
	 */
	@Override
	public int getNumChains() { return this.mChains.size(); }
	
	/**
	 * Return a chain which exists in this structure.
	 * <p>
	 * Note: Chains are zero-indexed.
	 * 
	 * @param	chainNumber	The zero-indexed chain in this structure to return.
	 * @return				The desired chain.
	 */
	@Override
	public FabrikChain2D getChain(int chainNumber) { return mChains.get(chainNumber); }
	
	/**
	 * Set the fixed-base mode on the first chain in this structure.
	 * 
	 * If disabling fixed-base mode and the first chain has a BaseBoneConstraintType of GLOBAL_ABSOLUTE
	 * then a RuntimeException will be thrown as this these options are mutually exclusive.
	 * 
	 * @param	fixedBaseMode	Whether we want this structure to operate in fixed-base mode (true) or not (false).
	 */
	public void setFixedBaseMode(boolean fixedBaseMode)
	{
		// Update our flag and set the fixed base mode on the first (i.e. 0th) chain in this structure.
		mFixedBaseMode = fixedBaseMode;		
		mChains.get(0).setFixedBaseMode(mFixedBaseMode);
	}
	
	@Override
	public String getName() {
		return this.mName;
	}

	/**
	 * Return a concise, human readable description of this FabrikStructure2D. 
	 * <p>
	 * If further details on a specific chain are required, then you should get and print each chain individually.
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("----- FabrikStructure2D: " + mName + " -----" + Utils.NEW_LINE);		
		sb.append("Number of chains: " + this.mChains.size()              + Utils.NEW_LINE);
		for (int loop = 0; loop < this.mChains.size(); ++loop)
		{
			sb.append(mChains.get(loop).toString() );
		}

		return sb.toString();
	}

} // End of FabrikStructure2D class
