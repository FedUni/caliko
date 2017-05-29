package au.edu.federation.caliko.demo3d;

import au.edu.federation.caliko.BoneConnectionPoint;
import au.edu.federation.caliko.FabrikBone3D;
import au.edu.federation.caliko.FabrikChain3D;
import au.edu.federation.caliko.FabrikChain3D.BaseboneConstraintType3D;
import au.edu.federation.caliko.FabrikStructure3D;
import au.edu.federation.utils.Colour4f;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Utils;
import au.edu.federation.utils.Vec3f;

/**
 * @author jsalvo
 */
public class LocalRotorConstrainedConnectedChains extends CalikoDemoStructure3D {

	@Override
	public void setup() {
		this.structure = new FabrikStructure3D("Demo 10 - Local Rotor Constrained Connected Chains");
		Colour4f boneColour = new Colour4f(Utils.GREEN);
		
		// Create a new chain...				
		FabrikChain3D chain = new FabrikChain3D();
			
		// ...then create a basebone, set its draw colour and add it to the chain.
		Vec3f startLoc        = new Vec3f(0.0f, 0.0f, 40.0f);
		Vec3f endLoc          = startLoc.plus( defaultBoneDirection.times(defaultBoneLength) );
		FabrikBone3D basebone = new FabrikBone3D(startLoc, endLoc);
		basebone.setColour(boneColour);
		chain.addBone(basebone);
		
		// Add additional consecutive, unconstrained bones to the chain				
		for (int boneLoop = 0; boneLoop < 7; boneLoop++)
		{
			boneColour = (boneLoop % 2 == 0) ? boneColour.lighten(0.4f) : boneColour.darken(0.4f);
			chain.addConsecutiveBone(defaultBoneDirection, defaultBoneLength, boneColour);
		}
		
		// Finally, add the chain to the structure
		this.structure.addChain(chain);		
		
		// Create a second chain which will have a relative (i.e. local) rotor basebone constraint about the X axis.
		FabrikChain3D secondChain = new FabrikChain3D("Second Chain");
		basebone = new FabrikBone3D( new Vec3f(), new Vec3f(15.0f, 0.0f, 0.0f) );
		secondChain.addBone(basebone);
		secondChain.setRotorBaseboneConstraint(BaseboneConstraintType3D.LOCAL_ROTOR, X_AXIS, 45.0f);				
		
		// Add some additional bones
		secondChain.addConsecutiveBone(X_AXIS, 15.0f);
		secondChain.addConsecutiveBone(X_AXIS, 15.0f);
		secondChain.addConsecutiveBone(X_AXIS, 15.0f);
		secondChain.setColour(Utils.RED);
		
		// Connect this second chain to the start point of bone 3 in chain 0 of the structure
		this.structure.connectChain(secondChain, 0, 3, BoneConnectionPoint.START);
	}

	@Override
	public void drawTarget(Mat4f mvpMatrix) {
		// Do nothing
	}

}
