package au.edu.federation.caliko.visualisation;

import java.nio.FloatBuffer;

import au.edu.federation.utils.Colour4f;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Utils;
import au.edu.federation.utils.Vec2f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Class to draw a line in 2D space.
 * 
 * @author Al Lansley
 * @version 0.6.1 - 20/07/2016
 */
public class Line2D
{
	// ----- Static Properties -----

	// A line has 2 vertices. Each vertex has 2 location components (x/y) and 4 colour components (r/g/b/a)
	private static final int NUM_VERTICES      = 2;
	private static final int VERTEX_COMPONENTS = 2;
	private static final int COLOUR_COMPONENTS = 4;

	// Total number of float components per vertex (2 + 4 = 6)
	private static final int COMPONENT_COUNT = VERTEX_COMPONENTS + COLOUR_COMPONENTS;

	/** Static ShaderProgram shared across all Line2D instances. */
	private static ShaderProgram lineShaderProgram;

	/** Static FloatBuffer to hold the ModelViewProjection matrix - shared across all line 2D instances and updated for each line drawn. */
	private static FloatBuffer mvpMatrixFloatBuffer;

	/** GLSL version 330 fragment shader code. */	
	private static final String VERTEX_SHADER_SOURCE =
			"#version 330"                                                                      + Utils.NEW_LINE +
			"in vec2 vertexLocation; // Incoming vertex attribute"                              + Utils.NEW_LINE +
			"in vec4 vertexColour;   // Incoming colour value"                                  + Utils.NEW_LINE +
			"out vec4 fragColour;    // Outgoing colour value"                                  + Utils.NEW_LINE +
			"uniform mat4 mvpMatrix; // Combined Model/View/Projection matrix"                  + Utils.NEW_LINE +
			"void main(void) {"                                                                 + Utils.NEW_LINE +
			"   fragColour = vertexColour;                             // Pass through colour"  + Utils.NEW_LINE +
			"	gl_Position = mvpMatrix * vec4(vertexLocation, 0, 1) ; // Project our geometry" + Utils.NEW_LINE +
			"}";

	/** GLSL version 330 fragment shader code. */
	private static final String FRAGMENT_SHADER_SOURCE =
			"#version 330"                                                  + Utils.NEW_LINE +
			"in vec4 fragColour;     // Incoming colour from vertex shader" + Utils.NEW_LINE +
			"out vec4 vOutputColour; // Outgoing colour value"              + Utils.NEW_LINE +
			"void main() {"                                                 + Utils.NEW_LINE +
			"	vOutputColour = fragColour;"                                + Utils.NEW_LINE +
			"}";

	private static int         lineVaoId;             // The Vertex Array Object ID which holds our shader attributes
	private static int         lineVertexBufferId;    // The id of the vertex buffer containing the grid vertex data
	private static float[]     lineData;              // Array of floats used to draw the grid
	private static FloatBuffer vertexFloatBuffer;     // Vertex buffer to hold the gridArray vertex data
	private static FloatBuffer lineWidthFloatBuffer;  // Vertex buffer to hold the gridArray vertex data
	
	static {
		lineData = new float[NUM_VERTICES * COMPONENT_COUNT];
		vertexFloatBuffer = Utils.createFloatBuffer(NUM_VERTICES * COMPONENT_COUNT);
		mvpMatrixFloatBuffer = Utils.createFloatBuffer(16);
		lineWidthFloatBuffer = Utils.createFloatBuffer(16); // Minimum size is 16, even though we only want to store 1 float			

		// ----- Grid shader program setup -----

		lineShaderProgram = new ShaderProgram();

		lineShaderProgram.initFromStrings(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);

		// ----- Grid shader attributes and uniforms -----

		// Add the shader attributes
		lineShaderProgram.addAttribute("vertexLocation");
		lineShaderProgram.addAttribute("vertexColour");

		// Add the shader uniforms
		lineShaderProgram.addUniform("mvpMatrix");


		// ----- Set up our Vertex Array Object (VAO) to hold the shader attributes -----

		// Get an Id for the Vertex Array Object (VAO) and bind to it
		lineVaoId = glGenVertexArrays();
		glBindVertexArray(lineVaoId);

		// ----- Location Vertex Buffer Object (VBO) -----

		// Generate an id for the locationBuffer and bind to it
		lineVertexBufferId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, lineVertexBufferId);

		// Place the location data into the VBO...
		glBufferData(GL_ARRAY_BUFFER, vertexFloatBuffer, GL_DYNAMIC_DRAW);

		// ...and specify the data format.
		glVertexAttribPointer(lineShaderProgram.attribute("vertexLocation"), // Vertex location attribute index
                                                            VERTEX_COMPONENTS, // Number of normal components per vertex
                                                                     GL_FLOAT, // Data type
                                                                        false, // Normalised?
                                                COMPONENT_COUNT * Float.BYTES, // Stride
                                                                           0); // Offset

		// ...and specify the data format.
		glVertexAttribPointer(lineShaderProgram.attribute("vertexColour"),  // Vertex colour attribute index
                                                          COLOUR_COMPONENTS,  // Number of colour components per vertex
                                                                   GL_FLOAT,  // Data type
                                                                       true,  // Normalised? Colour values are between 0.0 and 1.0, so yes!
                                              COMPONENT_COUNT * Float.BYTES,  // Stride
                                      (long)VERTEX_COMPONENTS * Float.BYTES);  // Offset
		
		// Unbind VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		// Enable the vertex attributes
		glEnableVertexAttribArray(lineShaderProgram.attribute("vertexLocation"));
		glEnableVertexAttribArray(lineShaderProgram.attribute("vertexColour"));

		// Unbind our Vertex Array object - all the buffer and attribute settings above will be associated with our VAO!
		glBindVertexArray(0);
	}

	/** Constructor */
	public Line2D()
	{
		//
	}

	/**
	 * Draw a line with interpolated colours from the first to second vertices.
	 * <p>
	 * @param	p1			The first point.
	 * @param	p2			The second point.
	 * @param 	c1			The colour at the first point.
	 * @param	c2			The colour at the second point.
	 * @param	lineWidth	The width of the line in pixels.
	 * @param	mvpMatrix	The ModelViewProjection matrix with which to draw the line.
	 */
	public void draw(Vec2f p1, Vec2f p2, Colour4f c1, Colour4f c2, float lineWidth, Mat4f mvpMatrix)
	{
		setLineData(p1, p2, c1, c2);

		// Transfer the data into the vertex float buffer
		vertexFloatBuffer.put(lineData);
		vertexFloatBuffer.flip();

		// Convert projection matrix to float buffer
		mvpMatrixFloatBuffer.put( mvpMatrix.toArray() );
		mvpMatrixFloatBuffer.flip();

		// Specify we're using our shader program
		lineShaderProgram.use();

		// Bind to our vertex buffer object
		glBindVertexArray(lineVaoId);

		// Bind to the vertex buffer object (VBO) and place the new data into it
		glBindBuffer(GL_ARRAY_BUFFER, lineVertexBufferId);
		glBufferData(GL_ARRAY_BUFFER, vertexFloatBuffer, GL_DYNAMIC_DRAW);

		// Provide the projection matrix uniform
		glUniformMatrix4fv(lineShaderProgram.uniform("mvpMatrix"), false, mvpMatrixFloatBuffer);

		// Store the current GL_LINE_WIDTH
		// IMPORTANT: We MUST allocate a minimum of 16 floats in our FloatBuffer in LWJGL, we CANNOT just get a FloatBuffer with 1 float!
		// ALSO: glPushAttrib(GL_LINE_BIT); /* do stuff */ glPopAttrib(); should work instead of this in theory - but LWJGL fails with 'function not supported'.
		glGetFloatv(GL_LINE_WIDTH, lineWidthFloatBuffer);

		/// Set the GL_LINE_WIDTH to be the width requested, as passed to the constructor
		glLineWidth(lineWidth);

		// 	Draw the axis lines
		glDrawArrays(GL_LINES, 0, NUM_VERTICES);

		// Reset the line width to the previous value
		glLineWidth( lineWidthFloatBuffer.get(0) );

		// Unbind from VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		// Unbind from our vertex array object and disable our shader program
		glBindVertexArray(0);

		// Disable our shader program
		lineShaderProgram.disable();
		
	} // End of draw method
	
	/**
	 * Draw a line in a single colour.
	 * <p>
	 * @param	p1			The first point.
	 * @param	p2			The second point.
	 * @param 	colour		The colour to draw the line.
	 * @param	lineWidth	The width of the line in pixels.
	 * @param	mvpMatrix	The ModelViewProjection matrix with which to draw the line.
	 */
	public void draw(Vec2f p1, Vec2f p2, Colour4f colour, float lineWidth, Mat4f mvpMatrix)
	{
		this.draw(p1, p2, colour, colour,  lineWidth, mvpMatrix);
	}
	
	private static void setLineData(Vec2f p1, Vec2f p2, Colour4f c1, Colour4f c2) {
		// First vertex x/y location
		lineData[0] = p1.x;
		lineData[1] = p1.y;

		// First vertex colour
		lineData[2]  = c1.r;
		lineData[3]  = c1.g;
		lineData[4]  = c1.b;
		lineData[5]  = c1.a;

		// Second vertex x/y location
		lineData[6]  = p2.x;
		lineData[7]  = p2.y;

		// Second vertex colour
		lineData[8]  = c2.r;
		lineData[9]  = c2.g;
		lineData[10] = c2.b;
		lineData[11] = c2.a;		
	}
	
} // End of Line2D class
