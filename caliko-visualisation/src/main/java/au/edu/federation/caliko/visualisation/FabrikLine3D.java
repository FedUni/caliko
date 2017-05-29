package au.edu.federation.caliko.visualisation;

import au.edu.federation.caliko.FabrikBone3D;
import au.edu.federation.caliko.FabrikChain3D;
import au.edu.federation.caliko.FabrikStructure3D;
import au.edu.federation.utils.Colour4f;
import au.edu.federation.utils.Mat4f;

/**
 * A static class used to draw a 3D lines to represent FabrikBone3D, FabrikChain3D or FabrikStructure3D objects.
 * <p>
 * These draw methods are only provided to assist with prototyping and debugging.
 * <p>
 * The GLSL shaders used to draw the lines require a minimum OpenGL version of 3.3 (i.e. GLSL #version 330).
 * 
 * @author  Al Lansley
 * @version 0.4.1 - 11/01/2016
 */
public class FabrikLine3D
{	
	// A static Line3D object used to draw lines.
	private static Line3D line = new Line3D();
	
	private FabrikLine3D() {}
	
	// ---------- Bone Drawing Methods ----------
	
	/**
	 * Draw this bone as a line.
	 * <p>
	 * The line will be drawn in the colour and line width as taken from the bone.
	 * 
	 * @param	bone		The bone to draw.
	 * @param	mvpMatrix	The ModelViewProjection matrix with which to draw the bone.
	 * @see		FabrikBone3D#setColour(Colour4f)
	 * @see		FabrikBone3D#setLineWidth(float)
	 * @see		Mat4f#createPerspectiveProjectionMatrix(float, float, float, float)
	 * @see		Mat4f#createPerspectiveProjectionMatrix(float, float, float, float, float, float)
	 */
	public static void draw(FabrikBone3D bone, Mat4f mvpMatrix)
	{
		line.draw(bone.getStartLocation(), bone.getEndLocation(), bone.getColour(), bone.getLineWidth(), mvpMatrix);
	}	
	
	/**
	 * Draw this bone as a line using a specific line width.
	 * <p>
	 * The line will be drawn in the colour as taken from the bone.
	 * 
	 * @param	bone		The bone to draw. 
	 * @param	lineWidth	The width of the line use to draw the bone.
	 * @param	mvpMatrix	The ModelViewProjection matrix with which to draw the bone.
	 */
	public static void draw(FabrikBone3D bone, float lineWidth, Mat4f mvpMatrix)
	{
		FabrikLine3D.line.draw(bone.getStartLocation(), bone.getEndLocation(), bone.getColour(), lineWidth, mvpMatrix);
	}

	/**
	 * Draw this bone as a line using a specific colour and line width.
	 * <p>
	 * The line will be drawn in the colour and line width as taken from the provided arguments.
	 * 
	 * @param	bone		The bone to draw.
	 * @param	colour		The colour to draw the bone.
	 * @param	lineWidth	The width of the line use to draw the bone.
	 * @param	mvpMatrix	The ModelViewProjection matrix with which to draw the bone.
	 * @see		Mat4f#createOrthographicProjectionMatrix(float, float, float, float, float, float)
	 */
	public static void draw(FabrikBone3D bone, Colour4f colour, float lineWidth, Mat4f mvpMatrix)
	{
		FabrikLine3D.line.draw(bone.getStartLocation(), bone.getEndLocation(), colour, lineWidth, mvpMatrix);
	}
	
	// ---------- Chain Drawing Methods ----------
	
	/**
	 * Draw the provided IK chain as a series of lines using the colour and line width properties of each bone.
	 * 
	 * @param	chain		The chain to draw.
	 * @param	mvpMatrix	The ModelViewProjection matrix with which to draw the chain.
	 * @see		Mat4f#createOrthographicProjectionMatrix(float, float, float, float, float, float)
	 */
	public static void draw(FabrikChain3D chain, Mat4f mvpMatrix)
	{
		int numBones = chain.getNumBones();
		for (int loop = 0; loop < numBones; ++loop)
		{
			draw(chain.getBone(loop), mvpMatrix);
		}
	}

	/**
	 * Draw a FabrikChain3D as a series of lines.
	 * <p>
	 * Each line is drawn using the specified line width, with the colour taken from the bone's
	 * {@link FabrikBone3D#mColour} property.
	 * 
	 * @param	chain		The chain to draw. 
	 * @param	lineWidth	The width of the line use to draw the bone..
	 * @param	mvpMatrix	The ModelViewProjection matrix with which to draw the chain.
	 */
	public static void draw(FabrikChain3D chain, float lineWidth, Mat4f mvpMatrix)
	{
		int numBones = chain.getNumBones();
		for (int loop = 0; loop < numBones; ++loop)
		{
			FabrikLine3D.draw(chain.getBone(loop), lineWidth, mvpMatrix);
		}
	}	

	// ---------- Structure Drawing Methods ----------
	
	/**
	 * Draw the provided FabrikStructure3D by drawing each chain in this structure.
	 * <p>
	 * All bones in all chains are drawn with line widths and colours as per their member properties.
	 * 
	 * @param	structure	The structure to draw.
	 * @param	mvpMatrix	The ModelViewProjection matrix with which to draw the structure.
	 * @see		Mat4f#createOrthographicProjectionMatrix(float, float, float, float, float, float)
	 */
	public static void draw(FabrikStructure3D structure, Mat4f mvpMatrix)
	{
		int numChains = structure.getNumChains();
		for (int loop = 0; loop < numChains; ++loop)
		{
			draw(structure.getChain(loop), mvpMatrix);
		}
	}
	
	/**
	 * Draw the provided FabrikStructure3D by drawing each chain in this structure.
	 * <p>
	 * All bones in all chains are draw with colours as per their member properties, but with the provided line width.
	 * 
	 * @param	structure	The structure to draw.
	 * @param	lineWidth	The width of the line use to draw the structure.
	 * @param	mvpMatrix	The ModelViewProjection matrix with which to draw the structure.
	 * @see		Mat4f#createOrthographicProjectionMatrix(float, float, float, float, float, float)
	 */
	public static void draw(FabrikStructure3D structure, float lineWidth, Mat4f mvpMatrix)
	{
		int numChains = structure.getNumChains();
		for (int loop = 0; loop < numChains; ++loop)
		{
			draw(structure.getChain(loop), lineWidth, mvpMatrix);
		}
	}
	
} // End of FabrikLine3D class