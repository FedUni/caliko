package au.edu.federation.caliko.demo;

/**
 * This abstract class is the parent of the CalikoDemo2D and CalikoDemo3D classes and is merely
 * used to provide the ability to switch between 2D and 3D demonstration scenarios at runtime.
 */
public interface CalikoDemo
{	
	public abstract void setup(int demoNumber);
	public abstract void setFixedBaseMode(boolean value);
	public abstract void handleCameraMovement(int key, int action);
	public abstract void draw();
}
