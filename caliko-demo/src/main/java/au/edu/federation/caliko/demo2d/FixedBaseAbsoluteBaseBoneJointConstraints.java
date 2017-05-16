package au.edu.federation.caliko.demo2d;

import au.edu.federation.caliko.FabrikBone2D;
import au.edu.federation.caliko.FabrikChain2D;
import au.edu.federation.caliko.FabrikChain2D.BaseboneConstraintType2D;
import au.edu.federation.caliko.FabrikStructure2D;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Vec2f;

/**
 * @author jsalvo
 */
public class FixedBaseAbsoluteBaseBoneJointConstraints extends CalikoDemoStructure2D {

	@Override
	public void setup() {
		// Instantiate our FabrikStructure2D
		this.structure = new FabrikStructure2D("Demo 1 - Chain with fixed base, GLOBAL_ABSOLUTE base-bone constraints, and joint constraints.");
		
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
		this.structure.addChain(chain);				
	}

	@Override
	public void drawTarget(Mat4f mvpMatrix) {
		// Do nothing
	}

}
