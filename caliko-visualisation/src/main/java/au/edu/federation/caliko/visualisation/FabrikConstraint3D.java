package au.edu.federation.caliko.visualisation;

import au.edu.federation.caliko.FabrikBone3D;
import au.edu.federation.caliko.FabrikChain3D;
import au.edu.federation.caliko.FabrikStructure3D;
import au.edu.federation.utils.Colour4f;
import au.edu.federation.utils.Mat3f;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Utils;
import au.edu.federation.utils.Vec3f;

/**
 * A class to draw various constraints for ball and hinge joints on FabrikBone3D objects.
 * 
 * @author Al Lansley
 * @version 0.3.1 - 20/07/2016
 */
public class FabrikConstraint3D
{
	// Constant to convert degrees to radians
	private static final float DEGS_TO_RADS = (float)Math.PI / 180.0f;
	
	// Constraint colours
	private static final Colour4f ANTICLOCKWISE_CONSTRAINT_COLOUR = new Colour4f(1.0f, 0.0f, 0.0f, 1.0f);
	private static final Colour4f CLOCKWISE_CONSTRAINT_COLOUR     = new Colour4f(0.0f, 0.0f, 1.0f, 1.0f);	
	private static final Colour4f BALL_JOINT_COLOUR               = new Colour4f(1.0f, 0.0f, 0.0f, 1.0f);
	private static final Colour4f GLOBAL_HINGE_COLOUR             = new Colour4f(1.0f, 1.0f, 0.0f, 1.0f);
	private static final Colour4f LOCAL_HINGE_COLOUR              = new Colour4f(0.0f, 1.0f, 1.0f, 1.0f);
	private static final Colour4f REFERENCE_AXIS_COLOUR           = new Colour4f(1.0f, 0.0f, 1.0f, 1.0f);
	
	// The drawn length of the rotor cone and the radius of the cone and circle describing the hinge axes
	private static final float CONE_LENGTH_FACTOR  = 0.3f;
	private static final float RADIUS_FACTOR       = 0.25f;	
	private static final int NUM_CONE_LINES        = 12;
	
	private static float rotStep = 360.0f / (float)NUM_CONE_LINES;

	private Circle3D mCircle; // Used to draw hinges
	private Line3D   mLine;   // Used to draw hinge axes, reference axes and ball-joint cones

	private boolean initialised = false;

	/** Default constructor. */
	public FabrikConstraint3D()
	{
		if (!initialised)
		{
			initialised = true;			
			mCircle      = new Circle3D();
			mLine        = new Line3D();
		}
	}	

	/**
	 * Draw the constraint for this FabrikBone3D.
	 * <p>
	 * Ball joints are drawn as a series of lines forming a cone, while global and local hinge joints are drawn
	 * as circles aligned to the hinge rotation axis, with an optional reference axis within the plane of the
	 * circle if required.
	 * <p>
	 * Line widths may commonly be between 1.0f and 32.0f, values outside of this range may result in unspecified behaviour.
	 * 
	 * @param	bone		The FabrikBone3D object to draw the constraint about.
	 * @param	referenceDirection As bones are constrained about the direction relative to the previous bone in the chain, this is the direction of the previous bone.
	 * @param	lineWidth	The width of the line used to draw the rotor constraint in pixels.
	 * @param	colour		The colour of the line.
	 * @param	mvpMatrix	The ModelViewProjection matrix with which to draw the circle.
	 */
	private void draw(FabrikBone3D bone, Vec3f referenceDirection, float lineWidth, Mat4f mvpMatrix)
	{
		float boneLength = bone.length();
		Vec3f lineStart = bone.getStartLocation();
		switch ( bone.getJointType() )
		{
			case BALL: {				
				float constraintAngleDegs = bone.getBallJointConstraintDegs();
				
				// If the ball joint constraint is 180 degrees then it's not really constrained, so we won't draw it
				if ( Utils.approximatelyEquals(constraintAngleDegs, 180.0f, 0.01f) ) { return; }
				
				// The constraint direction is the direction of the previous bone rotated about a perpendicular axis by the constraint angle of this bone
				Vec3f constraintDirection = Vec3f.rotateAboutAxisDegs(referenceDirection, constraintAngleDegs, Vec3f.genPerpendicularVectorQuick(referenceDirection) ).normalised();
				
				// Draw the lines about the the bone (relative to the reference direction)				
				Vec3f lineEnd;
				for (int loop = 0; loop < NUM_CONE_LINES; ++loop)
				{	
					lineEnd = lineStart.plus( constraintDirection.times(boneLength * CONE_LENGTH_FACTOR) );					
					constraintDirection = Vec3f.rotateAboutAxisDegs(constraintDirection, rotStep, referenceDirection).normalised();					
					mLine.draw(lineStart, lineEnd, BALL_JOINT_COLOUR, lineWidth, mvpMatrix);
				}
				
				// Draw the circle at the top of the cone
				float pushDistance = (float)Math.cos(constraintAngleDegs * DEGS_TO_RADS) * boneLength;
				float radius       = (float)Math.sin(constraintAngleDegs * DEGS_TO_RADS) * boneLength;				
				Vec3f circleCentre = lineStart.plus( referenceDirection.times(pushDistance * CONE_LENGTH_FACTOR) );
				mCircle.draw(circleCentre, referenceDirection, radius * CONE_LENGTH_FACTOR, BALL_JOINT_COLOUR, lineWidth, mvpMatrix);
				break;
			}

			case GLOBAL_HINGE: {
				// Get the hinge rotation axis and draw the circle describing the hinge rotation axis
				Vec3f hingeRotationAxis = bone.getJoint().getHingeRotationAxis();				
				float radius = boneLength * RADIUS_FACTOR;
				mCircle.draw(lineStart, hingeRotationAxis, radius, GLOBAL_HINGE_COLOUR, lineWidth, mvpMatrix);
				
				// Note: While ACW rotation is negative and CW rotation about an axis is positive, we store both 
				// of these as positive values between the range 0 to 180 degrees, as such we'll negate the
				// clockwise rotation value for it to turn in the correct direction.
				float anticlockwiseConstraintDegs =  bone.getHingeJointAnticlockwiseConstraintDegs();
				float clockwiseConstraintDegs     = -bone.getHingeJointClockwiseConstraintDegs(); 
				
				// If both the anticlockwise (positive) and clockwise (negative) constraint angles are not 180 degrees (i.e. we
				// are constraining the hinge about a reference direction which lies in the plane of the hinge rotation axis)...
				if ( !Utils.approximatelyEquals(anticlockwiseConstraintDegs, 180.0f, 0.01f) &&
				     !Utils.approximatelyEquals(    clockwiseConstraintDegs, 180.0f,  0.01f) )
				{	
					Vec3f hingeReferenceAxis = bone.getJoint().getHingeReferenceAxis();
					
					// ...then draw the hinge reference axis and ACW/CW constraints about it.
					mLine.draw(lineStart, lineStart.plus( hingeReferenceAxis.times(boneLength * RADIUS_FACTOR) ), REFERENCE_AXIS_COLOUR, lineWidth, mvpMatrix);
					
					Vec3f anticlockwiseDirection = Vec3f.rotateAboutAxisDegs(hingeReferenceAxis, anticlockwiseConstraintDegs, hingeRotationAxis);
					Vec3f clockwiseDirection     = Vec3f.rotateAboutAxisDegs(hingeReferenceAxis, clockwiseConstraintDegs, hingeRotationAxis);   
					Vec3f anticlockwisePoint = lineStart.plus( anticlockwiseDirection.times(radius) );
					Vec3f clockwisePoint     = lineStart.plus( clockwiseDirection.times(radius)     );
					mLine.draw(lineStart, anticlockwisePoint, ANTICLOCKWISE_CONSTRAINT_COLOUR, lineWidth, mvpMatrix);
					mLine.draw(lineStart,     clockwisePoint,     CLOCKWISE_CONSTRAINT_COLOUR, lineWidth, mvpMatrix);
				}
				break;
			}

			case LOCAL_HINGE: {				
				// Construct a rotation matrix based on the reference direction (i.e. the previous bone's direction)...
				Mat3f m = Mat3f.createRotationMatrix(referenceDirection);
				
				// ...and transform the hinge rotation axis into the previous bone's frame of reference
				Vec3f relativeHingeRotationAxis  = m.times( bone.getJoint().getHingeRotationAxis() ).normalise();
				
				// Draw the circle describing the hinge rotation axis
				float radius = boneLength * RADIUS_FACTOR;
				mCircle.draw(lineStart, relativeHingeRotationAxis, radius, LOCAL_HINGE_COLOUR, lineWidth, mvpMatrix);
															
				// Draw the hinge reference and clockwise/anticlockwise constraints if necessary
				float anticlockwiseConstraintDegs =  bone.getHingeJointAnticlockwiseConstraintDegs();
				float clockwiseConstraintDegs     = -bone.getHingeJointClockwiseConstraintDegs(); 
				if ( !Utils.approximatelyEquals(anticlockwiseConstraintDegs, 180.0f, 0.01f) &&
				     !Utils.approximatelyEquals(    clockwiseConstraintDegs, 180.0f, 0.01f) )
				{	
					// Get the relative hinge rotation axis and draw it...
					bone.getJoint().getHingeReferenceAxis().projectOntoPlane(relativeHingeRotationAxis);
					
					Vec3f relativeHingeReferenceAxis = m.times(bone.getJoint().getHingeReferenceAxis()).normalise();
					
					mLine.draw(lineStart, lineStart.plus( relativeHingeReferenceAxis.times(radius) ), REFERENCE_AXIS_COLOUR, lineWidth, mvpMatrix);
					
					// ...as well as the clockwise and anticlockwise constraints.
					Vec3f anticlockwiseDirection = Vec3f.rotateAboutAxisDegs(relativeHingeReferenceAxis, anticlockwiseConstraintDegs, relativeHingeRotationAxis);
					Vec3f clockwiseDirection     = Vec3f.rotateAboutAxisDegs(relativeHingeReferenceAxis, clockwiseConstraintDegs, relativeHingeRotationAxis);   
					Vec3f anticlockwisePoint = lineStart.plus( anticlockwiseDirection.times(radius) );
					Vec3f clockwisePoint     = lineStart.plus( clockwiseDirection.times(radius) );
					mLine.draw(lineStart, anticlockwisePoint, ANTICLOCKWISE_CONSTRAINT_COLOUR, lineWidth, mvpMatrix);
					mLine.draw(lineStart,     clockwisePoint,     CLOCKWISE_CONSTRAINT_COLOUR, lineWidth, mvpMatrix);
				}
				break;
			}
			
		} // End of switch statement
	}
	
	/**
	 * Draw the constraints on all bones in a FabrikChain3D object.
	 * 
	 * Line widths may commonly be between 1.0f and 32.0f, values outside of this range may result in unspecified behaviour.
	 * 
	 * @param chain			The chain to use.
	 * @param lineWidth		The width of the lines to draw.
	 * @param mvpMatrix		The ModelViewProjection matrix to use.
	 */
	public void draw(FabrikChain3D chain, float lineWidth, Mat4f mvpMatrix)
	{
		int numBones = chain.getNumBones();
		if (numBones > 0)
		{
			// Draw the base bone, using the constraint UV as the relative direction
			switch ( chain.getBaseboneConstraintType() )
			{
				case NONE:
					break;
			
				case GLOBAL_ROTOR: 
				case GLOBAL_HINGE:
					draw( chain.getBone(0), chain.getBaseboneConstraintUV(), lineWidth, mvpMatrix );
					break;
				
				case LOCAL_ROTOR:
				case LOCAL_HINGE:					
					// If the structure hasn't been solved yet then we won't have a relative basebone constraint which we require
					// to draw the constraint itself - so our best option is to simply not draw the constraint until we can. 
					if (chain.getBaseboneRelativeConstraintUV().length() > 0.0f) 
					{
						draw( chain.getBone(0), chain.getBaseboneRelativeConstraintUV(), lineWidth, mvpMatrix );
					}
					break;
			
				// No need for a default - constraint types are enums and we've covered them all.
			}
				
			// Draw all the bones AFTER the base bone, using the previous bone direction as the relative direction
			for (int loop = 1; loop < numBones; ++loop)
			{
				draw( chain.getBone(loop), chain.getBone(loop-1).getDirectionUV(), lineWidth, mvpMatrix );
			}
		}
	}	

	/**
	 * Draw the constraints on all bones in a FabrikChain3D object using the default line width.
	 * 
	 * If the chain does not contain any bones then an IllegalArgumentException is thrown.
	 * 
	 * @param chain			The chain to use.
	 * @param mvpMatrix		The ModelViewProjection matrix to use.
	 */
	public void draw(FabrikChain3D chain, Mat4f mvpMatrix) { draw(chain, 1.0f, mvpMatrix); }
	
	/**
	 * Draw the constraints on all chains and all bones in each chain of a FabrikStructure3D object.
	 * 
	 * If any chain in the structure does not contain any bones then an IllegalArgumentException is thrown.
	 * Line widths may commonly be between 1.0f and 32.0f, values outside of this range may result in unspecified behaviour.
	 * 
	 * @param structure		The structure to use.
	 * @param lineWidth		The width of the lines to draw.
	 * @param mvpMatrix		The ModelViewProjection matrix to use.
	 */
	public void draw(FabrikStructure3D structure, float lineWidth, Mat4f mvpMatrix)
	{
		int numChains = structure.getNumChains();
		for (int loop = 0; loop < numChains; ++loop)
		{
			draw( structure.getChain(loop), lineWidth, mvpMatrix );
		}
	}
	
	/**
	 * Draw the constraints on all chains and all bones in each chain of a FabrikStructure3D object using the default line width.
	 * 
	 * If any chain in the structure does not contain any bones then an IllegalArgumentException is thrown.
	 * 
	 * @param structure		The structure to use.
	 * @param mvpMatrix		The ModelViewProjection matrix to use.
	 */
	public void draw(FabrikStructure3D structure, Mat4f mvpMatrix) { draw(structure, 1.0f, mvpMatrix); }
	
} // End of FabrikConstraint3D class
