package au.edu.federation.caliko.visualisation;

import java.nio.FloatBuffer;

import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Utils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Grid
{
	// ----- Static Properties -----

    /** Each vertex has an X, Y and Z component. */
	private static final int VERTEX_COMPONENTS = 3;
	
	/** The shader program used to draw all grids. */
	private static ShaderProgram gridShaderProgram;

	/** The ModelViewProjection matrix float buffer used to draw any given grid which is updated via the draw() method. */
	private static FloatBuffer mvpMatrixFB;
	
	/** The FloatBuffer used to get and restore the current line width. */
	private static FloatBuffer currentLineWidthFB = Utils.createFloatBuffer(16);

	//Define our vertex shader source code
	//Note: The R" notation is for raw strings and preserves all spaces, indentation,
	//newlines etc. in utf-8|16|32 wchar_t format, but requires C++0x or C++11.
	private static final String VERTEX_SHADER_SOURCE =
			"#version 330"                                                                    + Utils.NEW_LINE +
			"in vec3 vertexLocation; // Incoming vertex attribute"                            + Utils.NEW_LINE +
			"uniform mat4 mvpMatrix; // Combined Model/View/Projection matrix"                + Utils.NEW_LINE +
			"void main(void) {"                                                               + Utils.NEW_LINE +
			"	gl_Position = mvpMatrix * vec4(vertexLocation, 1.0); // Project our geometry" + Utils.NEW_LINE +
			"}";

	//Define our fragment shader source code
	private static final String FRAGMENT_SHADER_SOURCE =
			"#version 330"                                                               + Utils.NEW_LINE +
			"out vec4 vOutputColour; // Outgoing colour value"                           + Utils.NEW_LINE +
			"void main() {"                                                              + Utils.NEW_LINE +
			"	vOutputColour = vec4(1.0); // Draw our pixel in white with full opacity" + Utils.NEW_LINE +
			"}";

	// ----- Non-Static Properties -----

	private int         gridVaoId;          // The Vertex Array Object ID which holds our shader attributes
	private int         gridVertexBufferId; // The id of the vertex buffer containing the grid vertex data

	private int         numVerts;           // How many vertices in this grid?
	private float[]     gridArray;          // Array of floats used to draw the grid
	private FloatBuffer vertexFloatBuffer;  // Vertex buffer to hold the gridArray vertex data
	
	static {
		// Create our MVP matrix float buffer
		mvpMatrixFB = Utils.createFloatBuffer(16);

		// Set up out shader
		gridShaderProgram = new ShaderProgram();
		gridShaderProgram.initFromStrings(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
		gridShaderProgram.addAttribute("vertexLocation");
		gridShaderProgram.addUniform("mvpMatrix");
	}

	/**
	 * Constructor.
	 * <p>
	 * Width is along +/- X-axis, depth is along +/- Z-axis, height is the location of
	 * the grid on the Y-axis, numDivisions is how many lines to draw across each axis.
	 * 
	 * @param	width			The width of the grid in world-space units.
	 * @param	depth			The depth of the grid in world-space units.
	 * @param	height			The location of the grid on the Y-axis.
	 * @param	numDivisions	The number of divisions in the grid.
	 */
	public Grid( float width,  float depth,  float height,  int numDivisions)
	{
		// Calculate how many vertices our grid will consist of.
		// Multiplied by 2 because 2 verts per line, and times 2 again because our
		// grid is composed of -z to +z lines, as well as -x to +x lines. Add +4 to
		// the total for the final two lines to 'close off' the grid.
		numVerts = (numDivisions * 2 * 2) + 4;

		// So for this many vertices, we're going to be using this many floats...
		int gridFloatCount = numVerts * VERTEX_COMPONENTS;

		// ...which we'll store in this float array!
		gridArray = new float[gridFloatCount];

		// For a grid of width and depth, the extent goes from -halfWidth to +halfWidth,
		// and -halfDepth to +halfDepth.
		float halfWidth = width / 2.0f;
		float halfDepth = depth / 2.0f;

		// How far we move our vertex locations each time through the loop
		float xStep = width / numDivisions;
		float zStep = depth / numDivisions;

		// Starting locations
		float xLoc = -halfWidth;
		float zLoc = -halfDepth;

		// Split the vertices into half for -z to +z lines, and half for -x to +x
		int halfNumVerts = numVerts / 2;

		// Our counter will keep track of the index of the float value we're working on
		int counter = 0;

		// Near to far lines
		// Note: Step by 2 because we're setting two vertices each time through the loop
		for (int loop = 0; loop < halfNumVerts; loop += 2)
		{
			// Far vertex
			gridArray[counter++] = xLoc;       // x
			gridArray[counter++] = height;     // y
			gridArray[counter++] = -halfDepth; // z

			gridArray[counter++]  = xLoc;      // x
			gridArray[counter++]  = height;    // y
			gridArray[counter++]  = halfDepth; // z

			// Move across on the x-axis
			xLoc += xStep;
		}

		// Left to right lines
		// Note: Step by 2 because we're setting two vertices each time through the loop
		for (int loop = halfNumVerts; loop < numVerts; loop += 2)
		{
			// Left vertex
			gridArray[counter++] = -halfWidth; // x
			gridArray[counter++] = height;     // y
			gridArray[counter++] = zLoc;       // z

			// Right vertex
			gridArray[counter++] = halfWidth; // x
			gridArray[counter++] = height;    // y
			gridArray[counter++] = zLoc;      // z

			// Move across on the z-axis
			zLoc += zStep;
		}

		// Transfer the data into the vertex float buffer
		vertexFloatBuffer = Utils.createFloatBuffer(counter);
		vertexFloatBuffer.put( gridArray );
		vertexFloatBuffer.flip();

		// Mark our grid array as null so the GC can free the memory
		gridArray = null;

		// ----- Set up our Vertex Array Object (VAO) to hold the shader attributes -----
		// Note: The grid VAO cannot be static because we may have multiple grids of various sizes.


		// Get an Id for the Vertex Array Object (VAO) and bind to it
		gridVaoId = glGenVertexArrays();
		glBindVertexArray(gridVaoId);

		// ----- Location Vertex Buffer Object (VBO) -----

		// Generate an id for the locationBuffer and bind to it
		gridVertexBufferId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, gridVertexBufferId);

		// Place the location data into the VBO...
		glBufferData(GL_ARRAY_BUFFER, vertexFloatBuffer, GL_STATIC_DRAW);

		// ...and specify the data format.
		glVertexAttribPointer(gridShaderProgram.attribute("vertexLocation"),  // 0, Vertex attribute index
				3,  // Number of normal components per vertex
				GL_FLOAT,  // Data type
				false,  // Normalised?
				0,  // Stride
				0); // Offset
		// Unbind VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		// Enable the vertex attribute at this location
		glEnableVertexAttribArray(gridShaderProgram.attribute("vertexLocation"));

		// Unbind our Vertex Array object - all the buffer and attribute settings above will be associated with our VAO!
		glBindVertexArray(0);
	}

	/**
	 * Draw the grid.
	 * 
	 * @param	mvpMatrix	The ModelViewProjection matrix used to draw the grid.
	 */
	public void draw(Mat4f mvpMatrix)
	{
		// Copy the ModelViewProjection matrix into the floatbuffer
		mvpMatrixFB.put( mvpMatrix.toArray() );
		mvpMatrixFB.flip();

		// Specify we're using our shader program
		gridShaderProgram.use();

		// Bind to our vertex buffer object
		glBindVertexArray(gridVaoId);

			// Provide the projection matrix uniform
			// Params: uniform name, normalised?, data source
			glUniformMatrix4fv(gridShaderProgram.uniform("mvpMatrix"), false, mvpMatrixFB);
	
			// Store the current GL_LINE_WIDTH
			// IMPORTANT: We MUST allocate a minimum of 16 floats in our FloatBuffer in LWJGL, we CANNOT just get a FloatBuffer with 1 float!
			// ALSO: glPushAttrib(GL_LINE_BIT); /* do stuff */ glPopAttrib(); should work instead of this in theory - but LWJGL fails with 'function not supported'.			
			glGetFloatv(GL_LINE_WIDTH, currentLineWidthFB);
	
			/// Set the GL_LINE_WIDTH to be the width requested, as passed to the constructor
			glLineWidth(1.0f);
	
			// Draw the grid as lines
			glDrawArrays(GL_LINES, 0, numVerts);
	
			// Reset the line width to the previous value
			glLineWidth( currentLineWidthFB.get(0) );

		// Unbind from our vertex array objext and disable our shader program
		glBindVertexArray(0);

		// Disable our shader program
		gridShaderProgram.disable();
	}
}
