package au.edu.federation.caliko.demo2d;

import au.edu.federation.caliko.FabrikBone2D;
import au.edu.federation.caliko.FabrikChain2D;
import au.edu.federation.caliko.FabrikChain2D.BaseboneConstraintType2D;
import au.edu.federation.caliko.FabrikStructure2D;
import au.edu.federation.utils.Colour4f;
import au.edu.federation.utils.Utils;
import au.edu.federation.utils.Vec2f;

/**
 * @author jsalvo
 */
public class MultipleNestedChainsSemiRandom extends FixedTargetDemo {
	
	@Override
	public void setup() {
		// Instantiate our FabrikStructure2D
		this.structure = new FabrikStructure2D("Demo 8 - Multiple nested chains in a semi-random configuration");
						
		this.structure.addChain( createRandomChain() );
		int chainsInStructure = 1;
		
		int maxChains = 3;
		for (int chainLoop = 0; chainLoop < maxChains; chainLoop++)
		{	
			FabrikChain2D tempChain = createRandomChain();
			tempChain.setBaseboneConstraintType(BaseboneConstraintType2D.LOCAL_RELATIVE);
			tempChain.setBaseboneConstraintUV(UP);
			
			this.structure.connectChain( createRandomChain(), Utils.randRange(0, chainsInStructure++), Utils.randRange(0, 5) );
		}
	}
	
	/**
	 * Create and return random FabrikChain2D.
	 * 
	 * @return A random FabrikChain2D.
	 */
	private FabrikChain2D createRandomChain()
	{
		float boneLength           = 20.0f;
		float boneLengthRatio      = 0.8f;		
		float constraintAngleDegs  = 20.0f;
		float constraintAngleRatio = 1.4f; 
					
		// ----- Vertical chain -----
		FabrikChain2D chain = new FabrikChain2D();
		chain.setFixedBaseMode(true);	
		
		FabrikBone2D basebone = new FabrikBone2D( new Vec2f(), UP, boneLength);
		basebone.setClockwiseConstraintDegs(constraintAngleDegs);
		basebone.setAnticlockwiseConstraintDegs(constraintAngleDegs);
		chain.setBaseboneConstraintType(BaseboneConstraintType2D.LOCAL_RELATIVE);
		chain.addBone(basebone);	
		
		int numBones = 6;
		float perturbLimit = 0.4f;
		for (int boneLoop = 0; boneLoop < numBones; boneLoop++)
		{
			boneLength          *= boneLengthRatio;
			constraintAngleDegs *= constraintAngleRatio;
			Vec2f perturbVector  = new Vec2f( Utils.randRange(-perturbLimit, perturbLimit), Utils.randRange(-perturbLimit, perturbLimit) );
			
			chain.addConsecutiveConstrainedBone( UP.plus(perturbVector), boneLength, constraintAngleDegs, constraintAngleDegs );					
		}				
		
		chain.setColour( Colour4f.randomOpaqueColour() );
		
		return chain;
	}

}
