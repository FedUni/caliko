package au.edu.federation.caliko.visualisation;

import java.nio.FloatBuffer;

import au.edu.federation.caliko.FabrikBone3D;
import au.edu.federation.caliko.FabrikChain3D;
import au.edu.federation.caliko.FabrikStructure3D;
import au.edu.federation.utils.Mat3f;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Utils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * A class to represent a 3D axis which consists of three drawn lines, one each in the +x, +y and +z directions.
 * <p>
 * Because any Axis objects are be drawn using a provided ModelViewProjection matrix, they are a
 * convenient way to visualise a coordinate system.
 *  
 * @author  Al Lansley
 * @version  0.4.1 - 20/07/2016
 */
public class Axis
{
	// ----- Static Properties -----
	
	// We're drawing 3 lines and each line has 2 vertices, which brings our total to 6 vertices
	private static final int NUM_VERTICES = 6;

	// Each vertex has three positional components - the x, y and z values. 
	private static final int VERTEX_COMPONENTS = 3;
	
	// Each vertex has four colour components - the red, green, blue and alpha values
	private static final int COLOUR_COMPONENTS = 4;

	// In combination, this gives us 7 floats per vertex - three for the XYZ and four for the RGBA
	private static final int COMPONENT_COUNT = VERTEX_COMPONENTS + COLOUR_COMPONENTS;
	
	// A single static ShaderProgram is used to draw all axes
	private static ShaderProgram axisShaderProgram;

	// Vertex shader source
	private static final String VERTEX_SHADER_SOURCE =
			"#version 330"                                                                  + Utils.NEW_LINE +
			"in vec3 vertexLocation;   // Incoming vertex attribute"                        + Utils.NEW_LINE +
			"in vec4 vertexColour;     // Incoming colour value"                            + Utils.NEW_LINE +
			"flat out vec4 fragColour; // Outgoing colour value"                            + Utils.NEW_LINE +
			"uniform mat4 mvpMatrix;   // Combined Model/View/Projection matrix"            + Utils.NEW_LINE +
			"void main(void) {"                                                             + Utils.NEW_LINE +
			"   fragColour = vertexColour;                         // Pass through colour"  + Utils.NEW_LINE +
			"	gl_Position = mvpMatrix * vec4(vertexLocation, 1); // Project our geometry" + Utils.NEW_LINE +
			"}";

	// Fragment shader source
	private static final String FRAGMENT_SHADER_SOURCE =
			"#version 330"                                                   + Utils.NEW_LINE +
			"flat in vec4 fragColour; // Incoming colour from vertex shader" + Utils.NEW_LINE +
			"out vec4 vOutputColour;  // Outgoing colour value"              + Utils.NEW_LINE +
			"void main() {"                                                  + Utils.NEW_LINE +
			"	vOutputColour = fragColour;"                                 + Utils.NEW_LINE +
			"}";

    // Hold id values for the Vertex Array Object (VAO) and Vertex Buffer Object (VBO)
	private static int axisVaoId;
	private static int axisVboId;
	
	/** The FloatBuffer which contains the ModelViewProjection matrix. */
	private static FloatBuffer mvpMatrixFB;
	
	// We'll keep track of and restore the current OpenGL line width, which we'll store in this FloatBuffer.
	// Note: Although we only need a single float for this, LWJGL insists upon a minimum size of 16 floats.
	private static FloatBuffer currentLineWidthFB;
	
	// The FloatBuffer which will contain our vertex data
	private static FloatBuffer vertexFloatBuffer;
	
	// ----- Non-Static Properties -----
	
	// The float array storing the axis vertex (including colour) data
	private float[] axisData;	

	// The line width with which to draw the axis lines
	private float mAxisLineWidth = 1.0f;
	
	static {
		vertexFloatBuffer  = Utils.createFloatBuffer(NUM_VERTICES * COMPONENT_COUNT);
		mvpMatrixFB        = Utils.createFloatBuffer(16);
		currentLineWidthFB = Utils.createFloatBuffer(16);

		// ----- Grid shader program setup -----

		axisShaderProgram = new ShaderProgram();
		axisShaderProgram.initFromStrings(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);

		// ----- Grid shader attributes and uniforms -----

		// Add the shader attributes and uniforms
		axisShaderProgram.addAttribute("vertexLocation");
		axisShaderProgram.addAttribute("vertexColour");
		axisShaderProgram.addUniform("mvpMatrix");

		// ----- Set up our Vertex Array Object (VAO) to hold the shader attributes -----

		// Create a VAO and bind to it
		axisVaoId = glGenVertexArrays();
		glBindVertexArray(axisVaoId);

		// ----- Vertex Buffer Object (VBO) -----

		// Create a VBO and bind to it
		axisVboId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, axisVboId);

		// Note: We do NOT copy the data into the buffer at this time - we do that on draw!
		
		// Vertex attribute configuration
		glVertexAttribPointer(axisShaderProgram.attribute("vertexLocation"), // Vertex location attribute index
				                                          VERTEX_COMPONENTS, // Number of location components per vertex
				                                                   GL_FLOAT, // Data type
				                                                      false, // Normalised?
				                              COMPONENT_COUNT * Float.BYTES, // Stride
				                                                         0); // Offset

		glVertexAttribPointer(axisShaderProgram.attribute("vertexColour"),  // Vertex colour attribute index
				                                        COLOUR_COMPONENTS,  // Number of colour components per vertex
			                                                     GL_FLOAT,  // Data type
				                                                    true,   // Normalised?
				                            COMPONENT_COUNT * Float.BYTES,  // Stride
				                      (long)VERTEX_COMPONENTS * Float.BYTES);  // Offset
		
		// Unbind VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		// Enable the vertex attributes
		glEnableVertexAttribArray(axisShaderProgram.attribute("vertexLocation"));
		glEnableVertexAttribArray(axisShaderProgram.attribute("vertexColour"));

		// Unbind VAO - all the buffer and attribute settings above will now be associated with our VAO
		glBindVertexArray(0);
	}
	
	// ----- Methods -----
	
	/**
	 * Construct an axis which will be draw with a given line length and line width.
	 * <p>
	 * If the axis length is less than zero or the axis line width is less than 1.0f then an IllegalArgumentException is thrown.
	 * 
	 * @param	axisLength		The length of the line representing the X/Y/Z axes.
	 * @param	axisLineWidth	The width of the line representing the X/Y/Z axes.
	 */
	public Axis(float axisLength, float axisLineWidth)
	{
		// Set the axis length and size - these methods throw IllegalArgumentExceptions if required
		setAxisLength(axisLength);
		setLineWidth(axisLineWidth);

		// Note: We cannot just transfer the data into the vertex buffer here once instead of per frame
		// because we may have multiple Axis objects, each with their own axis size.
	}

	/**
	 * Set the line width with which to draw this axis.
	 * <p>
	 * While many OpenGL implementations allow for varying line widths, only a line width of 1.0f is guaranteed to work.
	 * The maximum line width you can use depends on the OpenGL implementation.
	 * <p>
	 * If an axis line width of less than 1.0f is specified then an IllegalArgumentException is thrown.
	 * 
	 * @param	axisLineWidth	The width to draw the lines of the axis in pixels. 
	 */
	public void setLineWidth(float axisLineWidth)
	{
		if (axisLineWidth <  1.0f)
		{
			throw new IllegalArgumentException("Axis line width must be greater than 1.0f.");
		}
		mAxisLineWidth = axisLineWidth;
	}
	
	/**
	 * Set the length of the lines used to draw this axis object.
	 * 
	 * @param	axisLength	The desired axis line length.
	 */
	public void setAxisLength(float axisLength)
	{
		// Specify the length of each line in the axis 
		//                                x,             y,          z,    r,    g,    b,    a
		axisData = new float[] {       0.0f,       	  0.0f,       0.0f, 1.0f, 0.0f, 0.0f, 1.0f,   // Origin vertex - red
			                     axisLength,       	  0.0f,       0.0f, 1.0f, 0.0f, 0.0f, 1.0f,   // +X vertex     - red
		                               0.0f,          0.0f,       0.0f, 0.0f, 1.0f, 0.0f, 1.0f,   // Origin vertex - green
		                               0.0f,    axisLength,       0.0f, 0.0f, 1.0f, 0.0f, 1.0f,   // +Y vertex     - green
						               0.0f,          0.0f,       0.0f, 0.0f, 0.0f, 1.0f, 1.0f,   // Origin vertex - blue
						               0.0f,          0.0f, axisLength, 0.0f, 0.0f, 1.0f, 1.0f }; // +Z vertex     - blue
	}
	
	
	
	/**
	 * Draw an axis given a ModelViewProjection matrix.
	 * 
	 * @param mvpMatrix	The ModelViewProjection matrix used to draw the axis
	 * @see Mat4f#createPerspectiveProjectionMatrix(float, float, float, float)
	 * @see Mat4f#createPerspectiveProjectionMatrix(float, float, float, float, float, float)
	 */
	public void draw(Mat4f mvpMatrix)
	{
		// Enable our shader program and bind to our VAO
		axisShaderProgram.use();
		glBindVertexArray(axisVaoId);

		// Bind to our VBO so we can update the axis data for this particular axis object
		glBindBuffer(GL_ARRAY_BUFFER, axisVboId);

		// Copy the data for this particular axis into the vertex float buffer
		vertexFloatBuffer.put(axisData);
		vertexFloatBuffer.flip();
		glBufferData(GL_ARRAY_BUFFER, vertexFloatBuffer, GL_DYNAMIC_DRAW);

		// Provide the projection matrix uniform
		mvpMatrixFB.put( mvpMatrix.toArray() );
		mvpMatrixFB.flip();
		glUniformMatrix4fv(axisShaderProgram.uniform("mvpMatrix"), false, mvpMatrixFB);

		// Store the current GL_LINE_WIDTH
		// IMPORTANT: We MUST allocate a minimum of 16 floats in our FloatBuffer in LWJGL, we CANNOT just get a FloatBuffer with 1 float!
		// ALSO: glPushAttrib(GL_LINE_BIT); /* do stuff */ glPopAttrib(); should work instead of this in theory - but LWJGL fails with 'function not supported'.
		glGetFloatv(GL_LINE_WIDTH, currentLineWidthFB);

		/// Set the GL_LINE_WIDTH to be the width requested, as passed to the constructor
		glLineWidth(mAxisLineWidth);

		// 	Draw the axis lines
		glDrawArrays(GL_LINES, 0, NUM_VERTICES);

		// Reset the line width to the previous value
		glLineWidth( currentLineWidthFB.get(0) );

		// Unbind from our VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		// Unbind from our VAO
		glBindVertexArray(0);

		// Disable our shader program
		axisShaderProgram.disable();
	}
	
	/**
	 * Method to draw an axis at each bone in a structure.
	 * <p>
	 * The X axis is red, the Y axis is green and the Z axis (along which the axis is aligned with the bone) is blue.
	 * 
	 * @param	structure			The FabrikStructure3D to draw.
	 * @param	viewMatrix			The view matrix, typically extracted from the camera.
	 * @param	projectionMatrix	The projection matrix, typically extracted from the OpenGLWindow.
	 */
	public void draw(FabrikStructure3D structure, Mat4f viewMatrix, Mat4f projectionMatrix)	
	{	
		int numChains = structure.getNumChains();
		for (int chainLoop = 0; chainLoop < numChains; ++chainLoop)
		{	
			FabrikChain3D chain = structure.getChain(chainLoop);
			
			int numBones = chain.getNumBones();
			for (int boneLoop = 0; boneLoop < numBones; ++boneLoop)
			{	
				FabrikBone3D bone = chain.getBone(boneLoop);				
				Mat4f modelMatrix = new Mat4f( Mat3f.createRotationMatrix( bone.getDirectionUV() ), bone.getStartLocation() );				
				Mat4f mvpMatrix   = projectionMatrix.times(viewMatrix).times(modelMatrix);				
				draw(mvpMatrix);
			}
		}	
	}
	
} // End of Axis class
