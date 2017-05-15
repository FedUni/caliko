package au.edu.federation.caliko.demo2d;

import au.edu.federation.caliko.demo.OpenGLWindow;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Vec2f;

/**
 * @author jsalvo
 */
public abstract class FixedTargetDemo extends CalikoDemoStructure2D {
	
	/** Offset amount used by demos 7 and 8. */
	Vec2f mRotatingOffset       = new Vec2f(30.0f, 0.0f);
	
	/** Base location used by demos 7 and 8. */
	Vec2f mOrigBaseLocation = new Vec2f(0.0f, -80.0f);		

	@Override
	public void drawTarget(Mat4f mvpMatrix) {
		this.preDrawTarget();
		this.postDrawTarget();
	}
	
	protected void preDrawTarget() {
		// Rotate offset and apply to base location of first chain
		this.mRotatingOffset = Vec2f.rotateDegs(this.mRotatingOffset, 1.0f);			
		this.structure.getChain(0).setBaseLocation( this.mOrigBaseLocation.plus(this.mRotatingOffset) );
	}
	
	protected void postDrawTarget() {
		// Update the structure. Even though we're not moving the target, we ARE moving the 
		// base location, so this forces the IK chain to be resolved for the new base location.
		this.structure.solveForTarget( OpenGLWindow.worldSpaceMousePos );	
	}
	

}
