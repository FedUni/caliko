package au.edu.federation.caliko.demo3d;

import au.edu.federation.caliko.FabrikBone3D;
import au.edu.federation.caliko.FabrikChain3D;
import au.edu.federation.caliko.FabrikStructure3D;
import au.edu.federation.utils.Colour4f;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Utils;
import au.edu.federation.utils.Vec3f;

/**
 * @author jsalvo
 */
public class RotorBallJointConstrainedBones extends CalikoDemoStructure3D {

	int numChains             = 3;
	float rotStep             = 360.0f / (float)numChains;
	float constraintAngleDegs = 45.0f;
	Colour4f boneColour       = new Colour4f();
	
	@Override
	public void setup() {
		this.structure = new FabrikStructure3D("Demo 2 - Rotor / Ball Joint Constrained Bones");
		
		for (int chainLoop = 0; chainLoop < numChains; ++chainLoop) 
		{
			// Create a new chain
			FabrikChain3D chain = new FabrikChain3D();
			
			// Choose the bone colour
			switch (chainLoop % numChains) 
			{
				case 0:	boneColour.set(Utils.MID_RED);   break;
				case 1:	boneColour.set(Utils.MID_GREEN); break;
				case 2:	boneColour.set(Utils.MID_BLUE);  break;
			}
			
			// Set up the initial base bone location...
			Vec3f startLoc = new Vec3f(0.0f, 0.0f, -40.0f);
			startLoc       = Vec3f.rotateYDegs(startLoc, rotStep * (float)chainLoop);
			Vec3f endLoc   = new Vec3f(startLoc);
			endLoc.z      -= defaultBoneLength;
			
			// ...then create a base bone, set its colour and add it to the chain.
			FabrikBone3D basebone = new FabrikBone3D(startLoc, endLoc);
			basebone.setColour(boneColour);					
			chain.addBone(basebone);
			
			// Add additional consecutive rotor (i.e. ball joint) constrained bones to the chain					
			for (int boneLoop = 0; boneLoop < 7; boneLoop++)
			{
				boneColour = (boneLoop % 2 == 0) ? boneColour.lighten(0.4f) : boneColour.darken(0.4f);						
				chain.addConsecutiveRotorConstrainedBone(defaultBoneDirection, defaultBoneLength, constraintAngleDegs, boneColour);						
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
