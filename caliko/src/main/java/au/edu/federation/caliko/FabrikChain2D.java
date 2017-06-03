package au.edu.federation.caliko;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import au.edu.federation.caliko.FabrikChain2D.BaseboneConstraintType2D;
import au.edu.federation.utils.Colour4f;
import au.edu.federation.utils.Utils;
import au.edu.federation.utils.Vec2f;

/**
 * Class to represent a 2D Inverse Kinematics (IK) chain that can be solved for a given target using the FABRIK algorithm.
 *  
 * @author Al Lansley
 * @version 1.1 - 02/08/2016
 */
@XmlRootElement(name="2dchain")
@XmlAccessorType(XmlAccessType.NONE)
public class FabrikChain2D implements FabrikChain<FabrikBone2D,Vec2f,FabrikJoint2D,BaseboneConstraintType2D>
{	
	/**
	 * Basebone constraint types.
	 *
	 * Basebone constraints constrain the basebone (i.e. the first bone in a chain) and may be of the following types:
	 * <ul>
	 * <li>NONE - No constraint.</li>
	 * <li>GLOBAL_ABSOLUTE - Constrained about a world-space direction.</li>
	 * <li>LOCAL_RELATIVE - Constrained about the direction of the connected bone.</li>
	 * <li>LOCAL_ABSOLUTE - Constrained about a direction with relative to the direction of the connected bone.</li>
	 * </ul>
	 * 
	 * @see #setBaseboneConstraintType(BaseboneConstraintType2D)
	 */
	public enum BaseboneConstraintType2D	implements BaseboneConstraintType { NONE,	GLOBAL_ABSOLUTE, LOCAL_RELATIVE, LOCAL_ABSOLUTE }
	
	// ---------- Private Properties ----------

	/**
	 * The core of a FabrikChain2D is a list of FabrikBone2D objects, where each bone contains a start and end location, and a joint
	 * that stores any rotational constraints.
	 */
	@XmlElementWrapper(name="2dbones")
	@XmlElement(name="2dbone")	
	private List<FabrikBone2D> mChain = new ArrayList<>();

	/** 
	 * The name of this FabrikChain2D object.
	 * <p>
	 * Although entirely optional, it may be used to uniquely identify a specific FabrikChain2D in an an array/vector/map
	 * or such.
	 * 
	 * @see #setName(String)
	 * @see #getName(String)
	 */
	private String mName;

	/** 
	 * The distance threshold we must meet in order to consider this FabrikChain2D to be successfully solved for distance.
	 * <p>
	 * When we solve a chain so that the distance between the end effector and target is less than or equal to the distance
	 * threshold, then we consider the chain to be successfully solved and will dynamically abort any further attempts to solve the chain.
	 * <p>
	 * The minimum valid distance threshold is 0.0f, however a slightly higher value should be used to avoid forcing the IK
	 * chain solve process to run repeatedly when an <strong>acceptable</strong> (but not necessarily <em>perfect</em>)
	 * solution is found.
	 * <p>	
	 * Although this property is the main criteria used to establish whether or not we have solved a given IK chain, it works
	 * in combination with the {@link #mMaxIterationAttempts} and {@link mMinIterationChange} fields to improve the
	 * performance of the algorithm in situations where we may not be able to solve a given IK chain. Such situations may arise
	 * when bones in the chain are highly constrained, or when the target is further away than the length of a chain which has
	 * a fixed base location.
	 * 
	 * @default 1.0f
	 * 
	 * @see {@link #setSolveDistanceThreshold(float)}
	 */
	private float mSolveDistanceThreshold = 1.0f;

	/** 
	 * Specifies the maximum number of attempts that will be performed in order to solve this FabrikChain2D.
	 *
	 * As FABRIK is an iterative algorithm, after a single solve attempt we may not be within the solve distance threshold for the chain, so must take
	 * the current chain configuration and perform another solve attempt. With a reasonable value for the solve distance threshold (i.e. the distance which
	 * we must solve the chain for in order to consider it solved), it is common to be able to solve a given IK chain within a small number of attempts.
	 *
	 * However, some IK chains cannot be solved, whether this is due to joint constraints, or (in the case of an IK chain with a fixed base location)
	 * where the distance between the base location and the target is greater than the length of the chain itself. In these cases, we may abort
	 * any further attempts to solve the chain after this number of solve attempts. This decreases the execution time of the algorithm, as there
	 * is no point in repeatedly attempting to solve an IK chain which cannot be solved.
	 * <p>
	 * Further, we may find that we are making unacceptably low progress towards solving the chain, then will also abort solving the chain based on the
	 * {@link mMinIterationChange} threshold.
	 * 
	 * @default 15
	 * @see {@link #setMaxIterationAttempts(int)}
	 */
	private int mMaxIterationAttempts  = 15;

	/** 
	 * Specifies the minimum change in the solve distance which must be made from one solve attempt to the next in order for us to believe it
	 * worthwhile to continue making attempts to solve the IK chain.
	 * <p>
	 * As FABRIK is an iterative algorithm, after a single solve attempt we may not be within the solve distance threshold for the chain, so must take
	 * the current chain configuration and perform another solve attempt. With a reasonable value for the solve distance threshold (i.e. the distance which
	 * we must solve the chain for in order to consider it solved), it is common to be able to solve a given IK chain within a small number of attempts.
	 * <p>
	 * However, some IK chains cannot be solved, whether this is due to joint constraints, or (in the case of an IK chain with a fixed base location)
	 * where the distance between the base location and the target is greater than the length of the chain itself. In these cases, we abort attempting
	 * to solve the chain if we are making very little progress towards solving the chain between solve attempts. This decreases the execution time of
	 * the algorithm, as there  is no point in repeatedly attempting to solve an IK chain which cannot be solved.
	 * <p> 
	 * This property must be greater than or equal to zero.
	 *
	 * @default 0.01f
	 * @see {@link #setMinIterationChange(float)}
	 */
	private float mMinIterationChange = 0.01f;

	/**
	 * The chainLength is the combined length of all bones in this FabrikChain2D object.
	 *
	 * When a FabrikBone2D is added or removed from the chain using the addBone/addConsecutiveBone or removeBone methods, then
	 * the chainLength is updated to reflect this. 
	 */
	private float mChainLength;

	/** 
	 * The location of the start joint of the first bone added to the chain.
	 * <p>
	 * By default, FabrikChain2D objects are created with a fixed base location, that is, the start joint of the first bone in the chain is
	 * immovable. As such, the start joint location of the first bone in the chain is stored in this property so that the base of the chain
	 * can be snapped back to this location during the FABRIK solve process.
	 * <p>
	 * Base locations do not <em>have</em> to be fixed - this property can be modified to via the setFixedBaseLocation method.
	 * 
	 * @see {@link #setFixedBaseMode(boolean)}
	 */	
	private Vec2f mBaseLocation = new Vec2f();


	/** mFixedBaseMode	Whether this FabrikChain2D has a fixed (i.e. immovable) base location.
	 *
	 * By default, the location of the start joint of the first bone added to the IK chain is considered fixed. This
	 * 'anchors' the base of the chain in place. Optionally, a user may call the setFixedBaseMode method to disable
	 * fixing the base bone start location in place.
	 * 
	 * @default true
	 * @see {@link #setFixedBaseMode(boolean)}
	 */
	private boolean mFixedBaseMode = true;
	
	/**
	 * The type of constraint applied to the basebone of this chain.
	 * 
	 * @default BaseboneConstraintType2D.NONE
	 */
	private BaseboneConstraintType2D mBaseboneConstraintType = BaseboneConstraintType2D.NONE;
	
	/**
	 * If this chain is connected to a bone in another chain, then this property controls whether
	 * it should connect to the start end location of the host bone.
	 * 
	 * @default BoneConnectionPoint.END
	 * @see {@link #setBoneConnectionPoint(BoneConnectionPoint)}
	 * {see {@link FabrikStructure2D#connectChain(FabrikChain2D, int, int, BoneConnectionPoint)}
	 */
	private BoneConnectionPoint mBoneConnectionPoint = BoneConnectionPoint.END;
	
	/**
	 * The direction around which we should constrain the base bone, as provided to the setBaseboneConstraintUV method.
	 * 
	 * @see {@link #setBaseboneConstraintUV(Vec2f)} 
	 */
	private Vec2f mBaseboneConstraintUV = new Vec2f();
	
	/**
	 * The (internally used) constraint unit vector when this chain is connected to another chain in either
	 * LOCAL_ABSOLUTE or LOCAL_RELATIVE modes.
	 * <p>
	 * This property cannot be accessed by users and is updated in the FabrikStructure2D.updateForTarget() method.
	 */
	private Vec2f mBaseboneRelativeConstraintUV = new Vec2f();
	
	/**
	 * mLastTargetLocation	The last target location for the end effector of this IK chain.
	 * <p>
	 * The last target location is used during the solve attempt and is specified as a property of the chain to avoid
	 * memory allocation at runtime.
	 */
	private Vec2f mLastTargetLocation = new Vec2f(Float.MAX_VALUE, Float.MAX_VALUE);
	
	/** 
	 * mLastBaseLocation - The previous base location of the chain from the last solve attempt.
	 * 
	 * @default Vec2f(Float.MAX_VALUE, Float.MAX_VALUE)
	 * @see {@link setFixedBaseLocation}
	 */	
	private Vec2f mLastBaseLocation = new Vec2f(Float.MAX_VALUE, Float.MAX_VALUE);
	
	/**
	 * mEmbeddedTarget	An embedded target location which can be used to solve this chain.
	 * <p>
	 * Embedded target locations allow structures to be solved for multiple targets (one per chain in the structure)
	 * rather than all chains being solved for the same target. To use embedded targets, the mUseEmbeddedTargets flag
	 * must be true (which is not the default) - this flag can be set via a call to setEmbeddedTarget(true).
	 * 
	 * @see {@link useEmbeddedTarget(boolean) }
	 */
	private Vec2f mEmbeddedTarget = new Vec2f();
	
	/**
	 * mUseEmbeddedTarget	Whether or not to use the mEmbeddedTarget location when solving this chain.
	 * <p>
	 * This flag may be toggled by calling the setEmbeddedTargetMode(boolean) method on the chain.
	 * 
	 * @default false
	 * @see {@link setEmbeddedTargetMode(boolean) }
	 */
	private boolean mUseEmbeddedTarget = false;
	
	/**
	 * mCurrentSolveDistance	The current distance between the end effector and the target location for this IK chain.
	 * <p>
	 * The current solve distance is updated when an attempt is made to solve IK chain as triggered by a call to the
	 * {@link updateForTarget(Vec2f)} or (@link updateForTarget(float, float) methods.
	 */
	private float mCurrentSolveDistance = Float.MAX_VALUE;
	
	/**
	 * The zero-indexed number of the chain this chain is connected to in a FabrikStructure2D.
	 * <p>	 * 
	 * If this value is -1 (default) then this chain is not connected to another chain.
	 * 
	 * @default -1
	 */
	private int mConnectedChainNumber = -1;
	
	/**
	 * The zero-indexed number of the bone that this chain is connected to, if it's connected to another chain at all.
	 * <p>
	 * If the value is -1 (default) then it's not connected to another bone or chain.
	 * 
	 * @default -1
	 */ 
	private int mConnectedBoneNumber  = -1;

	// ---------- Constructors ----------

	/** Default constructor. */
	public FabrikChain2D() { }
	
	/**
	 * Naming constructor.
	 * 
	 * @param	name	The name of this chain.
	 */
	public FabrikChain2D(String name) {	mName = name; }
	
	/**
	 * Copy constructor.
	 *
	 * @param	source	The FabrikChain2D to use as the basis for this new FabrikChain2D.
	 */
	public FabrikChain2D(FabrikChain2D source)
	{
		// Force copy by value
		mChain = source.cloneChainVector();
		mBaseLocation.set(source.mBaseLocation);
		mLastTargetLocation.set(source.mLastTargetLocation);
		mLastBaseLocation.set(source.mLastBaseLocation);
		mBaseboneConstraintUV.set(source.mBaseboneConstraintUV);
		mBaseboneRelativeConstraintUV.set(source.mBaseboneRelativeConstraintUV);	
		mEmbeddedTarget.set(source.mEmbeddedTarget);
		
		// Native copy by value for primitive members
		mChainLength            = source.mChainLength;
		mCurrentSolveDistance   = source.mCurrentSolveDistance;
		mConnectedChainNumber   = source.mConnectedChainNumber;
		mConnectedBoneNumber    = source.mConnectedBoneNumber;
		mBaseboneConstraintType = source.mBaseboneConstraintType;
		mBoneConnectionPoint    = source.mBoneConnectionPoint;
		mName                   = source.mName;
		mUseEmbeddedTarget      = source.mUseEmbeddedTarget;
	}

	// ---------- Public Methods ------------

	/**
	 * Add a bone to the end of this IK chain of this FabrikChain2D object.
	 * <p>
	 * This chain's {@link mChainLength} property is updated to take into account the length of the
	 * new bone added to the chain.
	 * <p>
	 * In addition, if the bone being added is the very first bone, then this chain's
	 * {@link mBaseLocation} property is set from the start joint location of the bone.
	 * 
	 * @param	bone	The FabrikBone2D object to add to this FabrikChain2D.
	 * @see		#mChainLength
	 * @see		#mBaseLocation
	 */
	@Override
	public void addBone(FabrikBone2D bone)
	{
		// Add the new bone to the end of the chain
		mChain.add(bone);

		// If this is the basebone...
		if (this.mChain.size()==1)
		{
			// ...then keep a copy of the fixed start location...
			mBaseLocation.set( bone.getStartLocation() );
			
			// ...and set the basebone constraint UV to be around the bone direction
			mBaseboneConstraintUV = bone.getDirectionUV();
		}

		// ...and update the desired length of the chain (i.e. combined length of all bones)
		updateChainLength();
	}

	/**
	 * Add a constrained bone to the end of this IK chain.
	 * <p>
	 * This method can only be used when the IK chain contains a base bone, as without it we do not
	 * have a start location for this bone (i.e. the end location of the previous bone).
	 * <p>
	 * If this method is called on a chain which does not contain a base bone
	 * then a {@link RuntimeException} is thrown.
	 * <p>
	 * Specifying a direction unit vector of zero will result in an IllegalArgumentException being thrown.
	 * 
	 * @param	directionUV			The initial direction of the new bone
	 * @param	length				The length of the new bone
	 * @param	clockwiseDegs		The clockwise constraint angle in degrees.
	 * @param	anticlockwiseDegs	The anti-clockwise constraint angle in degrees.
	 */
	public void addConsecutiveConstrainedBone(Vec2f directionUV, float length, float clockwiseDegs, float anticlockwiseDegs)
	{
		addConsecutiveConstrainedBone( directionUV, length, clockwiseDegs, anticlockwiseDegs, new Colour4f() );
	}
	
	/**
	 * Add a constrained bone to the end of this IK chain.
	 * <p>
	 * This method can only be used when the IK chain contains a base bone, as without it we do not
	 * have a start location for this bone (i.e. the end location of the previous bone).
	 * <p>
	 * If this method is called on a chain which does not contain a base bone
	 * then a {@link RuntimeException} is thrown.
	 * <p>
	 * Specifying a direction unit vector of zero will result in an IllegalArgumentException being thrown.
	 * 
	 * @param	directionUV			The initial direction of the new bone
	 * @param	length				The length of the new bone
	 * @param	clockwiseDegs		The clockwise constraint angle in degrees.
	 * @param	anticlockwiseDegs	The anti-clockwise constraint angle in degrees.
	 * @param	colour				The colour to draw the bone.
	 */
	public void addConsecutiveConstrainedBone(Vec2f directionUV, float length, float clockwiseDegs, float anticlockwiseDegs, Colour4f colour)
	{
		// Validate the direction unit vector - throws an IllegalArgumentException if it has a magnitude of zero
		Utils.validateDirectionUV(directionUV);
		
		// Validate the length of the bone - throws an IllegalArgumentException if it is not a positive value
		Utils.validateLength(length);
				
		// If we have at least one bone already in the chain...
		if (!this.mChain.isEmpty())
		{		
			// Get the end location of the last bone, which will be used as the start location of the new bone
			Vec2f prevBoneEnd = mChain.get(this.mChain.size()-1).getEndLocation();
				
			// Add a bone to the end of this IK chain
			addBone( new FabrikBone2D(prevBoneEnd, Vec2f.normalised(directionUV), length, clockwiseDegs, anticlockwiseDegs, colour) );
		}
		else // Attempting to add a relative bone when there is no base bone for it to be relative to?
		{
			throw new RuntimeException("You cannot add the base bone to a chain using this method as it does not provide a start location.");
		}
	}

	/**
	 * Add a unconstrained bone to the end of this IK chain.
	 * <p>
	 * This method can only be used when the IK chain contains a base bone, as without it we do not
	 * have a start location for this bone (i.e. the end location of the previous bone).
	 * <p>
	 * If this method is called on a chain which does not contain a base bone
	 * then a {@link RuntimeException} is thrown.
	 * <p>
	 * Specifying a direction unit vector of zero will result in an IllegalArgumentException being thrown.
	 * 
	 * @param	directionUV			The initial direction of the new bone
	 * @param	length				The length of the new bone
	 */
	@Override
	public void addConsecutiveBone(Vec2f directionUV, float length)
	{
		addConsecutiveConstrainedBone( directionUV, length, 180.0f, 180.0f, new Colour4f() );
	}	
	
	/**
	 * Return the basebone constraint type of this chain.
	 * 
	 * @return The basebone constraint type of this chain.
	 */
	@Override
	public BaseboneConstraintType2D getBaseboneConstraintType() { return mBaseboneConstraintType; }
	
	/**
	 * Return the basebone constraint unit vector.
	 * 
	 * @return  The basebone constraint unit vector of this FabrikChain2D.
	 */
	@Override
	public Vec2f getBaseboneConstraintUV() { return mBaseboneConstraintUV; }
	
	/**
	 * Return 
	 * <p>
	 * Regardless of how many bones are contained in the chain, the base location is always the start location of the first bone in the chain.
	 * <p>
	 * If there are no bones in the chain when this method is called then a RuntimeException is thrown.
	 * 
	 * @return	The base location of this FabrikChain2D.
	 */
	@Override
	public Vec2f getBaseLocation() 
	{
		if (!this.mChain.isEmpty())
		{
			return mChain.get(0).getStartLocation();
		}
		else
		{
			throw new RuntimeException("Cannot get base location as there are zero bones in the chain.");
		}
	}	

	/**
	 * Return a bone by its location in the IK chain.
	 * <p>
	 * The base bone is always bone 0, each additional bone increases the number by 1.
	 * 
	 * @param	boneNumber	The number of the bone to return from the Vector of FabrikBone2D objects.
	 * @return	The FabrikBone2D at the given location in this chain. 
	 */
	@Override
	public FabrikBone2D getBone(int boneNumber)
	{
		return mChain.get(boneNumber);
	}
	
	/**
	 * Return the bone connection point of this chain.
	 * 
	 * The connection point will be either BoneConnectionPoint2D.START or BoneConnectionPoint2D.END.
	 *
	 * @return	The bone connection point of this chain.
	 */
	public BoneConnectionPoint getBoneConnectionPoint() { return mBoneConnectionPoint; }
	
	/**
	 * Return the actual IK chain of this FabrikChain2D object,
	 * 
	 * @return		The IK chain of this FabrikChain2D as a List of FabrikBone2D objects.
	 */
	@Override
	public List<FabrikBone2D> getChain() { return mChain; }

	/**
	 * Return the current length of this IK chain.
	 * <p>
	 * This method does not dynamically re-calculate the length of the chain - it merely returns the previously
	 * calculated chain length, which gets updated each time a bone is added to the chain.
	 * 
	 * @return	The length of this IK chain as stored in the mChainLength property.
	 */
	@Override
	public float getChainLength() { return mChainLength; }
	
	/**
	 * Return the number of the bone in another chain that this FabrikChain2D is connected to.
	 *
	 * @return The connected bone number as stored in the mConnectedBoneNumber property.
	 */
	@Override
	public int getConnectedBoneNumber() { return mConnectedBoneNumber; }
	
	/**
	 * Return the number of the chain in the structure containing this FabrikChain2D that this chain is connected to.
	 *
	 * @return The connected chain number as stored in the mConnectedChainNumber property.
	 */
	@Override
	public int getConnectedChainNumber() { return mConnectedChainNumber; }
	
	/**
	 * Return the location of the end effector in the IK chain.
	 * <p>
	 * Regardless of how many bones are contained in the chain, the end effector is always
	 * the end location of the final bone in the chain.
	 * <p>
	 * If there are no bones in the chain when this method is called then a RuntimeException is thrown.
	 *  
	 * @return	Vec2f
	 */
	@Override
	public Vec2f getEffectorLocation()
	{
		if (!this.mChain.isEmpty())
		{
			return mChain.get(this.mChain.size()-1).getEndLocation();
		}
		else
		{
			throw new RuntimeException("Cannot get effector location as there are zero bones in the chain.");
		}
	}
	
	/**
	 * Return whether or not this chain uses an embedded target.
	 * 
	 * Embedded target mode may be enabled or disabled using setEmbeddededTargetMode(boolean).
	 * 
	 * @return whether or not this chain uses an embedded target.
	 */
	@Override
	public boolean getEmbeddedTargetMode() { return mUseEmbeddedTarget; }
	
	/**
	 * Return the embedded target location.
	 * 
	 * @return the embedded target location.
	 */
	@Override
	public Vec2f getEmbeddedTarget() { return mEmbeddedTarget; }
	
	/**
	 * Return the last target location for which we  attempted to solve this IK chain.
	 * <p>
	 * The last target location and the current target location may not be the same.
	 * 
	 * @return	The last target location that we attempted to solve this chain for.
	 */
	@Override
	public Vec2f getLastTargetLocation() { return mLastTargetLocation; }
	
	/**
	 * Return the name of this FabrikChain2D.
	 * 
	 * @return	The name of his chain, or <strong>null</strong> if the name has not been set.
	 * 
	 * @see #setName(String)
	 * @see #getName()
	 */
	@Override
	public String getName() { return mName; }
	
	/**
	 * Return the number of bones in the IK chain.
	 * <p>
	 * Bones may be added to the chain via the addBone or addConsecutiveBone methods.
	 * 
	 * @return	The number of bones in the FabrikChain2D.
	 */
	@Override
	public int getNumBones() { return this.mChain.size(); }
	
	/**
	 * Remove a bone from this IK chain.
	 * <p>
	 * Bone numbers start at 0, so if there were 3 bones in a chain they would be at locations 0, 1 and 2.
	 * <p> 
	 * This chain's {@link mChainLength} property is updated to take into account the new chain length.
	 * <p>
	 * If this chain does not contain a bone at the specified location then an IllegalArgumentException is thrown.
	 * 
	 * @param	boneNumber	The zero-indexed bone to remove from this IK chain.
	 * @see		#mChainLength
	 * @see		#mBaseLocation
	 */
	@Override
	public void removeBone(int boneNumber)
	{
		// If the bone number is a bone which exists...
		if (boneNumber < this.mChain.size())
		{
			
			// ...then remove the bone, decrease the bone count and update the chain length.
			mChain.remove(boneNumber);
			updateChainLength();
		}
		else
		{
			throw new IllegalArgumentException("Bone " + boneNumber + " does not exist in this chain.");
		}
	}
	
	/** Set the basebone constraint type on this chain.
	 * 
	 * @param type The BaseboneConstraintType2D to set.
	 */
	public void setBaseboneConstraintType(BaseboneConstraintType2D type) { mBaseboneConstraintType = type; }

	/**
	 * Set the constraint unit vector for the basebone of this chain..
	 * <p>
	 * The bone will be constrained about the clockwise and anticlockwise constraint angles with regard to this
	 * direction methods on the FabrikBone2D to be constrained.
	 * 
	 * @param  constraintUV  The direction unit vector to constrain the base bone to.
	 * @see #setBaseboneConstraintType(BaseboneConstraintType2D)
	 * @see au.edu.federation.caliko.FabrikJoint2D#setClockwiseConstraintDegs
	 * @see au.edu.federation.caliko.FabrikJoint2D#setAnticlockwiseConstraintDegs
	 */
	@Override
	public void setBaseboneConstraintUV(Vec2f constraintUV)
	{
		// Sanity checking
		Utils.validateDirectionUV(constraintUV);
		
		// All good? Then normalise the constraint direction and set it
		mBaseboneConstraintUV.set( Vec2f.normalised(constraintUV) );
	}

	/**
	 * Set the base location of this chain.
	 *
	 * @param	baseLocation	The location to set the mBaseLocation property.
	 * @see		#mBaseLocation
	 */
	public void setBaseLocation(Vec2f baseLocation) { mBaseLocation.set(baseLocation); }
	
	/**
	 * Set the bone connection point of this chain.
	 * <p>
	 * When this chain is connected to another chain, it may either connect to the bone in the other chain at
	 * the start (BoneConnectionPoint.START) or end (BoneConnectionPoint.END) of the bone.
	 * <p>
	 * This property is held per-chain rather than per-bone for efficiency.
	 * 
	 * @param	boneConnectionPoint	The BoneConnectionPoint to set on this chain.
	 */
	public void setBoneConnectionPoint(BoneConnectionPoint boneConnectionPoint) { mBoneConnectionPoint = boneConnectionPoint; }

	/**
	 * Set the List%lt;FabrikBone2D%gt; of this FabrikChain2D to the provided list by reference.
	 * 
	 * @param	chain	The List%lt;FabrikBone2D%gt; of FabrikBone2D objects to assign to this chain.
	 * @see		#mChain
	 */
	public void setChain(List<FabrikBone2D> chain)
	{
		// Assign this chain to be a reference to the chain provided as an argument to this method
		this.mChain = chain;
	}
	
	/**
	 * Set the colour of all bones in this chain to the specified colour.
	 * 
	 * @param	colour	The colour to set on all bones in this chain.
	 */
	public void setColour(Colour4f colour)
	{			
		for (FabrikBone2D aBone : this.mChain)
		{
			aBone.setColour(colour);
		}
	}
	
	/**
	 * Set which number bone this chain is connected to in another chain in a FabrikStructure2D object.
	 * 
	 * @param	boneNumber	The number of the bone this chain is connected to.
	 */
	void setConnectedBoneNumber(int boneNumber)	{ mConnectedBoneNumber = boneNumber; }
	
	/**
	 * Set which number chain this chain is connected to in a FabrikStructure2D object.
	 * 
	 * @param	chainNumber	The number of the chain that this chain is connected to.
	 */
	void setConnectedChainNumber(int chainNumber) {	mConnectedChainNumber = chainNumber; }
	
	/**
	 * Set the fixed base bone mode for this chain.
	 * <p>
	 * If the base bone is 'fixed' in place, then its start location cannot move unless it is attached
	 * to another chain (which moves). The bone is still allowed to rotate, with or without constraints.
	 * <p>
	 * Specifying a non-fixed base location while this chain is connected to another chain will result in a
	 * RuntimeException being thrown.
	 * <p>
	 * Fixing the base bone's start location in place and constraining to a global absolute direction are
	 * mutually exclusive. Disabling fixed base mode while the chain's constraint type is
	 * BaseBoneConstraintType2D.GLOBAL_ABSOLUTE will result in a RuntimeException being thrown. 
	 *  
	 * @param  value  Whether or not to fix the base bone start location in place.
	 */
	@Override
	public void setFixedBaseMode(boolean value)
	{	
		if (!value && mConnectedChainNumber != -1)
		{
			throw new RuntimeException("This chain is connected to another chain so must remain in fixed base mode.");
		}
		
		// We cannot have a freely moving base location AND constrain the base bone - so if we have both
		// enabled then we disable the base bone angle constraint here.
		if (mBaseboneConstraintType == BaseboneConstraintType2D.GLOBAL_ABSOLUTE && !value)
		{
			throw new RuntimeException("Cannot set a non-fixed base mode when the chain's constraint type is BaseBoneConstraintType2D.GLOBAL_ABSOLUTE.");
		}
		
		// Above conditions met? Set the fixedBaseMode
		mFixedBaseMode = value;
	}

	/**
	 * Set the maximum number of attempts that will be made to solve this IK chain.
	 * <p>
	 * The FABRIK algorithm may require more than a single pass in order to solve
	 * a given IK chain for an acceptable distance threshold. If we reach this
	 * iteration limit then we stop attempting to solve the IK chain.
	 * <p>
	 * Specifying a maxIterations value of less than 1 will result in an IllegalArgumentException is thrown.
	 * 
	 * @param maxIterations  The maximum number of attempts that will be made to solve this IK chain.
	 */
	@Override
	public void setMaxIterationAttempts(int maxIterations)
	{
		// Ensure we have a valid maximum number of iteration attempts
		if (maxIterations < 1)
		{
			throw new IllegalArgumentException("The maximum number of attempts to solve this IK chain must be at least 1.");
		}
		
		// All good? Set the new maximum iteration attempts property
		mMaxIterationAttempts = maxIterations;
	}
	
	/**
	 * Set the minimum iteration change before we dynamically abort any further attempts to solve this IK chain.
	 * <p>
	 * If the current solve distance changes by less than this amount between solve attempt then we consider the
	 * solve process to have stalled and dynamically abort any further attempts to solve the chain to minimise CPU usage.
	 * <p>
	 * If a minIterationChange value of less than zero is provided then an IllegalArgumentException is thrown.
	 * 
	 * @param minIterationChange  The minimum change in solve distance from one iteration to the next.
	 */
	@Override
	public void setMinIterationChange(float minIterationChange)
	{
		// Ensure we have a valid maximum number of iteration attempts
		if (minIterationChange < 0.0f)
		{
			throw new IllegalArgumentException("The minimum iteration change value must be more than or equal to zero.");
		}
		
		// All good? Set the new minimum iteration change distance
		mMinIterationChange = minIterationChange;
	}
	
	/** 
	 * Set the name of this chain, capped to 100 characters if required.
	 * 
	 * @param	name	The name to set.
	 */
	 @Override
	public void setName(String name) { mName = Utils.getValidatedName(name); }

	/**
	 * Set the distance threshold within which we consider the IK chain to be solved.
	 * <p>
	 * If a solve distance of less than zero is provided then an IllegalArgumentException is thrown.
	 * 
	 * @param  solveDistance  The distance between the end effector of this IK chain and target within which we will accept the solution.
	 */
	 @Override
	public void setSolveDistanceThreshold(float solveDistance)
	{
		// Ensure we have a valid solve distance
		if (solveDistance < 0.0f)
		{
			throw new IllegalArgumentException("The solve distance threshold must be greater than or equal to zero.");
		}
		
		// All good? Set the new solve distance threshold
		mSolveDistanceThreshold = solveDistance;
	}
	
	/**
	 * Solve the Inverse Kinematics (IK) problem using the FABRIK algorithm in two dimensions.
	 * <p>
	 * The FABRIK algorithm itself can be found in the following paper:
	 * Aristidou, A., & Lasenby, J. (2011). FABRIK: a fast, iterative solver for the inverse kinematics problem. Graphical Models, 73(5), 243-260.
	 * <p> 
	 * The end result of running this method is that the IK chain configuration is updated (i.e. joint locations for
	 * each bone in the chain are modified in order to solve the chain for the provided target location).
	 * <p>
	 * @param 	target	The target location to solve the IK chain for.
	 * @return	float	The distance between the end effector and the given target location after attempting to solve the IK chain.
	 */
	private float solveIK(Vec2f target)
	{	
		// ---------- Step 1 of 2 - Forward pass from end-effector to base -----------

		// Loop over all bones in the chain, from the end effector (numBones-1) back to the base bone (0)		
		for (int loop = this.mChain.size()-1; loop >= 0; --loop)
		{
			// Get the length of the bone we're working on
			float boneLength = mChain.get(loop).length();

			// If we are not working on the end effector bone
			if (loop != this.mChain.size() - 1)
			{
				// Get the outer-to-inner unit vector of the bone further out
				Vec2f outerBoneOuterToInnerUV = mChain.get(loop+1).getDirectionUV().negated();

				// Get the outer-to-inner unit vector of this bone
				Vec2f thisBoneOuterToInnerUV = mChain.get(loop).getDirectionUV().negated();
				
				// Constrain the angle between the outer bone and this bone.
				// Note: On the forward pass we constrain to the limits imposed by the first joint of the outer bone.
				float clockwiseConstraintDegs     = mChain.get(loop+1).getJoint().getClockwiseConstraintDegs();
				float antiClockwiseConstraintDegs = mChain.get(loop+1).getJoint().getAnticlockwiseConstraintDegs();
				Vec2f constrainedUV = Vec2f.getConstrainedUV(thisBoneOuterToInnerUV, outerBoneOuterToInnerUV, clockwiseConstraintDegs, antiClockwiseConstraintDegs);				

				// At this stage we have a outer-to-inner unit vector for this bone which is within our constraints,
				// so we can set the new inner joint location to be the end joint location of this bone plus the
				// outer-to-inner direction unit vector multiplied by the length of the bone.
				Vec2f newStartLocation = mChain.get(loop).getEndLocation().plus( constrainedUV.times(boneLength) );

				// Set the new start joint location for this bone
				mChain.get(loop).setStartLocation(newStartLocation);

				// If we are not working on the base bone, then we set the end joint location of
				// the previous bone in the chain (i.e. the bone closer to the base) to be the new
				// start joint location of this bone also.
				if (loop > 0)
				{
					mChain.get(loop-1).setEndLocation(newStartLocation);
				}
			}
			else // If we are working on the end effector bone (which we do not constrain during this forward pass)...
			{
				// Snap the end effector's end location to the target
				mChain.get(loop).setEndLocation(target);
				
				// Get the UV between the target / end-location (which are now the same) and the start location of this bone
				Vec2f thisBoneOuterToInnerUV = mChain.get(loop).getDirectionUV().negated();
								
				// Calculate the new start joint location as the end joint location plus the outer-to-inner direction UV
				// multiplied by the length of the bone.
				Vec2f newStartLocation = mChain.get(loop).getEndLocation().plus( thisBoneOuterToInnerUV.times(boneLength) );
				
				// Set the new start joint location for this bone to be new start location...
				mChain.get(loop).setStartLocation(newStartLocation);

				// ...and set the end joint location of the bone further in to also be at the new start location.
				if (loop > 0) { 
				  mChain.get(loop-1).setEndLocation(newStartLocation);
				}
			}

		} // End of forward-pass loop over all bones

		// ---------- Step 2 of 2 - Backward pass from base to end effector -----------

		for (int loop = 0; loop < this.mChain.size(); ++loop)
		{
			// Get the length of the bone we're working on
			float boneLength = mChain.get(loop).length();

			// If we are not working on the base bone
			if (loop != 0)
			{
				// Get the inner-to-outer direction of this bone as well as the previous bone to use as a baseline
				Vec2f thisBoneInnerToOuterUV = mChain.get(loop).getDirectionUV();
				Vec2f prevBoneInnerToOuterUV = mChain.get(loop-1).getDirectionUV();
				
				// Constrain the angle between this bone and the inner bone.
				// Note: On the backward pass we constrain to the limits imposed by the first joint of this bone.
				float clockwiseConstraintDegs     = mChain.get(loop).getJoint().getClockwiseConstraintDegs();
				float antiClockwiseConstraintDegs = mChain.get(loop).getJoint().getAnticlockwiseConstraintDegs();
				Vec2f constrainedUV = Vec2f.getConstrainedUV(thisBoneInnerToOuterUV, prevBoneInnerToOuterUV, clockwiseConstraintDegs, antiClockwiseConstraintDegs);				

				// At this stage we have an inner-to-outer unit vector for this bone which is within our constraints,
				// so we can set the new end location to be the start location of this bone plus the constrained
				// inner-to-outer direction unit vector multiplied by the length of this bone.
				Vec2f newEndLocation = mChain.get(loop).getStartLocation().plus( constrainedUV.times(boneLength) );

				// Set the new end joint location for this bone
				mChain.get(loop).setEndLocation(newEndLocation);

				// If we are not working on the end bone, then we set the start joint location of
				// the next bone in the chain (i.e. the bone closer to the end effector) to be the
				// new end joint location of this bone also.
				if (loop < this.mChain.size()-1)
				{
					mChain.get(loop+1).setStartLocation(newEndLocation);
				}
			}
			else // If we ARE working on the base bone...
			{	
				// If the base location is fixed then snap the start location of the base bone back to the fixed base
				if (mFixedBaseMode)
				{
					mChain.get(0).setStartLocation(mBaseLocation);
				}
				else // If the base location is not fixed...
				{
					// ...then set the new base bone start location to be its the end location minus the
					// bone direction multiplied by the length of the bone (i.e. projected backwards).
					//float boneZeroLength = mChain.get(0).length();
					Vec2f boneZeroUV = mChain.get(0).getDirectionUV();
					Vec2f boneZeroEndLocation = mChain.get(0).getEndLocation();
					Vec2f newBoneZeroStartLocation = boneZeroEndLocation.minus( boneZeroUV.times(boneLength) );
					mChain.get(0).setStartLocation(newBoneZeroStartLocation);
				}
				
				// If the base bone is unconstrained then process it as usual...
				if (mBaseboneConstraintType == BaseboneConstraintType2D.NONE)
				{
					// Get the inner to outer direction of this bone
					Vec2f thisBoneInnerToOuterUV = mChain.get(loop).getDirectionUV();
	
					// Calculate the new end location as the start location plus the direction times the length of the bone
					Vec2f newEndLocation = mChain.get(loop).getStartLocation().plus( thisBoneInnerToOuterUV.times(boneLength) );
	
					// Set the new end joint location
					mChain.get(0).setEndLocation(newEndLocation);
	
					// Also, set the start location of the next bone to be the end location of this bone
					if (mChain.size() > 1) { 
					  mChain.get(1).setStartLocation(newEndLocation);
					}
				}
				else // ...otherwise we must constrain it to the base bone constraint unit vector
				{	
					// Note: The mBaseBoneConstraintUV is either fixed, or it may be dynamically updated from
					// a FabrikStructure2D if this chain is connected to another chain.
					
					// Get the inner-to-outer direction of this bone
					Vec2f thisBoneInnerToOuterUV = mChain.get(loop).getDirectionUV();

					// Get the constrained direction unit vector between the base bone and the base bone constraint unit vector
					// Note: On the backward pass we constrain to the limits imposed by the first joint of this bone.
					float clockwiseConstraintDegs     = mChain.get(loop).getJoint().getClockwiseConstraintDegs();
					float antiClockwiseConstraintDegs = mChain.get(loop).getJoint().getAnticlockwiseConstraintDegs();
					
					// LOCAL_ABSOLUTE? (i.e. local-space directional constraint) - then we must constraint about the relative basebone constraint UV...
					Vec2f constrainedUV;
					if (mBaseboneConstraintType == BaseboneConstraintType2D.LOCAL_ABSOLUTE)
					{
						constrainedUV = Vec2f.getConstrainedUV(thisBoneInnerToOuterUV, mBaseboneRelativeConstraintUV, clockwiseConstraintDegs, antiClockwiseConstraintDegs);
						
//						System.out.println("----- In LOCAL_ABSOLUTE --------");
//						System.out.println("This bone UV = " + thisBoneInnerToOuterUV);
//						System.out.println("Constraint UV = " + mBaseboneConstraintUV);
//						System.out.println("Relative constraint UV = " + mBaseboneRelativeConstraintUV);
					}
					else // ...otherwise we're free to use the standard basebone constraint UV.
					{
						constrainedUV = Vec2f.getConstrainedUV(thisBoneInnerToOuterUV, mBaseboneConstraintUV, clockwiseConstraintDegs, antiClockwiseConstraintDegs);
					}
					
					// At this stage we have an inner-to-outer unit vector for this bone which is within our constraints,
					// so we can set the new end location to be the start location of this bone plus the constrained
					// inner-to-outer direction unit vector multiplied by the length of the bone.
					Vec2f newEndLocation = mChain.get(loop).getStartLocation().plus( constrainedUV.times(boneLength) );

					// Set the new end joint location for this bone
					mChain.get(loop).setEndLocation(newEndLocation);

					// If we are not working on the end bone, then we set the start joint location of
					// the next bone in the chain (i.e. the bone closer to the end effector) to be the
					// new end joint location of this bone.
					if (loop < this.mChain.size()-1)
					{
						mChain.get(loop+1).setStartLocation(newEndLocation);
					}					
				}				

			} // End of base bone handling section

		} // End of backward-pass loop over all bones

		// Update our last target location
		mLastTargetLocation.set(target);
		
		// Finally, get the current effector location...
		Vec2f currentEffectorLocation = mChain.get(this.mChain.size()-1).getEndLocation();
				
		// ...and calculate and return the distance between the current effector location and the target.
		return Vec2f.distanceBetween(currentEffectorLocation, target);
	}
	
	/**
	 * Specify whether we should use the embedded target location when solving the IK chain.
	 * 
	 * @param	value	Whether we should use the embedded target location when solving the IK chain.
	 */
	@Override
	public void setEmbeddedTargetMode(boolean value) { mUseEmbeddedTarget = value; }
	
	/**
	 * Return a concise, human-readable of the IK chain.
	 * @return String
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
			sb.append("----- FabrikChain2D: " + mName + " -----" + Utils.NEW_LINE);				
			sb.append("Number of bones: " + this.mChain.size() + Utils.NEW_LINE);
			
			sb.append("Fixed base mode: ");
			if (mFixedBaseMode) { 
			  sb.append("Yes." + Utils.NEW_LINE); 
			} else { 
			  sb.append("No." + Utils.NEW_LINE); 
			}
			
			sb.append("Base location: " + getBaseLocation() );
		
		return sb.toString();
	} 

	// ---------- Private Methods ----------
	
	/**
	 * Clone and return the IK Chain of this FabrikChain2D, that is, the Vector of FabrikBone2D objects.
	 * <p>
	 * This does not clone the entire FabrikChain2D object, only its IK chain.
	 * 
	 * @return	The clones list of FabrikBone2D objects
	 */
	private List<FabrikBone2D> cloneChainVector()
	{
		// How many bones are in this chain?
		int numBones = mChain.size();
		
		// Create a new Vector of FabrikBone2D objects large enough to contain all the bones in this chain
		List<FabrikBone2D> clonedChain = new ArrayList<>(numBones);

		// For each bone in the chain being cloned...		
		for (int loop = 0; loop < numBones; ++loop)
		{
			// ...use the FabrikBone2D copy constructor to create a new FabrikBone2D with the properties as set from the source bone
			// and add it to the cloned chain
			clonedChain.add(  new FabrikBone2D( mChain.get(loop) ) );
		}
		
		return clonedChain;
	}
	
	/***
	 * Calculate the length of this IK chain by adding up the lengths of each bone.
	 * <p>
	 * The resulting chain length is stored in the mChainLength property.
	 * <p>
	 * This method is called each time a bone is added to the chain. In addition, the
	 * length of each bone is recalculated during the process to ensure that our chain
	 * length is accurate. As the typical usage of a FabrikChain2D is to add a number
	 * of bones once (during setup) and then use them, this should not have any
	 * performance implication on the typical execution cycle of a FabrikChain2D object,
	 * as this method will not be called in any method which executes regularly. 
	 */
	private void updateChainLength()
	{
		mChainLength = 0.0f;
		for (FabrikBone2D aBone : this.mChain)
		{
			mChainLength += aBone.length();
		}
	}
	
	
	/**
	 * Update the embedded target for this chain.
	 * 
	 * The internal mEmbeddedTarget object is updated with the location of the provided parameter.
	 * If the chain is not in useEmbeddedTarget mode then a RuntimeException is thrown.
	 * Embedded target mode can be enabled by calling setEmbeddedTargetMode(true) on the chain.
	 * 
	 * @param newEmbeddedTarget	The location of the embedded target.
	 */
	public void updateEmbeddedTarget(Vec2f newEmbeddedTarget)
	{
		// Using embedded target mode? Overwrite embedded target with provided location
		if (mUseEmbeddedTarget) { 
		  mEmbeddedTarget.set(newEmbeddedTarget); 
		}
		else { 
		  throw new RuntimeException("This chain does not have embedded targets enabled - enable with setEmbeddedTargetMode(true)."); 
		}
	}
	
	/**
	 * Update the embedded target for this chain.
	 * 
	 * The internal mEmbeddedTarget object is updated with the location of the provided parameter.
	 * If the chain is not in useEmbeddedTarget mode then a RuntimeException is thrown.
	 * Embedded target mode can be enabled by calling setEmbeddedTargetMode(true) on the chain.
	 * 
	 * @param x	The x location of the embedded target.
	 * @param y	The y location of the embedded target.
	 */
	public void updateEmbeddedTarget(float x, float y)
	{
		// Using embedded target mode? Overwrite embedded target with provided location
		if (mUseEmbeddedTarget) { 
		  mEmbeddedTarget.set( new Vec2f(x, y) ); 
		}
		else { 
		  throw new RuntimeException("This chain does not have embedded targets enabled - enable with setEmbeddedTargetMode(true)."); 
		}
	}
	
	/**
	 * Solve this IK chain for the current embedded target location.
	 * 
	 * The embedded target location can be updated by calling updateEmbeddedTarget(Vec2f).
	 * 
	 * @return The distance between the end effector and the chain's embedded target location for our best solution.
	 */
	@Override
	public float solveForEmbeddedTarget()
	{
		if (mUseEmbeddedTarget) { 
		  return solveForTarget(mEmbeddedTarget); 
		}
		else { 
		  throw new RuntimeException("This chain does not have embedded targets enabled - enable with setEmbeddedTargetMode(true)."); 
		}
	}
	
	/**
	 * Solve the IK chain for this target to the best of our ability.
	 * <p>
	 * We will iteratively attempt up to solve the chain up to a maximum of mMaxIterationAttempts.
	 * 
	 * This method may return early if any of the following conditions are met:
	 * <ul>
	 * <li>We've already solved for this target location,</li>
	 * <li>We successfully solve for distance, or</li>
	 * <li>We grind to a halt (i.e. low iteration change compared to previous solution).</li>
	 * </ul>
	 * <p>
	 * This method simply constructs a Vec2f from the provided arguments and calls the {@link Vec2f} version of the updateForTarget method.
	 * By making this available, the user does not need to use our custom {@link Vec2f} class in their application.
	 * 
	 * @param targetX	(float)	The x location of the target.
	 * @param targetY	(float) The y location of the target.
	 * @see 	mMaxIterationAttempts
	 * @see 	mSolveDistanceThreshold
	 * @see 	mMinIterationChange
	 * @return	The distance between the end effector and the target for our best solution
	 */
	public float solveForTarget(float targetX, float targetY)
	{
		return solveForTarget( new Vec2f(targetX, targetY) );
	}
	
	/**
	 * Solve the IK chain for this target to the best of our ability.
	 * <p>
	 * We will iteratively attempt up to solve the chain up to a maximum of mMaxIterationAttempts.
	 * 
	 * This method may return early if any of the following conditions are met:
	 * <ul>
	 * <li>We've already solved for this target location,</li>
	 * <li>We successfully solve for distance, or</li>
	 * <li>We grind to a halt (i.e. low iteration change compared to previous solution).</li>
	 * </ul>
	 * 
	 * @param newTarget	The target location to solve this IK chain for.
	 * 
	 * @see 	mMaxIterationAttempts
	 * @see 	mSolveDistanceThreshold
	 * @see 	mMinIterationChange
	 * @return	The distance between the end effector and the target for our best solution
	 */
	public float solveForTarget(Vec2f newTarget)
	{		
		// Same target as before? Abort immediately and save ourselves some cycles
		if ( mLastTargetLocation.approximatelyEquals(newTarget, 0.001f) && mLastBaseLocation.approximatelyEquals(mBaseLocation, 0.001f) )
		{
			return mCurrentSolveDistance;
		}
		
		// Not the same target? Then we must solve the chain for the new target.
		// We'll start by creating a list of bones to store our best solution
		List<FabrikBone2D> bestSolution = new ArrayList<>(this.mChain.size());
		
		// We'll keep track of our best solve distance, starting it at a huge value which will be beaten on first attempt			
		float bestSolveDistance     = Float.MAX_VALUE;
		float lastPassSolveDistance = Float.MAX_VALUE;
		
		// Allow up to our iteration limit attempts at solving the chain
		float solveDistance;
		for (int loop = 0; loop < mMaxIterationAttempts; ++loop)
		{	
			// Solve the chain for this target
			solveDistance = solveIK(newTarget);
			
			// Did we solve it for distance? If so, update our best distance and best solution, and also
			// update our last pass solve distance. Note: We will ALWAYS beat our last solve distance on the first run. 
			if (solveDistance < bestSolveDistance)
			{	
				bestSolveDistance = solveDistance;
				bestSolution = this.cloneChainVector();
				
				// Did we solve for distance? Great! Break out of the loop.
				if (solveDistance <= mSolveDistanceThreshold) { 
				  break; 
				}
			}
			else // Did not solve to our satisfaction? Okay...
			{
				// Did we grind to a halt? If so then it's time to break out of the loop.
				if (Math.abs(solveDistance - lastPassSolveDistance) < mMinIterationChange) { 
				  break; 
				}
			}
			
			// Update the last pass solve distance
			lastPassSolveDistance = solveDistance;
		}
		
		// At this point the best distance and best solution are our stored best values, so set them accordingly on this chain
		mCurrentSolveDistance = bestSolveDistance;
		mChain                = bestSolution;		
		
		// Update our last base and target locations so we know whether we need to solve for this start/end configuration next time
		mLastBaseLocation.set(mBaseLocation);
		mLastTargetLocation.set(newTarget);
		
		// Finally, return the solve distance we ended up with
		return mCurrentSolveDistance;
	}
	
	/**
	 * Return the relative constraint UV about which this bone connects to a bone in another chain.
	 *
	 * @return	The relative constraint UV about which this bone connects to a bone in another chain.
	 */
	@Override
	public Vec2f getBaseboneRelativeConstraintUV() { return mBaseboneRelativeConstraintUV; }
	
	/**
	 * Set the constraint UV about which this bone connects to a bone in another chain.
	 * <p>
	 * This method is used by the FabrikStructure2D.updateForTarget() method.
	 *
	 * @param	constraintUV	The basebone relative constraint unit vector to set.
	 */
	public void setBaseboneRelativeConstraintUV(Vec2f constraintUV) { mBaseboneRelativeConstraintUV.set(constraintUV); }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getMaxIterationAttempts() {
		return this.mMaxIterationAttempts;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public float getMinIterationChange() {
		return this.mMinIterationChange;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public float getSolveDistanceThreshold() {
		return this.mSolveDistanceThreshold;
	}
	

} // End of FabrikChain2D class
