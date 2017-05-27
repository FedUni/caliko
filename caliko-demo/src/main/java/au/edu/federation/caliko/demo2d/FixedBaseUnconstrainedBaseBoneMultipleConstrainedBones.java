package au.edu.federation.caliko.demo2d;

import au.edu.federation.caliko.FabrikBone2D;
import au.edu.federation.caliko.FabrikChain2D;
import au.edu.federation.caliko.FabrikStructure2D;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Vec2f;

/**
 * @author jsalvo
 */
public class FixedBaseUnconstrainedBaseBoneMultipleConstrainedBones extends CalikoDemoStructure2D {

	@Override
	public void setup() {
		// Instantiate our FabrikStructure2D
		this.structure = new FabrikStructure2D("Demo 3 - Chain with fixed base (toggle with F), unconstrained base-bone, and multiple constrained bones.");
		
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
			Vec2f rotatedUV = Vec2f.rotateDegs(defaultUV, (float)loop * numBones);
			
			// Constrained
			chain.addConsecutiveConstrainedBone(rotatedUV, boneLength, 60.0f, 60.0f);
		}
		
		// The the chain to have a fixed base location and specify the constraint line length & width values
		chain.setFixedBaseMode(true);
		
		// Finally, add the chain to the structure
		this.structure.addChain(chain);
	}
	
	@Override
	public void drawTarget(Mat4f mvpMatrix) {
		// Do nothing
	}	

}
