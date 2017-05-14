package au.edu.federation.caliko.demo3d;

import au.edu.federation.caliko.FabrikBone3D;
import au.edu.federation.caliko.FabrikChain3D;
import au.edu.federation.caliko.FabrikStructure3D;
import au.edu.federation.caliko.FabrikChain3D.BaseboneConstraintType3D;
import au.edu.federation.utils.Colour4f;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Utils;
import au.edu.federation.utils.Vec3f;

/**
 * @author jsalvo
 */
public class RotorConstrainedBaseBones extends CalikoDemoStructure3D {

	@Override
	public void setup() {
		this.structure = new FabrikStructure3D("Demo 3 - Rotor Constrained Base Bones");
		int numChains                     = 3;
		float rotStep                     = 360.0f / (float)numChains;
		float baseBoneConstraintAngleDegs = 20.0f;
		
		// ... and add multiple chains to it.
		Colour4f boneColour          = new Colour4f();
		Colour4f baseBoneColour      = new Colour4f();					
		Vec3f baseBoneConstraintAxis = new Vec3f();
		for (int chainLoop = 0; chainLoop < numChains; ++chainLoop) 
		{					
			// Choose the bone colours and base bone constraint axes
			switch (chainLoop % 3) 
			{
				case 0:
					boneColour.set(Utils.MID_RED);
					baseBoneColour.set(Utils.RED);
					baseBoneConstraintAxis = X_AXIS;
					break;
				case 1:
					boneColour.set(Utils.MID_GREEN);
					baseBoneColour.set(Utils.MID_GREEN);
					baseBoneConstraintAxis = Y_AXIS;
					break;
				case 2:
					boneColour.set(Utils.MID_BLUE);
					baseBoneColour.set(Utils.BLUE);
					baseBoneConstraintAxis = Z_AXIS.negated();
					break;
			}
		
			// Create a new chain
			FabrikChain3D chain = new FabrikChain3D();
			
			// Set up the initial base bone location...
			Vec3f startLoc = new Vec3f(0.0f, 0.0f, -40.0f);
			startLoc       = Vec3f.rotateYDegs(startLoc, rotStep * (float)chainLoop);					
			Vec3f endLoc   = startLoc.plus( baseBoneConstraintAxis.times( defaultBoneLength * 2.0f) );
			
			// ...then create a base bone, set its colour, add it to the chain and specify that it should be global rotor constrained.
			FabrikBone3D basebone = new FabrikBone3D(startLoc, endLoc);
			basebone.setColour(baseBoneColour);					
			chain.addBone(basebone);
			chain.setRotorBaseboneConstraint(BaseboneConstraintType3D.GLOBAL_ROTOR, baseBoneConstraintAxis, baseBoneConstraintAngleDegs);
		
			// Add additional consecutive, unconstrained bones to the chain
			for (int boneLoop = 0; boneLoop < 7; boneLoop++)
			{	
				boneColour = (boneLoop % 2 == 0) ? boneColour.lighten(0.5f) : boneColour.darken(0.5f);
				chain.addConsecutiveBone(defaultBoneDirection, defaultBoneLength, boneColour);
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
