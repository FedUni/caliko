package au.edu.federation.caliko.demo2d;

import au.edu.federation.caliko.FabrikBone2D;
import au.edu.federation.caliko.FabrikChain2D;
import au.edu.federation.caliko.FabrikStructure2D;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Vec2f;

/**
 * @author jsalvo
 */
public class FixedBaseUnconstrainedBaseBaseMultipleUnconstrainedBones extends CalikoDemoStructure2D {

	@Override
	public void setup() {
		// Instantiate our FabrikStructure2D
		this.structure = new FabrikStructure2D("Demo 2 - Chain with fixed base (toggle with F), unconstrained base-bone, and multiple unconstrained bones.");
		
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
		this.structure.addChain(chain);
	}

	@Override
	public void drawTarget(Mat4f mvpMatrix) {
		// Do nothing
	}

}
