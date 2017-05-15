package au.edu.federation.caliko.demo;

import au.edu.federation.caliko.FabrikStructure2D;
import au.edu.federation.caliko.demo2d.CalikoDemoStructure2D;
import au.edu.federation.caliko.demo2d.CalikoDemoStructure2DFactory;
import au.edu.federation.caliko.visualisation.FabrikLine2D;
import au.edu.federation.caliko.visualisation.Point2D;
import au.edu.federation.utils.Utils;

/**
 * Class to demonstrate some of the features of the Caliko library in 2D.
 * 
 * @author Al Lansley
 * @version 0.8 - 02/08/2016
 */
public class CalikoDemo2D implements CalikoDemo
{	
	/** Each demo works with a single structure composed of one or more IK chains. */
	static FabrikStructure2D mStructure; 
	
	/** The target is drawn at the mouse cursor location and is updated to the cursor location when the LMB is held down. */
	private static Point2D mTargetPoint = new Point2D();
	
	/**
	 * Length of constraint lines to draw in pixels.
	 * 
	 * @default 10.0f
	 */
	private float mConstraintLineLength = 10.0f;
	
	/**
	 * Width of constraint lines to draw in pixels.
	 * 
	 * @default 2.0f
	 */
	private float mConstraintLineWidth  = 2.0f;
	
	private CalikoDemoStructure2D demoStructure2d;
	
	/**
	 * Constructor
	 * 
	 * @param	demoNumber	The number of the demo to set up.
	 */
	public CalikoDemo2D(int demoNumber) { setup(demoNumber); }
	
	/**
	 * Set up a demo consisting of an arrangement of 2D IK chain(s).
	 * 
	 * @param	demoNumber	The number of the demo to set Up.
	 */
	public synchronized void setup(int demoNumber)
	{	
		try {
			this.demoStructure2d = CalikoDemoStructure2DFactory.makeDemoStructure2D(demoNumber);
			this.demoStructure2d.setup();
			mStructure = this.demoStructure2d.getStructure();	
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}		
		
		Application.window.setWindowTitle(mStructure.getName());	
	}	
	
	/** Set all chains in the structure to be in fixed-base mode whereby the base locations cannot move. */
	public void setFixedBaseMode(boolean value) { mStructure.setFixedBaseMode(value); }
	
	/** Dummy method to set the base locations of any chains in the structure to rotate about the origin. */
	public void rotateBaseLocations() { 
		// Do nothing
	}
	
	/** Dummy method to handle the movement of the camera using the W/S/A/D keys - as this is 2D we aren't actually doing any camera movement. */
	@Override
	public void handleCameraMovement(int key, int action) { 
		// Do nothing as we are in 2D
	}
	
	/** Draw the currentstate of this demo / FabrikStructure2D and any contained IK chains. */
	public void draw()
	{	
		this.demoStructure2d.drawTarget(Application.window.getMvpMatrix());
		
		// Draw our structure
		FabrikLine2D.draw( mStructure, 4.0f, Application.window.getMvpMatrix() );
		
		// Draw bone constraints if the draw constraints flag is true
		if (Application.drawConstraints)
		{
			//FabrikLine2D.drawConstraints( mStructure, mCurrentConstraintLineLength, mCurrentConstraintLineWidth, Application.window.getProjectionMatrixMatrix() );
			FabrikLine2D.drawConstraints( mStructure, mConstraintLineLength, mConstraintLineWidth, Application.window.getMvpMatrix() );
		}
				
		// Draw our target as a yellow point
		mTargetPoint.draw( OpenGLWindow.worldSpaceMousePos, Utils.YELLOW, 5.0f, Application.window.getMvpMatrix() );		

	} // End of draw method
	
} // End of CalikoDemo2D class
