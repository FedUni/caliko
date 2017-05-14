package au.edu.federation.caliko.demo3d;

import au.edu.federation.caliko.FabrikStructure3D;
import au.edu.federation.caliko.demo.CalikoDemoStructure;
import au.edu.federation.utils.Vec3f;

/**
 * @author jsalvo
 */
public abstract class CalikoDemoStructure3D implements CalikoDemoStructure {
	
	public static final Vec3f X_AXIS = new Vec3f(1.0f, 0.0f, 0.0f);
	public static final Vec3f Y_AXIS = new Vec3f(0.0f, 1.0f, 0.0f);	
	public static final Vec3f Z_AXIS = new Vec3f(0.0f, 0.0f, 1.0f);
	
	protected static Vec3f defaultBoneDirection   = new Vec3f(Z_AXIS).negated();
	protected static float defaultBoneLength      = 10.0f;
	protected static float boneLineWidth          = 5.0f;
	protected static float constraintLineWidth    = 2.0f;	
	protected static float baseRotationAmountDegs = 0.3f;	
	
	protected FabrikStructure3D structure;
	
	public FabrikStructure3D getStructure() {
		return structure;
	}
	

}
