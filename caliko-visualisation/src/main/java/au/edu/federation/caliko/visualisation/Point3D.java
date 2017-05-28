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
 * Class to draw a point in 3D space.
 * 
 * @author Al Lansley
 * @version 0.6.1 - 20/07/2016
 */
public class Point3D
{
	// ----- Static Properties -----
	
	// Get the newline character for this system
	private static final String newLine = System.lineSeparator();

	// We're drawing 1 point, which has a single vertex
	private static final int NUM_VERTICES = 1;

	private static final int VERTEX_COMPONENTS = 3; // Each vertex has an x, y and z component...
	private static final int COLOUR_COMPONENTS = 4; // ...as well as red, green, blue and alpha colour components.

	// Total number of float components per vertex (3 + 4 = 7)
	private static final int COMPONENT_COUNT = VERTEX_COMPONENTS + COLOUR_COMPONENTS;	

	// Provide a definition of our static pointer to a ShaderProgram
	private static ShaderProgram pointShaderProgram;

	// Keep a FloatBuffer around to hold the ModelViewProjection matrix
	private static FloatBuffer mvpMatrixFloatBuffer;

	// We get the current OpenGL GL_POINT_SIZE before we use our own and then restore it drawing
	private static FloatBuffer pointSizeFloatBuffer;

	//Define our vertex shader source code
	private static final String VERTEX_SHADER_SOURCE =
			"#version 330"                                                                                     + newLine +
			"in vec3 vertexLocation;                                 // Incoming vertex attribute"             + newLine +
			"in vec4 vertexColour;                                   // Incoming colour value"                 + newLine +
			"flat out vec4 fragColour;                               // Outgoing non-interpolated colour"      + newLine +
			"uniform mat4 mvpMatrix;                                 // Combined Model/View/Projection matrix" + newLine +
			"void main(void) {"                                                                                + newLine +
			"   fragColour = vertexColour;                           // Pass through colour"                   + newLine +
			"	gl_Position = mvpMatrix * vec4(vertexLocation, 1.0); // Project our geometry"                  + newLine +
			"}";

	//Define our fragment shader source code
	private static final String FRAGMENT_SHADER_SOURCE =
			"#version 330"                                                   + newLine +
			"flat in vec4 fragColour; // Incoming colour from vertex shader" + newLine +
			"out vec4 vOutputColour;  // Outgoing colour value"              + newLine +
			"void main() {"                                                  + newLine +
			"	vOutputColour = fragColour;"                                 + newLine +
			"}";

	private static int         pointVaoId;          // The Vertex Array Object ID which holds our shader attributes
	private static int         pointVertexBufferId; // The id of the vertex buffer containing the grid vertex data
	private static float[]     pointData;          // Array of floats used to draw the grid
	private static FloatBuffer vertexFloatBuffer;  // Vertex buffer to hold the gridArray vertex data
	
	static {
		// Instantiate our float data array which we'll transfer data from into the float buffer
		pointData = new float[NUM_VERTICES * COMPONENT_COUNT];

		// Create the float buffer which will hold the geometry data to draw
		vertexFloatBuffer = Utils.createFloatBuffer(NUM_VERTICES * COMPONENT_COUNT);

		// Create the float buffer which will hold the ModelViewProjection matrix
		mvpMatrixFloatBuffer = Utils.createFloatBuffer(16);

		// Create the FloatBuffer to hold the current OpenGL GL_POINT_SIZE so we can restore it later
		// Note: LWJGL minimum size to get OpenGL data is 16, even though we only want to store 1 float.
		pointSizeFloatBuffer = Utils.createFloatBuffer(16);			

		// ----- Shader program setup -----

		// Instantiate the shader program
		pointShaderProgram = new ShaderProgram();

		// Load, compile and link the shaders into the shader program
		pointShaderProgram.initFromStrings(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);

		// ----- Grid shader attributes and uniforms -----

		// Add the shader attributes
		pointShaderProgram.addAttribute("vertexLocation");
		pointShaderProgram.addAttribute("vertexColour");

		// Add the shader uniforms
		pointShaderProgram.addUniform("mvpMatrix");


		// ----- Set up our Vertex Array Object (VAO) to hold the shader attributes -----

		// Get an Id for the Vertex Array Object (VAO) and bind to it
		pointVaoId = glGenVertexArrays();
		glBindVertexArray(pointVaoId);

		// ----- Location Vertex Buffer Object (VBO) -----

		// Generate an id for the locationBuffer and bind to it
		pointVertexBufferId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, pointVertexBufferId);

		// Place the location data into the VBO...
		glBufferData(GL_ARRAY_BUFFER, vertexFloatBuffer, GL_DYNAMIC_DRAW);

		// ...and specify the data format.
		glVertexAttribPointer(pointShaderProgram.attribute("vertexLocation"), // Vertex location attribute index
                                                               VERTEX_COMPONENTS, // Number of vertex components per vertex
                                                                        GL_FLOAT, // Data type
                                                                           false, // Normalised?
                                                   COMPONENT_COUNT * Float.BYTES, // Stride
                                                                              0); // Offset

		// ...and specify the data format.
		glVertexAttribPointer(pointShaderProgram.attribute("vertexColour"),  // Vertex colour attribute index
                                                           COLOUR_COMPONENTS,  // Number of colour components per vertex
                                                                    GL_FLOAT,  // Data type
                                                                       false,  // Normalised?
                                               COMPONENT_COUNT * Float.BYTES,  // Stride
                                       (long)VERTEX_COMPONENTS * Float.BYTES);  // Offset
		// Unbind VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		// Enable the vertex attributes
		glEnableVertexAttribArray( pointShaderProgram.attribute("vertexLocation") );
		glEnableVertexAttribArray( pointShaderProgram.attribute("vertexColour")   );

		// Unbind our Vertex Array object - all the buffer and attribute settings above will be associated with our VAO
		glBindVertexArray(0);
	}

	// Constructor
	// Note: width is along +/- x-axis, depth is along +/- z-axis, height is the location on
	// the y-axis, numDivisions is how many points to draw across each axis
	public Point3D()
	{
		//
	} 

	/**
	 * Draw a Point3D as a GL_POINT.
	 * 
	 * @param  location   The location of the point to draw as a Vec3f
	 * @param  colour     The colour with which to draw the point
	 * @param  pointSize  The size of the point to draw  
	 * @param  mvpMatrix  The ModelViewProjection matrix with which to draw the point
	 */
	// to pass to the shader as a uniform.
	public void draw(Vec3f location, Colour4f colour, float pointSize, Mat4f mvpMatrix)
	{
		setPointData(location, colour);

		// Transfer the point data into the vertex float buffer and flip it ready for use
		vertexFloatBuffer.put(pointData);
		vertexFloatBuffer.flip();

		// Transfer the ModelViewProjection matrix into the mvp float buffer and flip it ready for use
		mvpMatrixFloatBuffer.put( mvpMatrix.toArray() );
		mvpMatrixFloatBuffer.flip();

		// Enable our point shader program
		pointShaderProgram.use();

		// Bind to our vertex buffer object
		glBindVertexArray(pointVaoId);
	
		// Bind to the vertex buffer object (VBO) and place the new data into it
		glBindBuffer(GL_ARRAY_BUFFER, pointVertexBufferId);
		glBufferData(GL_ARRAY_BUFFER, vertexFloatBuffer, GL_DYNAMIC_DRAW);		
	
		// Provide the projection matrix uniform
		glUniformMatrix4fv(pointShaderProgram.uniform("mvpMatrix"), false, mvpMatrixFloatBuffer);
	
		// Store the current GL_POINT_SIZE
		// Note: We MUST allocate a minimum of 16 floats in our FloatBuffer in LWJGL, we CANNOT
		// just get a FloatBuffer with 1 float!
		// Also: Previously we could have used 'glPushAttrib(GL_POINT_BIT); /* do stuff */ glPopAttrib();'
		// - but this was deprecated in OpenGL 3.2 as a means to remove some of the state tracking
		// from OpenGL after the move to a programmable pipeline (i.e. shader based) architecture.
		glGetFloatv(GL_POINT_SIZE, pointSizeFloatBuffer);
	
		// Set the GL_POINT_SIZE to be the size requested
		glPointSize(pointSize);
	
		// 	Draw the axis points
		glDrawArrays(GL_POINTS, 0, NUM_VERTICES);
	
		// Restore the point width to the previous value
		glPointSize( pointSizeFloatBuffer.get(0) );

		// Unbind from VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		// Unbind from our VAO
		glBindVertexArray(0);

		// Disable our shader program
		pointShaderProgram.disable();
	}
	
	private static void setPointData(Vec3f location, Colour4f colour) {
		// Point x/y/z
		pointData[0] = location.x;
		pointData[1] = location.y;
		pointData[2] = location.z;

		// Point colour
		pointData[3] = colour.r;
		pointData[4] = colour.g;
		pointData[5] = colour.b;
		pointData[6] = colour.a;
	}
	
} // End of Point3D class
