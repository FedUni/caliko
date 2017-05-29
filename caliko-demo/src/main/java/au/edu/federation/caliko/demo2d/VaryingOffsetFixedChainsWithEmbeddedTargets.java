package au.edu.federation.caliko.demo2d;

import au.edu.federation.caliko.BoneConnectionPoint;
import au.edu.federation.caliko.FabrikBone2D;
import au.edu.federation.caliko.FabrikChain2D;
import au.edu.federation.caliko.FabrikChain2D.BaseboneConstraintType2D;
import au.edu.federation.caliko.FabrikStructure2D;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Utils;
import au.edu.federation.utils.Vec2f;

/**
 * @author jsalvo
 */
public class VaryingOffsetFixedChainsWithEmbeddedTargets extends FixedTargetDemo {
	
	/** Targets and offsets for chains with embedded targets in demo 7. */
	private Vec2f mSmallRotatingTargetLeft  = new Vec2f(-70.0f, 40.0f);
	private Vec2f mSmallRotatingTargetRight = new Vec2f( 50.0f, 20.0f);
	private Vec2f mSmallRotatingOffsetLeft  = new Vec2f( 25.0f, 0.0f);
	private Vec2f mSmallRotatingOffsetRight = new Vec2f(  0.0f, 30.0f);	
	
	@Override
	public void setup() {
		// Instantiate our FabrikStructure2D. We're only drawing a single chain in this instance, but because
		// our demo code is geared to draw a structure, we're going to create a structure and add a chain to it.
		this.structure = new FabrikStructure2D("Demo 7 - Varying-offset 'fixed' chains with embedded targets");
		
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
		this.structure.addChain(chain);
		
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
		this.structure.connectChain(leftChain, 0, 1, BoneConnectionPoint.START);
		
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
		this.structure.connectChain(rightChain, 0, 2, BoneConnectionPoint.START);
	}
	
	@Override
	public void drawTarget(Mat4f mvpMatrix) {
		super.preDrawTarget();
		
		// Rotate offset and apply to base location of first chain
		this.mRotatingOffset = Vec2f.rotateDegs(this.mRotatingOffset, 1.0f);			
		this.structure.getChain(0).setBaseLocation( this.mOrigBaseLocation.plus(this.mRotatingOffset) );
		
		// Rotate offsets for left and right chains
		this.mSmallRotatingOffsetLeft  = Vec2f.rotateDegs(this.mSmallRotatingOffsetLeft, -1.0f);
		this.mSmallRotatingOffsetRight = Vec2f.rotateDegs(this.mSmallRotatingOffsetRight, 2.0f);
		
		Vec2f newEmbeddedTargetLoc = new Vec2f();
		newEmbeddedTargetLoc.set(this.structure.getChain(1).getEmbeddedTarget() );				
		this.structure.getChain(1).updateEmbeddedTarget( this.mSmallRotatingTargetLeft.plus(this.mSmallRotatingOffsetLeft)   );
		this.structure.getChain(2).updateEmbeddedTarget( this.mSmallRotatingTargetRight.plus(this.mSmallRotatingOffsetRight) );
		
		super.postDrawTarget();
	}	

}
