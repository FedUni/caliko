package au.edu.federation.caliko.visualisation;

import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Vec3f;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Class to represent a 3D camera.
 * <p>
 * Update: No longer need to reset the mouse cursor in the mouse movement callback - GLFW3 now
 * uses a 'virtual' mode which hides the mouse cursor and does not limit its values, so instead
 * of comparing the cursor location to the center of the screen we simply compare it to the last value.
 * 
 * @author Al Lansley
 * @version 0.5.1 - 20/07/2016
 */
public class Camera
{
	// ----- Static properties -----
	
	// Cardinal axes
	private static final Vec3f X_AXIS = new Vec3f(1.0f, 0.0f, 0.0f);
	private static final Vec3f Y_AXIS = new Vec3f(0.0f, 1.0f, 0.0f);
	private static final Vec3f Z_AXIS = new Vec3f(0.0f, 0.0f, 1.0f);
	
	// Conversion factor from degrees to radians
	private static final float DEGS_TO_RADS = (float)Math.PI / 180.0f;
	
	// Used to build up the movement from held keys in the move method
	private static final Vec3f movement = new Vec3f();
	
	// ----- Non-static properties -----
	
	// Our camera controls the view matrix
	private final Mat4f mViewMatrix = new Mat4f(1.0f);
	
	/** How quickly the camera moves.
	 * 
	 * @default 80.0f
	 */
	private float  mMovementSpeedFactor = 80.0f;
	
	/** How sensitive mouse movements affect looking up and down.
	 * 
	 * @default 0.1
	 */
	private double mPitchSensitivity = 0.1;
	
	/** How sensitive mouse movements affect looking left and right.
	 * 
	 * @default 0.1
	 */
	private double mYawSensitivity = 0.1;

	// Current location and orientation of the camera
	private Vec3f mLocation     = new Vec3f();
	private Vec3f mRotationDegs = new Vec3f();

	// Movement flags
	private boolean mHoldingForward     = false;
	private boolean mHoldingBackward    = false;
	private boolean mHoldingLeftStrafe  = false;
	private boolean mHoldingRightStrafe = false;

	// The middle of the screen in window coordinates
	private double mWindowMidX;
	private double mWindowMidY;
	
	// Location of mouse cursor as of last update
	private double mLastMouseX, mLastMouseY;

	// Constructor
	public Camera(Vec3f location, Vec3f rotationDegs, int windowWidth, int windowHeight)
	{
		// Set initial location and rotation values as per constructor arguments
		mLocation.set(location);
		mRotationDegs = rotationDegs;

		// When using the mouse to look around, we place the mouse cursor at the centre of the window each frame
		mWindowMidX = mLastMouseX = windowWidth  / 2.0;
		mWindowMidY = mLastMouseY = windowHeight / 2.0;
	}
	
	public Mat4f getViewMatrix()
	{
		// Reset to identity
		mViewMatrix.setIdentity();
		
		// Rotate to the orientation of the camera		
		mViewMatrix.rotateAboutLocalAxisDegs(mRotationDegs.x, X_AXIS);
		mViewMatrix.rotateAboutLocalAxisDegs(mRotationDegs.y, Y_AXIS);
		
		// Only rotate around Z if we have any Z-axis rotation - in FPS camera controls we do not.
		if (mRotationDegs.z > 0.0f)
		{
			mViewMatrix.rotateAboutLocalAxisDegs(mRotationDegs.z, Z_AXIS);
		}
		
		// Translate to our camera location and return the resulting matrix
		return mViewMatrix.translate( mLocation.negated() );
	}

	public void updateWindowSize(double windowWidth, double windowHeight)
	{
		// When using the mouse to look around, we place the mouse cursor at the centre of the window each frame
		mWindowMidX = mLastMouseX = windowWidth  / 2.0;
		mWindowMidY = mLastMouseY = windowHeight / 2.0;
	}
	
	// Method called when the LMB is released - this stops the camera 'jumping' on first mouse movement after LMB down
	public void resetLastCursorPosition() {	mLastMouseX = mWindowMidX; mLastMouseY = mWindowMidY; }

	// Method to set the movement flags depending on which keys are being pressed or released
	public void handleKeypress(int key, int action)
	{
		// Set the movement flags based on whether the key was pressed or released
		if ( key == GLFW_KEY_W ) { mHoldingForward     = (action == GLFW_PRESS || action == GLFW_REPEAT) ? true : false; }
		if ( key == GLFW_KEY_S ) { mHoldingBackward    = (action == GLFW_PRESS || action == GLFW_REPEAT) ? true : false; }
		if ( key == GLFW_KEY_A ) { mHoldingLeftStrafe  = (action == GLFW_PRESS || action == GLFW_REPEAT) ? true : false; }
		if ( key == GLFW_KEY_D ) { mHoldingRightStrafe = (action == GLFW_PRESS || action == GLFW_REPEAT) ? true : false; }
	}

	/**
	 * Method to deal with mouse position changes.
	 * <p>
	 * The pitch (up and down) and yaw (left and right) sensitivity of the camera can be modified via the setSensitivity method.
	 * 
	 * @param	mouseX	The x location of the mouse cursor.
	 * @param	mouseY	The y location of the mouse cursor.
	 */
	public void handleMouseMove(double mouseX, double mouseY)
	{
		// Calculate our horizontal and vertical mouse movement
		// Note: Swap the mouseX/Y and lastMouseX/Y to invert the direction of movement.
		double horizMouseMovement = (mouseX - mLastMouseX) * mYawSensitivity;
		double vertMouseMovement  = (mouseY - mLastMouseY) * mPitchSensitivity;
		
		// Keep the last mouse cursor location so we can tell the relative movement of the mouse
		// cursor the next time this method is called.
		mLastMouseX = mouseX;
		mLastMouseY = mouseY;
		
		// Apply the mouse movement to our rotation Vec3. The vertical (look up and down) movement is applied on
		// the X axis, and the horizontal (look left and right) movement is applied on the Y Axis
		mRotationDegs.x += vertMouseMovement;
		mRotationDegs.y += horizMouseMovement;

		// Limit looking up and down to vertically up and down
		if (mRotationDegs.x < -90.0f) { mRotationDegs.x = -90.0f; }
		if (mRotationDegs.x >  90.0f) { mRotationDegs.x = 90.0f;  }

		// Looking left and right - keep angles in the range 0.0 to 360.0
		// 0 degrees is looking directly down the negative Z axis "North", 90 degrees is "East", 180 degrees is "South", 270 degrees is "West"
		if (mRotationDegs.y < 0.0f  ) { mRotationDegs.y += 360.0f; }
		if (mRotationDegs.y > 360.0f) { mRotationDegs.y -= 360.0f; }		

		// Note: If you prefer to keep the angles in the range -180 to +180 use this code and comment out the 0 to 360 code above
		//if (rotation.y < -180.0f) { rotation.y += 360.0f; }
		//if (rotation.y >  180.0f) { rotation.y -= 360.0f; }		
	}

	/**
	 * Calculate which direction we need to move the camera and by what amount.
	 * <p>
	 * If you are not calculating frame duration then you may want to pass it 1.0f divided by the refresh rate of the monitor in use.
	 * 
	 * @param	deltaTime	The frame duration in milliseconds.
	 */
	public void move(float deltaTime)
	{
		// Zero our movement vector
		movement.zero();

		// Get the Sine and Cosine of our x and y axes
		// Note: Java's Math.sin/cos/tan etc. expects angles in radians. As we're working in degrees we must convert degrees to radians to get valid results
		float sinXRot = (float)Math.sin( Math.toRadians( mRotationDegs.x ) );
		float cosXRot = (float)Math.cos( Math.toRadians( mRotationDegs.x ) );

		float sinYRot = (float)Math.sin( Math.toRadians( mRotationDegs.y ) );
		float cosYRot = (float)Math.cos( Math.toRadians( mRotationDegs.y ) );

		float pitchLimitFactor = cosXRot; // This cancels out moving on the Z axis when we're looking up or down

		// Move appropriately depending on which key(s) are currently being held
		if (mHoldingForward)     { Vec3f.add(movement, new Vec3f(sinYRot * pitchLimitFactor, -sinXRot, -cosYRot * pitchLimitFactor) ); }
		if (mHoldingBackward)    { Vec3f.add(movement, new Vec3f(-sinYRot * pitchLimitFactor, sinXRot, cosYRot * pitchLimitFactor) );  }
		if (mHoldingLeftStrafe)  { Vec3f.subtract(movement, new Vec3f(cosYRot, 0.0f, sinYRot) );                                       }
		if (mHoldingRightStrafe) { Vec3f.add(movement, new Vec3f( cosYRot, 0.0f, sinYRot) );                                           }

		// If we have any movement at all, then normalise our movement vector
		if (movement.length() > 0.0f) { movement.normalise(); }

		// Apply our framerate-independent factor to our movement vector so that we move at the same speed
		// regardless of our framerate (assuming a correct frame duration is provided to this method).
		Vec3f.times(movement, mMovementSpeedFactor * deltaTime);

		// Finally, apply the movement to our position
		mLocation = mLocation.plus(movement);
	}

	// ----- Getters -----

	public Vec3f getLocation()     { return mLocation;       }
	public float getXLoc()         { return mLocation.x;     }
	public float getYLoc()         { return mLocation.y;     }
	public float getZLoc()         { return mLocation.z;     }

	public Vec3f getRotationDegs() { return mRotationDegs;   }
	public float getXRotDegs()     { return mRotationDegs.x; }
	public float getYRotDegs()     { return mRotationDegs.y; }
	public float getZRotDegs()     { return mRotationDegs.z; }

	public Vec3f getRotationRads() { return new Vec3f( getXRotRads(), getYRotRads(), getZRotRads() );  }
	public float getXRotRads()     { return mRotationDegs.x * Camera.DEGS_TO_RADS;                     }
	public float getYRotRads()     { return mRotationDegs.y * Camera.DEGS_TO_RADS;                     }
	public float getZRotRads()     { return mRotationDegs.z * Camera.DEGS_TO_RADS;                     }
	
	// ----- Setters -----
	
	/**
	 * Set the movement speed of the camera.
	 * <p>
	 * If this is set to zero, then the camera cannot move.
	 * <p>
	 * If a movement speed of less than zero is specified then an IllegalArgumentException is thrown.
	 * 
	 * @param	movementSpeed	The speed which the camera moves forward/back/left/right.
	 */
	public void setMovementSpeed(float movementSpeed)
	{
		if (movementSpeed <= 0.0f) { throw new IllegalArgumentException("The movement speed factor must be a positive value."); }
		mMovementSpeedFactor = movementSpeed;
	}
	
	/**
	 * Set the mouse look sensitivity of the camera.
	 * <p>
	 * Setting a value of zero to the pitch sensitivity will mean the camera cannot look up and down.
	 * Setting a value of zero to the yaw   sensitivity will mean the camera cannot look left and right.
	 * <p>
	 * It is allowable to set negative values to the pitch and yaw sensitivity values, which will result
	 * in the mouse direction 'looking' in the opposite direction - for example, a negative pitch sensitivity
	 * will result in 'flight-simulator' controls.
	 * 
	 * @param	pitchSensitivity	The pitch (up and down) sensitivity.
	 * @param	yawSensitivity		The yaw (left and right) sensitivity.
	 */
	public void setMouseLookSensitivity(double pitchSensitivity, double yawSensitivity)
	{
		mPitchSensitivity = pitchSensitivity;
		mYawSensitivity   = yawSensitivity;
	}

} // End of Camera class
