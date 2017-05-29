package au.edu.federation.caliko.demo2d;

import au.edu.federation.caliko.BoneConnectionPoint;
import au.edu.federation.caliko.FabrikBone2D;
import au.edu.federation.caliko.FabrikChain2D;
import au.edu.federation.caliko.FabrikStructure2D;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Utils;
import au.edu.federation.utils.Vec2f;

/**
 * @author jsalvo
 */
public class MultipleConnectedChainsNoBaseBoneConstraints extends CalikoDemoStructure2D {

	@Override
	public void setup() {
		// Instantiate our FabrikStructure2D
		this.structure = new FabrikStructure2D("Demo 4 - Multiple connected chains with no base-bone constraints.");
		
		// ----- Vertical chain -----
		float boneLength = 50.0f;
		FabrikChain2D verticalChain = new FabrikChain2D();
		FabrikBone2D basebone = new FabrikBone2D( new Vec2f(0.0f, -50.0f), UP, boneLength);
		
		// Note: Default basebone constraint type is NONE
		verticalChain.addBone(basebone);
		
		// Add two additional consecutive bones
		verticalChain.addConsecutiveConstrainedBone(UP,  boneLength, 90.0f, 90.0f);
		verticalChain.addConsecutiveConstrainedBone(UP,  boneLength,  90.0f, 90.0f);
		
		// Add our main chain to structure
		this.structure.addChain(verticalChain);
		
		// ----- Left branch chain -----				
		boneLength = 30.0f;
		
		// Create the base bone and set its colour
		basebone = new FabrikBone2D( new Vec2f(), new Vec2f(-boneLength, 0.0f) );
		basebone.setColour(Utils.MID_GREEN);
		
		// Create the chain and add the basebone to it
		FabrikChain2D leftChain = new FabrikChain2D();
		leftChain.addBone(basebone);
		
		// Add consecutive constrained bones
		// Note: The base-bone is unconstrained, but these bones ARE constrained				
		leftChain.addConsecutiveConstrainedBone(LEFT, boneLength, 90.0f, 90.0f, Utils.MID_GREEN);
		leftChain.addConsecutiveConstrainedBone(LEFT, boneLength, 90.0f, 90.0f, Utils.MID_GREEN);
		
		// Add the chain to the structure, connecting to the end of bone 0 in chain 0
		this.structure.connectChain(leftChain, 0, 0, BoneConnectionPoint.END);
		
		// ----- Right branch chain -----
					
		// Create the base bone
		basebone = new FabrikBone2D( new Vec2f(), new Vec2f(boneLength, 0.0f) );
		basebone.setColour(Utils.GREY);
		
		// Create the chain and add the basebone to it
		FabrikChain2D rightChain = new FabrikChain2D();
		rightChain.addBone(basebone);
						
		// Add two consecutive constrained bones to the chain
		// Note: The base-bone is unconstrained, but these bones ARE constrained
		rightChain.addConsecutiveConstrainedBone(RIGHT, boneLength, 60.0f, 60.0f, Utils.GREY);
		rightChain.addConsecutiveConstrainedBone(RIGHT, boneLength, 60.0f, 60.0f, Utils.GREY);
		
		// Add the chain to the structure, connecting to the end of bone 1 in chain 0
		this.structure.connectChain(rightChain, 0, 1, BoneConnectionPoint.END);
	}
	
	@Override
	public void drawTarget(Mat4f mvpMatrix) {
		// Do nothing
	}	

}
