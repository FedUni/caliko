package au.edu.federation.caliko.demo3d;

import au.edu.federation.caliko.FabrikBone3D;
import au.edu.federation.caliko.FabrikChain3D;
import au.edu.federation.caliko.FabrikStructure3D;
import au.edu.federation.caliko.FabrikJoint3D.JointType;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Utils;
import au.edu.federation.utils.Vec3f;

/**
 * @author jsalvo
 */
public class GlobalHingesWithReferenceAxisConstraints extends CalikoDemoStructure3D {

	@Override
	public void setup() {
		this.structure = new FabrikStructure3D("Demo 5 - Global Hinges With Reference Axis Constraints");
		
		// Create a new chain				
		FabrikChain3D chain = new FabrikChain3D();
			
		// Set up the initial base bone location...
		Vec3f startLoc = new Vec3f(0.0f, 30f, -40.0f);
		Vec3f endLoc   = new Vec3f(startLoc);
		endLoc.y      -= defaultBoneLength;
			
		// ...then create a base bone, set its colour, and add it to the chain.
		FabrikBone3D basebone = new FabrikBone3D(startLoc, endLoc);
		basebone.setColour(Utils.YELLOW);
		chain.addBone(basebone);
			
		// Add alternating global hinge constrained and unconstrained bones to the chain
		float cwDegs  = 120.0f;
		float acwDegs = 120.0f;
		for (int boneLoop = 0; boneLoop < 8; ++boneLoop)
		{
			if (boneLoop % 2 == 0)
			{
				// Params: bone direction, bone length, joint type, hinge rotation axis, clockwise constraint angle, anticlockwise constraint angle, hinge constraint reference axis, colour
				// Note: There is a version of this method where you do not specify the colour - the default is to draw the bone in white.
				chain.addConsecutiveHingedBone(Y_AXIS.negated(), defaultBoneLength, JointType.GLOBAL_HINGE, Z_AXIS, cwDegs, acwDegs, Y_AXIS.negated(), Utils.GREY );
			}
			else
			{
				chain.addConsecutiveBone(Y_AXIS.negated(), defaultBoneLength, Utils.MID_GREEN);
			}
		}
			
		// Finally, add the chain to the structure
		this.structure.addChain(chain);	
	}

	@Override
	public void drawTarget(Mat4f mvpMatrix) {
		// Do nothing
	}

}
