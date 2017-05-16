package au.edu.federation.caliko.demo3d;

import au.edu.federation.caliko.FabrikBone3D;
import au.edu.federation.caliko.FabrikChain3D;
import au.edu.federation.caliko.FabrikStructure3D;
import au.edu.federation.caliko.FabrikJoint3D.JointType;
import au.edu.federation.utils.Colour4f;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Utils;
import au.edu.federation.utils.Vec3f;

/**
 * @author jsalvo
 */
public class FreelyRotatingGlobalHinges extends CalikoDemoStructure3D {

	@Override
	public void setup() {
		this.structure = new FabrikStructure3D("Demo 4 - Freely Rotating Global Hinges");
		int numChains = 3;
		float rotStep = 360.0f / (float)numChains;
		
		// We'll create a circular arrangement of 3 chains which are each constrained about different global axes.
		// Note: Although I've used the cardinal X/Y/Z axes here, any axis can be used.
		Vec3f globalHingeAxis = new Vec3f();
		for (int chainLoop = 0; chainLoop < numChains; ++chainLoop)
		{	
			// Set colour and axes							
			Colour4f chainColour = new Colour4f();
			switch (chainLoop % numChains)
			{
				case 0:
					chainColour.set(Utils.RED);
					globalHingeAxis = X_AXIS;
					break;
				case 1:
					chainColour.set(Utils.GREEN);
					globalHingeAxis = Y_AXIS;
					break;
				case 2:
					chainColour.set(Utils.BLUE);
					globalHingeAxis = Z_AXIS;
					break;
			}
			
			// Create a new chain
			FabrikChain3D chain = new FabrikChain3D();
			
			// Set up the initial base bone location...
			Vec3f startLoc = new Vec3f(0.0f, 0.0f, -40.0f);
			startLoc       = Vec3f.rotateYDegs(startLoc, rotStep * (float)chainLoop);
			Vec3f endLoc   = startLoc.plus( defaultBoneDirection.times(defaultBoneLength) );
			
			// ...then create a base bone, set its colour, and add it to the chain.
			FabrikBone3D basebone = new FabrikBone3D(startLoc, endLoc);
			basebone.setColour(chainColour);
			chain.addBone(basebone);
			
			// Add alternating global hinge constrained and unconstrained bones to the chain
			for (int boneLoop = 0; boneLoop < 7; boneLoop++)
			{
				if (boneLoop % 2 == 0)
				{
					chain.addConsecutiveFreelyRotatingHingedBone(defaultBoneDirection, defaultBoneLength, JointType.GLOBAL_HINGE, globalHingeAxis, Utils.GREY);
				}
				else
				{
					chain.addConsecutiveBone(defaultBoneDirection, defaultBoneLength, chainColour);
				}						
			}
			
			// Finally, add the chain to the structure
			this.structure.addChain(chain);					
		}					
	}

	@Override
	public void drawTarget(Mat4f mvpMatrix) {
		// Do nothing
	}

}
