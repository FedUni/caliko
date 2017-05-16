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
public class FreelyRotatingLocalHinges extends CalikoDemoStructure3D {

	@Override
	public void setup() {
		this.structure = new FabrikStructure3D("Demo 6 - Freely Rotating Local Hinges");
		int numChains = 3;
		
		// We'll create a circular arrangement of 3 chains with alternate bones each constrained about different local axes.
		// Note: Local hinge rotation axes are relative to the rotation matrix of the previous bone in the chain.
		Vec3f hingeRotationAxis  = new Vec3f();;
		
		float rotStep = 360.0f / (float)numChains;
		for (int loop = 0; loop < numChains; loop++)
		{	
			// Set colour and axes							
			Colour4f chainColour = new Colour4f();
			switch (loop % 3)
			{
				case 0:
					chainColour = Utils.RED;
					hingeRotationAxis  = new Vec3f(X_AXIS);
					break;
				case 1:
					chainColour = Utils.GREEN;
					hingeRotationAxis = new Vec3f(Y_AXIS);
					break;
				case 2:
					chainColour = Utils.BLUE;
					hingeRotationAxis = new Vec3f(Z_AXIS);
					break;
			}
			
			// Create a new chain
			FabrikChain3D chain = new FabrikChain3D();
			
			// Set up the initial base bone location...
			Vec3f startLoc = new Vec3f(0.0f, 0.0f, -40.0f);
			startLoc       = Vec3f.rotateYDegs(startLoc, rotStep * (float)loop);					
			Vec3f endLoc   = startLoc.plus( defaultBoneDirection.times(defaultBoneLength) );
			
			// ...then create a base bone, set its colour, and add it to the chain.
			FabrikBone3D basebone = new FabrikBone3D(startLoc, endLoc);
			basebone.setColour(chainColour);
			chain.addBone(basebone);
			
			// Add alternating local hinge constrained and unconstrained bones to the chain
			for (int boneLoop = 0; boneLoop < 6; boneLoop++)
			{
				if (boneLoop % 2 == 0)
				{
					chain.addConsecutiveFreelyRotatingHingedBone(defaultBoneDirection, defaultBoneLength, JointType.LOCAL_HINGE, hingeRotationAxis, Utils.GREY);
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
