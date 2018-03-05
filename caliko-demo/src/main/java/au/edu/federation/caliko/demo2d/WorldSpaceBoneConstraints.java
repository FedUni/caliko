package au.edu.federation.caliko.demo2d;

import au.edu.federation.caliko.FabrikBone2D;
import au.edu.federation.caliko.FabrikChain2D;
import au.edu.federation.caliko.FabrikChain2D.BaseboneConstraintType2D;
import au.edu.federation.caliko.FabrikJoint2D.ConstraintCoordinateSystem;
import au.edu.federation.caliko.FabrikStructure2D;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Vec2f;

/**
 * @author alansley
 */
public class WorldSpaceBoneConstraints extends CalikoDemoStructure2D {

	@Override
	public void setup() {
		// Instantiate our FabrikStructure2D
		this.structure = new FabrikStructure2D("Demo 9 - Chain with fixed base and world space (GLOBAL) bone constaints.");
		
		// Create a new chain
		FabrikChain2D chain = new FabrikChain2D();
		
		float boneLength = 40.0f;
		
		// Create and add first bone - 25 clockwise, 90 anti-clockwise
		FabrikBone2D basebone;
		basebone = new FabrikBone2D(new Vec2f(0.0f, -boneLength), new Vec2f(0.0f, 0.0f) );
		basebone.setClockwiseConstraintDegs(90.0f);
		basebone.setAnticlockwiseConstraintDegs(90.0f);		
		chain.addBone(basebone);
		
		// Fix the base bone to its current location, and constrain it to the positive Y-axis
		chain.setFixedBaseMode(true);		
		chain.setBaseboneConstraintType(BaseboneConstraintType2D.GLOBAL_ABSOLUTE);
		chain.setBaseboneConstraintUV( new Vec2f(0.0f, 1.0f) );
		
		chain.addConsecutiveBone(UP, boneLength);
		chain.addConsecutiveBone(UP, boneLength);
		
		// Create and add the fourth 'gripper' bone - locked in place facing right (i.e. 0 degree movement allowed both clockwise & anti-clockwise)
		// Note: The start location of (50.0f, 50.0f) is ignored because we're going to add this to the end of the chain, wherever that may be.
		FabrikBone2D gripper = new FabrikBone2D(new Vec2f(50.0f, 50.0f), RIGHT, boneLength / 2.0f, 30.0f, 30.0f);		
		gripper.setJointConstraintCoordinateSystem(ConstraintCoordinateSystem.GLOBAL);
		gripper.setGlobalConstraintUV(RIGHT);		
		chain.addConsecutiveBone(gripper);
		
		// Finally, add the chain to the structure
		this.structure.addChain(chain);				
	}

	@Override
	public void drawTarget(Mat4f mvpMatrix) {
		// Do nothing
	}

}
