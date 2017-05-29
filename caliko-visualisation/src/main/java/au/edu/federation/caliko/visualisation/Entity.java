package au.edu.federation.caliko.visualisation;

import java.nio.FloatBuffer;

import au.edu.federation.utils.Mat3f;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Utils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * A wrapper class which takes a Model and a ShaderProgram, then performs Vertex Array Object setup in order to
 * allow easy drawing of the Model via the ShaderProgram.
 * 
 * @version 0.5 - 12/01/2016
 */ 
public class Entity
{	
	// ---------- Static Properties ----------
	
	/** Projection matrix float buffer - static because all entities will have a 4x4 projection matrix we can overwrite rather than allocate new memory to. */
	private FloatBuffer projectionMatrixFB;
	
	/** View matrix float buffer - static because all entities will have a 4x4 view matrix we can overwrite rather than allocate new memory to. */
	private FloatBuffer viewMatrixFB;
	
	/** Model matrix float buffer - static because all entities will have a 4x4 model matrix we can overwrite rather than allocate new memory to. */
	private FloatBuffer modelMatrixFB;
	
	/** Normal matrix float buffer - static because all entities will have a 4x4 normal matrix we can overwrite rather than allocate new memory to. */
	private FloatBuffer normalMatrixFB;
	
	/** How many entities exist - if this is zero (which it will be on startup as that's the default) then buffers are allocated in the constructor. */
	private int numEntities = 0;

	// ---------- Private Properties ------------
	
	/** The shader program attached to this entity. */
	private ShaderProgram mShaderProgram;
	
	/** The Vertex Array Object (VAO) id attached to this entity. */
	private int mVaoId;

	/** The model associated with this entity. */
	private Model mModel;
	
	/** The vertex float buffer for the model. */
	private FloatBuffer mVertexFloatBuffer;
	
	/** The normal float buffer for the model. */
	private FloatBuffer mNormalFloatBuffer;
	
	/**
	 * Constructor.
	 * <p>
	 * Models may have any number of vertices and vertex normals.
	 * <p>
	 * Shader programs should have the following uniforms ([OPTIONAL] uniforms are just that - optional):
	 * <ul>
	 * <li>mat4 projectionMatrix</li>
	 * <li>mat4 viewMatrixMatrix</li>
	 * <li>mat4 modelMatrix</li>
	 * <li>mat3 normalMatrix [OPTIONAL]</li>
	 * </ul>
	 * Shader programs must also have the following per-vertex attributes:
	 * <ul>
	 * <li>vec3 vertexLocation</li>
	 * <li>vec3 vertexNormal [OPTIONAL]</li>
	 * </ul>
	 * 
	 * @param	model			The model associated with this entity.
	 * @param	shaderProgram	The shader program used to draw this model.
	 */
	public Entity(Model model, ShaderProgram shaderProgram)
	{
		// We share static projection/view/model/normal matrix float buffers across all entities - so
		// only instantiate them on first entity (they get overwritten per entity as and when required).
		if (0 == numEntities)
		{
			projectionMatrixFB = Utils.createFloatBuffer(16);
			viewMatrixFB       = Utils.createFloatBuffer(16);
			modelMatrixFB      = Utils.createFloatBuffer(16);
			normalMatrixFB     = Utils.createFloatBuffer(9);

			++numEntities;
		}

		// Our model object is a reference to the provided model object
		mModel = model;

		// Setup FloatBuffers for this model so that it can be drawn.
		// Note: A Model, once loaded, has all its data in the vertexData and normalData float arrays - however,
		// it does not have this data available as a FloatBuffer, so that's what we do here.
		setupModelFloatBuffers();

		// Our shader program is a reference to the provided shader program
		mShaderProgram = shaderProgram;

		// Construct the Vertex Array Object which will hold the details of the shader attributes and uniforms.
		// Note: This must be called after setting up the FloatBuffers for this entity.
		constructVAO();
	}

	/** Private method to create FloatBuffers of the model data and prepare them for use. */
	private void setupModelFloatBuffers()
	{
		// Set up our vertex buffer. Note: The size is * 3 because there are 3 floats per vertex.
		mVertexFloatBuffer = Utils.createFloatBuffer( mModel.getNumVertices() * 3);

		// Transfer the data into the float buffer and flip it ready for use
		mVertexFloatBuffer.put( mModel.getVertexFloatArray() );
		mVertexFloatBuffer.flip();

		// Although vertices are required, normals are optional - if we have them, use them.
		if ( mModel.hasNormals() )
		{
			// Do the same for our normal buffer. Note: The size is * 3 because there are 3 floats per normal.
			mNormalFloatBuffer = Utils.createFloatBuffer( mModel.getNumNormals() * 3);
			mNormalFloatBuffer.put( mModel.getNormalFloatArray() );
			mNormalFloatBuffer.flip();
		}
	}

	/** Private method to create this entity's Vertex Array Object necessary to pass data to the shader. */
	private void constructVAO()
	{
		// Get an Id for the Vertex Array Object (VAO) and bind to it
		mVaoId = glGenVertexArrays();
		glBindVertexArray(mVaoId);

		// ----- Location Vertex Buffer Object (VBO) -----

		// Generate an id for the locationBuffer and bind to it
		int vertexBufferId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);

		// Place the location data into the VBO...
		glBufferData(GL_ARRAY_BUFFER, mVertexFloatBuffer, GL_STATIC_DRAW);

		// ...and specify the data format.
		glVertexAttribPointer(mShaderProgram.attribute("vertexLocation"),  // Vertex attribute index
				                                                       3,  // Number of normal components per vertex
                                                                GL_FLOAT,  // Data type
                                                                   false,  // Normalised?
                                                                       0,  // Stride
                                                                       0); // Offset

		// Unbind VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		// Enable the vertex attribute at this location
		glEnableVertexAttribArray(mShaderProgram.attribute("vertexLocation"));


		// ----- Normal Vertex Buffer Object (VBO) -----

		// Only set up the normal VBO if the model uses normals
		if ( mModel.hasNormals() )
		{
			// Generate an id for the colourBuffer and bind to it
			int normalBufferId = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, normalBufferId);

			// Place the colour data into the VBO...
			glBufferData(GL_ARRAY_BUFFER, mNormalFloatBuffer, GL_STATIC_DRAW);

			// ...and specify the data format.
			glVertexAttribPointer(mShaderProgram.attribute("vertexNormal"),  // Vertex attribute index
                                                                         3,  // Number of normal components per vertex
                                                                  GL_FLOAT,  // Data type
                                                                     false,  // Normalised?
                                                                         0,  // Stride
                                                                         0); // Offset

			// Unbind VBO
			glBindBuffer(GL_ARRAY_BUFFER, 0);

			// Enable the vertex attribute at this location
			glEnableVertexAttribArray(mShaderProgram.attribute("vertexNormal"));
		}

		// ---- End of Vertex Array Object Setup -----

		// Unbind from VAO to save all settings
		glBindVertexArray(0);
	}

	/**
	 * Draw this entity's model using it's shader program.
	 * <p>
	 * Specify NULL for the normal matrix if the model associated with this entity does not contain vertex normals.
	 * <p>
	 * Valid draw modes (as per the LWJGL3 GL11 class) are:
	 * GL_POINTS,
	 * GL_LINES,
	 * GL_LINE_LOOP,
	 * GL_LINE_STRIP,
	 * GL_TRIANGLES,
	 * GL_TRIANGLE_STRIP,
	 * GL_TRIANGLE_FAN,
	 * GL_QUADS,
	 * GL_QUAD_STRIP, and
	 * GL_POLYGON.
	 * 
	 * @param	projectionMatrix	The projection matrix.
	 * @param	viewMatrix			The view matrix.
	 * @param	modelMatrix			The model matrix.
	 * @param	normalMatrix		The normal matrix.
	 * @param	drawMode			The draw mode to use (GL_POINTS etc). 
	 */ 
	public void draw(Mat4f projectionMatrix, Mat4f viewMatrix, Mat4f modelMatrix, Mat3f normalMatrix, int drawMode)
	{
		// Bind to our shader program
		mShaderProgram.use();

			// Copy the projection matrix to float buffer
			projectionMatrixFB.put( projectionMatrix.toArray() );
			projectionMatrixFB.flip();
	
			// Copy the view matrix to float buffer
			viewMatrixFB.put( viewMatrix.toArray() );
			viewMatrixFB.flip();
	
			// Copy the model matrix to float buffer
			modelMatrixFB.put( modelMatrix.toArray() );
			modelMatrixFB.flip();
	
			// If necessary, copy the normal matrix to float buffer.
			// Note: The normal matrix is a 3x3 so we only need 9 floats, not 16!
			if (modelMatrix != null)
			{
				normalMatrixFB.put ( normalMatrix.toArray() );
				normalMatrixFB.flip();
			}
	
			// Bind to our vertex array object
			glBindVertexArray(mVaoId);
	
				// Provide the projection, view and model matrix (4x4) and the normal matrix (3x3) uniform data
				glUniformMatrix4fv(mShaderProgram.uniform("projectionMatrix"), false, projectionMatrixFB);
				glUniformMatrix4fv(mShaderProgram.uniform("viewMatrix"), false, viewMatrixFB);
				glUniformMatrix4fv(mShaderProgram.uniform("modelMatrix"), false, modelMatrixFB);
				glUniformMatrix3fv(mShaderProgram.uniform("normalMatrix"), false, normalMatrixFB);
	
				// Draw the model
				glDrawArrays(drawMode, 0, mModel.getNumVertices() );
	
			// Unbind from our VAO
			glBindVertexArray(0);

		// Unbind from our shader program
		mShaderProgram.disable();
	}

	/**
	 * Utility method to draw our entity where the model does not contain vertex normals.
	 * 
	 * @param projectionMatrix	The projection matrix.
	 * @param viewMatrix		The view matrix.
	 * @param modelMatrix		The model matrix.
	 * @param drawMode			The draw mode.
	 */
	public void draw(Mat4f projectionMatrix, Mat4f viewMatrix, Mat4f modelMatrix, int drawMode)
	{
		draw(projectionMatrix, viewMatrix, modelMatrix, null, drawMode);
	}

	/**
	 * Return whether or not the model attached to this entity has normals.
	 * 
	 * @return	whether or not the model attached to this entity has normals.
	 */
	public boolean modelHasNormals() { return mModel.hasNormals(); }

	/**
	 * Update the model attached to this entity to be the provided model.
	 * <p>
	 * The model provided is assigned by reference.
	 * 
	 * @param	model	The model to associate with this entity.
	 */
	public void updateModel(Model model) { mModel = model; }
	
	/**
	 * Update the shader program attached to this entity to be the provided shader program.
	 * <p>
	 * The shader program provided is assigned by reference.
	 * 
	 * @param	shaderProgram	The shaderProgram to associate with this entity.
	 */
	public void updateShaderProgram(ShaderProgram shaderProgram) { mShaderProgram = shaderProgram; }
}
