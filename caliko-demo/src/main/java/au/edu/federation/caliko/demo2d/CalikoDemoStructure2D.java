package au.edu.federation.caliko.demo2d;

import au.edu.federation.caliko.FabrikStructure2D;
import au.edu.federation.caliko.demo.CalikoDemoStructure;
import au.edu.federation.utils.Vec2f;

/**
 * @author jsalvo
 */
public abstract class CalikoDemoStructure2D implements CalikoDemoStructure {
	
	/** Define world-space cardinal axes. */
	protected static final Vec2f UP    = new Vec2f( 0.0f, 1.0f);
	protected static final Vec2f LEFT  = new Vec2f(-1.0f, 0.0f);
	protected static final Vec2f RIGHT = new Vec2f( 1.0f, 0.0f);
	
	protected FabrikStructure2D structure;

	public FabrikStructure2D getStructure() {
		return structure;
	}	
}
