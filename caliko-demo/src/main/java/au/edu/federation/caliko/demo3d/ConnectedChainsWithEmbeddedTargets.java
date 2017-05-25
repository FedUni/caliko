package au.edu.federation.caliko.demo3d;

import au.edu.federation.caliko.BoneConnectionPoint;
import au.edu.federation.caliko.FabrikBone3D;
import au.edu.federation.caliko.FabrikChain3D;
import au.edu.federation.caliko.FabrikChain3D.BaseboneConstraintType3D;
import au.edu.federation.caliko.FabrikStructure3D;
import au.edu.federation.caliko.visualisation.Point3D;
import au.edu.federation.utils.Colour4f;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Utils;
import au.edu.federation.utils.Vec3f;

/**
 * @author jsalvo
 */
public class ConnectedChainsWithEmbeddedTargets extends CalikoDemoStructure3D {
	
	private Point3D mTargetPoint = new Point3D();
	private Vec3f mSecondChainTarget = new Vec3f(20.0f, 0.0f, 20.0f);
	private Vec3f mSecondChainRotatingOffset = new Vec3f(20.0f, 0.0f, 0.0f);	

	@Override
	public void setup() {
		this.structure = new FabrikStructure3D("Demo 12 - Connected chains with embedded targets");
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
		secondChain.setEmbeddedTargetMode(true);
		FabrikBone3D base = new FabrikBone3D( new Vec3f(), new Vec3f(15.0f, 0.0f, 0.0f) );
		secondChain.addBone(base);
		
		// Set this second chain to have a freely rotating global hinge which rotates about the Y axis
		// Note: We MUST add the basebone to the chain before we can set the basebone constraint on it.				
		secondChain.setHingeBaseboneConstraint(BaseboneConstraintType3D.GLOBAL_HINGE, Y_AXIS, 90.0f, 45.0f, X_AXIS);
		
		/** Other potential options for basebone constraint types **/
		//secondChain.setFreelyRotatingGlobalHingedBasebone(Y_AXIS);
		//secondChain.setFreelyRotatingLocalHingedBasebone(Y_AXIS);
		//secondChain.setHingeBaseboneConstraint(BaseboneConstraintType3D.GLOBAL_HINGE, Y_AXIS, 90.0f, 45.0f, X_AXIS);
		//secondChain.setRotorBaseboneConstraint(BaseboneConstraintType3D.GLOBAL_ROTOR, Z_AXIS, 30.0f, 60.0f, Y_AXIS);
		//secondChain.setRotorBaseboneConstraint(BaseboneConstraintType3D.LOCAL_ROTOR, Z_AXIS, 30.0f, 60.0f, Y_AXIS);
		
		// Add some additional bones
		secondChain.addConsecutiveBone(X_AXIS, 20.0f);
		secondChain.addConsecutiveBone(X_AXIS, 20.0f);
		secondChain.addConsecutiveBone(X_AXIS, 20.0f);
		secondChain.setColour(Utils.GREY);
		
		// Connect this second chain to the start point of bone 3 in chain 0 of the structure
		this.structure.connectChain(secondChain, 0, 3, BoneConnectionPoint.START);
	}

	@Override
	public void drawTarget(Mat4f mvpMatrix) {
		// Specify a secondary target for connected chain which rotates on the Y axis
  	//Vec3f secondChainTarget = new Vec3f(20.0f, 20.0f, 20.0f);            	
  	this.mSecondChainRotatingOffset = Vec3f.rotateYDegs(this.mSecondChainRotatingOffset, 1.0f);
  	this.mSecondChainRotatingOffset = Vec3f.rotateXDegs(this.mSecondChainRotatingOffset, 0.5f);
  	
  	// Update the embedded target on that chain
  	this.structure.getChain(1).updateEmbeddedTarget( this.mSecondChainTarget.plus(this.mSecondChainRotatingOffset) );
  	
  	// Draw the target
  	this.mTargetPoint.draw( this.mSecondChainTarget.plus(this.mSecondChainRotatingOffset), Utils.YELLOW, 4.0f, mvpMatrix);
	}

}
