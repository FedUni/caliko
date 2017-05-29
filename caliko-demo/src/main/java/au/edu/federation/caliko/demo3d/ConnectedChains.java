package au.edu.federation.caliko.demo3d;

import au.edu.federation.caliko.BoneConnectionPoint;
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
public class ConnectedChains extends CalikoDemoStructure3D {

	@Override
	public void setup() {
		this.structure = new FabrikStructure3D("Demo 8 - Connected Chains");
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
		for (int boneLoop = 0; boneLoop < 5; boneLoop++)
		{
			boneColour = (boneLoop % 2 == 0) ? boneColour.lighten(0.4f) : boneColour.darken(0.4f);
			chain.addConsecutiveBone(defaultBoneDirection, defaultBoneLength, boneColour);
		}
		
		// Finally, add the chain to the structure
		this.structure.addChain(chain);		
			
		FabrikChain3D secondChain = new FabrikChain3D("Second Chain");
		FabrikBone3D base = new FabrikBone3D( new Vec3f(100.0f), new Vec3f(110.0f) );
		secondChain.addBone(base);
		secondChain.addConsecutiveBone(X_AXIS, 20.0f);
		secondChain.addConsecutiveBone(Y_AXIS, 20.0f);
		secondChain.addConsecutiveBone(Z_AXIS, 20.0f);
		
		// Set the colour of all bones in the chain in a single call, then connect it to the chain...
		secondChain.setColour(Utils.RED);
		this.structure.connectChain(secondChain, 0, 0, BoneConnectionPoint.START);
		
		// ...we can keep adding the same chain at various points if we like, because the chain we
		// connect is actually a clone of the one we provide, and not the original 'secondChain' argument.
		secondChain.setColour(Utils.WHITE);
		this.structure.connectChain(secondChain, 0, 2, BoneConnectionPoint.START);
		
		// We can also set connect the chain to the end of a specified bone (this overrides the START/END 
		// setting of the bone we connect to).
		secondChain.setColour(Utils.BLUE);
		this.structure.connectChain(secondChain, 0, 4, BoneConnectionPoint.END);
	}

	@Override
	public void drawTarget(Mat4f mvpMatrix) {
		// Do nothing
	}

}
