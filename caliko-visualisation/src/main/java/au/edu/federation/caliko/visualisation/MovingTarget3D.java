package au.edu.federation.caliko.visualisation;

import au.edu.federation.utils.Colour4f;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Utils;
import au.edu.federation.utils.Vec3f;

/**
 * Class to move a target around in 3D.
 * <p>
 * The location can be used as the to solve FabrikChain3D / FabrikStructure3D objects.
 * 
 * @author Al Lansley
 * @version 0.3.1 - 20/07/2016
 */
public class MovingTarget3D
{
	/** Current location of the target. */
	private Vec3f mCurrentLocation;
	
	/** Location of the waypoint this target is moving towards. */
	private Vec3f mWaypointLocation;

	/** How far to 'step' each frame to reach our destination. */
	private Vec3f mStepValue;

	/** The center of the X/Y/Z extent about which waypoints may be chosen, */
	private Vec3f mCenter;
	
	/** The Point3D used to draw the current location of this MovingTarget3D. */
	private Point3D mPoint;
	
	/** A Line3D used so we can draw a vertical line through the current target position to assist in depth perception. */
	private Line3D  mDepthLine;
	
	/** The extent of the vertical line drawn through the current target location in world-space units. */
	private float mDepthLineHeight;

	/**
	 *  How far +/- the mCenter can our next waypoint be i.e. (50, 100, 150) would mean
	 *  a range of 100 on the x (-50 to +50), a range of 200 on the y (-100 to + 100)
	 *  and a range of 300 on the z (-150 to + 150) - all these values are mRanges around
	 *  our mCenter point.
	 */  
	private Vec3f mRanges;

	/** How many steps it should take to move from one waypoint to the next waypoint. */
	private int mNumSteps;

	/** Whether the movement between waypoints is paused or not.
	 * 
	 * @default	false
	 */
	private boolean mPaused = false;
	
	private static Vec3f lineTop    = new Vec3f();
	private static Vec3f lineBottom = new Vec3f();
	
	/**
	 * Constructur.
	 * 
	 * @param	center			The center point about which new waypoints are generated.
	 * @param	ranges			The X/Y/Z range about which new waypoints are generated.
	 * @param	numSteps		The number of steps we should use to traverse from one waypoint to the next.
	 * @param	depthLineHeight	The height (on the Y-axis) of the vertical line passing through the target.
	 */
	public MovingTarget3D(Vec3f center, Vec3f ranges, int numSteps, float depthLineHeight)
	{
		// Set properties from arguments
		mCenter          = center;
		mRanges          = ranges;
		mNumSteps        = numSteps;
		mDepthLineHeight = depthLineHeight;

		// Generate a new random location to start at
		mCurrentLocation   = new Vec3f();
		mCurrentLocation.x = Utils.randRange(mCenter.x - mRanges.x, mCenter.x + mRanges.x);
		mCurrentLocation.y = Utils.randRange(mCenter.y - mRanges.y, mCenter.y + mRanges.y);
		mCurrentLocation.z = Utils.randRange(mCenter.z - mRanges.z, mCenter.z + mRanges.z);

		// Generate a new random waypoint that we'll move towards
		mWaypointLocation   = new Vec3f();
		mWaypointLocation.x = Utils.randRange(mCenter.x - mRanges.x, mCenter.x + mRanges.x);
		mWaypointLocation.y = Utils.randRange(mCenter.y - mRanges.y, mCenter.y + mRanges.y);
		mWaypointLocation.z = Utils.randRange(mCenter.z - mRanges.z, mCenter.z + mRanges.z);

		// Calculate the step value to get to our waypoint in 'numSteps' steps
		mStepValue   = new Vec3f();
		mStepValue.x = (mWaypointLocation.x - mCurrentLocation.x) / numSteps;
		mStepValue.y = (mWaypointLocation.y - mCurrentLocation.y) / numSteps;
		mStepValue.z = (mWaypointLocation.z - mCurrentLocation.z) / numSteps;
		
		// Instantiate the point and line we'll draw to represent this target
		mPoint     = new Point3D();
		mDepthLine = new Line3D();
	}

	/**
	 * Step from our current location to our waypoint location.
	 * <p>
	 * Does nothing if the mPaused property is true.
	 * 
	 * @see #pause()
	 * @see #resume()
	 */
	public void step()
	{
		if (!mPaused)
		{
			//  Add step to location
			mCurrentLocation = mCurrentLocation.plus(mStepValue);

			// Generate a new waypoint when we're within one unit of the current waypoint
			float arrivalDistance = 1.0f;
			if ( (Math.abs(mCurrentLocation.x  - mWaypointLocation.x) < arrivalDistance) &&
                 (Math.abs(mCurrentLocation.y  - mWaypointLocation.y) < arrivalDistance) &&
                 (Math.abs(mCurrentLocation.z  - mWaypointLocation.z) < arrivalDistance) )
			{
				mWaypointLocation.x = Utils.randRange(mCenter.x - mRanges.x, mCenter.x + mRanges.x);
				mWaypointLocation.y = Utils.randRange(mCenter.y - mRanges.y, mCenter.y + mRanges.y);
				mWaypointLocation.z = Utils.randRange(mCenter.z - mRanges.z, mCenter.z + mRanges.z);

				// Recalculate our step value for the new waypoint location
				mStepValue.x = (mWaypointLocation.x - mCurrentLocation.x) / mNumSteps;
				mStepValue.y = (mWaypointLocation.y - mCurrentLocation.y) / mNumSteps;
				mStepValue.z = (mWaypointLocation.z - mCurrentLocation.z) / mNumSteps;
			}
		}
	}

	/**
	 * Return the current location of this target.
	 * 
	 * @return	the current location of this target.
	 */
	public Vec3f getCurrentLocation() { return mCurrentLocation; }
	
	/**
	 * Draw this target.
	 * 
	 * @param	colour		The colour to draw this target.
	 * @param	pointSize	The size of the point used to draw this target in pixels.
	 * @param	mvpMatrix	The ModelViewProjection matrix used to draw this target.
	 */
	public void draw(Colour4f colour, float pointSize, Mat4f mvpMatrix)
	{
		// Draw the point...
		mPoint.draw(mCurrentLocation, colour, pointSize, mvpMatrix);
		
		// ...and the line that assists in recognising the depth of the target location.
		lineTop.set(mCurrentLocation.x,  mDepthLineHeight, mCurrentLocation.z);
		lineBottom.set(mCurrentLocation.x, -mDepthLineHeight, mCurrentLocation.z);		
		mDepthLine.draw(lineTop, lineBottom, 1.0f, mvpMatrix);
	}
		
	/**
	 * Pause movement of the target.
	 * 
	 * @see #resume()
	 */
	public void pause() { mPaused = true; }
	
	/**
	 * Resume movement of the target after it has been paused.
	 * 
	 * @see #pause()
	 */
	public void resume() { mPaused = false; }
	
	/** Toggle the mPaused flag between false and true or vice versa. */
	public void togglePause() { mPaused = !mPaused; }
}