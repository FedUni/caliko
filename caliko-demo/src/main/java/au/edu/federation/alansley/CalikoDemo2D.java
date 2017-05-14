package au.edu.federation.alansley;

import au.edu.federation.caliko.FabrikBone2D;
import au.edu.federation.caliko.FabrikChain2D;
import au.edu.federation.caliko.FabrikChain2D.BaseboneConstraintType2D;
import au.edu.federation.caliko.FabrikChain2D.BoneConnectionPoint2D;
import au.edu.federation.caliko.FabrikStructure2D;
import au.edu.federation.caliko.visualisation.FabrikLine2D;
import au.edu.federation.caliko.visualisation.Point2D;
import au.edu.federation.utils.Colour4f;
import au.edu.federation.utils.Utils;
import au.edu.federation.utils.Vec2f;

/**
 * Class to demonstrate some of the features of the Caliko library in 2D.
 * 
 * @author Al Lansley
 * @version 0.8 - 02/08/2016
 */
public class CalikoDemo2D extends CalikoDemo
{	
	/** Each demo works with a single structure composed of one or more IK chains. */
	static FabrikStructure2D mStructure; 
	
	/** The target is drawn at the mouse cursor location and is updated to the cursor location when the LMB is held down. */
	private static Point2D mTargetPoint = new Point2D();
	
	/** Define world-space cardinal axes. */
	private static final Vec2f UP    = new Vec2f( 0.0f, 1.0f);
	private static final Vec2f LEFT  = new Vec2f(-1.0f, 0.0f);
	private static final Vec2f RIGHT = new Vec2f( 1.0f, 0.0f);
	
	/**
	 * Length of constraint lines to draw in pixels.
	 * 
	 * @default 10.0f
	 */
	private float mConstraintLineLength = 10.0f;
	
	/**
	 * Width of constraint lines to draw in pixels.
	 * 
	 * @default 2.0f
	 */
	private float mConstraintLineWidth  = 2.0f;
	
	/** Offset amount used by demos 7 and 8. */
	private Vec2f mRotatingOffset       = new Vec2f(30.0f, 0.0f);
	
	/** Targets and offsets for chains with embedded targets in demo 7. */
	private Vec2f mSmallRotatingTargetLeft  = new Vec2f(-70.0f, 40.0f);
	private Vec2f mSmallRotatingTargetRight = new Vec2f( 50.0f, 20.0f);
	private Vec2f mSmallRotatingOffsetLeft  = new Vec2f( 25.0f, 0.0f);
	private Vec2f mSmallRotatingOffsetRight = new Vec2f(  0.0f, 30.0f);
	
	/** Base location used by demos 7 and 8. */
	private Vec2f mOrigBaseLocation = new Vec2f(0.0f, -80.0f);
	
	/**
	 * Constructor
	 * 
	 * @param	demoNumber	The number of the demo to set up.
	 */
	public CalikoDemo2D(int demoNumber) { setup(demoNumber); }
	
	/**
	 * Set up a demo consisting of an arrangement of 2D IK chain(s).
	 * 
	 * @param	demoNumber	The number of the demo to set Up.
	 */
	public void setup(int demoNumber)
	{	
		switch (demoNumber)
		{
			case 1:
			{	
				// Update window title
				String demoName = "Demo 1 - Chain with fixed base, GLOBAL_ABSOLUTE base-bone constraints, and joint constraints.";
				Application.window.setWindowTitle(demoName);
				
				// Instantiate our FabrikStructure2D
				mStructure = new FabrikStructure2D(demoName);
				
				// Create a new chain
				FabrikChain2D chain = new FabrikChain2D();
				
				float boneLength = 40.0f;
				
				// Create and add first bone - 25 clockwise, 90 anti-clockwise
				FabrikBone2D basebone;
				basebone = new FabrikBone2D(new Vec2f(0.0f, -boneLength), new Vec2f(0.0f, 0.0f) );
				basebone.setClockwiseConstraintDegs(25.0f);
				basebone.setAnticlockwiseConstraintDegs(90.0f);		
				chain.addBone(basebone);
				
				// Fix the base bone to its current location, and constrain it to the positive Y-axis
				chain.setFixedBaseMode(true);		
				chain.setBaseboneConstraintType(BaseboneConstraintType2D.GLOBAL_ABSOLUTE);
				chain.setBaseboneConstraintUV( new Vec2f(0.0f, 1.0f) );
		
				// Create and add the second bone - 50 clockwise, 90 anti-clockwise
				chain.addConsecutiveConstrainedBone(new Vec2f(0.0f, 1.0f), boneLength, 50.0f, 90.0f);
				
				// Create and add the third bone - 75 clockwise, 90 anti-clockwise
				chain.addConsecutiveConstrainedBone(new Vec2f(0.0f, 1.0f), boneLength, 75.0f, 90.0f);
				
				// Finally, add the chain to the structure
				mStructure.addChain(chain);				
				break;
			}
		
			case 2:
			{
				// Update window title
				String demoName = "Demo 2 - Chain with fixed base (toggle with F), unconstrained base-bone, and multiple unconstrained bones.";
				Application.window.setWindowTitle(demoName);
				
				// Instantiate our FabrikStructure2D
				mStructure = new FabrikStructure2D(demoName);
				
				// Create a new chain
				FabrikChain2D chain = new FabrikChain2D();
				
				// Create the base bone and add it to the chain. Params: Start location, direction, length
				FabrikBone2D baseBone = new FabrikBone2D( new Vec2f(), new Vec2f(1.0f, 0.0f), 10.0f);
				chain.addBone(baseBone);
				
				// Add a series of additional bones 
				float boneLength = 10.0f;				
				Vec2f defaultUV  = new Vec2f(1.0f, 0.0f);				
				float numBones   = 15;
				float rotStep    = 360.0f / numBones;
				for (int loop = 0; loop < numBones; loop++)
				{
					// Initially, each bone added will be rotated 10 degrees further than the last
					Vec2f rotatedUV = Vec2f.rotateDegs(defaultUV, loop * rotStep);
					
					// Add an unconstrained consecutive bone
					chain.addConsecutiveBone(rotatedUV, boneLength);
				}
				
				// The the chain to have a fixed base location and, finally, add the chain to the structure
				chain.setFixedBaseMode(true);
				mStructure.addChain(chain);				
				break;
			}
			
			case 3:
			{				
				// Update window title
				String demoName = "Demo 3 - Chain with fixed base (toggle with F), unconstrained base-bone, and multiple constrained bones.";
				Application.window.setWindowTitle(demoName);
				
				// Instantiate our FabrikStructure2D
				mStructure = new FabrikStructure2D(demoName);
				
				// Create a new chain
				FabrikChain2D chain = new FabrikChain2D();
				
				// Create the base bone and add it to the chain
				FabrikBone2D baseBone = new FabrikBone2D( new Vec2f(), new Vec2f(1.0f, 0.0f), 10.0f);
				chain.addBone(baseBone);
				
				// Add a series of additional bones 
				float boneLength = 10.0f;
				Vec2f defaultUV  = new Vec2f(1.0f, 0.0f);
				int numBones = 15;
				for (int loop = 0; loop < numBones; loop++)
				{
					// Each bone added will be rotated 10 degrees further than the last
					Vec2f rotatedUV = Vec2f.rotateDegs(defaultUV, loop * numBones);
					
					// Constrained
					chain.addConsecutiveConstrainedBone(rotatedUV, boneLength, 60.0f, 60.0f);
				}
				
				// The the chain to have a fixed base location and specify the constraint line length & width values
				chain.setFixedBaseMode(true);
				
				// Finally, add the chain to the structure
				mStructure.addChain(chain);				
				break;
			}
			
			case 4:
			{				 
				String demoName = "Demo 4 - Multiple connected chains with no base-bone constraints.";
				Application.window.setWindowTitle(demoName);
				
				// Instantiate our FabrikStructure2D
				mStructure = new FabrikStructure2D(demoName);
				
				// ----- Vertical chain -----
				float boneLength = 50.0f;
				FabrikChain2D verticalChain = new FabrikChain2D();
				FabrikBone2D basebone = new FabrikBone2D( new Vec2f(0.0f, -50.0f), UP, boneLength);
				
				// Note: Default basebone constraint type is NONE
				verticalChain.addBone(basebone);
				
				// Add two additional consecutive bones
				verticalChain.addConsecutiveConstrainedBone(UP,  boneLength, 90.0f, 90.0f);
				verticalChain.addConsecutiveConstrainedBone(UP,  boneLength,  90.0f, 90.0f);
				
				// Add our main chain to structure
				mStructure.addChain(verticalChain);
				
				// ----- Left branch chain -----				
				boneLength = 30.0f;
				
				// Create the base bone and set its colour
				basebone = new FabrikBone2D( new Vec2f(), new Vec2f(-boneLength, 0.0f) );
				basebone.setColour(Utils.MID_GREEN);
				
				// Create the chain and add the basebone to it
				FabrikChain2D leftChain = new FabrikChain2D();
				leftChain.addBone(basebone);
				
				// Add consecutive constrained bones
				// Note: The base-bone is unconstrained, but these bones ARE constrained				
				leftChain.addConsecutiveConstrainedBone(LEFT, boneLength, 90.0f, 90.0f, Utils.MID_GREEN);
				leftChain.addConsecutiveConstrainedBone(LEFT, boneLength, 90.0f, 90.0f, Utils.MID_GREEN);
				
				// Add the chain to the structure, connecting to the end of bone 0 in chain 0
				mStructure.addConnectedChain(leftChain, 0, 0, BoneConnectionPoint2D.END);
				
				// ----- Right branch chain -----
							
				// Create the base bone
				basebone = new FabrikBone2D( new Vec2f(), new Vec2f(boneLength, 0.0f) );
				basebone.setColour(Utils.GREY);
				
				// Create the chain and add the basebone to it
				FabrikChain2D rightChain = new FabrikChain2D();
				rightChain.addBone(basebone);
								
				// Add two consecutive constrained bones to the chain
				// Note: The base-bone is unconstrained, but these bones ARE constrained
				rightChain.addConsecutiveConstrainedBone(RIGHT, boneLength, 60.0f, 60.0f, Utils.GREY);
				rightChain.addConsecutiveConstrainedBone(RIGHT, boneLength, 60.0f, 60.0f, Utils.GREY);
				
				// Add the chain to the structure, connecting to the end of bone 1 in chain 0
				mStructure.addConnectedChain(rightChain, 0, 1, BoneConnectionPoint2D.END);
				break;	
			}
			
			case 5:
			{				
				String demoName = "Demo 5 - Multiple connected chains with LOCAL_RELATIVE base-bone constraints.";
				Application.window.setWindowTitle(demoName);
				
				// Instantiate our FabrikStructure2D
				mStructure = new FabrikStructure2D(demoName);
				
				// ----- Vertical chain -----
				float boneLength = 50.0f;
				FabrikChain2D verticalChain = new FabrikChain2D();
				
				FabrikBone2D basebone = new FabrikBone2D( new Vec2f(0.0f, -100.0f), UP, boneLength);
				basebone.setClockwiseConstraintDegs(15.0f);
				basebone.setAnticlockwiseConstraintDegs(15.0f);
				
				verticalChain.addBone(basebone);
				verticalChain.addConsecutiveConstrainedBone(UP,  boneLength, 30.0f, 30.0f);
				verticalChain.addConsecutiveConstrainedBone(UP,  boneLength, 30.0f, 30.0f);
				verticalChain.setBaseboneConstraintType(BaseboneConstraintType2D.GLOBAL_ABSOLUTE);
				verticalChain.setBaseboneConstraintUV(UP);
				
				// Add chain to structure
				mStructure.addChain(verticalChain);
				
				// ----- Left branch chain -----				
				boneLength = 30.0f;
				
				// Create the base bone
				basebone = new FabrikBone2D( new Vec2f(), new Vec2f(-boneLength, 0.0f) );
				basebone.setClockwiseConstraintDegs(60.0f);
				basebone.setAnticlockwiseConstraintDegs(60.0f);
				basebone.setColour(Utils.MID_GREEN);
				
				// Create the chain to add it to and enable base bone constraint mode
				FabrikChain2D leftChain = new FabrikChain2D();		
				leftChain.setBaseboneConstraintType(BaseboneConstraintType2D.LOCAL_RELATIVE);
				
				// Add the basebone to the chain
				leftChain.addBone(basebone);
				
				// Add consecutive constrained bones
				leftChain.addConsecutiveConstrainedBone(LEFT, boneLength, 60.0f, 60.0f, Utils.MID_GREEN);
				leftChain.addConsecutiveConstrainedBone(LEFT, boneLength, 60.0f, 60.0f, Utils.MID_GREEN);
						
				// Params: The chain to add to the structure, which chain it links to, which bone in that chain it links to
				mStructure.addConnectedChain(leftChain, 0, 0, BoneConnectionPoint2D.END);
				
				// ----- Right branch chain -----				
				// Create the base bone
				basebone = new FabrikBone2D( new Vec2f(), new Vec2f(boneLength, 0.0f) );
				basebone.setClockwiseConstraintDegs(30.0f);
				basebone.setAnticlockwiseConstraintDegs(30.0f);
				basebone.setColour(Utils.GREY);
				
				// Create the chain to add it to and enable base bone constraint mode
				FabrikChain2D rightChain = new FabrikChain2D();
				rightChain.setBaseboneConstraintType(BaseboneConstraintType2D.LOCAL_RELATIVE);
				
				// Add the basebone to the chain
				rightChain.addBone(basebone);
				
				// Add two consecutive constrained bones to the chain
				rightChain.addConsecutiveConstrainedBone(RIGHT, boneLength, 15.0f, 15.0f, Utils.GREY);
				rightChain.addConsecutiveConstrainedBone(RIGHT, boneLength, 15.0f, 15.0f, Utils.GREY);
				
				// Add the chain to the structure, connecting at the end of bone 1 in chain 0
				mStructure.addConnectedChain(rightChain, 0, 1, BoneConnectionPoint2D.END);				
				break;
			}
			
			case 6:
			{				
				String demoName = "Demo 6 - Multiple connected chains with LOCAL_ABSOLUTE base-bone constraints.";
				Application.window.setWindowTitle(demoName);
				
				// Instantiate our FabrikStructure2D
				mStructure = new FabrikStructure2D(demoName);
				
				float boneLength = 50.0f;
							
				// ----- Central chain -----
				FabrikChain2D verticalChain = new FabrikChain2D();
								
				// Fix the base bone to its current location, and constrain it to the positive Y-axis
				verticalChain.setFixedBaseMode(true);		
				verticalChain.setBaseboneConstraintType(BaseboneConstraintType2D.GLOBAL_ABSOLUTE);
				verticalChain.setBaseboneConstraintUV(UP);
				
				// Create our basebone and add it to the chain
				FabrikBone2D basebone = new FabrikBone2D( new Vec2f(0.0f, -100.0f), UP, boneLength);
				basebone.setClockwiseConstraintDegs(15.0f);
				basebone.setAnticlockwiseConstraintDegs(15.0f);
				
				// Add the basebone and two additional bones to the chain
				verticalChain.addBone(basebone);
				verticalChain.addConsecutiveConstrainedBone(UP, boneLength, 15.0f, 15.0f);
				verticalChain.addConsecutiveConstrainedBone(UP, boneLength, 15.0f, 15.0f);
				
				// Add chain to structure
				mStructure.addChain(verticalChain);
				
				// ----- Left branch chain -----				
				boneLength = 30.0f;
				
				// Create the base bone
				basebone = new FabrikBone2D( new Vec2f(), new Vec2f(-boneLength, 0.0f) );
				basebone.setClockwiseConstraintDegs(15.0f);
				basebone.setAnticlockwiseConstraintDegs(15.0f);
				basebone.setColour(Utils.MID_GREEN);
				
				// Create the chain to add it to and enable base bone constraint mode
				FabrikChain2D leftChain = new FabrikChain2D();
				leftChain.setBaseboneConstraintType(BaseboneConstraintType2D.LOCAL_ABSOLUTE);
				leftChain.setBaseboneConstraintUV(LEFT);	
				
				// Add the basebone to the chain
				leftChain.addBone(basebone);
				
				// Add consecutive constrained bones
				leftChain.addConsecutiveConstrainedBone(LEFT, boneLength, 90.0f, 90.0f, Utils.MID_GREEN);
				leftChain.addConsecutiveConstrainedBone(LEFT, boneLength, 90.0f, 90.0f, Utils.MID_GREEN);
										
				// Add the chain to the structure, connecting at the end of bone 0 in chain 0
				mStructure.addConnectedChain(leftChain, 0, 0, BoneConnectionPoint2D.END);
				
				// ----- Right branch chain -----				
				// Create the base bone
				basebone = new FabrikBone2D( new Vec2f(), new Vec2f(boneLength, 0.0f) );
				basebone.setClockwiseConstraintDegs(30.0f);
				basebone.setAnticlockwiseConstraintDegs(30.0f);
				basebone.setColour(Utils.GREY);
				
				// Create the chain to add it to and enable base bone constraint mode
				FabrikChain2D rightChain = new FabrikChain2D();
				rightChain.setBaseboneConstraintType(BaseboneConstraintType2D.LOCAL_ABSOLUTE);
				rightChain.setBaseboneConstraintUV(RIGHT);
				
				// Add the basebone to the chain
				rightChain.addBone(basebone);
				
				// Add two consecutive constrained bones to the chain
				rightChain.addConsecutiveConstrainedBone(RIGHT, boneLength, 60.0f, 60.0f, Utils.GREY);
				rightChain.addConsecutiveConstrainedBone(RIGHT, boneLength, 90.0f, 90.0f, Utils.GREY);
				
				// Add the chain to the structure, connecting at the end of bone 1 in chain 0
				mStructure.addConnectedChain(rightChain, 0, 1, BoneConnectionPoint2D.END);
				
				break;
			}
			
			case 7:
			{
				// Update the window title
				String demoName = "Demo 7 - Varying-offset 'fixed' chains with embedded targets";
				Application.window.setWindowTitle(demoName);
				
				// Instantiate our FabrikStructure2D. We're only drawing a single chain in this instance, but because
				// our demo code is geared to draw a structure, we're going to create a structure and add a chain to it.
				mStructure = new FabrikStructure2D();
				
				// Create a new chain
				FabrikChain2D chain = new FabrikChain2D();
				
				float boneLength = 50.0f;
				float startY     = -100.0f;
				
				// ----- Central white chain ------
				// Create the first bone, configure it, and add it to the chain
				FabrikBone2D basebone;
				basebone = new FabrikBone2D(new Vec2f(0.0f, startY), new Vec2f(0.0f, startY + boneLength) );
				basebone.setClockwiseConstraintDegs(65.0f);
				basebone.setAnticlockwiseConstraintDegs(65.0f);
				chain.addBone(basebone);
				
				// Fix the base bone to its current location, and constrain it to the positive Y-axis
				chain.setFixedBaseMode(true);		
				chain.setBaseboneConstraintType(BaseboneConstraintType2D.GLOBAL_ABSOLUTE);
				chain.setBaseboneConstraintUV(UP);
		
				// Add second and third bones
				chain.addConsecutiveBone(UP, boneLength);
				chain.addConsecutiveBone(UP, boneLength);
				
				// Finally, add the chain to the structure
				mStructure.addChain(chain);
				
				// ----- Left green chain with embedded target -----		
				FabrikChain2D leftChain = new FabrikChain2D();				
				leftChain.setEmbeddedTargetMode(true); // Enable embedded targets - we actually set the embedded target location in the demo loop
				basebone = new FabrikBone2D(new Vec2f(), new Vec2f(-boneLength / 6.0f, 0.0f) );
						
				// Add fifteen bones
				leftChain.addBone(basebone);
				for (int boneLoop = 0; boneLoop < 14; ++boneLoop)
				{	
					leftChain.addConsecutiveConstrainedBone(RIGHT, boneLength / 6.0f, 25.0f, 25.0f);
				}
				
				// Set chain colour and basebone constraint type
				leftChain.setColour(Utils.MID_GREEN);
				
				// Add the left chain to the structure, connected to the start of bone 1 in chain 0
				mStructure.addConnectedChain(leftChain, 0, 1, BoneConnectionPoint2D.START);
				
				// ----- Right grey chain with embedded target -----
				FabrikChain2D rightChain = new FabrikChain2D();				
				rightChain.setEmbeddedTargetMode(true); // Enable embedded targets - we actually set the embedded target location in the demo loop
				basebone = new FabrikBone2D(new Vec2f(), new Vec2f(boneLength / 5.0f, 0.0f) );
				basebone.setClockwiseConstraintDegs(60.0f);
				basebone.setAnticlockwiseConstraintDegs(60.0f);
						
				// Add ten bones
				rightChain.addBone(basebone);
				for (int boneLoop = 0; boneLoop < 9; ++boneLoop)
				{
					rightChain.addConsecutiveBone(RIGHT, boneLength / 5.0f);
				}
				
				// Set chain colour and basebone constraint type
				rightChain.setColour(Utils.GREY);
				rightChain.setBaseboneConstraintType(BaseboneConstraintType2D.LOCAL_ABSOLUTE);
				rightChain.setBaseboneRelativeConstraintUV(RIGHT);
				
				// Add the right chain to the structure, connected to the start of bone 2 in chain 0
				mStructure.addConnectedChain(rightChain, 0, 2, BoneConnectionPoint2D.START);
				
				break;
			}
			
			case 8:
			{				
				String demoName = "Demo 8 - Multiple nested chains in a semi-random configuration";
				Application.window.setWindowTitle(demoName);
				
				// Instantiate our FabrikStructure2D
				mStructure = new FabrikStructure2D(demoName);
								
				mStructure.addChain( createRandomChain() );
				int chainsInStructure = 1;
				
				int maxChains = 3;
				for (int chainLoop = 0; chainLoop < maxChains; chainLoop++)
				{	
					FabrikChain2D tempChain = createRandomChain();
					tempChain.setBaseboneConstraintType(BaseboneConstraintType2D.LOCAL_RELATIVE);
					tempChain.setBaseboneConstraintUV(UP);
					
					mStructure.addConnectedChain( createRandomChain(), Utils.randRange(0, chainsInStructure++), Utils.randRange(0, 5) );
				}				
				break;
			}
			
			default:
				throw new RuntimeException("No such demo.");
		}
	}
	
	/**
	 * Create and return random FabrikChain2D.
	 * 
	 * @return A random FabrikChain2D.
	 */
	private FabrikChain2D createRandomChain()
	{
		float boneLength           = 20.0f;
		float boneLengthRatio      = 0.8f;		
		float constraintAngleDegs  = 20.0f;
		float constraintAngleRatio = 1.4f; 
					
		// ----- Vertical chain -----
		FabrikChain2D chain = new FabrikChain2D();
		chain.setFixedBaseMode(true);	
		
		FabrikBone2D basebone = new FabrikBone2D( new Vec2f(), UP, boneLength);
		basebone.setClockwiseConstraintDegs(constraintAngleDegs);
		basebone.setAnticlockwiseConstraintDegs(constraintAngleDegs);
		chain.setBaseboneConstraintType(BaseboneConstraintType2D.LOCAL_RELATIVE);
		chain.addBone(basebone);	
		
		int numBones = 6;
		float perturbLimit = 0.4f;
		for (int boneLoop = 0; boneLoop < numBones; boneLoop++)
		{
			boneLength          *= boneLengthRatio;
			constraintAngleDegs *= constraintAngleRatio;
			Vec2f perturbVector  = new Vec2f( Utils.randRange(-perturbLimit, perturbLimit), Utils.randRange(-perturbLimit, perturbLimit) );
			
			chain.addConsecutiveConstrainedBone( UP.plus(perturbVector), boneLength, constraintAngleDegs, constraintAngleDegs );					
		}				
		
		chain.setColour( Colour4f.randomOpaqueColour() );
		
		return chain;
	}
	
	/** Set all chains in the structure to be in fixed-base mode whereby the base locations cannot move. */
	public void setFixedBaseMode(boolean value) { mStructure.setFixedBaseMode(value); }
	
	/** Dummy method to set the base locations of any chains in the structure to rotate about the origin. */
	public void rotateBaseLocations() { }
	
	/** Dummy method to handle the movement of the camera using the W/S/A/D keys - as this is 2D we aren't actually doing any camera movement. */
	public void handleCameraMovement(int key, int action) { }
	
	/** Draw the currentstate of this demo / FabrikStructure2D and any contained IK chains. */
	public void draw()
	{	
		// Demo 7 or 8? Offset the base location...
		if (Application.demoNumber == 7 || Application.demoNumber == 8)
		{
			// Rotate offset and apply to base location of first chain
			mRotatingOffset = Vec2f.rotateDegs(mRotatingOffset, 1.0f);			
			mStructure.getChain(0).setBaseLocation( mOrigBaseLocation.plus(mRotatingOffset) );
			
			if (Application.demoNumber == 7)
			{
				// Rotate offsets for left and right chains
				mSmallRotatingOffsetLeft  = Vec2f.rotateDegs(mSmallRotatingOffsetLeft, -1.0f);
				mSmallRotatingOffsetRight = Vec2f.rotateDegs(mSmallRotatingOffsetRight, 2.0f);
				
				Vec2f newEmbeddedTargetLoc = new Vec2f();
				newEmbeddedTargetLoc.set(mStructure.getChain(1).getEmbeddedTarget() );				
				mStructure.getChain(1).updateEmbeddedTarget( mSmallRotatingTargetLeft.plus(mSmallRotatingOffsetLeft)   );
				mStructure.getChain(2).updateEmbeddedTarget( mSmallRotatingTargetRight.plus(mSmallRotatingOffsetRight) );
			}

			// Update the structure. Even though we're not moving the target, we ARE moving the 
			// base location, so this forces the IK chain to be resolved for the new base location.
			mStructure.solveForTarget( OpenGLWindow.worldSpaceMousePos );
		}
		
		// Draw our structure
		FabrikLine2D.draw( mStructure, 4.0f, Application.window.getMvpMatrix() );
		
		// Draw bone constraints if the draw constraints flag is true
		if (Application.drawConstraints)
		{
			//FabrikLine2D.drawConstraints( mStructure, mCurrentConstraintLineLength, mCurrentConstraintLineWidth, Application.window.getProjectionMatrixMatrix() );
			FabrikLine2D.drawConstraints( mStructure, mConstraintLineLength, mConstraintLineWidth, Application.window.getMvpMatrix() );
		}
				
		// Draw our target as a yellow point
		mTargetPoint.draw( OpenGLWindow.worldSpaceMousePos, Utils.YELLOW, 5.0f, Application.window.getMvpMatrix() );		

	} // End of draw method
	
} // End of CalikoDemo2D class
