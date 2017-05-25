package au.edu.federation.caliko.demo2d;

import au.edu.federation.caliko.BoneConnectionPoint;
import au.edu.federation.caliko.FabrikBone2D;
import au.edu.federation.caliko.FabrikChain2D;
import au.edu.federation.caliko.FabrikChain2D.BaseboneConstraintType2D;
import au.edu.federation.caliko.FabrikStructure2D;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Utils;
import au.edu.federation.utils.Vec2f;

/**
 * @author jsalvo
 */
public class MultipleConnectedChainsLocalRelativeBaseBoneConstraints extends CalikoDemoStructure2D {

	@Override
	public void setup() {
		// Instantiate our FabrikStructure2D
		this.structure = new FabrikStructure2D("Demo 5 - Multiple connected chains with LOCAL_RELATIVE base-bone constraints.");
		
		// ----- Vertical chain -----
		float boneLength = 50.0f;
		FabrikChain2D verticalChain = new FabrikChain2D();
		
		FabrikBone2D basebone = new FabrikBone2D( new Vec2f(0.0f, -100.0f), UP, boneLength);
		basebone.setClockwiseConstraintDegs(15.0f);
		basebone.setAnticlockwiseConstraintDegs(15.0f);
		
		verticalChain.addBone(basebone);
		verticalChain.addConsecutiveConstrainedBone(UP,  boneLength, 30.0f, 30.0f);
		verticalChain.addConsecutiveConstrainedBone(UP,  boneLength, 30.0f, 30.0f);
		verticalChain.setBaseboneConstraintType(BaseboneConstraintType2D.GLOBAL_ABSOLUTE);
		verticalChain.setBaseboneConstraintUV(UP);
		
		// Add chain to structure
		this.structure.addChain(verticalChain);
		
		// ----- Left branch chain -----				
		boneLength = 30.0f;
		
		// Create the base bone
		basebone = new FabrikBone2D( new Vec2f(), new Vec2f(-boneLength, 0.0f) );
		basebone.setClockwiseConstraintDegs(60.0f);
		basebone.setAnticlockwiseConstraintDegs(60.0f);
		basebone.setColour(Utils.MID_GREEN);
		
		// Create the chain to add it to and enable base bone constraint mode
		FabrikChain2D leftChain = new FabrikChain2D();		
		leftChain.setBaseboneConstraintType(BaseboneConstraintType2D.LOCAL_RELATIVE);
		
		// Add the basebone to the chain
		leftChain.addBone(basebone);
		
		// Add consecutive constrained bones
		leftChain.addConsecutiveConstrainedBone(LEFT, boneLength, 60.0f, 60.0f, Utils.MID_GREEN);
		leftChain.addConsecutiveConstrainedBone(LEFT, boneLength, 60.0f, 60.0f, Utils.MID_GREEN);
				
		// Params: The chain to add to the structure, which chain it links to, which bone in that chain it links to
		this.structure.connectChain(leftChain, 0, 0, BoneConnectionPoint.END);
		
		// ----- Right branch chain -----				
		// Create the base bone
		basebone = new FabrikBone2D( new Vec2f(), new Vec2f(boneLength, 0.0f) );
		basebone.setClockwiseConstraintDegs(30.0f);
		basebone.setAnticlockwiseConstraintDegs(30.0f);
		basebone.setColour(Utils.GREY);
		
		// Create the chain to add it to and enable base bone constraint mode
		FabrikChain2D rightChain = new FabrikChain2D();
		rightChain.setBaseboneConstraintType(BaseboneConstraintType2D.LOCAL_RELATIVE);
		
		// Add the basebone to the chain
		rightChain.addBone(basebone);
		
		// Add two consecutive constrained bones to the chain
		rightChain.addConsecutiveConstrainedBone(RIGHT, boneLength, 15.0f, 15.0f, Utils.GREY);
		rightChain.addConsecutiveConstrainedBone(RIGHT, boneLength, 15.0f, 15.0f, Utils.GREY);
		
		// Add the chain to the structure, connecting at the end of bone 1 in chain 0
		this.structure.connectChain(rightChain, 0, 1, BoneConnectionPoint.END);
	}
	
	@Override
	public void drawTarget(Mat4f mvpMatrix) {
		// Do nothing
	}	

}
