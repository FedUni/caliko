package au.edu.federation.caliko;

import java.util.ArrayList;
import java.util.List;

import au.edu.federation.caliko.FabrikJoint3D.JointType;
import au.edu.federation.caliko.utils.Colour4f;
import au.edu.federation.caliko.utils.Mat3f;
import au.edu.federation.caliko.utils.Utils;
import au.edu.federation.caliko.utils.Vec3f;

/** Class to represent a 3D Inverse Kinematics (IK) chain that can be solved for a given target using the FABRIK algorithm.
 * <p>
 * A FabrikChain3D consists primarily of a list of connected {@link au.edu.federation.caliko.FabrikBone3D} objects, and a number of parameters which
 * keep track of settings related to how we go about solving the IK chain.
 * 
 * @author Al Lansley
 * @version 0.4 - 29/12/2015
 */
public class FabrikChain3D
{	
	private static final String NEW_LINE = System.lineSeparator();
	
	/**
	 * Various types of basebone constraint types.
	 */
	public static enum BaseboneConstraintType3D
	{
		NONE,         // No constraint - basebone may rotate freely
		GLOBAL_ROTOR, // World-space rotor constraint
		LOCAL_ROTOR,  // Rotor constraint in the coordinate space of (i.e. relative to) the direction of the connected bone
		GLOBAL_HINGE, // World-space hinge constraint
		LOCAL_HINGE   // Hinge constraint in the coordinate space of (i.e. relative to) the direction of the connected bone
	};
	
	// ---------- Private Properties ----------
	
	/**
	 * The core of a FabrikChain3D is a list of FabrikBone3D objects. It is this chain that we attempt to solve for a specified
	 * target location via the {@link updateTarget} method.
	 */
	private List<FabrikBone3D> mChain = new ArrayList<FabrikBone3D>();

	/** 
	 * The name of this FabrikChain3D object.
	 * <p>
	 * Although entirely optional, it may be used to uniquely identify a specific FabrikChain3D in an an array/list/map
	 * or such of FabrikChain3D objects.
	 * 
	 * @see  #setName
	 * @see  #getName
	 */
	private String mName;

	/** 
	 * The distance threshold we must meet in order to consider this FabrikChain3D to be successfully solved for distance.
	 * <p>
	 * When we solve a chain so that the distance between the end effector and target is less than or equal to the distance
	 * threshold, then we consider the chain to be solved and will dynamically abort any further attempts to solve the chain.
	 * <p>
	 * The default solve distance threshold is <strong>1.0f</strong>.
	 * <p>
	 * The minimum valid distance threshold is 0.0f, however a slightly higher value should be used to avoid forcing the IK
	 * chain solve process to run repeatedly when an <strong>acceptable</strong> (but not necessarily <em>perfect</em>)
	 * solution is found. Setting a very low solve distance threshold may result in significantly increased processor usage and
	 * hence increased processing time to solve a given IK chain.
	 * <p>	
	 * Although this property is the main criteria used to establish whether or not we have solved a given IK chain, it works
	 * in combination with the {@link #mMaxIterationAttempts} and {@link mMinIterationChange} fields to improve the
	 * performance of the algorithm in situations where we may not be able to solve a given IK chain. Such situations may arise
	 * when bones in the chain are highly constrained, or when the target is further away than the length of a chain which has
	 * a fixed base location.
	 * 
	 * @see  #setSolveDistanceThreshold(float)
	 * @see  #maxIterationAttempts
	 * @see  #minIterationChange
	 * @see  #setFixedBaseLocation
	 */
	private float mSolveDistanceThreshold = 0.1f;

	/**
	 * maxIterationAttempts (int)	Specifies the maximum number of attempts that will be performed in order to solve the IK chain.
	 * If we have not solved the chain to within the solve distance threshold after this many attempts then we accept the best
	 * solution we have best on solve distance to target.
	 * 
	 * @default 20
	 */
	private int mMaxIterationAttempts  = 20;

	/** 
	 * minIterationChange	(float)	Specifies the minimum distance improvement which must be made per solve attempt in order for us to believe it
	 * worthwhile to continue making attempts to solve the IK chain. If this iteration change is not exceeded then we abort any further solve
	 * attempts and accept the best solution we have based on solve distance to target.
	 * 
	 * @default 0.01f
	 */
	private float mMinIterationChange = 0.01f;

	/**
	 * chainLength	(float)	The chainLength is the combined length of all bones in this FabrikChain3D object.
	 *
	 * When a FabrikBone3D is added or removed from the chain using the {@Link addBone} or {@Link removeBone) methods, then
	 * the chainLength is updated to reflect this.
	 * @see {@Link addBone}
	 * @see {@Link removeBone}
	 */
	private float mChainLength;

	/** The number of bones in this IK chain. */
	private int mNumBones = 0;	

	/** 
	 * mBaseLocation (Vec3f)	The location of the start joint of the first bone in the IK chain.
	 * <p>
	 * By default, FabrikChain3D objects are created with a fixed base location, that is the start joint
	 * of the first bone in the chain is not moved during the solving process. A user may still move this
	 * base location by calling setFixedBaseLocation(some_location) and the FABRIK algorithm will then
	 * honour this new location as the 'fixed' base location.
	 * 
	 * @default: Vec3f(0.f, 0.0f)
	 * @see {@link setFixedBaseLocation}
	 */	
	private Vec3f mFixedBaseLocation = new Vec3f();

	/** mFixedBaseMode	Whether this FabrikChain3D has a fixed (i.e. immovable) base location.
	 *
	 * By default, the location of the start joint of the first bone added to the IK chain is considered fixed. This
	 * 'anchors' the base of the chain in place. Optionally, a user may toggle this behaviour by calling
	 * {@link #setFixedBaseMode(boolean)} to enable or disable locking the basebone to a fixed starting location.
	 * 
	 * @see {@link #setFixedBaseMode(boolean)}
	 */
	private boolean mFixedBaseMode = true;
	
	/**
	 * Each chain has a BaseboneConstraintType3D - this may be either:
	 * - NONE,         // No constraint - basebone may rotate freely
	 * - GLOBAL_ROTOR, // World-space rotor (i.e. ball joint) constraint
	 * - LOCAL_ROTOR,  // Rotor constraint which is relative to the coordinate space of the connected bone
	 * - GLOBAL_HINGE, // World-space hinge constraint, or
	 * - LOCAL_HINGE   // Hinge constraint which is relative to the coordinate space of the connected bone
	 */ 
	private BaseboneConstraintType3D mBaseboneConstraintType = BaseboneConstraintType3D.NONE;
	
	/** mBaseboneConstraintUV	The direction around which we should constrain the basebone, as provided to the {@link constrainBasebone}
	 * method.
	 * <p>
	 * To ensure correct operation, the provided Vec3f is normalised inside the {@link constBaseboneToDirectionUV} method. Passing a Vec3f
	 * with a magnitude of zero will result in the constraint not being set.
	 */
	private Vec3f mBaseboneConstraintUV = new Vec3f();
	
	/**
	 * mBaseboneRelativeConstraintUV	The basebone direction constraint in the coordinate space of the bone in another chain
	 * that this chain is connected to.
	 */
	private Vec3f mBaseboneRelativeConstraintUV = new Vec3f();
	
	/**
	 * mTargetLocation	The target location for the end effector of this IK chain.
	 * <p>
	 * The target location can be updated via the {@link updateTargtet(Vec3f)} or (@link updateTarget(float, float)}  methods, which in turn
	 * will call the solveIK(Vec3f) method to attempt to solve the IK chain, resulting in an updated chain configuration.
	 * 
	 * @default Vec3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)
	 */
	private Vec3f mLastTargetLocation = new Vec3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
	
	/**
	 * The width in pixels of the line used to draw constraints for this chain.
	 * 
	 * The valid range is 1.0f to 32.0f inclusive.
	 * 
	 * @default 2.0 
	 */
	private float mConstraintLineWidth = 2.0f;
	
	/** 
	 * The previous location of the start joint of the first bone added to the chain.
	 * <p>
	 * We keep track of the previous base location in order to use it to determine if the current base location and
	 * previous base location are the same, i.e. has the base location moved between the last run to this run? If
	 * the base location has moved, then we MUST solve the IK chain for this new base location - even if the target
	 * location has remained the same between runs.
	 * 
	 * @default Vec3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)
	 * @see {@link #setFixedBaseLocation}
	 */	
	private Vec3f mLastBaseLocation = new Vec3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

	/**
	 * mCurrentSolveDistance	The current distance between the end effector and the target location for this IK chain.
	 * <p>
	 * The current solve distance is updated when an attempt is made to solve IK chain as triggered by a call to the
	 * {@link updateTargtet(Vec3f)} or (@link updateTarget(float, float) methods.
	 */
	private float mCurrentSolveDistance = Float.MAX_VALUE;
	
	/**
	 * The zero-indexed number of the chain this chain is connected to in a FabrikStructure3D.
	 * <p>
	 * If the value is -1 then it's not connected to another bone or chain.
	 * 
	 * @default -1
	 */
	private int mConnectedChainNumber = -1;
	
	/**
	 * The zero-indexed number of the bone that this chain is connected to, if it's connected to another chain at all.
	 * <p>
	 * If the value is -1 then it's not connected to another bone or chain.
	 * 
	 * @default -1
	 */ 
	private int mConnectedBoneNumber  = -1;

	// ---------- Constructors ----------

	/** Default constructor */
	public FabrikChain3D() { }
	
	/**
	 * Copy constructor.
	 * 
	 * @param	source	The chain to duplicate.
	 */
	public FabrikChain3D(FabrikChain3D source)
	{
		// Force copy by value
		mChain = source.cloneIkChain();
		
		mFixedBaseLocation.set( source.getBaseLocation() );
		mLastTargetLocation.set(source.mLastTargetLocation);
		mLastBaseLocation.set(source.mLastBaseLocation);
				
		// Copy the basebone constraint UV if there is one to copy
		if (source.mBaseboneConstraintType != BaseboneConstraintType3D.NONE)
		{
			mBaseboneConstraintUV.set(source.mBaseboneConstraintUV);
			mBaseboneRelativeConstraintUV.set(source.mBaseboneRelativeConstraintUV);
		}		
		
		// Native copy by value for primitive members
		mChainLength            = source.mChainLength;
		mNumBones               = source.mNumBones;
		mCurrentSolveDistance   = source.mCurrentSolveDistance;
		mConnectedChainNumber   = source.mConnectedChainNumber;
		mConnectedBoneNumber    = source.mConnectedBoneNumber;
		mBaseboneConstraintType = source.mBaseboneConstraintType;			
		mName                   = source.mName;
		mConstraintLineWidth    = source.mConstraintLineWidth;
	}
	
	/**
	 * Naming constructor.
	 *
	 * @param	name	The name to set for this chain.
	 */
	public FabrikChain3D(String name) { mName = name; }

	// ---------- Public Methods ------------

	/**
	 * Add a bone to the end of this IK chain of this FabrikChain3D object.
	 * <p>
	 * This chain's {@link mChainLength} property is updated to take into account the length of the
	 * new bone added to the chain.
	 * <p>
	 * In addition, if the bone being added is the very first bone, then this chain's
	 * {@link mFixedBaseLocation} property is set from the start joint location of the bone.
	 * 
	 * @param	bone	The FabrikBone3D object to add to this FabrikChain3D.
	 * @see		#mChainLength
	 * @see		#mFixedBaseLocation
	 */
	public void addBone(FabrikBone3D bone)
	{
		// Add the new bone to the end of the ArrayList of bones
		mChain.add(bone);

		// If this is the basebone...
		if (mNumBones == 0)
		{
			// ...then keep a copy of the fixed start location...
			mFixedBaseLocation.set( bone.getStartLocation() );
			
			// ...and set the basebone constraint UV to be around the initial bone direction
			mBaseboneConstraintUV = bone.getDirectionUV();
		}
		
		// Increment the number of bones in the chain and update the chain length
		++mNumBones;
		updateChainLength();
	}

	/**
	 * Add a bone to the end of this IK chain given the direction unit vector and length of the new bone to add.
	 * <p>
	 * The bone added does not have any rotational constraints enforced, and will be drawn with a default colour
	 * of white at full opacity.
	 * <p>
	 * This method can only be used when the IK chain contains a basebone, as without it we do not
	 * have a start location for this bone (i.e. the end location of the previous bone).
	 * <p>
	 * If this method is executed on a chain which does not contain a basebone then a {@link RuntimeException}
	 * is thrown.
	 * <p>
	 * If this method is provided with a direction unit vector of zero, or a bone length of zero then then an
	 * {@link IllegalArgumentException} is thrown.
	 * 
	 * @param	directionUV The initial direction of the new bone
	 * @param	length		The length of the new bone
	 */
	public void addConsecutiveBone(Vec3f directionUV, float length) { addConsecutiveBone(directionUV, length, new Colour4f() ); }
	
	/**
	 * Add a consecutive bone to the end of this IK chain given the direction unit vector and length of the new bone to add.
	 * <p>
	 * The bone added does not have any rotational constraints enforced, and will be drawn with a default colour
	 * of white at full opacity.
	 * <p>
	 * This method can only be used when the IK chain contains a basebone, as without it we do not
	 * have a start location for this bone (i.e. the end location of the previous bone).
	 * <p>
	 * If this method is executed on a chain which does not contain a basebone then a {@link RuntimeException}
	 * is thrown.
	 * <p>
	 * If this method is provided with a direction unit vector of zero, or a bone length of zero then then an
	 * {@link IllegalArgumentException} is thrown.
	 * 
	 * @param	directionUV The initial direction of the new bone
	 * @param	length		The length of the new bone
	 * @param	colour		The colour with which to draw the bone
	 */
	public void addConsecutiveBone(Vec3f directionUV, float length, Colour4f colour)
	{
		// Validate the direction unit vector - throws an IllegalArgumentException if it has a magnitude of zero
		Utils.validateDirectionUV(directionUV);
		
		// Validate the length of the bone - throws an IllegalArgumentException if it is not a positive value
		Utils.validateLength(length);
				
		// If we have at least one bone already in the chain...
		if (mNumBones > 0)
		{				
			// Get the end location of the last bone, which will be used as the start location of the new bone
			Vec3f prevBoneEnd = mChain.get(mNumBones-1).getEndLocation();
				
			// Add a bone to the end of this IK chain
			// Note: We use a normalised version of the bone direction
			addBone( new FabrikBone3D(prevBoneEnd, directionUV.normalised(), length, colour) );
		}
		else // Attempting to add a relative bone when there is no basebone for it to be relative to?
		{
			throw new RuntimeException("You cannot add the basebone as a consecutive bone as it does not provide a start location. Use the addBone() method instead.");
		}
	}

	/**
	 * Add a consecutive hinge constrained bone to the end of this chain. The bone may rotate freely about the hinge axis.
	 * <p>
	 * The bone will be drawn with a default colour of white.
	 * <p>	 
	 * This method can only be used when the IK chain contains a basebone, as without it we do not
	 * have a start location for this bone (i.e. the end location of the previous bone).
	 * <p>
	 * If this method is executed on a chain which does not contain a basebone then a RuntimeException is thrown.
	 * If this method is provided with a direction unit vector of zero, then an IllegalArgumentException is thrown.
	 * If the joint type requested is not JointType.LOCAL_HINGE or JointType.GLOBAL_HINGE then an IllegalArgumentException is thrown.
	 * If this method is provided with a hinge rotation axis unit vector of zero, then an IllegalArgumentException is thrown.
	 * 
	 * @param	directionUV			The initial direction of the new bone.
	 * @param	length				The length of the new bone.
	 * @param	jointType			The type of hinge joint to be used - either JointType.LOCAL or JointType.GLOBAL.
	 * @param	hingeRotationAxis	The axis about which the hinge joint freely rotates.
	 */
	public void addConsecutiveFreelyRotatingHingedBone(Vec3f directionUV, float length, JointType jointType, Vec3f hingeRotationAxis)
	{
		// Because we aren't constraining this bone to a reference axis within the hinge rotation axis we don't care about the hinge constraint
		// reference axis (7th param) so we'll just generate an axis perpendicular to the hinge rotation axis and use that.
		addConsecutiveHingedBone( directionUV, length, jointType, hingeRotationAxis, 180.0f, 180.0f, Vec3f.genPerpendicularVectorQuick(hingeRotationAxis), new Colour4f() );
	}
	
	/**
	 * Add a consecutive hinge constrained bone to the end of this chain. The bone may rotate freely about the hinge axis.
	 * <p>
	 * The bone will be drawn with a default colour of white.
	 * <p>	 
	 * This method can only be used when the IK chain contains a basebone, as without it we do not
	 * have a start location for this bone (i.e. the end location of the previous bone).
	 * <p>
	 * If this method is executed on a chain which does not contain a basebone then a RuntimeException is thrown.
	 * If this method is provided with a direction unit vector of zero, then an IllegalArgumentException is thrown.
	 * If the joint type requested is not JointType.LOCAL_HINGE or JointType.GLOBAL_HINGE then an IllegalArgumentException is thrown.
	 * If this method is provided with a hinge rotation axis unit vector of zero, then an IllegalArgumentException is thrown.
	 * 
	 * @param	directionUV			The initial direction of the new bone.
	 * @param	length				The length of the new bone.
	 * @param	jointType			The type of hinge joint to be used - either JointType.LOCAL or JointType.GLOBAL.
	 * @param	hingeRotationAxis	The axis about which the hinge joint freely rotates.
	 * @param	colour				The colour to draw the bone.
	 */
	public void addConsecutiveFreelyRotatingHingedBone(Vec3f directionUV, float length, JointType jointType, Vec3f hingeRotationAxis, Colour4f colour)
	{
		// Because we aren't constraining this bone to a reference axis within the hinge rotation axis we don't care about the hinge constraint
		// reference axis (7th param) so we'll just generate an axis perpendicular to the hinge rotation axis and use that.
		addConsecutiveHingedBone(directionUV, length, jointType, hingeRotationAxis, 180.0f, 180.0f, Vec3f.genPerpendicularVectorQuick(hingeRotationAxis), colour);
	}
	
	/**
	 * Add a consecutive hinge constrained bone to the end of this IK chain.
	 * <p>
	 * The hinge type may be a global hinge where the rotation axis is specified in world-space, or
	 * a local hinge, where the rotation axis is relative to the previous bone in the chain.
	 * <p>
	 * If this method is executed on a chain which does not contain a basebone then a RuntimeException is thrown. 
	 * If this method is provided with bone direction or hinge constraint axis of zero then an IllegalArgumentException is thrown.
	 * If the joint type requested is not LOCAL_HINGE or GLOBAL_HINGE then an IllegalArgumentException is thrown.
	 * 
	 * @param	directionUV			The initial direction of the new bone.
	 * @param	length				The length of the new bone.
	 * @param	jointType			The joint type of the new bone.
	 * @param	clockwiseDegs		The clockwise constraint angle in degrees.
	 * @param	anticlockwiseDegs	The anticlockwise constraint angle in degrees.
	 * @param	hingeRotationAxis	The axis about which the hinge rotates.
	 * @param	clockwiseDegs		The clockwise constraint angle in degrees.
	 * @param	anticlockwiseDegs	The anticlockwise constraint angle in degrees.
	 * @param	hingeReferenceAxis	The axis about which any clockwise/anticlockwise rotation constraints are enforced.
	 * @param	colour				The colour to draw the bone.
	 */
	public void addConsecutiveHingedBone(Vec3f directionUV,
			                                  float length,
			                           JointType jointType,
			                       Vec3f hingeRotationAxis,
			                           float clockwiseDegs,
			                       float anticlockwiseDegs,
			                      Vec3f hingeReferenceAxis,
			                                Colour4f colour)
	{	
		// Validate the direction and rotation axis unit vectors, and the length of the bone.
		Utils.validateDirectionUV(directionUV);
		Utils.validateDirectionUV(hingeRotationAxis);
		Utils.validateLength(length);
				
		// Cannot add a consectuive bone of any kind if the there is no basebone
		if (mNumBones == 0) { throw new RuntimeException("You must add a basebone before adding a consectutive bone."); }
		
		// Normalise the direction and hinge rotation axis 
		directionUV.normalise();
		hingeRotationAxis.normalise();
			
		// Get the end location of the last bone, which will be used as the start location of the new bone
		Vec3f prevBoneEnd = mChain.get(mNumBones-1).getEndLocation();
			
		// Create a bone and set the draw colour...
		FabrikBone3D bone = new FabrikBone3D(prevBoneEnd, directionUV, length);
		bone.setColour(colour);
		
		// ...then create and set up a joint which we'll apply to that bone.
		FabrikJoint3D joint = new FabrikJoint3D();
		switch (jointType)
		{
			case GLOBAL_HINGE:
				joint.setAsGlobalHinge(hingeRotationAxis, clockwiseDegs, anticlockwiseDegs, hingeReferenceAxis);
				break;
			case LOCAL_HINGE:
				joint.setAsLocalHinge(hingeRotationAxis, clockwiseDegs, anticlockwiseDegs, hingeReferenceAxis);
				break;
			default:
				throw new IllegalArgumentException("Hinge joint types may be only JointType.GLOBAL_HINGE or JointType.LOCAL_HINGE.");
		}
		
		// Set the joint we just set up on the the new bone we just created
		bone.setJoint(joint);
		
		// Finally, add the bone to this chain
		addBone(bone);	
	}
	
	/**
	 * Add a consecutive hinge constrained bone to the end of this IK chain.
	 * <p>
	 * The hinge type may be a global hinge where the rotation axis is specified in world-space, or
	 * a local hinge, where the rotation axis is relative to the previous bone in the chain.
	 * <p>
	 * This method can only be used when the IK chain contains a basebone, as without it we do not
	 * have a start location for this bone (i.e. the end location of the previous bone).
	 * <p>
	 * If this method is executed on a chain which does not contain a basebone then a RuntimeException is thrown. 
	 * If this method is provided with bone direction or hinge constraint axis of zero then an IllegalArgumentException is thrown.
	 * If the joint type requested is not LOCAL_HINGE or GLOBAL_HINGE then an IllegalArgumentException is thrown.
	 * 
	 * @param	directionUV						The initial direction of the new bone.
	 * @param	length							The length of the new bone.
	 * @param	jointType						The joint type of the new bone. 
	 * @param	clockwiseDegs					The clockwise constraint angle in degrees.
	 * @param	anticlockwiseDegs				The anticlockwise constraint angle in degrees.
	 * @param	hingeRotationAxis				The axis about which the hinge rotates.
	 * @param	clockwiseDegs					The clockwise constraint angle in degrees.
	 * @param	anticlockwiseDegs				The anticlockwise constraint angle in degrees.
	 * @param	hingeConstraintReferenceAxis	The reference axis about which any clockwise/anticlockwise rotation constraints are enforced.
	 */
	public void addConsecutiveHingedBone(Vec3f directionUV,
			                                       float length,
			                                       JointType jointType,
			                                       Vec3f hingeRotationAxis,
			                                       float clockwiseDegs,
			                                       float anticlockwiseDegs,
			                                       Vec3f hingeConstraintReferenceAxis)
	{	
		addConsecutiveHingedBone(directionUV, length, jointType, hingeRotationAxis, clockwiseDegs, anticlockwiseDegs, hingeConstraintReferenceAxis, new Colour4f() );
	}
	
	/**
	 * Add a consecutive rotor (i.e. ball joint) constrained bone to the end of this IK chain.
	 * <p>
	 * This method can only be used when the IK chain contains a basebone, as without it we do not
	 * have a start location for this bone (i.e. the end location of the previous bone).
	 * <p>
	 * If this method is executed on a chain which does not contain a basebone then a RuntimeException is thrown. 
	 * If this method is provided with bone direction or hinge constraint axis of zero then an IllegalArgumentException is thrown.
	 * 
	 * @param	boneDirectionUV					The initial direction unit vector of the new bone.
	 * @param	boneLength						The length of the new bone.
	 * @param	constraintAngleDegs				The rotor constraint angle of the new bone.
	 * @param	colour							The colour to draw the bone.
	 */
	public void addConsecutiveRotorConstrainedBone(Vec3f boneDirectionUV, float boneLength, float constraintAngleDegs, Colour4f colour)
	{
		// Validate the bone direction and length and that we have a basebone
		Utils.validateDirectionUV(boneDirectionUV);
		Utils.validateLength(boneLength);
		if (mNumBones == 0) { throw new RuntimeException("Add a basebone before attempting to add consectuive bones."); }
				
		// Create the bone starting at the end of the previous bone, set its direction, constraint angle and colour
		// then add it to the chain. Note: The default joint type of a new FabrikBone3D is JointType.BALL.
		FabrikBone3D bone = new FabrikBone3D(mChain.get(mNumBones-1).getEndLocation(), boneDirectionUV.normalise(), boneLength, colour);
		bone.setBallJointConstraintDegs(constraintAngleDegs);
		addBone(bone);
	}
	
	/**
	 * Add a consecutive rotor (i.e. ball joint) constrained bone to the end of this IK chain.
	 * <p>
	 * The bone will be drawn in white at full opacity by default. This method can only be used when the IK chain contains
	 * a basebone, as without it we do not have a start location for this bone (i.e. the end location of the previous bone).
	 * <p>
	 * If this method is executed on a chain which does not contain a basebone then a RuntimeException is thrown. 
	 * If this method is provided with bone direction or hinge constraint axis of zero then an IllegalArgumentException is thrown.
	 * If the joint type requested is not LOCAL_HINGE or GLOBAL_HINGE then an IllegalArgumentException is thrown.
	 * 
	 * @param	boneDirectionUV		The initial direction unit vector of the new bone.
	 * @param	boneLength			The length of the new bone.
	 * @param	constraintAngleDegs	The rotor constraint angle for of the new bone.
	 */
	public void addConsecutiveRotorConstrainedBone(Vec3f boneDirectionUV, float boneLength, float constraintAngleDegs)
	{
		addConsecutiveRotorConstrainedBone( boneDirectionUV, boneLength, constraintAngleDegs, new Colour4f() );
	}	
	
	/**
	 * Return the basebone relative unit vector of this chain.
	 * 
	 * This direction is updated by the FabrikStructure3D when this chain is connected to another chain. There is
	 * no other possible way of doing it as we have no knowledge of other chains, but the structure does, allowing
	 * us to calculate this relative constraint UV.
	 *  
	 * @return The basebone relative constraint UV as updated (on solve) by the structure containing this chain.
	 */ 
	public Vec3f getBaseboneRelativeConstraintUV() { return mBaseboneRelativeConstraintUV; }
	
	/**
	 * Return the basebone constraint type of this chain.
	 *
	 * @return	The basebone constraint type of this chain.
	 */
	public BaseboneConstraintType3D getBaseboneConstraintType() { return mBaseboneConstraintType; }
	
	/**
	 * Method to set the line width (in pixels) with which to draw any constraint lines.
	 * <p>
	 * Valid values are 1.0f to 32.0f inclusive, although the OpenGL standard specifies that only line widths of 1.0f are guaranteed to work.
	 * Values outside of this range will result in an IllegalArgumentException being thrown.
	 * 
	 * @param	lineWidth	The width of the line used to draw constraint lines.
	 */
	public void setConstraintLineWidth(float lineWidth)
	{
		Utils.validateLineWidth(lineWidth);
		mConstraintLineWidth = lineWidth;
	}
	
	/**
	 * Get the directional constraint of the basebone.
	 * <p>
	 * If the basebone is not constrained then a RuntimeException is thrown. If you wish to check whether the
	 * basebone of this IK chain is constrained you may use the {@link #getBaseboneConstraintType()} method.
	 * 
	 * @return  The global directional constraint unit vector of the basebone of this IK chain.
	 */
	public Vec3f getBaseboneConstraintUV()
	{
		if ( !(mBaseboneConstraintType == BaseboneConstraintType3D.NONE) )
		{
			return mBaseboneConstraintUV;
		}
		else
		{
			throw new RuntimeException("Cannot return the basebone constraint when the basebone constraint type is NONE.");
		}
	}
	
	/**
	 * Return the base location of the IK chain.
	 * <p>
	 * Regardless of how many bones are contained in the chain, the base location is always the start location of the
	 * first bone in the chain.
	 * <p>
	 * This method does not return the mBaseLocation property of this chain because the start location of the basebone
	 * may be more up-to-date due to a moving 'fixed' location.
	 * 
	 * @return	The location of the start joint of the first bone in this chain.
	 */
	public Vec3f getBaseLocation() { return mChain.get(0).getStartLocation(); }	
	
	/**
	 * Return a bone by its zero-indexed location in the IK chain.
	 * 
	 * @param	boneNumber	The number of the bone to return from the Vector of FabrikBone3D objects.
	 * @return				The specified bone.
	 */
	public FabrikBone3D getBone(int boneNumber) { return mChain.get(boneNumber); }

	/**
	 * Return the List%lt;FabrikBone3D%gt; which comprises the actual IK chain of this FabrikChain3D object.
	 *  
	 * @return	The List%lt;FabrikBone3D%gt; which comprises the actual IK chain of this FabrikChain3D object.
	 */	
	public List<FabrikBone3D> getChain() { return mChain; }
	
	/**
	 * Return the current length of the IK chain.
	 * <p>
	 * This method does not dynamically re-calculate the length of the chain - it merely returns the previously
	 * calculated chain length, which gets updated each time a bone is added or removed from the chain. However,
	 * as the chain length is updated whenever necessary this should be fine.
	 * <p>
	 * If you need a calculated-on-the-fly value for the chain length, then use the getLiveChainLength() method.
	 * 
	 * @return	The pre-calculated length of the IK chain as stored in the mChainLength property.
	 */
	public float getChainLength() { return mChainLength; }
	
	/**
	 * Return the index of the bone in another chain that this this chain is connected to.
	 * <p>
	 * Returns -1 (default) if this chain is not connected to another chain.
	 * 
	 * @return	The zero-indexed number of the bone we are connected to in the chain we are connected to.
	 */ 
	public int getConnectedBoneNumber() { return mConnectedBoneNumber; }

	/**
	 * Return the index of the chain in a FabrikStructure3D that this this chain is connected to.
	 * <p>
	 * Returns -1 (default) if this chain is not connected to another chain.
	 * 
	 * @return	The zero-index number of the chain we are connected to.
	 */ 
	public int getConnectedChainNumber() { return mConnectedChainNumber; }
	
	/**
	 * Return the location of the end effector in the IK chain.
	 * <p>
	 * Regardless of how many bones are contained in the chain, the end effector is always the end location
	 * of the final bone in the chain. 
	 * 
	 * @return	The location of this chain's end effector.
	 */
	public Vec3f getEffectorLocation() { return mChain.get(mNumBones-1).getEndLocation(); }

	/**
	 * Return the target of the last solve attempt.
	 * <p>
	 * The target location and the effector location are not necessarily at the same location unless the chain has been solved
	 * for distance, and even then they are still likely to be <i>similar</i> rather than <b>identical</b> values.
	 * 
	 * @return	The target location of the last solve attempt.
	 */
	public Vec3f getLastTargetLocation() { return mLastTargetLocation; }
	
	/**
	 * Return the live calculated length of the chain.
	 * 
	 * Typically, the getChainLength() can be called which returns the length of the chain as updated /
	 * recalculated when a bone is added or removed from the chain (which is significantly faster as it
	 * doesn't require recalculation), but sometimes it may be useful to get the definitive most
	 * up-to-date chain length so you can check if operations being performed have altered the chain
	 * length - hence this method.
	 * 
	 * @return	The 'live' (i.e. calculated from scratch) length of the chain.
	 */
	public float getLiveChainLength()
	{
		float length = 0.0f;		
		for (int loop = 0; loop < mNumBones; ++loop)
		{  
			length += mChain.get(loop).liveLength();
		}		
		return length;
	}	
	
	/**
	 * Return the name of this IK chain.
	 *
	 * @return	The name of this IK chain.
	 */
	public String getName() { return mName; }
	
	/**
	 * Return the number of bones in this IK chain.
	 *
	 * @return	The number of bones in this IK chain.
	 */
	public int getNumBones() { return mNumBones; }
	
	/**
	 * Remove a bone from this IK chain by its zero-indexed location in the chain.
	 * <p> 
	 * This chain's {@link mChainLength} property is updated to take into account the new chain length.
	 * <p>
	 * If the bone number to be removed does not exist in the chain then an IllegalArgumentException is thrown.
	 * 
	 * @param	boneNumber	The zero-indexed bone to remove from this IK chain.
	 */
	public void removeBone(int boneNumber)
	{
		// If the bone number is a bone which exists...
		if (boneNumber < mNumBones)
		{	
			// ...then remove the bone, decrease the bone count and update the chain length.
			mChain.remove(boneNumber);
			--mNumBones;
			updateChainLength();
		}
		else
		{
			throw new IllegalArgumentException("Bone " + boneNumber + " does not exist to be removed from the chain. Bones are zero indexed.");
		}
	}
	
	/** 
	 * Set the relative basebone constraint UV - this direction should be relative to the coordinate space of the basebone.
	 *
	 * This function is deliberately made package-private as it should not be used by the end user - instead, the 
	 * FabrikStructure3D.updateTarget() method will update this mBaseboneRelativeConstraintUV property FOR USE BY this
	 * chain as required.
	 * 
	 * The reason for this is that this chain on its own cannot calculate the relative constraint
	 * direction, because it relies on direction of the connected / 'host' bone in the chain that this chain is connected
	 * to - only we have no knowledge of that other chain! But, the FabrikStructure3D DOES have knowledge of that other
	 * chain, and is hence able to calculate and update this relative basebone constraint direction for us.
	 **/
	void setBaseboneRelativeConstraintUV(Vec3f constraintUV)	{ mBaseboneRelativeConstraintUV = constraintUV;	}
	
	/**
	 * Set the basebone of this chain to be constrained to the given angle in degrees about the provided axis.
	 * <p>
	 * Depending on whether the constraint type is GLOBAL_ROTOR or LOCAL_ROTOR the constraint will be applied
	 * about global space or about the local coordinate system of a bone in another chain that this chain is
	 * attached to.
	 * 	 * <p>
	 * The angle provided should be between the range of 0.0f (completely constrained) to 180.0f (completely free to
	 * rotate). Values outside of this range will be clamped to the relevant minimum or maximum.
	 * <p>
	 * If this chain does not contain a basebone then a RuntimeException is thrown.
	 * If the constraint axis is a zero vector then an IllegalArgumentException is thrown.
	 * 
	 * @param	constraintType	The type of constraint to apply, this may be GLOBAL_ROTOR or LOCAL_ROTOR.
	 * @param	constraintAxis	The axis about which the rotor applies.
	 * @param	angleDegs		The angle about the constraint axis to limit movement in degrees. 
	 */
	public void setRotorBaseboneConstraint(BaseboneConstraintType3D constraintType, Vec3f constraintAxis, float angleDegs)
	{
		// Sanity checking
		if (mNumBones == 0)	                     { throw new RuntimeException("Chain must contain a basebone before we can specify the basebone constraint type."); }		
		if ( !(constraintAxis.length() > 0.0f) ) { throw new IllegalArgumentException("Constraint axis cannot be zero.");                                             }
		if (angleDegs < 0.0f  )                  { angleDegs = 0.0f;                                                                                                  }
		if (angleDegs > 180.0f)                  { angleDegs = 180.0f;                                                                                                }
				
		// Set the constraint type, axis and angle
		mBaseboneConstraintType = constraintType;
		mBaseboneConstraintUV   = constraintAxis.normalised();
		mBaseboneRelativeConstraintUV.set(mBaseboneConstraintUV);
		getBone(0).setBallJointConstraintDegs(angleDegs);
	}	
	
	/**
	 * Set this chain to have a globally hinged basebone constraint when attached to a bone in another chain.
	 * <p>
	 * If the number of bones in this chain is zero (i.e. it does not contain a basebone) then a RuntimeException is thrown.
	 * If the hinge rotation or reference axes are zero vectors then an IllegalArgumentException is thrown.
	 * If the hinge reference axis does not lie in the plane of the hinge rotation axis (that is, they are not perpendicular)
	 * then an IllegalArgumentException is thrown.
	 * 
	 * @param hingeRotationAxis		The axis about which the global hinge rotates.
	 * @param cwConstraintDegs		The clockwise constraint angle about the hinge reference axis in degrees.
	 * @param acwConstraintDegs		The clockwise constraint angle about the hinge reference axis in degrees.
	 * @param hingeReferenceAxis	The axis (perpendicular to the hinge rotation axis) about which the constraint angles apply.
	 */
	public void setGlobalHingeBaseboneConstraint(Vec3f hingeRotationAxis, float cwConstraintDegs, float acwConstraintDegs, Vec3f hingeReferenceAxis)
	{
		// Sanity checking
		if (mNumBones == 0)	{ throw new RuntimeException("Chain must contain a basebone before we can specify the basebone constraint type."); }		
		if ( !( hingeRotationAxis.length() > 0.0f) )  { throw new IllegalArgumentException("Hinge rotation axis cannot be zero.");		         }
		if ( !( hingeReferenceAxis.length() > 0.0f) ) { throw new IllegalArgumentException("Hinge reference axis cannot be zero.");		         }
		if ( !( Vec3f.perpendicular(hingeRotationAxis, hingeReferenceAxis) ) ) 
		{
			throw new IllegalArgumentException("The hinge reference axis must be in the plane of the hinge rotation axis, that is, they must be perpendicular.");
		}
		
		// Set the constraint type, axis and angle
		mBaseboneConstraintType = BaseboneConstraintType3D.GLOBAL_HINGE;
		mBaseboneConstraintUV.set( hingeRotationAxis.normalised() );
		
		FabrikJoint3D globalHinge = new FabrikJoint3D();
		globalHinge.setAsGlobalHinge(hingeRotationAxis, cwConstraintDegs, acwConstraintDegs, hingeReferenceAxis);
		getBone(0).setJoint(globalHinge);
	}
	
	/**
	 * Set this chain to have a freely rotating globally hinged basebone.
	 * <p>
	 * The clockwise and anticlockwise constraint angles are automatically set to 180 degrees and the hinge reference axis
	 * is generated to be any vector perpendicular to the hinge rotation axis.
	 * <p>
	 * If the number of bones in this chain is zero (i.e. it does not contain a basebone) then a RuntimeException is thrown.
	 * If the hinge rotation axis are zero vectors then an IllegalArgumentException is thrown.
	 * 
	 * @param hingeRotationAxis		The world-space axis about which the global hinge rotates.
	 */
	public void setFreelyRotatingGlobalHingedBasebone(Vec3f hingeRotationAxis)
	{
		setGlobalHingeBaseboneConstraint(hingeRotationAxis, 180.0f, 180.0f,  Vec3f.genPerpendicularVectorQuick(hingeRotationAxis) );
	}
	
	/**
	 * Set a directional constraint for the basebone.
	 * <p>
	 * This method constrains the <strong>basebone</strong> (<em>only</em>) to a global direction unit vector.
	 * <p>
	 * Attempting to set the basebone constraint when the bone has a basebone constraint type of NONE or providing
	 * a constraint vector of zero will result will result in an IllegalArgumentException being thrown.
	 * 
	 * @param	constraintUV	The direction unit vector to constrain the basebone to.
	 * @see		au.edu.federation.caliko.FabrikJoint3D#setBallJointConstraintDegs(float angleDegs)
	 * @see		au.edu.federation.caliko.FabrikJoint3D#setHingeJointClockwiseConstraintDegs(float)
	 * @see		au.edu.federation.caliko.FabrikJoint3D#setHingeJointAnticlockwiseConstraintDegs(float)
	 */
	public void setBaseboneConstraintUV(Vec3f constraintUV)
	{
		if (mBaseboneConstraintType == BaseboneConstraintType3D.NONE)
		{
			throw new IllegalArgumentException("Specify the basebone constraint type with setBaseboneConstraintTypeCannot specify a basebone constraint when the current constraint type is BaseboneConstraint.NONE.");
		}
		
		// Validate the constraint direction unit vector
		Utils.validateDirectionUV(constraintUV);
		
		// All good? Then normalise the constraint direction and set it
		constraintUV.normalise();
		mBaseboneConstraintUV.set(constraintUV);
	}

	/**
	 * Method used to move the base location of a chain relative to its connection point.
	 * <p>
	 * The assignment is made by reference so that this base location and the location where
	 * we attach to the other chain are the same Vec3f object.
	 * <p>
	 * Note: If this chain is attached to another chain then this 'fixed' base location will be updated
	 * as and when the connection point in the chain we are attached to moves.
	 *
	 * @param	baseLocation	The fixed base location for this chain.
	 */
	public void setBaseLocation(Vec3f baseLocation) { mFixedBaseLocation = baseLocation; }

	/**
	 * Set the list of FabrikBone3D of this FabrikChain3D to the provided list by reference.
	 * 
	 * @param	chain	The list of FabrikBone3D objects to assign to the {@link #mChain} property.
	 * @see		#mChain
	 */
	private void setChain(List<FabrikBone3D> chain) { this.mChain = chain; }

	/**
	 * Connect this chain to the specified bone in the specified chain in the provided structure.
	 * <p>
	 * In order to connect this chain to another chain, both chains must exist within the same structure.
	 * <p>
	 * If the structure does not contain the specified chain or bone then an IllegalArgumentException is thrown.
	 * 
	 * @param	structure	The structure which contains the chain which contains the bone to connect to.
	 * @param	chainNumber	The zero-indexed number of the chain in the structure to connect to.
	 * @param	boneNumber	The zero-indexed number of the bone in the chain to connect to.
	 */
	public void connectToStructure(FabrikStructure3D structure, int chainNumber, int boneNumber)
	{
		// Sanity check chain exists
		int numChains = structure.getNumChains();
		if (chainNumber > numChains) { throw new IllegalArgumentException("Structure does not contain a chain " + chainNumber + " - it has " + numChains + " chains."); }
		
		// Sanity check bone exists
		int numBones = structure.getChain(chainNumber).getNumBones();
		if (boneNumber > numBones) { throw new IllegalArgumentException("Chain does not contain a bone " + boneNumber + " - it has " + numBones + " bones."); }
		
		// All good? Set the connection details
		mConnectedChainNumber = chainNumber;
		mConnectedBoneNumber  = boneNumber;		
	}
	
	/**
	 * Set the fixed basebone mode for this chain.
	 * <p>
	 * If the basebone is 'fixed' in place, then its start location cannot move. The bone is still allowed to
	 * rotate, with or without constraints.
	 * <p>
	 * Specifying a non-fixed base location while this chain is connected to another chain will result in a
	 * RuntimeException being thrown.
	 * <p>
	 * Fixing the basebone's start location in place and constraining to a global absolute direction are
	 * mutually exclusive. Disabling fixed base mode while the chain's constraint type is
	 * BaseboneConstraintType3D.GLOBAL_ABSOLUTE will result in a RuntimeException being thrown.	 * 
	 *  
	 * @param  value  Whether or not to fix the basebone start location in place.
	 */
	public void setFixedBaseMode(boolean value)
	{	
		// Enforce that a chain connected to another chain stays in fixed base mode (i.e. it moves with the chain it's connected to instead of independently)
		if (value == false && mConnectedChainNumber != -1)
		{
			throw new RuntimeException("This chain is connected to another chain so must remain in fixed base mode.");
		}
		
		// We cannot have a freely moving base location AND constrain the basebone to an absolute direction
		if (mBaseboneConstraintType == BaseboneConstraintType3D.GLOBAL_ROTOR && value == false)
		{
			throw new RuntimeException("Cannot set a non-fixed base mode when the chain's constraint type is BaseboneConstraintType3D.GLOBAL_ABSOLUTE_ROTOR.");
		}
		
		// Above conditions met? Set the fixedBaseMode
		mFixedBaseMode = value;
	}
	
	/**
	 * Set the maximum number of attempts that will be made to solve this IK chain.
	 * <p>
	 * The FABRIK algorithm may require more than a single pass in order to solve
	 * a given IK chain for an acceptable distance threshold. If we reach this
	 * iteration limit then we stop attempting to solve the IK chain. Further details
	 * on this topic are provided in the {@link #mMaxIterationAttempts} documentation.
	 * <p>
	 * If a maxIterations value of less than 1 is provided then an IllegalArgumentException is
	 * thrown, as we must make at least a single attempt to solve an IK chain.
	 * 
	 * @param maxIterations  The maximum number of attempts that will be made to solve this IK chain.
	 */
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
	 * If the latest solution found has changed by less than this amount then we consider the progress being made
	 * to be not worth the computational effort and dynamically abort any further attempt to solve the chain for
	 * the current target to minimise CPU usage.
	 * <p>
	 * If a minIterationChange value of less than zero is specified then an IllegalArgumentException is
	 * thrown.
	 * 
	 * @param	minIterationChange  The minimum change in solve distance from one iteration to the next.
	 */
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
	public void setName(String name) { mName = Utils.getValidatedName(name); }
	
	/**
	 * Set the distance threshold within which we consider the IK chain to be successfully solved.
	 * <p>
	 * If a solve distance value of less than zero is specified then an IllegalArgumentException is thrown.
	 * 
	 * @param  solveDistance  The distance between the end effector of this IK chain and target within which we will accept the solution.
	 */
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
	 * Set the colour of all bones in this chain to the specified colour.
	 * 
	 * @param	colour	The colour to set all bones in this chain.
	 */
	public void setColour(Colour4f colour)
	{			
		for (int loop = 0; loop < mNumBones; ++loop)
		{
			getBone(loop).setColour(colour);
		}
	}	
	
	
	
	/**
	 * Method to solve this IK chain for the given target location.
	 * <p>
	 * The end result of running this method is that the IK chain configuration is updated.
	 * <p>
	 * To minimuse CPU usage, this method dynamically aborts if:
	 * - The solve distance (i.e. distance between the end effector and the target) is below the {@link mSolveDistanceThreshold},
	 * - A solution incrementally improves on the previous solution by less than the {@link mMinIterationChange}, or
	 * - The number of attempts to solve the IK chain exceeds the {@link mMaxIterationAttempts}. 
	 * 		
	 * @param	targetX	The x location of the target
	 * @param	targetY	The y location of the target
	 * @param	targetZ	The z location of the target
	 * @return			The resulting distance between the end effector and the new target location after solving the IK chain.
	 */
	public float updateTarget(float targetX, float targetY, float targetZ)
	{
		return updateTarget( new Vec3f(targetX, targetY, targetZ) );
	}

	/**
	 * Method to solve this IK chain for the given target location.
	 * <p>
	 * The end result of running this method is that the IK chain configuration is updated.
	 * <p>
	 * To minimuse CPU usage, this method dynamically aborts if:
	 * - The solve distance (i.e. distance between the end effector and the target) is below the {@link mSolveDistanceThreshold},
	 * - A solution incrementally improves on the previous solution by less than the {@link mMinIterationChange}, or
	 * - The number of attempts to solve the IK chain exceeds the {@link mMaxIterationAttempts}. 
	 * 
	 * @param	newTarget	The location of the target for which we will solve this IK chain.
	 * @return	float		The resulting distance between the end effector and the new target location after solving the IK chain.
	 */
	public float updateTarget(Vec3f newTarget)
	{	
		// If we have both the same target and base location as the last run then do not solve
		if ( mLastTargetLocation.approximatelyEquals(newTarget, 0.001f) &&
			 mLastBaseLocation.approximatelyEquals(getBaseLocation(), 0.001f) )
		{
			return mCurrentSolveDistance;
		}
		
		/***
		 * NOTE: We must allow the best solution of THIS run to be used for a new target or base location - we cannot
		 * just use the last solution (even if it's better) - because that solution was for a different target / base
		 * location combination and NOT for the current setup.
		 */
						
		// Declare a list of bones to use to store our best solution
		List<FabrikBone3D> bestSolution = new ArrayList<FabrikBone3D>();
		
		// We start with a best solve distance that can be easily beaten
		float bestSolveDistance = Float.MAX_VALUE;
		
		// We'll also keep track of the solve distance from the last pass
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
				bestSolution = this.cloneIkChain();
				
				// If we are happy that this solution meets our distance requirements then we can exit the loop now
				if (solveDistance < mSolveDistanceThreshold)
				{				
					break;
				}
			}
			else // Did not solve to our satisfaction? Okay...
			{
				// Did we grind to a halt? If so break out of loop to set the best distance and solution that we have
				if ( Math.abs(solveDistance - lastPassSolveDistance) < mMinIterationChange )
				{
					//System.out.println("Ground to halt on iteration: " + loop);
					break;
				}
			}
			
			// Update the last pass solve distance
			lastPassSolveDistance = solveDistance;
			
		} // End of loop
		
		// Update our solve distance and chain configuration to the best solution found
		mCurrentSolveDistance = bestSolveDistance;
		mChain = bestSolution;
		
		// Update our base and target locations
		mLastBaseLocation.set( getBaseLocation() );
		mLastTargetLocation.set(newTarget);
		
		return mCurrentSolveDistance;
	}
	
	/** Return a concise, human-readable of the IK chain. */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();		
		sb.append("--- FabrikChain3D: " + mName + " ---" + NEW_LINE);
			
		if (mNumBones > 0)
		{
			sb.append("Bone count:    : " + mNumBones         + NEW_LINE);			
			sb.append("Base location  : " + getBaseLocation() + NEW_LINE);
			sb.append("Chain length   : " + getChainLength()  + NEW_LINE);
			
			if (mFixedBaseMode) { sb.append("Fixed base mode: Yes" + NEW_LINE);	}
			else                { sb.append("Fixed base mode: No"  + NEW_LINE); }
			
			for (int loop = 0; loop < mNumBones; ++loop)
			{
				sb.append("--- Bone: " + loop + " ---" + NEW_LINE );
				sb.append( getBone(loop).toString()    + NEW_LINE );
			}
		}
		else
		{
			sb.append("Chain does not contain any bones." + NEW_LINE);
		}
		
		return sb.toString();
	} 

	// ---------- Private Methods ----------
	
	/**
	 * Solve the IK chain for the given target using the FABRIK algorithm.
	 * <p>
	 * If this chain does not contain any bones then a RuntimeException is thrown.
	 * 
	 * @return	The best solve distance found between the end-effector of this chain and the provided target.
	 */
	private float solveIK(Vec3f target)
	{	
		// Sanity check that there are bones in the chain
		if (mNumBones == 0) { throw new RuntimeException("It makes no sense to solve an IK chain with zero bones."); }
		
		// ---------- Forward pass from end effector to base -----------

		// Loop over all bones in the chain, from the end effector (numBones-1) back to the basebone (0)		
		for (int loop = mNumBones-1; loop >= 0; --loop)
		{
			// Get the length of the bone we're working on
			FabrikBone3D thisBone = mChain.get(loop);
			float thisBoneLength  = thisBone.length();
			FabrikJoint3D thisBoneJoint = thisBone.getJoint();
			JointType thisBoneJointType = thisBone.getJointType();

			// If we are NOT working on the end effector bone
			if (loop != mNumBones - 1)
			{
				// Get the outer-to-inner unit vector of the bone further out
				Vec3f outerBoneOuterToInnerUV = mChain.get(loop+1).getDirectionUV().negated();

				// Get the outer-to-inner unit vector of this bone
				Vec3f thisBoneOuterToInnerUV = thisBone.getDirectionUV().negated();
				
				// Get the joint type for this bone and handle constraints on thisBoneInnerToOuterUV
				
				if (thisBoneJointType == JointType.BALL)
				{	
					// Constrain to relative angle between this bone and the outer bone if required
					float angleBetweenDegs    = Vec3f.getAngleBetweenDegs(outerBoneOuterToInnerUV, thisBoneOuterToInnerUV);
					float constraintAngleDegs = thisBoneJoint.getBallJointConstraintDegs();
					if (angleBetweenDegs > constraintAngleDegs)
					{	
						thisBoneOuterToInnerUV = Vec3f.getAngleLimitedUnitVectorDegs(thisBoneOuterToInnerUV, outerBoneOuterToInnerUV, constraintAngleDegs);
					}
				}
				else if (thisBoneJointType == JointType.GLOBAL_HINGE)
				{	
					// Project this bone outer-to-inner direction onto the hinge rotation axis
					// Note: The returned vector is normalised.
					thisBoneOuterToInnerUV = thisBoneOuterToInnerUV.projectOntoPlane( thisBoneJoint.getHingeRotationAxis() ); 
					
					// NOTE: Constraining about the hinge reference axis on this forward pass leads to poor solutions... so we won't.
				}
				else if (thisBoneJointType == JointType.LOCAL_HINGE)
				{	
					// Construct a rotation matrix based on the previous bones inner-to-to-inner direction...
					Mat3f m = Mat3f.createRotationMatrix( mChain.get(loop-1).getDirectionUV() );
					
					// ...and transform the hinge rotation axis into the previous bones frame of reference.
					Vec3f relativeHingeRotationAxis = m.times( thisBoneJoint.getHingeRotationAxis() ).normalise();
										
					// Project this bone's outer-to-inner direction onto the plane described by the relative hinge rotation axis
					// Note: The returned vector is normalised.					
					thisBoneOuterToInnerUV = thisBoneOuterToInnerUV.projectOntoPlane(relativeHingeRotationAxis);
										
					// NOTE: Constraining about the hinge reference axis on this forward pass leads to poor solutions... so we won't.										
				}
					
				// At this stage we have a outer-to-inner unit vector for this bone which is within our constraints,
				// so we can set the new inner joint location to be the end joint location of this bone plus the
				// outer-to-inner direction unit vector multiplied by the length of the bone.
				Vec3f newStartLocation = thisBone.getEndLocation().plus( thisBoneOuterToInnerUV.times(thisBoneLength) );

				// Set the new start joint location for this bone
				thisBone.setStartLocation(newStartLocation);

				// If we are not working on the basebone, then we also set the end joint location of
				// the previous bone in the chain (i.e. the bone closer to the base) to be the new
				// start joint location of this bone.
				if (loop > 0)
				{
					mChain.get(loop-1).setEndLocation(newStartLocation);
				}
			}
			else // If we ARE working on the end effector bone...
			{
				// Snap the end effector's end location to the target
				thisBone.setEndLocation(target);
				
				// Get the UV between the target / end-location (which are now the same) and the start location of this bone
				Vec3f thisBoneOuterToInnerUV = thisBone.getDirectionUV().negated();
				
				// If the end effector is global hinged then we have to snap to it, then keep that
				// resulting outer-to-inner UV in the plane of the hinge rotation axis
				switch ( thisBoneJointType )
				{
					case BALL:
						// Ball joints do not get constrained on this forward pass
						break;						
					case GLOBAL_HINGE:
						// Global hinges get constrained to the hinge rotation axis, but not the reference axis within the hinge plane
						thisBoneOuterToInnerUV = thisBoneOuterToInnerUV.projectOntoPlane( thisBoneJoint.getHingeRotationAxis() );
						break;
					case LOCAL_HINGE:
						// Local hinges get constrained to the hinge rotation axis, but not the reference axis within the hinge plane
						
						// Construct a rotation matrix based on the previous bones inner-to-to-inner direction...
						Mat3f m = Mat3f.createRotationMatrix( mChain.get(loop-1).getDirectionUV() );
						
						// ...and transform the hinge rotation axis into the previous bones frame of reference.
						Vec3f relativeHingeRotationAxis = m.times( thisBoneJoint.getHingeRotationAxis() ).normalise();
											
						// Project this bone's outer-to-inner direction onto the plane described by the relative hinge rotation axis
						// Note: The returned vector is normalised.					
						thisBoneOuterToInnerUV = thisBoneOuterToInnerUV.projectOntoPlane(relativeHingeRotationAxis);
						break;
				}
												
				// Calculate the new start joint location as the end joint location plus the outer-to-inner direction UV
				// multiplied by the length of the bone.
				Vec3f newStartLocation = target.plus( thisBoneOuterToInnerUV.times(thisBoneLength) );
				
				// Set the new start joint location for this bone to be new start location...
				thisBone.setStartLocation(newStartLocation);

				// ...and set the end joint location of the bone further in to also be at the new start location (if there IS a bone
				// further in - this may be a single bone chain)
				if (loop > 0)
				{
					mChain.get(loop-1).setEndLocation(newStartLocation);
				}
			}
			
		} // End of forward pass

		// ---------- Backward pass from base to end effector -----------
 
		for (int loop = 0; loop < mNumBones; ++loop)
		{
			FabrikBone3D thisBone = mChain.get(loop);
			float thisBoneLength  = thisBone.length();

			// If we are not working on the basebone
			if (loop != 0)
			{
				// Get the inner-to-outer direction of this bone as well as the previous bone to use as a baseline
				Vec3f thisBoneInnerToOuterUV = thisBone.getDirectionUV();
				Vec3f prevBoneInnerToOuterUV = mChain.get(loop-1).getDirectionUV();
				
				// Dealing with a ball joint?
				FabrikJoint3D thisBoneJoint = thisBone.getJoint();
				JointType jointType = thisBoneJoint.getJointType();
				if (jointType == JointType.BALL)
				{					
					float angleBetweenDegs    = Vec3f.getAngleBetweenDegs(prevBoneInnerToOuterUV, thisBoneInnerToOuterUV);
					float constraintAngleDegs = thisBoneJoint.getBallJointConstraintDegs(); 
					
					// Keep this bone direction constrained within the rotor about the previous bone direction
					if (angleBetweenDegs > constraintAngleDegs)
					{
						thisBoneInnerToOuterUV = Vec3f.getAngleLimitedUnitVectorDegs(thisBoneInnerToOuterUV, prevBoneInnerToOuterUV, constraintAngleDegs);
					}
				}
				else if (jointType == JointType.GLOBAL_HINGE)
				{					
					// Get the hinge rotation axis and project our inner-to-outer UV onto it
					Vec3f hingeRotationAxis  =  thisBoneJoint.getHingeRotationAxis();
					thisBoneInnerToOuterUV = thisBoneInnerToOuterUV.projectOntoPlane(hingeRotationAxis);
					
					// If there are joint constraints, then we must honour them...
					float cwConstraintDegs   = -thisBoneJoint.getHingeClockwiseConstraintDegs();
					float acwConstraintDegs  =  thisBoneJoint.getHingeAnticlockwiseConstraintDegs();
					if ( !( Utils.approximatelyEquals(cwConstraintDegs, -FabrikJoint3D.MAX_CONSTRAINT_ANGLE_DEGS, 0.001f) ) &&
						 !( Utils.approximatelyEquals(acwConstraintDegs, FabrikJoint3D.MAX_CONSTRAINT_ANGLE_DEGS, 0.001f) ) )
					{
						Vec3f hingeReferenceAxis =  thisBoneJoint.getHingeReferenceAxis();
						
						// Get the signed angle (about the hinge rotation axis) between the hinge reference axis and the hinge-rotation aligned bone UV
						// Note: ACW rotation is positive, CW rotation is negative.
						float signedAngleDegs = Vec3f.getSignedAngleBetweenDegs(hingeReferenceAxis, thisBoneInnerToOuterUV, hingeRotationAxis);
						
						// Make our bone inner-to-outer UV the hinge reference axis rotated by its maximum clockwise or anticlockwise rotation as required
			        	if (signedAngleDegs > acwConstraintDegs)
			        	{	
			        		thisBoneInnerToOuterUV = Vec3f.rotateAboutAxisDegs(hingeReferenceAxis, acwConstraintDegs, hingeRotationAxis).normalised();		        		
			        	}
			        	else if (signedAngleDegs < cwConstraintDegs)
			        	{	
			        		thisBoneInnerToOuterUV = Vec3f.rotateAboutAxisDegs(hingeReferenceAxis, cwConstraintDegs, hingeRotationAxis).normalised();			        		
			        	}
					}
				}
				else if (jointType == JointType.LOCAL_HINGE)
				{	
					// Transform the hinge rotation axis to be relative to the previous bone in the chain
					Vec3f hingeRotationAxis  = thisBoneJoint.getHingeRotationAxis();
					
					// Construct a rotation matrix based on the previous bone's direction
					Mat3f m = Mat3f.createRotationMatrix(prevBoneInnerToOuterUV);
					
					// Transform the hinge rotation axis into the previous bone's frame of reference
					Vec3f relativeHingeRotationAxis  = m.times(hingeRotationAxis).normalise();
					
					// Project this bone direction onto the plane described by the hinge rotation axis
					// Note: The returned vector is normalised.
					thisBoneInnerToOuterUV = thisBoneInnerToOuterUV.projectOntoPlane(relativeHingeRotationAxis);
					
					// Constrain rotation about reference axis if required
					float cwConstraintDegs   = -thisBoneJoint.getHingeClockwiseConstraintDegs();
					float acwConstraintDegs  =  thisBoneJoint.getHingeAnticlockwiseConstraintDegs();
					if ( !( Utils.approximatelyEquals(cwConstraintDegs, -FabrikJoint3D.MAX_CONSTRAINT_ANGLE_DEGS, 0.001f) ) &&
						 !( Utils.approximatelyEquals(acwConstraintDegs, FabrikJoint3D.MAX_CONSTRAINT_ANGLE_DEGS, 0.001f) ) )
					{
						// Calc. the reference axis in local space
						Vec3f relativeHingeReferenceAxis = m.times( thisBoneJoint.getHingeReferenceAxis() ).normalise();
						
						// Get the signed angle (about the hinge rotation axis) between the hinge reference axis and the hinge-rotation aligned bone UV
						// Note: ACW rotation is positive, CW rotation is negative.
						float signedAngleDegs = Vec3f.getSignedAngleBetweenDegs(relativeHingeReferenceAxis, thisBoneInnerToOuterUV, relativeHingeRotationAxis);
						
						// Make our bone inner-to-outer UV the hinge reference axis rotated by its maximum clockwise or anticlockwise rotation as required
			        	if (signedAngleDegs > acwConstraintDegs)
			        	{	
			        		thisBoneInnerToOuterUV = Vec3f.rotateAboutAxisDegs(relativeHingeReferenceAxis, acwConstraintDegs, relativeHingeRotationAxis).normalise();		        		
			        	}
			        	else if (signedAngleDegs < cwConstraintDegs)
			        	{	
			        		thisBoneInnerToOuterUV = Vec3f.rotateAboutAxisDegs(relativeHingeReferenceAxis, cwConstraintDegs, relativeHingeRotationAxis).normalise();			        		
			        	}
					}
					
				} // End of local hinge section
				
				// At this stage we have a outer-to-inner unit vector for this bone which is within our constraints,
				// so we can set the new inner joint location to be the end joint location of this bone plus the
				// outer-to-inner direction unit vector multiplied by the length of the bone.
				Vec3f newEndLocation = thisBone.getStartLocation().plus( thisBoneInnerToOuterUV.times(thisBoneLength) );

				// Set the new start joint location for this bone
				thisBone.setEndLocation(newEndLocation);

				// If we are not working on the end effector bone, then we set the start joint location of the next bone in
				// the chain (i.e. the bone closer to the target) to be the new end joint location of this bone.
				if (loop < mNumBones - 1)
				{
					mChain.get(loop+1).setStartLocation(newEndLocation);
				}
			}
			else // If we ARE working on the basebone...
			{	
				// If the base location is fixed then snap the start location of the basebone back to the fixed base...
				if (mFixedBaseMode)
				{
					thisBone.setStartLocation(mFixedBaseLocation);
				}
				else // ...otherwise project it backwards from the end to the start by its length.
				{
					thisBone.setStartLocation( thisBone.getEndLocation().minus( thisBone.getDirectionUV().times(thisBoneLength) ) );
				}
				
				// If the basebone is unconstrained then process it as usual...
				if (mBaseboneConstraintType == BaseboneConstraintType3D.NONE)
				{
					// Set the new end location of this bone, and if there are more bones,
					// then set the start location of the next bone to be the end location of this bone
					Vec3f newEndLocation = thisBone.getStartLocation().plus( thisBone.getDirectionUV().times(thisBoneLength) );
					thisBone.setEndLocation(newEndLocation);	
					
					if (mNumBones > 1) { mChain.get(1).setStartLocation(newEndLocation); }
				}
				else // ...otherwise we must constrain it to the basebone constraint unit vector
				{	
					if (mBaseboneConstraintType == BaseboneConstraintType3D.GLOBAL_ROTOR)
					{	
						// Get the inner-to-outer direction of this bone
						Vec3f thisBoneInnerToOuterUV = thisBone.getDirectionUV();
								
						float angleBetweenDegs    = Vec3f.getAngleBetweenDegs(mBaseboneConstraintUV, thisBoneInnerToOuterUV);
						float constraintAngleDegs = thisBone.getBallJointConstraintDegs(); 
					
						if (angleBetweenDegs > constraintAngleDegs)
						{
							thisBoneInnerToOuterUV = Vec3f.getAngleLimitedUnitVectorDegs(thisBoneInnerToOuterUV, mBaseboneConstraintUV, constraintAngleDegs);
						}
						
						Vec3f newEndLocation = thisBone.getStartLocation().plus( thisBoneInnerToOuterUV.times(thisBoneLength) );
						
						thisBone.setEndLocation( newEndLocation );
						
						// Also, set the start location of the next bone to be the end location of this bone
						mChain.get(1).setStartLocation(newEndLocation);
					}
					else if (mBaseboneConstraintType == BaseboneConstraintType3D.LOCAL_ROTOR)
					{
						// Note: The mBaseBoneRelativeConstraintUV is updated in the FabrikStructure3D.updateTarget()
						// method BEFORE this FabrikChain3D.updateTarget() method is called. We no knowledge of the
						// direction of the bone we're connected to in another chain and so cannot calculate this 
						// relative basebone constraint direction on our own, but the FabrikStructure3D does it for
						// us so we are now free to use it here.
						
						// Get the inner-to-outer direction of this bone
						Vec3f thisBoneInnerToOuterUV = thisBone.getDirectionUV();
								
						// Constrain about the relative basebone constraint unit vector as neccessary
						float angleBetweenDegs    = Vec3f.getAngleBetweenDegs(mBaseboneRelativeConstraintUV, thisBoneInnerToOuterUV);
						float constraintAngleDegs = thisBone.getBallJointConstraintDegs();
						if (angleBetweenDegs > constraintAngleDegs)
						{
							thisBoneInnerToOuterUV = Vec3f.getAngleLimitedUnitVectorDegs(thisBoneInnerToOuterUV, mBaseboneRelativeConstraintUV, constraintAngleDegs);
						}
						
						// Set the end location
						Vec3f newEndLocation = thisBone.getStartLocation().plus( thisBoneInnerToOuterUV.times(thisBoneLength) );						
						thisBone.setEndLocation( newEndLocation );
						
						// Also, set the start location of the next bone to be the end location of this bone
						if (mNumBones > 1) { mChain.get(1).setStartLocation(newEndLocation); }
					}
					else if (mBaseboneConstraintType == BaseboneConstraintType3D.GLOBAL_HINGE)
					{
						FabrikJoint3D thisJoint  =  thisBone.getJoint();
						Vec3f hingeRotationAxis  =  thisJoint.getHingeRotationAxis();
						float cwConstraintDegs   = -thisJoint.getHingeClockwiseConstraintDegs();     // Clockwise rotation is negative!
						float acwConstraintDegs  =  thisJoint.getHingeAnticlockwiseConstraintDegs();
						
						// Get the inner-to-outer direction of this bone and project it onto the global hinge rotation axis
						Vec3f thisBoneInnerToOuterUV = thisBone.getDirectionUV().projectOntoPlane(hingeRotationAxis);
								
						// If we have a global hinge which is not freely rotating then we must constrain about the reference axis
						if ( !( Utils.approximatelyEquals(cwConstraintDegs , FabrikJoint3D.MAX_CONSTRAINT_ANGLE_DEGS, 0.01f) &&
							    Utils.approximatelyEquals(acwConstraintDegs, FabrikJoint3D.MAX_CONSTRAINT_ANGLE_DEGS, 0.01f) ) )
						{
							// Grab the hinge reference axis and calculate the current signed angle between it and our bone direction (about the hinge
							// rotation axis). Note: ACW rotation is positive, CW rotation is negative.
							Vec3f hingeReferenceAxis = thisJoint.getHingeReferenceAxis();
							float signedAngleDegs    = Vec3f.getSignedAngleBetweenDegs(hingeReferenceAxis, thisBoneInnerToOuterUV, hingeRotationAxis);
							
							// Constrain as necessary
				        	if (signedAngleDegs > acwConstraintDegs)
				        	{	
				        		thisBoneInnerToOuterUV = Vec3f.rotateAboutAxisDegs(hingeReferenceAxis, acwConstraintDegs, hingeRotationAxis).normalise();		        		
				        	}
				        	else if (signedAngleDegs < cwConstraintDegs)
				        	{	
				        		thisBoneInnerToOuterUV = Vec3f.rotateAboutAxisDegs(hingeReferenceAxis, cwConstraintDegs, hingeRotationAxis).normalise();			        		
				        	}
						}
						
						// Calc and set the end location of this bone
						Vec3f newEndLocation = thisBone.getStartLocation().plus( thisBoneInnerToOuterUV.times(thisBoneLength) );						
						thisBone.setEndLocation( newEndLocation );
						
						// Also, set the start location of the next bone to be the end location of this bone
						mChain.get(1).setStartLocation(newEndLocation);
					}
					else
					{
						throw new RuntimeException("LOCAL_HINGE basebone constraints are not supported at this time.");
					}
					
				} // End of basebone constraint handling section

			} // End of basebone handling section

		} // End of backward-pass loop over all bones

		// Update our last target location
		mLastTargetLocation.set(target);
				
		// DEBUG - check the live chain length and the originally calculated chain length are the same
		/*
		if (Math.abs( this.getLiveChainLength() - mChainLength) > 0.01f)
		{
			System.out.println("Chain length off by > 0.01f");
		}
		*/
		
		// Finally, calculate and return the distance between the current effector location and the target.
		return Vec3f.distanceBetween(mChain.get(mNumBones-1).getEndLocation(), target);
	}
	
	/***
	 * Calculate the length of this IK chain by adding up the lengths of each bone.
	 * <p>
	 * The resulting chain length is stored in the mChainLength property.
	 * <p>
	 * This method is called each time a bone is added to the chain. In addition, the
	 * length of each bone is recalculated during the process to ensure that our chain
	 * length is accurate. As the typical usage of a FabrikChain3D is to add a number
	 * of bones once (during setup) and then use them, this should not have any
	 * performance implication on the typical execution cycle of a FabrikChain3D object,
	 * as this method will not be called in any method which executes regularly. 
	 */
	private void updateChainLength()
	{
		// We start adding up the length of the bones from an initial length of zero
		mChainLength = 0.0f;

		// Loop over all the bones in the chain, adding the length of each bone to the mChainLength property
		for (int loop = 0; loop < mNumBones; ++loop)
		{
			mChainLength += mChain.get(loop).length();
		}
	}
	
	/**
	 * Clone and return the IK Chain of this FabrikChain3D, that is, the list of FabrikBone3D objects.
	 * 
	 * @return	A cloned List%lt;FabrikBone3D%gt;
	 */
	private List<FabrikBone3D> cloneIkChain()
	{
		// How many bones are in this chain?
		int numBones = mChain.size();
		
		// Create a new Vector of FabrikBone3D objects of that size
		List<FabrikBone3D> clonedChain = new ArrayList<FabrikBone3D>(numBones);

		// For each bone in the chain being cloned...		
		for (int loop = 0; loop < numBones; ++loop)
		{
			// Use the copy constructor to create a new FabrikBone3D with the values set from the source FabrikBone3D.
			// and add it to the cloned chain.
			clonedChain.add( new FabrikBone3D( mChain.get(loop) ) );
		}
		
		return clonedChain;
	}

} // End of FabrikChain3D class
