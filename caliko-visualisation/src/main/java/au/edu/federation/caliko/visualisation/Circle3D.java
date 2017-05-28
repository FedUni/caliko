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
 * Class to draw a circle in 3D space.
 * 
 * @author Al Lansley
 * @version 0.3.1 - 20/07/2016
 */
public class Circle3D
{
	// ----- Static Properties -----
	
	// We'll draw our circle with 40 vertices. Each vertex has 3 location components (x/y/z).
	private static final int NUM_VERTICES      = 40;
	private static final int VERTEX_COMPONENTS = 3;
	
	/** Shader program to draw all circles. **/
	private static ShaderProgram circleShaderProgram;
	
	/** Static FloatBuffer to hold the ModelViewProjection matrix - this gets updated in the draw() method. */
	private static FloatBuffer mvpMatrixFB;
	
	/** Static FloatBuffer to hold the colour to draw the circle. */
	private static FloatBuffer colourFB;
	
	/** Static FloatBuffer to store and restore the current line width used to draw the circle. */
	private static FloatBuffer currentLineWidthFB;
	
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
			"uniform vec4 fragColour;"                         + Utils.NEW_LINE +
			"void main() {"                                    + Utils.NEW_LINE +
			"	vOutputColour = fragColour;"                   + Utils.NEW_LINE +
			"}";

	/** Static Vertex Array Object )VAO) id. */
	private static int circleVaoId;
	
	/** Static Vertex Buffer Object (VBO) id. */
	private static int circleVboId;       
	
    /** Static array of floats used to draw the circle. */
	private static float[] circleData;
	
    /** Static vertex FloatBuffer to hold our vertex data. */
	private static FloatBuffer vertexFB;
	
	static {
		// Allocate memory for arrays and buffers
		circleData         = new float[NUM_VERTICES * VERTEX_COMPONENTS];
		vertexFB           = Utils.createFloatBuffer(NUM_VERTICES * VERTEX_COMPONENTS);
		mvpMatrixFB        = Utils.createFloatBuffer(16);
		colourFB           = Utils.createFloatBuffer(16); // Minimum size is 16, even though we only want to store 1 float
		currentLineWidthFB = Utils.createFloatBuffer(16); // Minimum size is 16, even though we only want to store 1 float

		// ----- Shader program setup -----

		circleShaderProgram = new ShaderProgram();
		circleShaderProgram.initFromStrings(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);

		// Add the shader attributes and uniforms
		circleShaderProgram.addAttribute("vertexLocation");
		circleShaderProgram.addUniform("mvpMatrix");
		circleShaderProgram.addUniform("fragColour");

		// Get an id for the Vertex Array Object (VAO) and bind to it
		circleVaoId = glGenVertexArrays();
		glBindVertexArray(circleVaoId);

		// Generate an id for our Vertex Buffer Object (VBO) and bind to it
		circleVboId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, circleVboId);

		// Place the location data into the VBO...
		glBufferData(GL_ARRAY_BUFFER, vertexFB, GL_DYNAMIC_DRAW);

		// ...and specify the data format.
		glVertexAttribPointer(circleShaderProgram.attribute("vertexLocation"),  // Vertex location attribute index
				                                            VERTEX_COMPONENTS,  // Number of normal components per vertex
				                                                     GL_FLOAT,  // Data type
				                                                        false,  // Normalised?
				                                                            0,  // Stride
				                                                            0); // Offset
		
		// Unbind VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		// Enable the vertex attributes
		glEnableVertexAttribArray(circleShaderProgram.attribute("vertexLocation"));

		// Unbind our from our VAO, saving all settings
		glBindVertexArray(0);
	}

	/** Constructor */
	public Circle3D()
	{
		//
	}
	
	/**
	 * Draw a circle in 3D space.
	 * <p>
	 * @param	location	The location to draw the circle.
	 * @param	axis		The axis the circle will be perpendicular to.
	 * @param 	radius		The radius of the circle in pixels.
	 * @param	colour		The colour with which to draw the circle.
	 * @param	lineWidth	The width of the lines comprising the circle in pixels.
	 * @param	mvpMatrix	The ModelViewProjection matrix with which to draw the circle.
	 */
	public void draw(Vec3f location, Vec3f axis, float radius, Colour4f colour, float lineWidth, Mat4f mvpMatrix)
	{
		// Generate the circle data and put it into the FloatBuffer
		for (int vertexNumLoop = 0; vertexNumLoop < NUM_VERTICES; vertexNumLoop++)
		{
			// Create our circle in the plane perpendicular to the axis provided
			float angleDegs = vertexNumLoop * (360.0f / (float)NUM_VERTICES);
			Vec3f perpAxis = Vec3f.genPerpendicularVectorQuick(axis);
			Vec3f point = new Vec3f(radius * perpAxis.x, radius * perpAxis.y, radius * perpAxis.z);
			
			// Rotate each point about the axis given
			point = Vec3f.rotateAboutAxisDegs(point, angleDegs, axis);
			
			// Translate to the given location
			point = point.plus(location);
			
			setCircleData(vertexNumLoop, point);
		}
		
		// Transfer the data into the vertex float buffer
		vertexFB.put(circleData);
		vertexFB.flip();

		// Transfer the MVP matrix uniform
		mvpMatrixFB.put( mvpMatrix.toArray() );
		mvpMatrixFB.flip();

		// Transfer the colour uniform
		colourFB.put( colour.toArray() );
		colourFB.flip();
		
		// Enable our shader program and bind to our VAO
		circleShaderProgram.use();
		glBindVertexArray(circleVaoId);

		// Bind to the vertex buffer object (VBO) and place the new data into it
		glBindBuffer(GL_ARRAY_BUFFER, circleVboId);
		glBufferData(GL_ARRAY_BUFFER, vertexFB, GL_DYNAMIC_DRAW);

		// Provide the projection matrix and colour uniforms to our shader
		glUniformMatrix4fv(circleShaderProgram.uniform("mvpMatrix"), false, mvpMatrixFB);
		glUniform4fv(circleShaderProgram.uniform("fragColour"), colourFB);

		// Store the current GL_LINE_WIDTH
		// IMPORTANT: We MUST allocate a minimum of 16 floats in our FloatBuffer in LWJGL, we CANNOT just get a FloatBuffer with 1 float!
		// ALSO: glPushAttrib(GL_LINE_BIT); /* do stuff */ glPopAttrib(); should work instead of this in theory - but LWJGL fails with 'function not supported'.
		glGetFloatv(GL_LINE_WIDTH, currentLineWidthFB);

		/// Set the GL_LINE_WIDTH to be the width requested, as passed to the constructor
		glLineWidth(lineWidth);

		// 	Draw the circle as a line loop
		glDrawArrays(GL_LINE_LOOP, 0, NUM_VERTICES);

		// Restore the previous GL_LINE_WIDTH
		glLineWidth( currentLineWidthFB.get(0) );

		// Unbind from VBO & VAO, then disable our shader
		glBindBuffer(GL_ARRAY_BUFFER, 0);
			
		glBindVertexArray(0);
		circleShaderProgram.disable();
	
	} // End of draw method
	
	private static void setCircleData(int vertexNumLoop, Vec3f point) {
		// Point x/y/z
		circleData[(vertexNumLoop * VERTEX_COMPONENTS)    ] = point.x;
		circleData[(vertexNumLoop * VERTEX_COMPONENTS) + 1] = point.y;
		circleData[(vertexNumLoop * VERTEX_COMPONENTS) + 2] = point.z;		
	}
	
	
} // End of Circle3D class
