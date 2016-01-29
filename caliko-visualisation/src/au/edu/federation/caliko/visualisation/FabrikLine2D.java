package au.edu.federation.caliko.visualisation;

import au.edu.federation.caliko.FabrikBone2D;
import au.edu.federation.caliko.FabrikChain2D;
import au.edu.federation.caliko.FabrikChain2D.BaseboneConstraintType2D;
import au.edu.federation.caliko.FabrikStructure2D;
import au.edu.federation.caliko.utils.Colour4f;
import au.edu.federation.caliko.utils.Mat4f;
import au.edu.federation.caliko.utils.Vec2f;

/**
 * A static class used to draw a 2D lines to represent FabrikBone2D, FabrikChain2D or FabrickStructure2D objects.
 * <p>
 * These draw methods are only provided to assist with prototyping and debugging.
 * <p>
 * The GLSL shaders used to draw the lines require a minimum OpenGL version of 3.3 (i.e. GLSL #version 330).
 * 
 * @author  Al Lansley
 * @version  0.5 - 12/01/2016
 */
public class FabrikLine2D
{
	/** Anticlockwise constraint lines will be drawn in red at full opacity. */
	private static final Colour4f ANTICLOCKWISE_CONSTRAINT_COLOUR = new Colour4f(1.0f, 0.0f, 0.0f, 1.0f);
	
	/** Clockwise constraint lines will be drawn in blue at full opacity. */
	private static final Colour4f CLOCKWISE_CONSTRAINT_COLOUR = new Colour4f(0.0f, 0.0f, 1.0f, 1.0f);
	
	/** A static Line2D object used to draw all lines. */
	private static Line2D line = new Line2D();
	
	// ---------- FabrikBone2D Drawing Methods ----------
	
	/**
	 * Draw this bone as a line.
	 * <p>
	 * The line will be drawn in the colour and line width as taken from the bone.
	 * 
	 * @param	bone		The bone to draw.
	 * @param	mvpMatrix	The ModelViewProjection matrix with which to draw the line
	 * @see		FabrikBone2D#setColour(Colour4f)
	 * @see		FabrikBone2D#setLineWidth(float)
	 * @see		Mat4f#createOrthographicProjectionMatrix(float, float, float, float, float, float)
	 */
	public static void draw(FabrikBone2D bone, Mat4f mvpMatrix)
	{
		line.draw(bone.getStartLocation(), bone.getEndLocation(), bone.getColour(), bone.getLineWidth(), mvpMatrix);
	}	
	
	/**
	 * Draw this bone as a line using a specific line width.
	 * <p>
	 * The line will be drawn in the colour as taken from the bone.
	 * 
	 * @param	bone		The bone the draw.
	 * @param	lineWidth	The width of the line to draw .
	 * @param	mvpMatrix	The ModelViewProjection matrix with which to draw the line.
	 * @see		FabrikBone2D#setColour(Colour4f)
	 * @see		Mat4f#createOrthographicProjectionMatrix(float, float, float, float, float, float)
	 */
	public static void draw(FabrikBone2D bone, float lineWidth, Mat4f mvpMatrix)
	{
		FabrikLine2D.line.draw(bone.getStartLocation(), bone.getEndLocation(), bone.getColour(), lineWidth, mvpMatrix);
	}

	/**
	 * Draw this bone as a line using a specific colour and line width.
	 * 
	 * @param	bone		The bone the draw.
	 * @param	colour		The colour of the line to draw.
	 * @param	lineWidth	The width of the line to draw.
	 * @param	mvpMatrix	The ModelViewProjection matrix with which to draw the line.
	 * @see		Mat4f#createOrthographicProjectionMatrix(float, float, float, float, float, float)
	 */
	public void draw(FabrikBone2D bone, Colour4f colour, float lineWidth, Mat4f mvpMatrix)
	{
		FabrikLine2D.line.draw(bone.getStartLocation(), bone.getEndLocation(), colour, lineWidth, mvpMatrix);
	}	
	
	// ---------- FabrikChain2D Drawing Methods ----------
		
	/**
	 * Draw the provided IK chain as a series of lines using the colour and line width properties of each bone.
	 * 
	 * @param	chain		The chain to draw.
	 * @param	mvpMatrix	The ModelViewProjection matrix with which to draw this chain.
	 * @see		Mat4f#createOrthographicProjectionMatrix(float, float, float, float, float, float)
	 */
	public static void draw(FabrikChain2D chain, Mat4f mvpMatrix)
	{
		int numBones = chain.getNumBones();
		for (int loop = 0; loop < numBones; ++loop)
		{
			draw(chain.getBone(loop), mvpMatrix);
		}
	}

	/**
	 * Draw a FabrikChain2D as a series of lines.
	 * <p>
	 * Each line is drawn using the specified line width, with the colour taken from the bone's
	 * {@link FabrikBone2D#mColour} property.
	 * 
	 * @param chain			The FabrikChain2D object to draw. 
	 * @param lineWidth		The width of the line used to draw each bone in the chain.
	 * @param mvpMatrix		The ModelViewProjection matrix with which to draw the chain.
	 */
	public static void draw(FabrikChain2D chain, float lineWidth, Mat4f mvpMatrix)
	{
		int numBones = chain.getNumBones();
		for (int loop = 0; loop < numBones; ++loop)
		{
			FabrikLine2D.draw(chain.getBone(loop), lineWidth, mvpMatrix);
		}
	}	
	
	/**
	 * Draw the constraint angles for each bone in an IK chain.
	 * <p>
	 * Line length values must be at least 1.0f otherwise an IllegalArgumentException is thrown.
	 * Line widths may commonly be between 1.0f and 32.0f, values outside of this range may result in unspecified behaviour. 
	 * 
	 * @param	chain		The chain to draw the constraint angle lines for
	 * @param	lineLength	The length of the constraint lines to be drawn
	 * @param	lineWidth   The width of the constrain lines to be drawn
	 * @param	mvpMatrix	The ModelViewProjection matrix used to draw our geometry
	 * @see		Mat4f#createOrthographicProjectionMatrix(float, float, float, float, float, float)
	 */
	public static void drawChainConstraintAngles(FabrikChain2D chain, float lineLength, float lineWidth, Mat4f mvpMatrix)
	{
		// Sanity checking
		if (lineLength < 1.0f) { throw new IllegalArgumentException("Constraint line length must be at least 1.0f"); }
		
		// Loop over all bones in the chain
		int numBones = chain.getNumBones();
		for (int loop = 0; loop < numBones; ++loop)
		{
			// If we're working on the base bone...
			if (loop == 0)
			{
				// ...and if the base bone is constrained...
				BaseboneConstraintType2D constraintType = chain.getBaseboneConstraintType();
				if (constraintType != BaseboneConstraintType2D.NONE)
				{					
					// If the basebone constraint type LOCAL_ABSOLUTE i.e. a direction in the host bone's local space...
					Vec2f baseboneConstraintUV;
					if (constraintType == BaseboneConstraintType2D.LOCAL_ABSOLUTE)					
					{
						// ...then get that relative constraint unit vector.
						baseboneConstraintUV = chain.getBaseboneRelativeConstraintUV();
					}
					else // Otherwise, if we have a GLOBAL_ABSOLUTE or LOCAL_RELATIVE constraint type we use the standard chain constraint UV.
					{
						baseboneConstraintUV = chain.getBaseboneConstraintUV();
					}
					
					// Draw the anticlockwise constraint in red
					// Note: To the user and internally, anticlockwise rotation angles are positive.
					Vec2f constraintUV = Vec2f.rotateDegs(baseboneConstraintUV, chain.getBone(0).getAnticlockwiseConstraintDegs() );
					Vec2f lineStartLocation = chain.getBaseLocation();
					Vec2f lineEndLocation = lineStartLocation.plus( constraintUV.times(lineLength) );
					FabrikLine2D.line.draw(lineStartLocation, lineEndLocation, ANTICLOCKWISE_CONSTRAINT_COLOUR, lineWidth, mvpMatrix);					
					
					// Draw the clockwise constraint in blue
					// Note: To the user all constraint angles are positive for ease of use. But internally, clockwise rotation angles
					// are negative, so we must negate the clockwise constraint angle before using it.
					constraintUV = Vec2f.rotateDegs( baseboneConstraintUV, -chain.getBone(0).getClockwiseConstraintDegs() );
					lineEndLocation = lineStartLocation.plus( constraintUV.times(lineLength) );
					FabrikLine2D.line.draw(lineStartLocation, lineEndLocation, CLOCKWISE_CONSTRAINT_COLOUR, lineWidth, mvpMatrix);
				}
			}
			else // Not working on the base bone? Then draw the constraints using the previous bone as a baseline.
			{
				// If we're not working on the base bone then we draw the constraints
				// with regard to the direction of the previous bone in the chain.
				// ...then get the base bone constraint unit vector
				Vec2f baselineUV = chain.getBone(loop - 1).getDirectionUV();
				
				// Draw the anti-clockwise constraint in red
				// Note: anti-clockwise rotation angles are positive
				Vec2f constraintUV = Vec2f.rotateDegs(baselineUV, chain.getBone(loop).getAnticlockwiseConstraintDegs() );
				Vec2f lineStartLocation = chain.getBone(loop).getStartLocation();
				Vec2f lineEndLocation = lineStartLocation.plus( constraintUV.times(lineLength) );
				FabrikLine2D.line.draw(lineStartLocation, lineEndLocation, ANTICLOCKWISE_CONSTRAINT_COLOUR, lineWidth, mvpMatrix);
				
				// Draw the clockwise constraint in blue
				// Note: Clockwise rotation angles are negative, so we must negate the clockwise constraint angle
				constraintUV = Vec2f.rotateDegs( baselineUV, -chain.getBone(loop).getClockwiseConstraintDegs() );
				lineEndLocation = lineStartLocation.plus( constraintUV.times(lineLength) );
				FabrikLine2D.line.draw(lineStartLocation, lineEndLocation, CLOCKWISE_CONSTRAINT_COLOUR, lineWidth, mvpMatrix);
			}
			
		} // End of loop over bones
		
	} // End of drawConstraintAngles method	

	// ---------- FabrikStructure2D Drawing Methods ----------
		
	/**
	 * Draw the provided FabrikStructure2D by drawing each chain in this structure.
	 * <p>
	 * All bones in all chains are drawn with line widths and colours as per their member properties.
	 * 
	 * @param	structure	The FabrikStructure2D to draw.
	 * @param	mvpMatrix	The ModelViewProjection matrix with which to draw this chain.
	 * @see		Mat4f#createOrthographicProjectionMatrix(float, float, float, float, float, float)
	 */
	public static void draw(FabrikStructure2D structure, Mat4f mvpMatrix)
	{
		int numChains = structure.getNumChains();
		for (int loop = 0; loop < numChains; ++loop)
		{
			draw(structure.getChain(loop), mvpMatrix);
		}
	}
	
	/**
	 * Draw the provided FabrikStructure2D by drawing each chain in this structure.
	 * <p>
	 * All bones in all chains are draw with colours as per their member properties, but with the provided line width.
	 * 
	 * @param	structure	The FabrikStructure2D to draw.
	 * @param	lineWidth	The width of the lines used to draw the structure.
	 * @param	mvpMatrix	The ModelViewProjection matrix with which to draw this structure.
	 * @see		Mat4f#createOrthographicProjectionMatrix(float, float, float, float, float, float)
	 */
	public static void draw(FabrikStructure2D structure, float lineWidth, Mat4f mvpMatrix)
	{
		int numChains = structure.getNumChains();
		for (int loop = 0; loop < numChains; ++loop)
		{
			draw(structure.getChain(loop), lineWidth, mvpMatrix);
		}
	}
	
	/**
	 * Draw the constraint lines for all bones in this structure.
	 *  
	 * @param	structure	The FabrikStructure2D to draw.
	 * @param	lineLength	The length of the constraint lines to draw in pixels.
	 * @param	lineWidth	The width of the constraint lines to draw in pixels.
	 * @param	mvpMatrix	The ModelViewProjection matrix with which to draw the constraint lines.
	 * @see		Mat4f#createOrthographicProjectionMatrix(float, float, float, float, float, float)
	 */
	public static void drawConstraints(FabrikStructure2D structure, float lineLength, float lineWidth, Mat4f mvpMatrix)
	{
		int numChains = structure.getNumChains();
		for (int loop = 0; loop < numChains; ++loop)
		{			
			drawChainConstraintAngles(structure.getChain(loop), lineLength, lineWidth, mvpMatrix);
		}
	}
	
} // End of FabrikLine2D class
