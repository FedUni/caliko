package au.edu.federation.caliko.visualisation;

import java.nio.FloatBuffer;

import au.edu.federation.utils.Colour4f;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Utils;
import au.edu.federation.utils.Vec3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Class to draw a lines as well as circles and cones made from lines in 3D space.
 * 
 * @author Al Lansley
 * @version 0.7.1 - 20/07/2016
 */
public class Line3D
{
	// ----- Static Properties -----

	private static Colour4f white = new Colour4f(1.0f, 1.0f, 1.0f, 0.3f);
	
	// A line has 2 vertices. Each vertex has 3 location components (x/y/z) and 4 colour components (r/g/b/a)
	private static final int NUM_VERTICES      = 2;
	private static final int VERTEX_COMPONENTS = 3;

	// Declare our shader program and the float buffer for our MVP matrix
	private static ShaderProgram shaderProgram;
	private static FloatBuffer mvpMatrixFloatBuffer;
	private static FloatBuffer colourFloatBuffer;

	//Define our vertex and fragement shader GLSL source code
	private static final String VERTEX_SHADER_SOURCE =
			"#version 330"                                                         + Utils.NEW_LINE +
			"in vec4 vertexLocation; // Incoming vertex attribute"                 + Utils.NEW_LINE +
			"out vec4 fragColour;    // Outgoing colour value"                     + Utils.NEW_LINE +
			"uniform mat4 mvpMatrix; // Combined Model/View/Projection matrix"     + Utils.NEW_LINE +
			"void main(void) {"                                                    + Utils.NEW_LINE +
			"	gl_Position = mvpMatrix * vertexLocation; // Project our geometry" + Utils.NEW_LINE +
			"}";

	private static final String FRAGMENT_SHADER_SOURCE =
			"#version 330"                                     + Utils.NEW_LINE +
			"out vec4 vOutputColour; // Outgoing colour value" + Utils.NEW_LINE +
			"uniform vec4 colour;"                             + Utils.NEW_LINE +
			"void main() {"                                    + Utils.NEW_LINE +
			"	vOutputColour = colour;"                       + Utils.NEW_LINE +
			"}";

	private static int         vaoId;                // The Vertex Array Object ID which holds our shader attributes
	private static int         vboId;                // The id of the vertex buffer containing the grid vertex data
	private static float[]     lineData;             // Array of floats used to draw the line
	private static FloatBuffer vertexFloatBuffer;    // Vertex buffer to hold the gridArray vertex data
	private static FloatBuffer lineWidthFloatBuffer; // Vertex buffer to hold the gridArray vertex data
	
	static {
		// Allocate memory for arrays and buffers
		lineData = new float[NUM_VERTICES * VERTEX_COMPONENTS];
		vertexFloatBuffer    = Utils.createFloatBuffer(NUM_VERTICES * VERTEX_COMPONENTS);
		mvpMatrixFloatBuffer = Utils.createFloatBuffer(16);
		lineWidthFloatBuffer = Utils.createFloatBuffer(16); // Minimum size is 16, even though we only want to store 1 float
		colourFloatBuffer    = Utils.createFloatBuffer(16); // Minimum size is 16, even though we only want to store 4 floats

		// ----- Shader program setup -----

		shaderProgram = new ShaderProgram();
		shaderProgram.initFromStrings(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);

		// Add the shader attributes and uniforms
		shaderProgram.addAttribute("vertexLocation");
		shaderProgram.addUniform("mvpMatrix");
		shaderProgram.addUniform("colour");

		// Get an id for the Vertex Array Object (VAO) and bind to it
		vaoId = glGenVertexArrays();
		glBindVertexArray(vaoId);

		// Generate an id for our Vertex Buffer Object (VBO) and bind to it
		vboId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboId);

		// Place the location data into the VBO...
		glBufferData(GL_ARRAY_BUFFER, vertexFloatBuffer, GL_DYNAMIC_DRAW);

		// ...and specify the data format.
		glVertexAttribPointer(shaderProgram.attribute("vertexLocation"), // Vertex location attribute index
				                                      VERTEX_COMPONENTS, // Number of normal components per vertex
				                                               GL_FLOAT, // Data type
				                                                  false, // Normalised?
				                        VERTEX_COMPONENTS * Float.BYTES, // Stride
				                                                     0); // Offset
			
	
		// Unbind VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		// Enable the vertex attributes
		glEnableVertexAttribArray(shaderProgram.attribute("vertexLocation"));

		// Unbind our from our VAO, saving all settings
		glBindVertexArray(0);
		
	}

	/** Constructor */
	public Line3D()
	{
		//
	}

	/**
	 * Draw a line with interpolated colours from the first to second vertices.
	 * <p>
	 * @param	p1			The first point.
	 * @param	p2			The second point.
	 * @param 	colour		The colour to draw the line.
	 * @param	lineWidth	The width of the line in pixels.
	 * @param	mvpMatrix	The ModelViewProjection matrix with which to draw the line.
	 */
	public void draw(Vec3f p1, Vec3f p2, Colour4f colour, float lineWidth, Mat4f mvpMatrix)
	{
		setLineData(p1, p2);
		
		// Transfer the line, matrix and colour data into the float buffers
		vertexFloatBuffer.put( lineData );
		vertexFloatBuffer.flip();
		mvpMatrixFloatBuffer.put( mvpMatrix.toArray() );
		mvpMatrixFloatBuffer.flip();
		colourFloatBuffer.put( colour.toArray() );
		colourFloatBuffer.flip();

		// Enable our shader program and bind to our VAO
		shaderProgram.use();
		glBindVertexArray(vaoId);

		// Bind to the vertex buffer object (VBO) and place the new data into it
		glBindBuffer(GL_ARRAY_BUFFER, vboId);
		glBufferData(GL_ARRAY_BUFFER, vertexFloatBuffer, GL_DYNAMIC_DRAW);

		// Provide the projection matrix uniform to our shader
		glUniformMatrix4fv(shaderProgram.uniform("mvpMatrix"), false, mvpMatrixFloatBuffer);
		glUniform4fv(shaderProgram.uniform("colour"), colourFloatBuffer);

		// Store the current GL_LINE_WIDTH
		// IMPORTANT: We MUST allocate a minimum of 16 floats in our FloatBuffer in LWJGL, we CANNOT just get a FloatBuffer with 1 float!
		// ALSO: glPushAttrib(GL_LINE_BIT); /* do stuff */ glPopAttrib(); should work instead of this in theory - but LWJGL fails with 'function not supported'.
		glGetFloatv(GL_LINE_WIDTH, lineWidthFloatBuffer);

		/// Set the line width to be the width requested
		glLineWidth(lineWidth);
		
		// 	Draw the line
		glDrawArrays(GL_LINES, 0, NUM_VERTICES);

		// Restore the previous GL_LINE_WIDTH
		glLineWidth( lineWidthFloatBuffer.get(0) );

		// Unbind from VBO & VAO, then disable our shader
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
		shaderProgram.disable();
	
	} // End of draw method
	
	/**
	 * Draw a line in a single colour.
	 * <p>
	 * @param	p1			The first point.
	 * @param	p2			The second point.
	 * @param	lineWidth	The width of the line in pixels.
	 * @param	mvpMatrix	The ModelViewProjection matrix with which to draw the line.
	 */
	public void draw(Vec3f p1, Vec3f p2, float lineWidth, Mat4f mvpMatrix)
	{
		draw(p1, p2, Line3D.white, lineWidth, mvpMatrix);
	}
	
	private static void setLineData(Vec3f p1, Vec3f p2) {
		// Point 1 x/y/z
		lineData[0] = p1.x;
		lineData[1] = p1.y;
		lineData[2] = p1.z;
		
		// Point 2 x/y/z
		lineData[3] = p2.x;
		lineData[4] = p2.y;
		lineData[5] = p2.z;
	}
	
} // End of Line3D class
